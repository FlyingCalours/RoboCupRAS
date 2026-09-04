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

import sample_team.module.complex.police.observation.URFPoliceMetrics;
import sample_team.module.complex.police.observation.URFPoliceStuckDetector;
import sample_team.module.complex.police.improvement.URFPoliceEscape;

/**
 * Returns ActionClear or null. Never returns ActionMove.
 *
 * Rule: if there is a blockade on the road under my feet and I can reach it,
 * cut towards it. Otherwise null, and DefaultTacticsPoliceForce falls through
 * to URFPoliceExtActionMove.
 *
 * It does not check whether the blockade is actually in the way. Clearing
 * rubble that was not blocking anything wastes a few timesteps but never
 * breaks the agent.
 */
public class URFPoliceExtActionClear extends ExtAction {

  /**
   * Road target published for URFPoliceExtActionMove to read.
   *
   * DefaultTacticsPoliceForce hands the move module the Search target, not the
   * RoadDetector target, so without this the police never walks to the road it
   * was told to open. Keyed by agent id because every police agent in this JVM
   * shares these statics.
   */
  private static final Map<Integer, EntityID> TARGETS = new ConcurrentHashMap<>();

  private final URFPoliceMetrics metrics;

  private final URFPoliceStuckDetector stuckDetector;

  private final int clearDistance;

  private EntityID target;

  private final URFPoliceEscape escape;

  /**
   * Creates the module and attaches it to this agent's observation objects.
   *
   * @param ai agent information supplied by the ADF
   * @param wi world model supplied by the ADF
   * @param si scenario configuration supplied by the ADF
   * @param moduleManager module registry supplied by the ADF
   * @param developData development configuration supplied by the ADF
   */
  public URFPoliceExtActionClear(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si,
    ModuleManager moduleManager, 
    DevelopData developData) {
    super(ai, wi, si, moduleManager, developData);

    int distance = si.getClearRepairDistance();
    this.clearDistance = distance > 0 ? distance : 10000;

    this.metrics = URFPoliceMetrics.forAgent(ai.getID());
    this.stuckDetector = URFPoliceStuckDetector.forAgent(ai.getID());

    this.target = null;
    this.escape = URFPoliceEscape.forAgent(ai.getID());
  }

  /**
   * Reads back the road target this module was given for an agent.
   *
   * Called by URFPoliceExtActionMove so it can walk to the road the detector
   * chose instead of the Search target the tactics class passes it.
   *
   * @param agentID the police force whose target is wanted, may be null
   *
   * @return the road target published this timestep, or null when the agent is
   *   unknown or had no target
   */
  public static EntityID targetOf(EntityID agentID) {
    return agentID == null ? null : TARGETS.get(agentID.getValue());
  }

  /**
   * Accepts the target chosen by URFRoadDetector.
   *
   * A blockade is resolved to the road it sits on. Anything that is not an
   * area is discarded.
   *
   * @param target the entity handed down by the tactics class, may be null
   *
   * @return this module, as required by the ADF ExtAction contract
   */
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

  /**
   * Chooses this timestep's clear action, publishes the target for the move
   * module, and records the outcome for observation.
   *
   * The stuck detector is only evaluated when an action was produced, because
   * a null result means URFPoliceExtActionMove still has to run and will
   * evaluate instead. evaluate() ignores repeated calls within one timestep,
   * so exactly one of the two takes effect.
   *
   * @return this module, as required by the ADF ExtAction contract
   */
  @Override
  public ExtAction calc() {
    long startNanos = System.nanoTime();
    this.result = null;

    this.publishTarget();

    this.result = this.decideClear();

    this.metrics.recordClearModuleResult(this.agentInfo.getTime(), this.target,
        this.result, System.nanoTime() - startNanos);

    if (this.result != null) {
      this.stuckDetector.evaluate(this.metrics);
      // Can Add Logging of system.out.println here
    }

    return this;
  }

  /**
   * Copies the current target into the shared map so the move module can read
   * it, or removes the entry when there is no target.
   *
   * ConcurrentHashMap rejects null values, which is why removal is used
   * instead of storing null.
   */
  private void publishTarget() {
    int agentKey = this.agentInfo.getID().getValue();
    if (this.target == null) {
      TARGETS.remove(agentKey);
    } else {
      TARGETS.put(agentKey, this.target);
    }
  }

  /**
   * Applies the clearing rule for the road the agent is standing on.
   *
   * Returns null in every case where cutting is impossible or pointless, which
   * hands the timestep to the move module.
   *
   * @return the clear command, or null when there is nothing to cut
   */
  private ActionClear decideClear() {

    if (this.escape.isStuck()) {
      return null;    // stand aside so the move module can force an escape
    }

    if (!(this.agentInfo.me() instanceof PoliceForce)) {
      return null;
    }
    
    PoliceForce police = (PoliceForce) this.agentInfo.me();

    StandardEntity here = this.worldInfo.getEntity(police.getPosition());
    if (!(here instanceof Road)) {
      // Blockades only live on roads.
      return null;
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
      return null;
    }

    if (nearestDistance > this.clearDistance) {
      // Too far to cut. Null lets URFPoliceExtActionMove walk us closer.
      return null;
    }

    double dx = aimX - agentX;
    double dy = aimY - agentY;
    double length = Math.hypot(dx, dy);

    if (length < 1.0) {
      // Standing exactly on it. Dividing by this would produce NaN and the
      // kernel would drop the command, so just pick a direction.
      dx = 1.0;
      dy = 0.0;
      length = 1.0;
    }

    int clearX = (int) (agentX + dx / length * this.clearDistance);
    int clearY = (int) (agentY + dy / length * this.clearDistance);

    // ActionClear(x, y, blockade) maps to AKClearArea. The one argument
    // ActionClear(blockade) is the legacy AKClear and most servers reject it.
    return new ActionClear(clearX, clearY, nearest);
  }

  /**
   * Finds the point on a blockade that the agent should aim at: the closest
   * polygon vertex when the shape is known, otherwise the reported centre.
   *
   * Package visible so URFPoliceExtActionMove uses the identical calculation.
   * If the two modules disagreed on where a blockade is, one could decide it
   * is in reach while the other decides it is not, and the agent would
   * oscillate between clearing and stepping.
   *
   * @param blockade the blockade to aim at, may be null
   * @param agentX the agent's x coordinate in millimetres
   * @param agentY the agent's y coordinate in millimetres
   *
   * @return an array of {x, y, distance}, or null when the blockade's position
   *   is completely unknown
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
          double distance =
              Math.hypot(apexes[i] - agentX, apexes[i + 1] - agentY);
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