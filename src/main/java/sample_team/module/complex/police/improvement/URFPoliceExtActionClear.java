package sample_team.module.complex.police.improvement;

import adf.core.agent.action.police.ActionClear;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.component.extaction.ExtAction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

/**
 * Returns ActionClear or null. Never ActionMove.
 *
 * Rule: if there is a blockade on the road I am standing on and I can reach
 * it, cut towards it. Otherwise null, and the tactics class moves on to
 * ExtActionMove.
 *
 * It does not care whether the blockade is "in the way". Clearing rubble that
 * was not blocking anything wastes a few timesteps but never breaks.
 */
public class URFPoliceExtActionClear extends ExtAction {

  /**
   * The road target, published so URFPoliceExtActionMove can read it.
   *
   * DefaultTacticsPoliceForce hands the move module the Search target, not the
   * RoadDetector target, so without this the police never walks to the road it
   * was told to open. Keyed by agent id because every police agent in this JVM
   * shares these statics.
   */
  private static final Map<Integer, EntityID> TARGETS = new ConcurrentHashMap<>();

  private final int clearDistance;

  private EntityID target;

  public URFPoliceExtActionClear(AgentInfo ai, WorldInfo wi, ScenarioInfo si,
      ModuleManager moduleManager, DevelopData developData) {
    
    super(ai, wi, si, moduleManager, developData);

    int distance = si.getClearRepairDistance();
    this.clearDistance = distance > 0 ? distance : 10000;

    this.target = null;
  }

  /** 
   * Read by URFPoliceExtActionMove. 
   * 
   * @param agentID agent entityID
   * @return null if agentID is null
   * @return EntityID 
   * */
  public static EntityID targetOf(EntityID agentID) {
    return agentID == null ? null : TARGETS.get(agentID.getValue());
  }

  @Override
  public ExtAction setTarget(EntityID target) {
    this.target = null;
    if (target == null) {
      return this;
    }
    StandardEntity entity = this.worldInfo.getEntity(target);
    if (entity instanceof Blockade) {
      this.target = ((Blockade) entity).getPosition();
    } else if (entity instanceof Area) {
      this.target = target;
    }
    return this;
  }

  @Override
  public ExtAction calc() {
    this.result = null;

    // Hand the target to the move module.
    int agentKey = this.agentInfo.getID().getValue();
    if (this.target == null) {
      TARGETS.remove(agentKey);
    } else {
      TARGETS.put(agentKey, this.target);
    }

    if (!(this.agentInfo.me() instanceof PoliceForce)) {
      return this;
    }
    PoliceForce police = (PoliceForce) this.agentInfo.me();

    StandardEntity here = this.worldInfo.getEntity(police.getPosition());
    if (!(here instanceof Road)) {
      // Blockades only live on roads.
      return this;
    }

    double agentX = police.getX();
    double agentY = police.getY();

    Blockade nearest = null;
    double aimX = 0;
    double aimY = 0;
    double nearestDistance = Double.MAX_VALUE;

    for (Blockade blockade : this.worldInfo.getBlockades((Road) here)) {
      double[] aim = aimPoint(blockade, agentX, agentY);
      if (aim == null) {
        continue;
      }
      if (aim[2] < nearestDistance) {
        nearestDistance = aim[2];
        aimX = aim[0];
        aimY = aim[1];
        nearest = blockade;
      }
    }

    if (nearest == null) {
      // Road is open, or we cannot see where the rubble is yet.
      // Can Add Logging of system.out.println here
      return this;
    }

    if (nearestDistance > this.clearDistance) {
      // Too far to cut. Null lets URFPoliceExtActionMove walk us closer.
      // Can Add Logging of system.out.println here
      return this;
    }

    double dx = aimX - agentX;
    double dy = aimY - agentY;
    double length = Math.hypot(dx, dy);

    if (length < 1.0) {
      // Standing exactly on it. Dividing by this would give NaN and the
      // kernel would drop the command, so just pick a direction.
      dx = 1.0;
      dy = 0.0;
      length = 1.0;
    }

    int clearX = (int) (agentX + dx / length * this.clearDistance);
    int clearY = (int) (agentY + dy / length * this.clearDistance);

    this.result = new ActionClear(clearX, clearY, nearest);

    // Can Add Logging of system.out.println here
    return this;
  }

  /**
   * Nearest point on a blockade: closest polygon vertex if the shape is known,
   * otherwise its reported centre.
   *
   * @return {x, y, distance} or null when nothing is known about it
   */
  static double[] aimPoint(Blockade blockade, double agentX, double agentY) {
    if (blockade == null) {
      return null;
    }

    if (blockade.isApexesDefined()) {
      int[] apexes = blockade.getApexes();
      if (apexes != null && apexes.length >= 6) {
        double bestX = 0;
        double bestY = 0;
        double best = Double.MAX_VALUE;
        for (int i = 0; i + 1 < apexes.length; i += 2) {
          double distance = Math.hypot(apexes[i] - agentX,
              apexes[i + 1] - agentY);
          if (distance < best) {
            best = distance;
            bestX = apexes[i];
            bestY = apexes[i + 1];
          }
        }
        return new double[] { bestX, bestY, best };
      }
    }

    if (blockade.isXDefined() && blockade.isYDefined()) {
      double x = blockade.getX();
      double y = blockade.getY();
      return new double[] { x, y, Math.hypot(x - agentX, y - agentY) };
    }

    return null;
  }
}