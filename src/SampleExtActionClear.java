package sample_team.module.complex;

/* Action Module - move,clear,rest */
import adf.core.agent.action.Action;
import adf.core.agent.action.common.ActionMove;
import adf.core.agent.action.common.ActionRest;
import adf.core.agent.action.police.ActionClear;

/* Big 6 data */
import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;

/* Precompute Data ? */
import adf.core.agent.precompute.PrecomputeData;

/* ExtAction and PathPlanning */
import adf.core.component.extaction.ExtAction;
import adf.core.component.module.algorithm.PathPlanning;

/* Container required by API - ArrayList, Collection and List */
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*Exeception Handling */
import rescuecore2.config.NoSuchConfigOptionException;

/* HitBox */
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

/* Entity */
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

/**
 * Turns "open this road" into a concrete police command.
 *
 * <p>
 * {@link SampleRoadDetector} says <em>which</em> road; this class works out
 * <em>how</em>. Every cycle it answers one of four things:
 * <ol>
 * <li>rest at a refuge, if the agent is about to die of accumulated damage;</li>
 * <li>clear, if a blockade stands between the agent and where it wants to go,
 * or if the agent is standing on the target road with rubble left on it;</li>
 * <li>move, along the path planner's route towards the target road;</li>
 * <li>nothing ({@code null}), which lets the tactics class fall through to
 * search-and-move.</li>
 * </ol>
 *
 * <p>
 * <b>Clearing model.</b> The modern server takes {@code AKClearArea(x, y)}: the
 * agent cuts a corridor of {@code clearRepairDistance} length from itself
 * towards the point {@code (x, y)}. So the interesting question is never "which
 * blockade" but "in which direction", and the answer is: towards the exit we
 * are trying to use. That is what {@link #clearFromHere} computes — it aims at
 * the midpoint of the edge leading to the next area on the route, and only
 * issues a clear if a blockade actually intersects that line. If nothing is in
 * the way, it returns {@code null} and the agent simply drives on. On the
 * target road itself there is no exit to aim at, so it sweeps the road properly
 * by repeatedly attacking the nearest blockade vertex until the road is empty.
 *
 * <p>
 * The {@code ActionClear(x, y, blockade)} constructor is used rather than the
 * legacy {@code ActionClear(blockade)}, because the latter maps to
 * {@code AKClear}, which many current server configurations reject.
 *
 * <p>
 * <b>Deadlock handling.</b> A police agent that keeps issuing the same clear
 * command from the same spot is stuck — typically wedged on a blockade edge
 * where the cut does not connect. After {@link #forcedMove} identical commands
 * the agent is forced to move into the cut instead, which shakes it loose.
 */
public class SampleExtActionClear extends ExtAction {

  private PathPlanning pathPlanning;

  /** Length of the corridor a single clear command cuts, in mm. */
  private int clearDistance;

  /** Identical clear commands tolerated before forcing a move. */
  private int forcedMove;

  /** Damage per cycle above which the agent breaks off and rests. */
  private int thresholdRest;

  private int kernelTime;

  private EntityID target;

  private int lastClearX;
  private int lastClearY;
  private int repeatCount;

  public SampleExtActionClear(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
    super(ai, wi, si, moduleManager, developData);

    this.clearDistance = si.getClearRepairDistance();
    this.forcedMove = developData.getInteger(
        "sample_team.module.complex.SampleExtActionClear.forcedMove", 3);
    this.thresholdRest = developData.getInteger(
        "sample_team.module.complex.SampleExtActionClear.rest", 100);

    this.target = null;
    this.kernelTime = -1;
    this.lastClearX = 0;
    this.lastClearY = 0;
    this.repeatCount = 0;

    switch (si.getMode()) {
      case PRECOMPUTATION_PHASE:
      case PRECOMPUTED:
      case NON_PRECOMPUTE:
        this.pathPlanning = moduleManager.getModule(
            "SampleExtActionClear.PathPlanning",
            "adf.impl.module.algorithm.DijkstraPathPlanning");
        break;
    }
  }


  // ---------------------------------------------------------------- lifecycle

  @Override
  public ExtAction precompute(PrecomputeData precomputeData) {
    super.precompute(precomputeData);
    if (this.getCountPrecompute() >= 2) {
      return this;
    }
    this.pathPlanning.precompute(precomputeData);
    this.readKernelTime();
    return this;
  }


  @Override
  public ExtAction resume(PrecomputeData precomputeData) {
    super.resume(precomputeData);
    if (this.getCountResume() >= 2) {
      return this;
    }
    this.pathPlanning.resume(precomputeData);
    this.readKernelTime();
    return this;
  }


  @Override
  public ExtAction preparate() {
    super.preparate();
    if (this.getCountPreparate() >= 2) {
      return this;
    }
    this.pathPlanning.preparate();
    this.readKernelTime();
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


  private void readKernelTime() {
    try {
      this.kernelTime = this.scenarioInfo.getKernelTimesteps();
    } catch (NoSuchConfigOptionException e) {
      this.kernelTime = -1;
    }
  }


  /**
   * Accepts a road directly, or a blockade (in which case the road it sits on
   * becomes the target). Anything else clears the target.
   * 
   * Receive only Road, Blockade and Area
   */
  @Override
  public ExtAction setTarget(EntityID target) {
    this.target = null;
    if (target == null) {
      return this;
    }
    StandardEntity entity = this.worldInfo.getEntity(target);
    if (entity instanceof Road) {
      this.target = target;
    } 
    else if (entity instanceof Blockade) {
      Blockade blockade = (Blockade) entity;
      if (blockade.isPositionDefined()) {
        this.target = blockade.getPosition();
      }
    } 
    else if (entity instanceof Area) {
      this.target = target;
    }
    return this;
  }


  // ------------------------------------------------------------------- action

  @Override
  public ExtAction calc() {
    this.result = null;

    // DEFINE Police
    if (!(this.agentInfo.me() instanceof PoliceForce)) {
      return this;
    }
    PoliceForce police = (PoliceForce) this.agentInfo.me();

    // (1) Survival first. A dead police agent clears nothing.
    if (this.needRest(police)) {
      Action rest = this.calcRest(police);
      if (rest != null) {
        this.result = rest;
        return this;
      }
    }

    if (this.target == null) {
      return this;
    }

    // GET Area (current position and target position)
    EntityID position = police.getPosition();
    StandardEntity positionEntity = this.worldInfo.getEntity(position);
    StandardEntity targetEntity = this.worldInfo.getEntity(this.target);
    if (!(positionEntity instanceof Area) || !(targetEntity instanceof Area)) {
      return this;
    }

    // GET Path, but path is too general, you need nextStep
    List<EntityID> path = null;
    EntityID nextArea = null;
    if (!position.equals(this.target)) {
      path = this.pathPlanning.getResult(position, this.target);
      nextArea = this.nextStep(path, position);
    }

    // (2) Cut through whatever is in the way, here and now.
    if (positionEntity instanceof Road) {
      // clearFromHere is self-defined
      Action clear = this.clearFromHere(police, (Road) positionEntity,nextArea);
      if (clear != null) {
        this.result = clear;
        return this;
      }
    }

    // (3) The way out is open, so drive on.
    if (path != null && !path.isEmpty()) {
      this.result = new ActionMove(path);
    }
    return this;
  }


  /**
   * Decides the clear command for the road the agent is standing on.
   *
   * @param nextArea
   *   the next area on the route, or {@code null} when the agent has already
   *   arrived at the target road
   *
   * @return a clear (or an unwedging move), or {@code null} if nothing here
   *   needs clearing
   */
  private Action clearFromHere(PoliceForce police, Road road, EntityID nextArea) {
    if (!road.isBlockadesDefined() || road.getBlockades().isEmpty()) {
      return null;
    }

    double agentX = police.getX();
    double agentY = police.getY();
    Collection<Blockade> blockades = this.worldInfo.getBlockades(road);

    if (nextArea != null) {
      // Passing through: only clear what actually blocks the exit we want.
      Edge edge = road.getEdgeTo(nextArea);
      if (edge == null) {
        return null;
      }
      double exitX = (edge.getStartX() + edge.getEndX()) / 2.0;
      double exitY = (edge.getStartY() + edge.getEndY()) / 2.0;
      for (Blockade blockade : blockades) {
        if (blockade == null || !blockade.isApexesDefined()) {
          continue;
        }
        if (this.intersects(agentX, agentY, exitX, exitY, blockade)) {
          return this.cutTowards(road, blockade, agentX, agentY, exitX, exitY);
        }
      }
      return null;
    }

    // Arrived at the target road: sweep it until nothing is left standing.
    Blockade nearest = null;
    double nearestDistance = Double.MAX_VALUE;
    double aimX = 0;
    double aimY = 0;
    for (Blockade blockade : blockades) {
      if (blockade == null || !blockade.isApexesDefined()) {
        continue;
      }
      int[] apexes = blockade.getApexes();
      for (int i = 0; i + 1 < apexes.length; i += 2) {
        double distance = Math.hypot(apexes[i] - agentX, apexes[i + 1] - agentY);
        if (distance < nearestDistance) {
          nearestDistance = distance;
          nearest = blockade;
          aimX = apexes[i];
          aimY = apexes[i + 1];
        }
      }
    }
    if (nearest == null) {
      return null;
    }
    if (nearestDistance > this.clearDistance) {
      // Out of reach: close in on it first.
      List<EntityID> here = new ArrayList<>();
      here.add(road.getID());
      return new ActionMove(here, (int) aimX, (int) aimY);
    }
    return this.cutTowards(road, nearest, agentX, agentY, aimX, aimY);
  }


  /**
   * Emits the actual clear command, scaled to the server's clear distance, with
   * the anti-deadlock check applied.
   */
  private Action cutTowards(Road road, Blockade blockade, double agentX,double agentY, double aimX, double aimY) {
    Vector2D direction = new Point2D(aimX, aimY).minus(new Point2D(agentX, agentY));

    // If zero vector, force move to one point , if hit wall , trigger forcedMove logic
    // Future Improvement : Walk within a distance inside the box
    if (direction.getLength() == 0) {
      List<EntityID> here = new ArrayList<>();
      here.add(road.getID);

      int nudgeX = (int) (agentX + clearDistance);
      int nudgeY = (int) agentY;

      return new ActionMove(here,nudgeX,nudgeY);
    }
    Vector2D scaled = direction.normalised().scale(this.clearDistance);
    int clearX = (int) (agentX + scaled.getX());
    int clearY = (int) (agentY + scaled.getY());

    if (this.samePoint(this.lastClearX, this.lastClearY, clearX, clearY)) {
      this.repeatCount++;
      if (this.repeatCount >= this.forcedMove) {
        this.repeatCount = 0;
        List<EntityID> here = new ArrayList<>();
        here.add(road.getID());
        return new ActionMove(here, clearX, clearY);
      }
    } 
    else {
      this.repeatCount = 0;
    }
    this.lastClearX = clearX;
    this.lastClearY = clearY;

    return new ActionClear(clearX, clearY, blockade);
  }


  /**
   * The path planner's result excludes the agent's own position, so the first
   * element is normally the next area. The index lookup is kept as a guard in
   * case a different planner is configured that includes the start.
   */
  private EntityID nextStep(List<EntityID> path, EntityID position) {
    if (path == null || path.isEmpty()) {
      return null;
    }
    int index = path.indexOf(position);
    if (index >= 0) {
      return (index + 1 < path.size()) ? path.get(index + 1) : null;
    }
    return path.get(0);
  }


  // -------------------------------------------------------------- geometry

  /** True if the segment agent-to-aim crosses any edge of the blockade. */
  private boolean intersects(double fromX, double fromY, double toX, double toY,Blockade blockade) {
    List<Line2D> lines = GeometryTools2D.pointsToLines(GeometryTools2D.vertexArrayToPoints(blockade.getApexes()), true);
    for (Line2D line : lines) {
      Point2D start = line.getOrigin();
      Point2D end = line.getEndPoint();
      if (java.awt.geom.Line2D.linesIntersect(fromX, fromY, toX, toY,
          start.getX(), start.getY(), end.getX(), end.getY())) {
        return true;
      }
    }
    return false;
  }


  private boolean samePoint(double aX, double aY, double bX, double bY) {
    double tolerance = 1000.0D;
    return Math.abs(aX - bX) < tolerance && Math.abs(aY - bY) < tolerance;
  }


  // ------------------------------------------------------------------ resting

  private boolean needRest(Human agent) {
    if (!agent.isHPDefined() || !agent.isDamageDefined()) {
      return false;
    }
    int hp = agent.getHP();
    int damage = agent.getDamage();
    if (hp == 0 || damage == 0) {
      return false;
    }
    if (this.kernelTime == -1) {
      this.readKernelTime();
    }
    int survivableCycles = (hp / damage) + ((hp % damage) != 0 ? 1 : 0);
    return damage >= this.thresholdRest
        || (survivableCycles + this.agentInfo.getTime()) < this.kernelTime;
  }


  private Action calcRest(PoliceForce police) {
    EntityID position = police.getPosition();
    Collection<EntityID> refuges = this.worldInfo
        .getEntityIDsOfType(StandardEntityURN.REFUGE);
    if (refuges.isEmpty()) {
      return null;
    }
    if (refuges.contains(position)) {
      return new ActionRest();
    }
    this.pathPlanning.setFrom(position);
    this.pathPlanning.setDestination(refuges);
    List<EntityID> path = this.pathPlanning.calc().getResult();
    if (path != null && !path.isEmpty()) {
      return new ActionMove(path);
    }
    return null;
  }
}