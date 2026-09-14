package urf.police.observation;

import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.component.module.complex.RoadDetector;
import rescuecore2.worldmodel.EntityID;
import urf.police.improvement.URFRoadDetector;

/**
 * Observation wrapper around URFRoadDetector.
 *
 * Observation responsibilities:
 * - record Area position and physical X/Y for this timestep
 * - record the road target selected by the URF scoring logic
 * - measure target-selection calculation time
 *
 * This class is the single owner of physical position observation.
 * CLEAR and MOVE observers must not record position again.
 *
 * Police behaviour is unchanged. Every decision still comes from
 * URFRoadDetector.
 */
public class URFObservedRoadDetector extends URFRoadDetector {

  private final URFPoliceMetrics metrics;

  private long lastCalcNanos;

  /**
   * @param agentInfo agent information supplied by the ADF
   * @param worldInfo world information supplied by the ADF
   * @param scenarioInfo scenario constants supplied by the ADF
   * @param moduleManager module manager used for sub module selection
   * @param developData development configuration supplied by the ADF
   */
  public URFObservedRoadDetector(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
                                 ModuleManager moduleManager, DevelopData developData) {
    super(agentInfo, worldInfo, scenarioInfo, moduleManager, developData);
    this.metrics = URFPoliceMetrics.forAgent(agentInfo.getID());
    this.lastCalcNanos = 0L;
  }

  /**
   * @return this module
   */
  @Override
  public RoadDetector calc() {

    long startNanos = System.nanoTime();

    /*
     * Preserve the complete URFRoadDetector target-selection behaviour.
     */
    super.calc();

    this.lastCalcNanos = System.nanoTime() - startNanos;

    EntityID position = this.agentInfo.getPosition();
    EntityID target = super.getTarget();

    /*
     * Store both Area position and physical X/Y.
     *
     * Without X/Y the stuck detector cannot calculate physical
     * displacement correctly.
     */
    this.metrics.recordPosition(
        this.agentInfo.getTime(),
        position,
        this.agentInfo.getX(),
        this.agentInfo.getY());

    this.metrics.recordTarget(target);

    /*
     * Do not print here.
     *
     * CLEAR/MOVE observers print and export after the final autonomous
     * action is known.
     */
    // Can Add Logging of system.out.println here

    return this;
  }

  /**
   * @return nanoseconds spent inside the most recent target selection
   */
  public final long getLastCalcNanos() {
    return this.lastCalcNanos;
  }
}