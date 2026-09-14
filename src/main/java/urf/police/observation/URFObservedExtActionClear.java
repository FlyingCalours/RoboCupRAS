package urf.police.observation;

import adf.core.agent.action.Action;
import adf.core.agent.action.police.ActionClear;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.component.extaction.ExtAction;
import rescuecore2.worldmodel.EntityID;
import urf.police.improvement.URFPoliceExtActionClear;

/**
 * Observation wrapper around URFPoliceExtActionClear.
 *
 * Observation responsibilities:
 * - measure total CLEAR cycle time, including the CSV export cost
 * - count CLEAR results against handoff results
 * - record the target requested by DefaultTacticsPoliceForce
 *
 * Position/X/Y are intentionally NOT recorded here.
 * URFObservedRoadDetector is the single owner of physical position
 * observation.
 *
 * The metrics recording and CSV export already happen inside
 * URFPoliceExtActionClear.calc(). This class therefore does NOT export a
 * second row. It only adds aggregate observation fields.
 *
 * Police behaviour is unchanged.
 *
 * REQUIRED PARENT EDIT:
 *
 *   public final class URFPoliceExtActionClear   ->   public class URFPoliceExtActionClear
 *
 * A final class cannot be extended. No other change to the parent is needed.
 */
public class URFObservedExtActionClear extends URFPoliceExtActionClear {

  /** Set to false to silence the observation line without removing it. */
  private static final boolean PRINT_OBSERVATION = true;

  /** Nanoseconds in one millisecond, used for readable log output. */
  private static final double NANOS_PER_MILLI = 1000000.0;

  private EntityID requestedTarget;

  private long observedCycles;
  private long observedClearCount;
  private long observedHandoffCount;
  private long observedTotalNanos;
  private long lastCycleNanos;

  /**
   * @param agentInfo agent information supplied by the ADF
   * @param worldInfo world information supplied by the ADF
   * @param scenarioInfo scenario constants supplied by the ADF
   * @param moduleManager module manager used for sub module selection
   * @param developData development configuration supplied by the ADF
   */
  public URFObservedExtActionClear(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
                                   ModuleManager moduleManager, DevelopData developData) {
    super(agentInfo, worldInfo, scenarioInfo, moduleManager, developData);
    this.requestedTarget = null;
    this.observedCycles = 0L;
    this.observedClearCount = 0L;
    this.observedHandoffCount = 0L;
    this.observedTotalNanos = 0L;
    this.lastCycleNanos = 0L;
  }

  /**
   * @param target the road target selected by the configured RoadDetector
   * @return this action module
   */
  @Override
  public ExtAction setTarget(EntityID target) {
    this.requestedTarget = target;
    /*
     * Preserve the complete URF target processing.
     */
    super.setTarget(target);
    return this;
  }

  /**
   * @return this action module
   */
  @Override
  public ExtAction calc() {

    long startNanos = System.nanoTime();

    /*
     * Execute the complete URF CLEAR implementation.
     *
     * URFPoliceExtActionClear already records the CLEAR-module result,
     * evaluates the stuck detector and exports the CSV row.
     */
    super.calc();

    this.lastCycleNanos = System.nanoTime() - startNanos;
    this.observedTotalNanos = this.observedTotalNanos + this.lastCycleNanos;
    this.observedCycles++;

    Action action = super.getAction();
    if (action instanceof ActionClear) {
      this.observedClearCount++;
    }
    else {
      /*
       * A null result means DefaultTacticsPoliceForce continues to the
       * configured MOVE module.
       */
      this.observedHandoffCount++;
    }

    if (PRINT_OBSERVATION) {
      System.out.println(this.toObservationLine());
    }

    return this;
  }

  /**
   * @return nanoseconds spent in the most recent CLEAR cycle
   */
  public final long getLastCycleNanos() {
    return this.lastCycleNanos;
  }

  /**
   * @return the number of CLEAR cycles observed so far
   */
  public final long getObservedCycles() {
    return this.observedCycles;
  }

  /**
   * @return the number of cycles that produced an ActionClear
   */
  public final long getObservedClearCount() {
    return this.observedClearCount;
  }

  /**
   * @return the number of cycles that handed control to the MOVE module
   */
  public final long getObservedHandoffCount() {
    return this.observedHandoffCount;
  }

  /**
   * @return mean nanoseconds per observed CLEAR cycle
   */
  public final double getAverageCycleNanos() {
    if (this.observedCycles == 0L) {
      return 0.0;
    }
    return (double) this.observedTotalNanos / (double) this.observedCycles;
  }

  /**
   * @return the aggregate observation fields for one log line
   */
  private String toObservationLine() {
    return "obsClearTarget=" + this.requestedTarget
        + " obsCycles=" + this.observedCycles
        + " obsClear=" + this.observedClearCount
        + " obsHandoff=" + this.observedHandoffCount
        + " obsCycleMs=" + (this.lastCycleNanos / NANOS_PER_MILLI)
        + " obsAvgCycleMs=" + (this.getAverageCycleNanos() / NANOS_PER_MILLI);
  }
}