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

/**
 * WHICH road should the police open.
 *
 * Rule: if I am standing on a blocked road, that one. Otherwise the nearest
 * blocked road I know about. Keep it until it is clear.
 */
public class URFRoadDetector extends RoadDetector {

  private EntityID result;

  public URFRoadDetector(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData) {

    super(ai, wi, si, moduleManager, developData);
    
    this.result = null;
  }

  @Override
  public EntityID getTarget() {
    return this.result;
  }

  @Override
  public RoadDetector calc() {

    EntityID position = this.agentInfo.getPosition();

    // Standing in rubble beats everything else.
    if (this.isBlocked(position)) {
      this.result = position;
      // Can Add Logging of system.out.println here
      return this;
    }

    // Keep the old target while it is still blocked.
    if (this.isBlocked(this.result)) {
      return this;
    }

    this.result = null;

    int bestDistance = Integer.MAX_VALUE;

    for (StandardEntity entity : this.worldInfo.getEntitiesOfType(StandardEntityURN.ROAD)) {

      EntityID roadID = entity.getID();
      if (!this.isBlocked(roadID)) {
        continue;
      }
      int distance = this.worldInfo.getDistance(position, roadID);
      if (distance < bestDistance) {
        bestDistance = distance;
        this.result = roadID;
      }
    }

    // Can Add Logging of system.out.println here
    return this;
  }

  /** True when we have actually seen blockades on this road. */
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