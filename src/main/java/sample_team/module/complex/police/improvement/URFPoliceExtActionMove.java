package sample_team.module.complex.police.improvement;

import adf.core.agent.action.common.ActionMove;
import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.extaction.ExtAction;
import adf.core.component.module.algorithm.PathPlanning;
import java.util.Collections;
import java.util.List;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

import sample_team.module.complex.police.observation.URFPoliceMetrics;
import sample_team.module.complex.police.observation.URFPoliceStuckDetector;
import sample_team.module.complex.police.improvement.URFPoliceEscape;


/**
 * Returns ActionMove or null. Never returns ActionClear.
 *
 * It only runs when URFPoliceExtActionClear returned null, so it never has to
 * reason about rubble in front of it.
 *
 * Rule: walk to the road target the clear module published. If there is none,
 * walk to whatever the tactics class passed in, which is the Search target.
 * If already standing on the goal, step towards a blockade that was too far
 * to cut.
 */
public class URFPoliceExtActionMove extends ExtAction {

  private final PathPlanning pathPlanning;

  private final URFPoliceMetrics metrics;

  private final URFPoliceStuckDetector stuckDetector;

  private final int clearDistance;

  private EntityID target;
  
  private final URFPoliceEscape escape;

  /**
   * Creates the module, resolves its path planner, and attaches it to this
   * agent's observation objects.
   *
   * @param ai agent information supplied by the ADF
   * @param wi world model supplied by the ADF
   * @param si scenario configuration supplied by the ADF
   * @param moduleManager module registry supplied by the ADF
   * @param developData development configuration supplied by the ADF
   */
  public URFPoliceExtActionMove(AgentInfo ai, WorldInfo wi, ScenarioInfo si,
      ModuleManager moduleManager, DevelopData developData) {
    super(ai, wi, si, moduleManager, developData);

    this.pathPlanning = moduleManager.getModule(
        "URFPoliceExtActionMove.PathPlanning",
        "adf.impl.module.algorithm.DijkstraPathPlanning");

    int distance = si.getClearRepairDistance();
    this.clearDistance = distance > 0 ? distance : 10000;

    this.metrics = URFPoliceMetrics.forAgent(ai.getID());
    this.stuckDetector = URFPoliceStuckDetector.forAgent(ai.getID());
    this.escape = URFPoliceEscape.forAgent(ai.getID());

    this.target = null;
  }

  /*
   * ExtAction has no registerModule(), so the path planner is forwarded by
   * hand in the four lifecycle methods below. Without this Dijkstra is never
   * initialised and always returns an empty path.
   */

  /**
   * Forwards the precompute phase to the path planner.
   *
   * @param precomputeData the precomputation store supplied by the ADF
   *
   * @return this module, as required by the ADF ExtAction contract
   */
  @Override
  public ExtAction precompute(PrecomputeData precomputeData) {
    super.precompute(precomputeData);
    if (this.getCountPrecompute() >= 2) {
      return this;
    }
    this.pathPlanning.precompute(precomputeData);
    return this;
  }

  /**
   * Forwards the resume phase to the path planner.
   *
   * @param precomputeData the precomputation store supplied by the ADF
   *
   * @return this module, as required by the ADF ExtAction contract
   */
  @Override
  public ExtAction resume(PrecomputeData precomputeData) {
    super.resume(precomputeData);
    if (this.getCountResume() >= 2) {
      return this;
    }
    this.pathPlanning.resume(precomputeData);
    return this;
  }

  /**
   * Forwards the non-precompute startup phase to the path planner.
   *
   * @return this module, as required by the ADF ExtAction contract
   */
  @Override
  public ExtAction preparate() {
    super.preparate();
    if (this.getCountPreparate() >= 2) {
      return this;
    }
    this.pathPlanning.preparate();
    return this;
  }

  /**
   * Forwards the per-timestep update to the path planner.
   *
   * @param messageManager the communication manager supplied by the ADF
   *
   * @return this module, as required by the ADF ExtAction contract
   */
  @Override
  public ExtAction updateInfo(MessageManager messageManager) {
    super.updateInfo(messageManager);
    if (this.getCountUpdateInfo() >= 2) {
      return this;
    }
    this.pathPlanning.updateInfo(messageManager);
    return this;
  }

  /**
   * Accepts the fallback target from the tactics class.
   *
   * Everything is resolved to the area that contains it, because only an area
   * can be walked to: a blockade becomes its road, a human becomes their
   * current position.
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
      entity = this.worldInfo.getEntity(((Blockade) entity).getPosition());
    } else if (entity instanceof Human) {
      entity = this.worldInfo.getPosition((Human) entity);
    }
    if (entity instanceof Area) {
      this.target = entity.getID();
    }
    return this;
  }

  /**
   * Chooses this timestep's move action and records the outcome.
   *
   * This is the last autonomous stage, so the stuck detector is evaluated
   * unconditionally. A null result here means the tactics class will emit
   * ActionRest, which the metrics record as TACTICS_FALLBACK_REST.
   *
   * @return this module, as required by the ADF ExtAction contract
   */
  @Override
  public ExtAction calc() {
    long startNanos = System.nanoTime();
    this.result = null;

    // The road target beats the Search target.
    EntityID goal = URFPoliceExtActionClear.targetOf(this.agentInfo.getID());
    if (goal == null) {
      goal = this.target;
    }

    this.result = this.decideMove(goal);

    this.metrics.recordMoveModuleResult(this.agentInfo.getTime(), goal,
        this.result, System.nanoTime() - startNanos);

    this.stuckDetector.evaluate(this.metrics);
    // Can Add Logging of system.out.println here

    return this;
  }

  /**
   * Applies the movement rule.
   *
   * @param goal the area to reach, may be null
   *
   * @return the move command, or null when there is nowhere useful to go
   */
  private ActionMove decideMove(EntityID goal) {

    if (goal == null || !(this.agentInfo.me() instanceof PoliceForce)) {
      return null;
    }
    PoliceForce police = (PoliceForce) this.agentInfo.me();

    EntityID position = police.getPosition();
    if (position == null) {
      return null;
    }

    if (this.escape.isStuck()) {
      ActionMove forced = this.escape.escapeMove(this.worldInfo, position);
      if (forced != null) {
        return forced;
      }
    }

    if (position.equals(goal)) {
      return this.stepTowardsBlockade(police, position);
    }

    this.pathPlanning.setFrom(position);
    this.pathPlanning.setDestination(goal);
    List<EntityID> path = this.pathPlanning.calc().getResult();

    if (path == null || path.isEmpty()) {
      return null;
    }
    return new ActionMove(path);
  }

  /**
   * Moves to a coordinate inside the current road, towards a blockade the
   * clear module reported as out of reach.
   *
   * Path planning cannot help here because the start and the destination are
   * the same area, so the route comes back empty and ActionMove(path) would be
   * a no-op. Without this branch the agent freezes for the rest of the run
   * whenever rubble on its own road sits outside clearRepairDistance.
   *
   * @param police the agent, used for its coordinates
   * @param position the road the agent is standing on
   *
   * @return the repositioning command, or null when nothing is out of reach
   */
  private ActionMove stepTowardsBlockade(PoliceForce police,
      EntityID position) {

    StandardEntity here = this.worldInfo.getEntity(position);
    if (!(here instanceof Road)) {
      return null;
    }

    double agentX = police.getX();
    double agentY = police.getY();

    double[] best = null;
    for (Blockade blockade : this.worldInfo.getBlockades((Road) here)) {
      double[] aim = URFPoliceExtActionClear.aimPoint(blockade, agentX, agentY);
      if (aim == null) {
        continue;
      }
      if (best == null || aim[2] < best[2]) {
        best = aim;
      }
    }

    if (best == null || best[2] <= this.clearDistance) {
      // Nothing there, or close enough that the clear module owns it.
      return null;
    }

    return new ActionMove(Collections.singletonList(position), (int) best[0],
        (int) best[1]);
  }
}