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

/**
 * Returns ActionMove or null. Never ActionClear.
 *
 * It only runs when URFPoliceExtActionClear returned null, so it never has to
 * worry about rubble in front of it.
 *
 * Rule: walk to the road target the clear module published. If none, walk to
 * whatever the tactics class passed in (the Search target). If already
 * standing on the goal, step towards a blockade that was too far to cut.
 */
public class URFPoliceExtActionMove extends ExtAction {

  private final PathPlanning pathPlanning;

  private final int clearDistance;

  private EntityID target;

  public URFPoliceExtActionMove(AgentInfo ai, WorldInfo wi, ScenarioInfo si,
      ModuleManager moduleManager, DevelopData developData) {
    super(ai, wi, si, moduleManager, developData);

    this.pathPlanning = moduleManager.getModule(
        "URFPoliceExtActionMove.PathPlanning",
        "adf.impl.module.algorithm.DijkstraPathPlanning");

    int distance = si.getClearRepairDistance();
    this.clearDistance = distance > 0 ? distance : 10000;

    this.target = null;
  }

  // ExtAction has no registerModule(), so the path planner is forwarded by
  // hand. Without this Dijkstra is never initialised and returns nothing.

  @Override
  public ExtAction precompute(PrecomputeData precomputeData) {
    super.precompute(precomputeData);
    if (this.getCountPrecompute() >= 2) {
      return this;
    }
    this.pathPlanning.precompute(precomputeData);
    return this;
  }

  @Override
  public ExtAction resume(PrecomputeData precomputeData) {
    super.resume(precomputeData);
    if (this.getCountResume() >= 2) {
      return this;
    }
    this.pathPlanning.resume(precomputeData);
    return this;
  }

  @Override
  public ExtAction preparate() {
    super.preparate();
    if (this.getCountPreparate() >= 2) {
      return this;
    }
    this.pathPlanning.preparate();
    return this;
  }

  @Override
  public ExtAction updateInfo(MessageManager messageManager) {
    super.updateInfo(messageManager);
    if (this.getCountUpdateInfo() >= 2) {
      return this;
    }
    this.pathPlanning.updateInfo(messageManager);
    return this;
  }

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

  @Override
  public ExtAction calc() {
    this.result = null;

    if (!(this.agentInfo.me() instanceof PoliceForce)) {
      return this;
    }
    PoliceForce police = (PoliceForce) this.agentInfo.me();

    // Road target beats the Search target.
    EntityID goal = URFPoliceExtActionClear.targetOf(this.agentInfo.getID());
    if (goal == null) {
      goal = this.target;
    }
    if (goal == null) {
      // Can Add Logging of system.out.println here
      return this;
    }

    EntityID position = police.getPosition();
    if (position == null) {
      return this;
    }

    if (position.equals(goal)) {
      // Path planning is useless here (start == destination), so step towards
      // the blockade by coordinate instead. Without this the agent freezes
      // when rubble on its own road sits outside clear range.
      this.result = this.stepTowardsBlockade(police, position);
      // Can Add Logging of system.out.println here
      return this;
    }

    this.pathPlanning.setFrom(position);
    this.pathPlanning.setDestination(goal);
    List<EntityID> path = this.pathPlanning.calc().getResult();

    if (path != null && !path.isEmpty()) {
      this.result = new ActionMove(path);
    }

    // Can Add Logging of system.out.println here
    return this;
  }

  private ActionMove stepTowardsBlockade(PoliceForce police, EntityID position) {

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