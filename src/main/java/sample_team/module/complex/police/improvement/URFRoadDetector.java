package sample_team.module.complex.police.improvement;

import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.component.module.complex.RoadDetector;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

import sample_team.module.complex.police.observation.URFPoliceMetrics;
import sample_team.module.complex.police.improvement.URFPoliceEscape;


/**
 * Decides WHICH road the police force should open.
 *
 * Rule: if I am standing on a blocked road, that one. Otherwise the nearest
 * blocked road I know about. Keep the target until it is clear.
 *
 * This module also owns position observation. It runs first inside
 * DefaultTacticsPoliceForce.think(), so it is the only safe place to advance
 * the metrics clock. URFPoliceStuckDetector cannot work without it.
 */
public class URFRoadDetector extends RoadDetector {

  private final URFPoliceMetrics metrics;

  private EntityID result;

  private final URFPoliceEscape escape;

  /**
   * Creates the detector and attaches it to this agent's metrics record.
   *
   * @param ai agent information supplied by the ADF
   * @param wi world model supplied by the ADF
   * @param si scenario configuration supplied by the ADF
   * @param moduleManager module registry supplied by the ADF
   * @param developData development configuration supplied by the ADF
   */
  public URFRoadDetector(AgentInfo ai, WorldInfo wi, ScenarioInfo si,
      ModuleManager moduleManager, DevelopData developData) {
    super(ai, wi, si, moduleManager, developData);

    this.metrics = URFPoliceMetrics.forAgent(ai.getID());
    this.result = null;
    this.escape = URFPoliceEscape.forAgent(ai.getID());
  }

  /**
   * Returns the road chosen by the most recent call to calc().
   *
   * @return the target road id, or null when no blocked road is known
   */
  @Override
  public EntityID getTarget() {
    return this.result;
  }

  /**
   * Picks the target road for this timestep and records the agent's position
   * into the metrics.
   *
   * The position must be recorded here, before any other metrics call in the
   * timestep, because recordPosition is what advances the metrics clock. If it
   * is skipped, URFPoliceStuckDetector.evaluate() sees a repeated timestep and
   * silently does nothing for the whole run.
   *
   * @return this module, as required by the ADF module contract
   */
  @Override
  public RoadDetector calc() {

    EntityID position = this.agentInfo.getPosition();

    // Observation first. Never place this behind an early return.
    this.metrics.recordPosition(
      this.agentInfo.getTime(),
      position,
      this.agentInfo.getX(), 
      this.agentInfo.getY()
    );

    this.escape.update(
      this.agentInfo.getTime(), 
      position,
      this.agentInfo.getX(), 
      this.agentInfo.getY()
    );

    this.result = this.chooseRoad(position);

    this.metrics.recordTarget(this.result);

    // Can Add Logging of system.out.println here
    return this;
  }

  /**
   * Applies the target selection rule.
   *
   * @param position the area the agent currently occupies
   *
   * @return the road to open, or null when no blocked road is known
   */
  private EntityID chooseRoad(EntityID position) {

    // Standing in rubble beats everything else.
    if (this.isBlocked(position)) {
      return position;
    }

    // Keep the previous target while it is still blocked.
    if (this.isBlocked(this.result)) {
      return this.result;
    }

    EntityID best = null;
    int bestDistance = Integer.MAX_VALUE;

    for (StandardEntity entity : this.worldInfo
        .getEntitiesOfType(StandardEntityURN.ROAD)) {

      EntityID roadID = entity.getID();
      if (!this.isBlocked(roadID)) {
        continue;
      }
      int distance = this.worldInfo.getDistance(position, roadID);
      if (distance < bestDistance) {
        bestDistance = distance;
        best = roadID;
      }
    }

    return best;
  }

  /**
   * Reports whether the agent has actually observed blockades on an area.
   *
   * An unvisited road returns false even when it is blocked in reality,
   * because isBlockadesDefined() stays false until the agent has seen it.
   *
   * @param id the area to test, may be null
   *
   * @return true when the entity is a road with at least one known blockade
   */
  private boolean isBlocked(EntityID id) {
    if (id == null) {
      return false;
    }
    StandardEntity entity = this.worldInfo.getEntity(id);
    if (!(entity instanceof Road)) {
      return false;
    }
    Road road = (Road) entity;
    return road.isBlockadesDefined() && !road.getBlockades().isEmpty();
  }
}