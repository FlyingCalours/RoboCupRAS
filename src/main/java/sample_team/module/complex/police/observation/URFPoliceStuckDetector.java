package sample_team.module.complex.police.observation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import rescuecore2.worldmodel.EntityID;

/**
 * Observation-only stuck detector for Police Force agents.
 *
 * Important:
 * An action chosen at timestep t is executed by the simulator
 * after the decision. Therefore, movement progress must be
 * evaluated at the next timestep.
 *
 * This class does not change Police actions.
 */
public final class URFPoliceStuckDetector {
    private static final Map<Integer, URFPoliceStuckDetector> INSTANCES = new ConcurrentHashMap<>();

    /*
    * Observation thresholds.
    *
    * These values do not affect Police behaviour.
    * They will be calibrated later using collected logs.
    */
    private static final double MIN_PROGRESS_DISTANCE = 500.0;
    private static final int POSSIBLE_STUCK_AFTER_FAILED_MOVES = 2;
    private static final int CONFIRMED_STUCK_AFTER_FAILED_MOVES = 4;
    private static final int CONFIRMED_STUCK_SAME_TARGET_FAILURES = 3;
  
    private final int agentId;
    private int lastEvaluationTime;

    /*
    * Action selected during the previous timestep.
    * Its result is evaluated when the next timestep arrives.
    */
    private String previousActionType;
    private String previousActionSource;
    private EntityID previousActionTarget;
    private int previousPathLength;

    /*
    * Target associated with the latest failed MOVE.
    */
    private EntityID lastFailedMoveTarget;

    /*
    * Consecutive evidence counters.
    */
    private int consecutiveFailedMoves;
    private int consecutiveSameTargetFailedMoves;
    private int totalFailedMoveOutcomes;

    /*
    * Event counters.
    */
    private int possibleStuckEvents;
    private int confirmedStuckEvents;

    /*
    * Current detector state.
    */
    private boolean possibleStuckActive;
    private boolean confirmedStuckActive;
    private String status;
    private String reason;
    private String lastMoveOutcome;
    private URFPoliceStuckDetector(EntityID agentId) {
        this.agentId = agentId.getValue();
        this.lastEvaluationTime = -1;
        this.previousActionType = "NONE";
        this.previousActionSource = "NONE";
        this.previousActionTarget = null;
        this.previousPathLength = 0;
        this.lastFailedMoveTarget = null;
        this.status = "INITIALIZING";
        this.reason = "WAITING_FOR_FIRST_ACTION_OUTCOME";
        this.lastMoveOutcome = "NOT_EVALUATED";
    }

    public static URFPoliceStuckDetector forAgent(EntityID agentId) {

        if (agentId == null) {

            throw new IllegalArgumentException("agentId must not be null");
        }

            return INSTANCES.computeIfAbsent(agentId.getValue(), id -> new URFPoliceStuckDetector(agentId));
    }

    public static void resetAgent(EntityID agentId) {
        if (agentId != null) {
            INSTANCES.remove(agentId.getValue());
        }
    }

  /**
   * Evaluates the previous final action using the
   * newly observed position at the current timestep.
   */
    public synchronized void evaluate(URFPoliceMetrics metrics) {
        if (metrics == null) {
            throw new IllegalArgumentException("metrics must not be null");
        }
        int simulationTime = metrics.getTime();

        /*
        * Prevent duplicate evaluation during the same
        * simulation timestep.
        */
        if (simulationTime < 0 || simulationTime == this.lastEvaluationTime) {
            return;
        }

        /*
        * The first timestep has no previous action
        * outcome to evaluate.
        */
        if (this.lastEvaluationTime >= 0) {
            evaluatePreviousActionOutcome(metrics);
        }

        boolean possible = this.consecutiveFailedMoves >= POSSIBLE_STUCK_AFTER_FAILED_MOVES;

        boolean confirmed = this.consecutiveFailedMoves >= CONFIRMED_STUCK_AFTER_FAILED_MOVES 
                            && this.consecutiveSameTargetFailedMoves >= CONFIRMED_STUCK_SAME_TARGET_FAILURES;

        if (confirmed) {
            this.status = "CONFIRMED_STUCK";
            this.reason = "REPEATED_MOVE_NO_PROGRESS_SAME_TARGET";
        } 

        else if (possible) {
            this.status = "POSSIBLE_STUCK";
            this.reason = "REPEATED_MOVE_NO_PROGRESS";
        }
        else {
            classifyCurrentAction(metrics);
        }

        /*
        * Count transitions into stuck states,
        * not every timestep spent in the state.
        */
        if (possible && !this.possibleStuckActive) {
            this.possibleStuckEvents++;
        }
        if (confirmed && !this.confirmedStuckActive) {
            this.confirmedStuckEvents++;
        }

        this.possibleStuckActive = possible || confirmed;
        this.confirmedStuckActive = confirmed;

        /*
        * Keep compatibility with the existing
        * URFPoliceMetrics possibleStuck field.
        */
        metrics.setPossibleStuck(this.possibleStuckActive);

        /*
        * Store the current final action so its physical
        * result can be checked on the next timestep.
        */
        snapshotCurrentAction(metrics);

        this.lastEvaluationTime = simulationTime;
    }

  /**
   * Determines whether the previous MOVE actually
   * produced movement progress.
   */
    private void evaluatePreviousActionOutcome(URFPoliceMetrics metrics) {

        /*
        * CLEAR and REST are legitimate stationary
        * activities and must not increase MOVE-failure
        * evidence.
        */
        if (!"MOVE".equals(this.previousActionType)) {
            this.lastMoveOutcome = "NOT_MOVE";
            resetFailedMoveEvidence();
            return;
        }

        /*
        * A MOVE without a usable path is classified
        * separately from physical stuck behaviour.
        */
        if (this.previousPathLength <= 0) {
            this.lastMoveOutcome = "NO_PATH";
            resetFailedMoveEvidence();
            return;
        }

        EntityID currentPosition = metrics.getCurrentPosition();
        EntityID previousPosition = metrics.getPreviousPosition();

        /*
        * Area transition is strong evidence of progress.
        */
        boolean areaChanged = currentPosition != null && previousPosition != null && !currentPosition.equals(previousPosition);

        /*
        * A Police agent may move inside the same road.
        * Therefore X/Y displacement is also checked.
        */
        boolean coordinateProgress = metrics.getLastStepDistance() >= MIN_PROGRESS_DISTANCE;
        if (areaChanged || coordinateProgress) {
            this.lastMoveOutcome = "PROGRESS";
            resetFailedMoveEvidence();
            return;
        }

        /*
        * Previous MOVE had a path but produced no
        * meaningful observed movement.
        */
        this.lastMoveOutcome = "NO_PROGRESS";
        this.consecutiveFailedMoves++;
        this.totalFailedMoveOutcomes++;

        /*
        * Repeated failures toward the same target provide
        * stronger stuck evidence than failures while
        * targets are continuously changing.
        */
        if (this.previousActionTarget != null && this.previousActionTarget.equals(this.lastFailedMoveTarget)) {
            this.consecutiveSameTargetFailedMoves++;
        } 
        else if (this.previousActionTarget != null) {
            this.lastFailedMoveTarget = this.previousActionTarget;
            this.consecutiveSameTargetFailedMoves = 1;
        } 
        else {
            this.lastFailedMoveTarget = null;
            this.consecutiveSameTargetFailedMoves = 0;
        }
    }

    /**
     * Classifies normal non-stuck states.
     */
    private void classifyCurrentAction(URFPoliceMetrics metrics) {
        String actionType = metrics.getLastActionType();
        String actionSource = metrics.getLastActionSource();
        if ("CLEAR".equals(actionType)) {
            this.status = "CLEARING";
            this.reason = "CLEAR_ACTION_ACTIVE";
            return;
        }

        if ("REST".equals(actionType)) {
            if ("TACTICS_FALLBACK_REST".equals(actionSource)) {
                this.status = "NO_MOVE_RESULT";
                this.reason = "MOVE_MODULE_RETURNED_NULL";
            } 
            else {
                this.status = "RESTING";
                this.reason = "REST_ACTION_ACTIVE";
            }
            return;
        }

        if ("MOVE".equals(actionType)) {

            if (metrics.getLastPathLength() <= 0) {
                this.status = "MOVE_WITHOUT_PATH";
                this.reason = "MOVE_ACTION_HAS_EMPTY_PATH";
            } 
            else {
                this.status = "MOVE_PENDING";
                this.reason = "WAITING_FOR_NEXT_TIMESTEP_PROGRESS";
            }
            return;
        }

        if ("NONE".equals(actionType)) {
            this.status = "NO_ACTION";
            this.reason = "NO_FINAL_ACTION_RECORDED";
            return;
        }

        this.status = "OTHER_ACTION";
        this.reason = actionType;
    }

    /**
     * Saves the current final action for evaluation
     * during the next timestep.
     */
    private void snapshotCurrentAction(URFPoliceMetrics metrics) {
        this.previousActionType = metrics.getLastActionType();
        this.previousActionSource = metrics.getLastActionSource();
        this.previousActionTarget = resolveCurrentActionTarget(metrics);
        this.previousPathLength = metrics.getLastPathLength();
    }

    /**
     * Resolves the logical target associated with the
     * action source.
     */
    private static EntityID resolveCurrentActionTarget(URFPoliceMetrics metrics) {

        String source = metrics.getLastActionSource();

        if ("CLEAR_MODULE".equals(source)) {

            return metrics.getLastClearRequestTarget();

        }
        if ("MOVE_MODULE".equals(source) || "TACTICS_FALLBACK_REST".equals(source)) {

            return metrics.getLastMoveRequestTarget();

        }

            return metrics.getSelectedTarget();
    }

    private void resetFailedMoveEvidence() {
        this.consecutiveFailedMoves = 0;
        this.consecutiveSameTargetFailedMoves = 0;
        this.lastFailedMoveTarget = null;
    }

    /**
     * Structured fields appended to the existing
     * URF_POLICE_METRICS runtime line.
     */
    public synchronized String toLogFields() {
        return "URF_STUCK"
            + " status=" + this.status
            + " reason=" + this.reason
            + " lastMoveOutcome=" + this.lastMoveOutcome
            + " failedMoveStreak=" + this.consecutiveFailedMoves
            + " sameTargetFailedMoveStreak=" + this.consecutiveSameTargetFailedMoves
            + " totalFailedMoveOutcomes=" + this.totalFailedMoveOutcomes
            + " possibleStuckEvents=" + this.possibleStuckEvents
            + " confirmedStuckEvents=" + this.confirmedStuckEvents
            + " trackedAction=" + this.previousActionType
            + " trackedActionSource=" + this.previousActionSource
            + " trackedActionTarget=" + idValue(this.previousActionTarget)
            + " trackedPathLength=" + this.previousPathLength;
    }

    public int getAgentId() {
        return this.agentId;
    }

    public synchronized String getStatus() {
        return this.status;
    }

    public synchronized int getConsecutiveFailedMoves() {
        return this.consecutiveFailedMoves;
    }

    public synchronized int getConfirmedStuckEvents() {
        return this.confirmedStuckEvents;
    }

    private static String idValue(EntityID id) {
        if (id == null) {
            return "null";
        }

        return Integer.toString(id.getValue());
    }
    }