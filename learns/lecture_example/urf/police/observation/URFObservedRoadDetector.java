package urf.police.observation;

import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.component.module.complex.RoadDetector;
import rescuecore2.worldmodel.EntityID;
import sample_team.module.complex.SampleRoadDetector;

/**
 * Observation-only wrapper around SampleRoadDetector.
 *
 * <p>IMPORTANT: This class is retained only for the observation baseline.
 *
 * <p>Competition Police development after PO-9 will use independent URF decision modules and will
 * not inherit SampleRoadDetector.
 */
public class URFObservedRoadDetector extends SampleRoadDetector {

  private final URFPoliceMetrics metrics;

  public URFObservedRoadDetector(
      AgentInfo agentInfo,
      WorldInfo worldInfo,
      ScenarioInfo scenarioInfo,
      ModuleManager moduleManager,
      DevelopData developData) {

    super(agentInfo, worldInfo, scenarioInfo, moduleManager, developData);

    this.metrics = URFPoliceMetrics.forAgent(agentInfo.getID());
  }

  @Override
  public RoadDetector calc() {

    /*
     * Preserve the complete SampleRoadDetector
     * target-selection behaviour.
     */
    super.calc();

    EntityID position = this.agentInfo.getPosition();

    EntityID target = super.getTarget();

    /*
     * PO-8.3 FIX:
     *
     * Store both Area position and physical X/Y.
     *
     * Without X/Y the previous implementation could
     * not calculate physical displacement correctly.
     */
    this.metrics.recordPosition(
        this.agentInfo.getTime(), position, this.agentInfo.getX(), this.agentInfo.getY());

    this.metrics.recordTarget(target);

    /*
     * Do not print here.
     *
     * CLEAR/MOVE observers print and export after
     * the final autonomous action is known.
     */

    return this;
  }
}