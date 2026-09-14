package urf.police.observation;

import adf.core.agent.action.Action;
import adf.core.agent.action.common.ActionMove;
import adf.core.agent.action.police.ActionClear;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.component.extaction.ExtAction;
import java.util.List;
import rescuecore2.worldmodel.EntityID;
import urf.police.improvement.URFMoveTargetSource;
import urf.police.improvement.URFPoliceExtActionMove;

/**
 * Observation wrapper around URFPoliceExtActionMove.
 *
 * Observation responsibilities:
 * - record which target source drove the move
 * - count how far down the fallback ladder the module had to go
 * - assert the never-null contract of the MOVE module
 * - measure total MOVE cycle time, including the CSV export cost
 *
 * Target resolution is read from the parent rather than recalculated
 * here. An earlier version mirrored the parent's resolution rule, which
 * would silently report the wrong source as soon as the two drifted
 * apart.
 *
 * Position/X/Y are intentionally NOT recorded here.
 * URFRoadDetector is the single owner of physical position observation.
 *
 * The metrics recording, stuck evaluation and CSV export already happen
 * inside URFPoliceExtActionMove.calc(). This class therefore does NOT
 * export a second row. It only adds aggregate observation fields.
 *
 * Police behaviour is unchanged.
 */
public class URFObservedExtActionMove extends URFPoliceExtActionMove {

  /** Set to false to silence the observation line without removing it. */
  private static final boolean PRINT_OBSERVATION = true;

  /** Nanoseconds in one millisecond, used for readable log output. */
  private static final double NANOS_PER_MILLI = 1000000.0;

  private long observedCycles;
  private long observedMoveCount;
  private long observedClearCount;
  private long observedNullCount;
  private long observedSelfLoopCount;
  private long observedPushThroughCount;
  private long observedRoadTargetCount;
  private long observedSearchTargetCount;
  private long observedTotalNanos;
  private long lastCycleNanos;

  /**
   * @param agentInfo agent information supplied by the ADF
   * @param worldInfo world model supplied by the ADF
   * @param scenarioInfo scenario configuration supplied by the ADF
   * @param moduleManager module registry supplied by the ADF
   * @param developData development configuration supplied by the ADF
   */
  public URFObservedExtActionMove(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      DevelopData developData) {
    super(agentInfo, worldInfo, scenarioInfo, moduleManager, developData);
    this.observedCycles = 0L;
    this.observedMoveCount = 0L;
    this.observedClearCount = 0L;
    this.observedNullCount = 0L;
    this.observedSelfLoopCount = 0L;
    this.observedPushThroughCount = 0L;
    this.observedRoadTargetCount = 0L;
    this.observedSearchTargetCount = 0L;
    this.observedTotalNanos = 0L;
    this.lastCycleNanos = 0L;
  }

  /**
   * @return this module
   */
  @Override
  public ExtAction calc() {

    long startNanos = System.nanoTime();

    /*
     * Execute the complete URF MOVE implementation.
     *
     * URFPoliceExtActionMove already records the MOVE-module result,
     * evaluates the stuck detector and exports the CSV row.
     */
    super.calc();

    this.lastCycleNanos = System.nanoTime() - startNanos;
    this.observedTotalNanos = this.observedTotalNanos + this.lastCycleNanos;
    this.observedCycles++;

    URFMoveTargetSource source = super.getTargetSource();
    if (source == URFMoveTargetSource.PUSH_THROUGH) {
      this.observedPushThroughCount++;
    }
    else if (source == URFMoveTargetSource.ROAD_TARGET) {
      this.observedRoadTargetCount++;
    }
    else if (source == URFMoveTargetSource.SEARCH_TARGET) {
      this.observedSearchTargetCount++;
    }

    Action action = super.getAction();

    if (action instanceof ActionMove) {
      this.observedMoveCount++;
      if (this.isSelfLoop((ActionMove) action)) {
        /*
         * The last rung of the fallback ladder: a move to the agent's
         * own area. Counting it separately shows how often the agent was
         * completely walled in.
         */
        this.observedSelfLoopCount++;
      }
    }
    else if (action instanceof ActionClear) {
      this.observedClearCount++;
    }
    else {
      /*
       * Contract violation.
       *
       * URFPoliceExtActionMove is documented never to return null,
       * because a null here lets the tactics fallthrough end in
       * ActionRest. This counter must stay at zero for every run.
       */
      this.observedNullCount++;
    }

    if (PRINT_OBSERVATION) {
      System.out.println(this.toObservationLine());
    }

    return this;
  }

  /**
   * @return nanoseconds spent in the most recent MOVE cycle
   */
  public final long getLastCycleNanos() {
    return this.lastCycleNanos;
  }

  /**
   * @return the number of MOVE cycles observed so far
   */
  public final long getObservedCycles() {
    return this.observedCycles;
  }

  /**
   * @return the number of cycles that produced an ActionMove
   */
  public final long getObservedMoveCount() {
    return this.observedMoveCount;
  }

  /**
   * @return the number of cycles that fell through to a bearing clear
   */
  public final long getObservedClearCount() {
    return this.observedClearCount;
  }

  /**
   * @return the number of cycles that returned null, which must be zero
   */
  public final long getObservedNullCount() {
    return this.observedNullCount;
  }

  /**
   * @return the number of cycles that moved the agent to its own area
   */
  public final long getObservedSelfLoopCount() {
    return this.observedSelfLoopCount;
  }

  /**
   * @return the number of cycles driven by the road target
   */
  public final long getObservedRoadTargetCount() {
    return this.observedRoadTargetCount;
  }

  /**
   * @return the number of cycles driven by the Search target
   */
  public final long getObservedSearchTargetCount() {
    return this.observedSearchTargetCount;
  }

  /**
   * @return mean nanoseconds per observed MOVE cycle
   */
  public final double getAverageCycleNanos() {
    if (this.observedCycles == 0L) {
      return 0.0;
    }
    return (double) this.observedTotalNanos / (double) this.observedCycles;
  }

  /**
   * @param action a move action produced by the parent
   * @return true when the action does not leave the current area
   */
  private boolean isSelfLoop(ActionMove action) {
    List<EntityID> path = action.getPath();
    if (path != null && path.size() > 1) {
      return false;
    }
    /*
     * A one-element path with usePosition set is a positional move inside
     * the current area, which is a real move. Only the plain one-element
     * path is the last rung of the fallback ladder.
     */
    return !action.getUsePosition();
  }

  /**
   * @return the aggregate observation fields for one log line
   */
  private String toObservationLine() {
    return "obsMoveSource=" + super.getTargetSource()
        + " obsMoveRoadTarget=" + super.getRoadTarget()
        + " obsMoveSearchTarget=" + super.getSearchTarget()
        + " obsMoveRequested=" + super.getRequestedTarget()
        + " obsMoveCycles=" + this.observedCycles
        + " obsMove=" + this.observedMoveCount
        + " obsMoveClear=" + this.observedClearCount
        + " obsMoveNull=" + this.observedNullCount
        + " obsMoveSelfLoop=" + this.observedSelfLoopCount
        + " obsMovePush=" + this.observedPushThroughCount
        + " obsMoveRoad=" + this.observedRoadTargetCount
        + " obsMoveSearch=" + this.observedSearchTargetCount
        + " obsMoveCycleMs=" + (this.lastCycleNanos / NANOS_PER_MILLI)
        + " obsMoveAvgCycleMs=" + (this.getAverageCycleNanos() / NANOS_PER_MILLI);
  }
}