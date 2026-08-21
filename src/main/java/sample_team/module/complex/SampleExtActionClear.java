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

/* Communication */
import adf.core.agent.communication.standard.bundle.information.MessagePoliceForce;
import adf.core.agent.communication.standard.bundle.information.MessageRoad;
import adf.core.component.communication.CommunicationMessage;

/* Precompute Data */
import adf.core.agent.precompute.PrecomputeData;

/* ExtAction and PathPlanning */
import adf.core.component.extaction.ExtAction;
import adf.core.component.module.algorithm.PathPlanning;

/* Containers */
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/* Exception Handling */
import rescuecore2.config.NoSuchConfigOptionException;

/* Geometry */
import rescuecore2.misc.geometry.Point2D;

/* Entity */
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
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
 * <li>rest at a safe, reachable refuge, if the agent is about to die;</li>
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
 * issues a clear if a blockade actually intersects that line. On the target
 * road itself there is no exit to aim at, so it sweeps the road by repeatedly
 * attacking the nearest blockade vertex until the road is empty.
 *
 * <p>
 * The {@code ActionClear(x, y, blockade)} constructor is used rather than the
 * legacy {@code ActionClear(blockade)}, because the latter sets
 * {@code useOldFunction} and maps to {@code AKClear}, which many current server
 * configurations reject.
 *
 * <p>
 * <b>What this class does not decide.</b> It does not choose targets and it
 * does not arbitrate between agents — that is the target allocator's job. The
 * only teammate logic here is a narrow safety valve (see
 * {@link #shouldYield}) plus the reporting the allocator needs in order to
 * arbitrate at all.
 */
public class SampleExtActionClear extends ExtAction {

  // LOGGER
  private static final Logger LOGGER = Logger.getLogger(SampleExtActionClear.class.getName());

  // CONFIG
  private static final String CONFIG_PREFIX = "sample_team.module.complex.SampleExtActionClear.";
  private static final String PATH_PLANNING_KEY = "SampleExtActionClear.PathPlanning";
  private static final String DEFAULT_PATH_PLANNING = "adf.impl.module.algorithm.DijkstraPathPlanning";

  // Error Handling Conflict
  private static final int CONFIG_ABSENT = Integer.MIN_VALUE;
  private static final int FALLBACK_CLEAR_DISTANCE = 10000;

  // Instance Fields : Wiring
  private final PathPlanning pathPlanning;
  private MessageManager messageManager;

  // Instance Fields : Configuration
  private final int clearDistance;
  private final int forcedMove;
  private final int thresholdRest;
  private final double sameCutTolerance;
  private final int maxApproachUnseen;
  private final boolean deconflict;

  private int kernelTime;

  private EntityID target;

  private int lastClearX;
  private int lastClearY;
  private int repeatCount;
  private int sweepAttempt;

  /** Road whose blockade shapes we are currently trying to perceive. */
  private EntityID unseenRoad;
  private int unseenApproach;

  /** target road -> teammate that claimed it, rebuilt every cycle. */
  private final Map<EntityID, EntityID> teammateTarget = new HashMap<>();

  /** teammate -> its reported position, rebuilt every cycle. */
  private final Map<EntityID, EntityID> teammatePosition = new HashMap<>();

  /** road -> last passability we broadcast, so we do not spam the channel. */
  private final Map<EntityID, Boolean> reportedRoadState = new HashMap<>();

  public SampleExtActionClear(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
    super(ai, wi, si, moduleManager, developData);

    // ---- FIX 7: every tunable is range-checked and says so when it falls back
    int kernelClearDistance = si.getClearRepairDistance();
    if (kernelClearDistance <= 0) {
      LOGGER.warning("scenario reports clearRepairDistance=" + kernelClearDistance
          + ", which cannot be right; using " + FALLBACK_CLEAR_DISTANCE);
      kernelClearDistance = FALLBACK_CLEAR_DISTANCE;
    }
    this.clearDistance = kernelClearDistance;

    this.forcedMove = readConfig(developData, "forcedMove", 3, 1, 100);
    this.thresholdRest = readConfig(developData, "rest", 100, 1, 10000);
    this.sameCutTolerance = readConfig(developData, "sameCutTolerance", 1000, 1,
        this.clearDistance);
    this.maxApproachUnseen = readConfig(developData, "maxApproachUnseen", 5, 1,
        100);
    this.deconflict = readConfig(developData, "deconflict", 1, 0, 1) == 1;

    this.target = null;
    this.kernelTime = -1;
    this.lastClearX = 0;
    this.lastClearY = 0;
    this.repeatCount = 0;
    this.sweepAttempt = 0;
    this.unseenRoad = null;
    this.unseenApproach = 0;

    // ---- FIX 5: a default branch, and a loud failure instead of a later NPE
    PathPlanning resolved;
    switch (si.getMode()) {
      case PRECOMPUTATION_PHASE:
      case PRECOMPUTED:
      case NON_PRECOMPUTE:
        resolved = moduleManager.getModule(PATH_PLANNING_KEY, DEFAULT_PATH_PLANNING);
        break;
      default:
        LOGGER.warning("unhandled ScenarioInfo mode " + si.getMode() + "; falling back to " + DEFAULT_PATH_PLANNING);
        resolved = moduleManager.getModule(PATH_PLANNING_KEY, DEFAULT_PATH_PLANNING);
        break;
    }
    if (resolved == null) {
      // Better to die here, with a sentence explaining why, than to die in
      // calc() with a bare NullPointerException 40 cycles into a run.
      throw new IllegalStateException("module.cfg key '" + PATH_PLANNING_KEY + 
      "' resolved to no PathPlanning implementation (default '"
          + DEFAULT_PATH_PLANNING + "' also unavailable)");
    }
    this.pathPlanning = resolved;
  }


  /**
   * FIX 7. {@code DevelopData.getInteger} returns the default both when the key
   * is missing and when develop mode is off, and never says which. Passing a
   * sentinel distinguishes the two, so a typo in {@code develop.json} produces
   * a warning instead of silence, while a normal (non-develop) run stays quiet.
   */
  private static int readConfig(DevelopData developData, String shortKey, int fallback, int min, int max) {
    String key = CONFIG_PREFIX + shortKey;
    int value = developData.getInteger(key, CONFIG_ABSENT);
    if (value == CONFIG_ABSENT) {
      if (developData.isDevelopMode()) {
        LOGGER.warning("develop mode is on but key '" + key
            + "' was not found; using default " + fallback
            + " (check the spelling)");
      }
      return fallback;
    }
    if (value < min || value > max) {
      LOGGER.warning("config '" + key + "' = " + value + " is outside ["
          + min + ", " + max + "]; using default " + fallback);
      return fallback;
    }
    return value;
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
    // Stashed before the guard so calc() always has this cycle's manager.
    this.messageManager = messageManager;
    if (this.getCountUpdateInfo() >= 2) {
      return this;
    }
    this.pathPlanning.updateInfo(messageManager);
    this.readTeammateReports(messageManager);
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
    } else if (entity instanceof Blockade) {
      Blockade blockade = (Blockade) entity;
      if (blockade.isPositionDefined()) {
        this.target = blockade.getPosition();
      }
    } else if (entity instanceof Area) {
      this.target = target;
    }
    return this;
  }


  // ------------------------------------------------------------------- action

  @Override
  public ExtAction calc() {
    this.result = null;

    if (!(this.agentInfo.me() instanceof PoliceForce)) {
      return this;
    }
    PoliceForce police = (PoliceForce) this.agentInfo.me();
    EntityID position = police.getPosition();

    // (1) Survival first. A dead police agent clears nothing.
    if (this.needRest(police)) {
      Action rest = this.calcRest(police);
      if (rest != null) {
        this.result = rest;
        this.report(police, rest, null);
        return this;
      }
    }

    if (this.target == null) {
      return this;
    }

    StandardEntity positionEntity = this.worldInfo.getEntity(position);
    StandardEntity targetEntity = this.worldInfo.getEntity(this.target);
    if (!(positionEntity instanceof Area) || !(targetEntity instanceof Area)) {
      return this;
    }

    // (2) FIX 1, narrow version: if a teammate is already standing on the road
    // we were sent to, doing nothing is better than piling on. Returning null
    // lets the tactics class fall through to search instead of freezing.
    if (this.deconflict && this.shouldYield(police, position)) {
      this.report(police, null, null);
      return this;
    }

    List<EntityID> path = null;
    EntityID nextArea = null;
    if (!position.equals(this.target)) {
      path = this.pathPlanning.getResult(position, this.target);
      nextArea = this.nextStep(path, position);
    }

    // (3) Cut through whatever is in the way, here and now.
    if (positionEntity instanceof Road) {
      Road here = (Road) positionEntity;
      Action clear = this.clearFromHere(police, here, nextArea);
      if (clear != null) {
        this.result = clear;
        this.report(police, clear, this.target);
        return this;
      }
      // FIX 3: nothing left to cut. If this is the road we were sent to open,
      // that is news the allocator and the station need.
      if (position.equals(this.target)) {
        this.reportRoad(here, null, true);
      }
    }

    // (4) The way out is open, so drive on.
    if (path != null && !path.isEmpty()) {
      this.result = new ActionMove(path);
    }
    this.report(police, this.result, this.target);
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
    Collection<Blockade> blockades = this.worldInfo.getBlockades(road);
    if (blockades.isEmpty()) {
      this.forgetUnseen(road.getID());
      return null;
    }

    double agentX = police.getX();
    double agentY = police.getY();

    // FIX 10: separate "I can see its shape" from "I know something is there".
    List<Blockade> visible = new ArrayList<>();
    List<Blockade> unseen = new ArrayList<>();
    for (Blockade blockade : blockades) {
      if (blockade == null) {
        continue;
      }
      if (blockade.isApexesDefined()
          && ClearGeometry.isUsablePolygon(blockade.getApexes())) {
        visible.add(blockade);
      } else {
        unseen.add(blockade);
      }
    }

    if (visible.isEmpty()) {
      // Everything on this road is a rumour. Walk closer so the sensors can
      // resolve it, instead of skipping it forever.
      return this.approachUnseen(road, unseen, agentX, agentY);
    }
    this.forgetUnseen(road.getID());

    if (nextArea != null) {
      // Passing through: only clear what actually blocks the exit we want.
      Edge edge = road.getEdgeTo(nextArea);
      if (edge == null) {
        return null;
      }
      double exitX = (edge.getStartX() + edge.getEndX()) / 2.0;
      double exitY = (edge.getStartY() + edge.getEndY()) / 2.0;
      for (Blockade blockade : visible) {
        if (ClearGeometry.segmentHitsPolygon(agentX, agentY, exitX, exitY,
            blockade.getApexes())) {
          this.reportRoad(road, blockade, false);
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
    for (Blockade blockade : visible) {
      int[] apexes = blockade.getApexes();
      int index = ClearGeometry.nearestApexIndex(apexes, agentX, agentY);
      if (index < 0) {
        continue;
      }
      double distance = Math.hypot(apexes[index] - agentX,
          apexes[index + 1] - agentY);
      if (distance < nearestDistance) {
        nearestDistance = distance;
        nearest = blockade;
        aimX = apexes[index];
        aimY = apexes[index + 1];
      }
    }
    if (nearest == null) {
      return null;
    }
    this.reportRoad(road, nearest, false);
    if (nearestDistance > this.clearDistance) {
      // Out of reach: close in on it first.
      return new ActionMove(singleton(road.getID()), (int) aimX, (int) aimY);
    }
    return this.cutTowards(road, nearest, agentX, agentY, aimX, aimY);
  }


  /**
   * FIX 10. A blockade with undefined apexes is not a reason to give up on the
   * road; it is a reason to get closer. This walks towards it for a bounded
   * number of cycles, tells the team what it suspects, and then logs a real
   * complaint rather than looping forever.
   */
  private Action approachUnseen(Road road, List<Blockade> unseen, double agentX, double agentY) {
    if (unseen.isEmpty()) {
      return null;
    }
    if (!road.getID().equals(this.unseenRoad)) {
      this.unseenRoad = road.getID();
      this.unseenApproach = 0;
    }
    this.unseenApproach++;

    for (Blockade blockade : unseen) {
      this.reportRoad(road, blockade, false);
    }

    if (this.unseenApproach > this.maxApproachUnseen) {
      LOGGER.warning("blockade shapes on road " + road.getID()
          + " never resolved after " + this.maxApproachUnseen
          + " cycles; moving on so the agent does not stall here");
      return null;
    }

    int aimX = road.getX();
    int aimY = road.getY();
    for (Blockade blockade : unseen) {
      if (blockade.isXDefined() && blockade.isYDefined()) {
        aimX = blockade.getX();
        aimY = blockade.getY();
        break;
      }
    }
    if (ClearGeometry.samePoint(agentX, agentY, aimX, aimY,
        ClearGeometry.EPSILON)) {
      // Standing on it already and still cannot see it — walking again buys
      // nothing.
      return null;
    }
    return new ActionMove(singleton(road.getID()), aimX, aimY);
  }


  private void forgetUnseen(EntityID roadID) {
    if (roadID.equals(this.unseenRoad)) {
      this.unseenRoad = null;
      this.unseenApproach = 0;
    }
  }


  /**
   * Emits the actual clear command, scaled to the server's clear distance, with
   * the anti-deadlock check applied.
   */
  private Action cutTowards(Road road, Blockade blockade, double agentX, double agentY, double aimX, double aimY) {
    // FIX 6: a zero-length direction used to be "walk +clearDistance along x",
    // which points into the same wall every cycle. Now the aim degrades through
    // road centre, then a neighbour edge, then a rotating bearing.
    Point2D cut = ClearGeometry.cutPoint(agentX, agentY, aimX, aimY,
        this.clearDistance);
    if (cut == null) {
      cut = this.escapeDegenerate(road, agentX, agentY);
    }

    int clearX = (int) cut.getX();
    int clearY = (int) cut.getY();

    if (ClearGeometry.samePoint(this.lastClearX, this.lastClearY, clearX,
        clearY, this.sameCutTolerance)) {
      this.repeatCount++;
      if (this.repeatCount >= this.forcedMove) {
        this.repeatCount = 0;
        // Next time we hit a degenerate aim, try a different bearing.
        this.sweepAttempt++;
        this.lastClearX = clearX;
        this.lastClearY = clearY;
        return new ActionMove(singleton(road.getID()), clearX, clearY);
      }
    } else {
      this.repeatCount = 0;
      this.sweepAttempt = 0;
    }
    this.lastClearX = clearX;
    this.lastClearY = clearY;

    return new ActionClear(clearX, clearY, blockade);
  }


  /** Aim of last resort when the agent is standing exactly on its target. */
  private Point2D escapeDegenerate(Road road, double agentX, double agentY) {
    Point2D cut = ClearGeometry.cutPoint(agentX, agentY, road.getX(),
        road.getY(), this.clearDistance);
    if (cut != null) {
      return cut;
    }
    for (EntityID neighbour : road.getNeighbours()) {
      Edge edge = road.getEdgeTo(neighbour);
      if (edge == null) {
        continue;
      }
      cut = ClearGeometry.cutPoint(agentX, agentY,
          (edge.getStartX() + edge.getEndX()) / 2.0,
          (edge.getStartY() + edge.getEndY()) / 2.0, this.clearDistance);
      if (cut != null) {
        return cut;
      }
    }
    LOGGER.fine("no usable aim on road " + road.getID() + "; sweeping bearing "
        + this.sweepAttempt);
    return ClearGeometry.sweepPoint(agentX, agentY, this.clearDistance,
        this.sweepAttempt);
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


  private static List<EntityID> singleton(EntityID id) {
    List<EntityID> list = new ArrayList<>(1);
    list.add(id);
    return list;
  }


  // -------------------------------------------------------------- fire safety

  /** FIX 4: a burning area, whether it is a plain building or a refuge. */
  private boolean isBurning(Area area) {
    if (!(area instanceof Building)) {
      return false;
    }
    Building building = (Building) area;
    return building.isFierynessDefined()
        && Building.BURNING.contains(building.getFierynessEnum());
  }


  /** True if the area, or anything it opens onto, is alight. */
  private boolean isNearFire(EntityID areaID) {
    if (areaID == null) {
      return false;
    }
    StandardEntity entity = this.worldInfo.getEntity(areaID);
    if (!(entity instanceof Area)) {
      return false;
    }
    Area area = (Area) entity;
    if (this.isBurning(area)) {
      return true;
    }
    for (EntityID neighbour : area.getNeighbours()) {
      StandardEntity next = this.worldInfo.getEntity(neighbour);
      if (next instanceof Area && this.isBurning((Area) next)) {
        return true;
      }
    }
    return false;
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

    // FIX 4: standing next to a fire means the damage rate is about to rise,
    // so break off earlier than the flat threshold would.
    int threshold = this.thresholdRest;
    if (agent.isPositionDefined() && this.isNearFire(agent.getPosition())) {
      threshold = Math.max(1, threshold / 2);
    }

    int survivableCycles = (hp / damage) + ((hp % damage) != 0 ? 1 : 0);
    return damage >= threshold
        || (survivableCycles + this.agentInfo.getTime()) < this.kernelTime;
  }


  /**
   * FIX 8. Picks a refuge that is not on fire, checks the planner can actually
   * reach it, and clears its way there rather than walking into rubble.
   */
  private Action calcRest(PoliceForce police) {
    EntityID position = police.getPosition();
    Collection<EntityID> refuges = this.worldInfo
        .getEntityIDsOfType(StandardEntityURN.REFUGE);
    if (refuges.isEmpty()) {
      return null;
    }

    List<EntityID> safe = new ArrayList<>();
    for (EntityID id : refuges) {
      StandardEntity entity = this.worldInfo.getEntity(id);
      if (entity instanceof Area && this.isBurning((Area) entity)) {
        continue;
      }
      safe.add(id);
    }
    if (safe.isEmpty()) {
      LOGGER.warning("every known refuge is burning; heading to the nearest "
          + "one anyway because standing in the street is worse");
      safe.addAll(refuges);
    }

    if (safe.contains(position)) {
      return new ActionRest();
    }

    this.pathPlanning.setFrom(position);
    this.pathPlanning.setDestination(safe);
    List<EntityID> path = this.pathPlanning.calc().getResult();
    if (path == null || path.isEmpty()) {
      // No route. Returning null lets calc() carry on with the clearing job
      // instead of standing still and bleeding out.
      LOGGER.fine("no reachable refuge from " + position
          + "; continuing to clear");
      return null;
    }

    StandardEntity positionEntity = this.worldInfo.getEntity(position);
    if (positionEntity instanceof Road) {
      Action clear = this.clearFromHere(police, (Road) positionEntity,
          this.nextStep(path, position));
      if (clear != null) {
        return clear;
      }
    }
    return new ActionMove(path);
  }


  // ------------------------------------------------------------ communication

  /** FIX 1: rebuilt each cycle from what the team said last cycle. */
  private void readTeammateReports(MessageManager messageManager) {
    this.teammateTarget.clear();
    this.teammatePosition.clear();
    EntityID me = this.agentInfo.me() != null ? this.agentInfo.me().getID()
        : null;
    for (CommunicationMessage raw : messageManager
        .getReceivedMessageList(MessagePoliceForce.class)) {
      MessagePoliceForce message = (MessagePoliceForce) raw;
      EntityID sender = message.getAgentID();
      if (sender == null || sender.equals(me)) {
        continue;
      }
      if (message.getPosition() != null) {
        this.teammatePosition.put(sender, message.getPosition());
      }
      if (message.isTargetDefined() && message.getTargetID() != null) {
        this.teammateTarget.put(message.getTargetID(), sender);
      }
    }
  }


  /**
   * FIX 1, deliberately narrow. Yielding is only correct when someone else is
   * literally standing on the road already; anything cleverer (distance
   * auctions, load balancing) belongs in the target allocator, which is the
   * only component that sees every agent at once.
   */
  private boolean shouldYield(PoliceForce police, EntityID position) {
    if (position.equals(this.target)) {
      return false;
    }
    EntityID owner = this.teammateTarget.get(this.target);
    if (owner == null || owner.equals(police.getID())) {
      return false;
    }
    EntityID theirPosition = this.teammatePosition.get(owner);
    if (theirPosition == null || !theirPosition.equals(this.target)) {
      return false;
    }
    LOGGER.fine("yielding road " + this.target + " to " + owner);
    return true;
  }


  /** FIX 3: say what we are doing, so allocators and the station can react. */
  private void report(PoliceForce police, Action action, EntityID reportTarget) {
    if (this.messageManager == null) {
      return;
    }
    int kind;
    if (action instanceof ActionClear) {
      kind = MessagePoliceForce.ACTION_CLEAR;
    } else if (action instanceof ActionRest) {
      kind = MessagePoliceForce.ACTION_REST;
    } else {
      kind = MessagePoliceForce.ACTION_MOVE;
    }
    this.messageManager
        .addMessage(new MessagePoliceForce(true, police, kind, reportTarget));
  }


  /**
   * FIX 3: broadcast a road's passability, but only when it changes. Radio
   * bandwidth is a scored resource; repeating "still blocked" every cycle
   * crowds out everyone else's traffic.
   */
  private void reportRoad(Road road, Blockade blockade, boolean passable) {
    if (this.messageManager == null) {
      return;
    }
    Boolean previous = this.reportedRoadState.get(road.getID());
    if (previous != null && previous.booleanValue() == passable) {
      return;
    }
    this.reportedRoadState.put(road.getID(), Boolean.valueOf(passable));
    this.messageManager.addMessage(
        new MessageRoad(true, road, blockade, Boolean.valueOf(passable),
            blockade != null));
  }
}