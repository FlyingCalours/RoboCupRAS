package sample_team.module.complex.police.improvement;

import adf.core.agent.action.common.ActionMove;
import adf.core.agent.info.WorldInfo;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

/**
 * Detects a physically stuck police force and forces it to move.
 *
 * This is separate from URFPoliceStuckDetector. That class only grades MOVE
 * actions, so an agent that hammers the same unclearable blockade forever is
 * reported as CLEARING and never flagged. This class ignores what the agent
 * decided and only asks one question: did the body actually go anywhere.
 *
 * Reacting takes two steps, because the clear module runs first and would
 * otherwise keep winning the timestep:
 *
 *   URFPoliceExtActionClear  - return null while isStuck()
 *   URFPoliceExtActionMove   - return escapeMove() while isStuck()
 *
 * Each escape walks to a different neighbouring area, so an agent wedged in a
 * corner eventually tries every exit instead of retrying the same one.
 */
public final class URFPoliceEscape {

    private static final Map<Integer, URFPoliceEscape> INSTANCES = new ConcurrentHashMap<>();

    /** Movement below this, in millimetres, counts as not having moved. */
    private static final double MIN_PROGRESS = 300.0;

    /** Consecutive motionless timesteps before the agent is called stuck. */
    private static final int STUCK_AFTER = 5;

    private final int agentId;

    private int lastTime;

    private EntityID lastPosition;

    private double lastX;

    private double lastY;

    private int stillCount;

    private int escapeAttempt;

    /**
     * Creates the tracker for one agent.
     *
     * @param agentId the police force this tracker belongs to
     */
    private URFPoliceEscape(EntityID agentId) {
        this.agentId = agentId.getValue();
        this.lastTime = -1;
        this.lastPosition = null;
        this.lastX = Double.NaN;
        this.lastY = Double.NaN;
        this.stillCount = 0;
        this.escapeAttempt = 0;
    }

    /**
     * Returns the tracker for one agent, creating it on first use.
     *
     * Every police agent in this JVM shares the static map, so the tracker must
     * be keyed by agent id. All three modules calling this get the same object.
     *
     * @param agentId the police force whose tracker is wanted
     *
     * @return the tracker for that agent
     *
     * @throws IllegalArgumentException when agentId is null
     */
    public static URFPoliceEscape forAgent(EntityID agentId) {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId must not be null");
        }
        return INSTANCES.computeIfAbsent(agentId.getValue(), id -> new URFPoliceEscape(agentId));
    }

    /**
     * Records where the agent is, once per timestep.
     *
     * Call this from URFRoadDetector.calc(), which runs before both ExtActions.
     * Repeated calls within the same timestep are ignored, so the counter cannot
     * be inflated by accident.
     *
     * @param simulationTime the current timestep
     * @param position the area the agent occupies, may be null
     * @param x the agent's x coordinate in millimetres
     * @param y the agent's y coordinate in millimetres
     */
    public synchronized void update(
        int simulationTime, 
        EntityID position,
        double x, 
        double y) {

        if (simulationTime == this.lastTime) {
            return;
        }

        boolean firstCall = this.lastTime < 0;
        this.lastTime = simulationTime;

        if (firstCall) {
            this.lastPosition = position;
            this.lastX = x;
            this.lastY = y;
            return;
        }

        boolean areaChanged = 
            position != null 
            && this.lastPosition != null
            && !position.equals(this.lastPosition);

        double travelled = Math.hypot(x - this.lastX, y - this.lastY);
        boolean moved = areaChanged || travelled >= MIN_PROGRESS;

        if (moved) {
            this.stillCount = 0;
            this.escapeAttempt = 0;
        } 
        else {
            this.stillCount++;
        }

        this.lastPosition = position;
        this.lastX = x;
        this.lastY = y;
    }

    /**
     * Reports whether the agent has failed to move for long enough to act on.
     *
     * @return true when the agent has been motionless for STUCK_AFTER timesteps
     */
    public synchronized boolean isStuck() {
        return this.stillCount >= STUCK_AFTER;
    }

    /**
     * Builds a move to a neighbouring area, ignoring the current target.
     *
     * The neighbour rotates on every call, so successive escapes try different
     * exits. The counter is reset here to give the move a few timesteps to take
     * effect before the agent is declared stuck again.
     *
     * @param worldInfo the world model, used to look up the area
     * @param position the area the agent is standing on, may be null
     *
     * @return a move to an adjacent area, or null when the agent is not in an
     *   area or the area has no known neighbours
     */
    public synchronized ActionMove escapeMove(WorldInfo worldInfo, EntityID position) {

        if (worldInfo == null || position == null) {
            return null;
        }

        StandardEntity here = worldInfo.getEntity(position);
        if (!(here instanceof Area)) {
            return null;
        }
        Area area = (Area) here;

        if (!area.isEdgesDefined()) {
            return null;
        }

        List<EntityID> neighbours = area.getNeighbours();
        if (neighbours == null || neighbours.isEmpty()) {
            return null;
        }

        EntityID exit = neighbours.get(this.escapeAttempt % neighbours.size());

        this.escapeAttempt++;
        this.stillCount = 0;

        // Current position is included so the path is unambiguously valid.
        return new ActionMove(Arrays.asList(position, exit));
    }

    /**
     * Returns how many consecutive timesteps the agent has been motionless.
     *
     * @return the current motionless streak
     */
    public synchronized int getStillCount() {
        return this.stillCount;
    }

    /**
     * Returns the agent this tracker belongs to.
     *
     * @return the agent id as a plain int
     */
    public int getAgentId() {
        return this.agentId;
    }
}