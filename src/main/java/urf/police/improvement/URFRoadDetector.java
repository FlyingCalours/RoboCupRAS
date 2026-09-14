package urf.police.improvement;

import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.component.module.algorithm.PathPlanning;
import adf.core.component.module.complex.RoadDetector;

import urf.police.improvement.URFStuckAgentEscort;
import urf.police.improvement.URFRefugeDoorSweep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

import adf.core.agent.communication.MessageManager;

/**
 * Independent URF Police road target selection module.
 *
 * PD-1 design goals:
 *
 * 1. Do not inherit SampleRoadDetector.
 * 2. Score every known blocked road by access value, not by distance alone.
 * 3. Prefer roads that open refuges, trapped agents, fires and casualties.
 * 4. Prefer central corridors over dead ends.
 * 5. Abandon a road that shows no progress, so low-value blockades are not
 *    cleared forever.
 * 6. Return null when nothing is worth clearing so that
 *    DefaultTacticsPoliceForce can continue to the Search module.
 *
 * IMPORTANT:
 *
 * This PD-1 module intentionally does NOT perform:
 *
 * - blockade selection inside a road (URFPoliceExtActionClear owns that),
 * - cluster ownership or multi-Police coordination,
 * - exploration.
 *
 * Those responsibilities belong to later independent URF modules.
 */
public class URFRoadDetector extends RoadDetector {

  /** Maximum contribution of a road that touches a refuge. */
  private static final double W_REFUGE_ACCESS = 6.0;

  /** Maximum contribution of platoon agents standing on the blocked road. */
  private static final double W_BLOCKED_AGENT = 5.0;

  /** Maximum contribution of a road that touches a burning building. */
  private static final double W_FIRE_ACCESS = 4.0;

  /** Maximum contribution of casualties in or beside the road. */
  private static final double W_CASUALTY_ACCESS = 3.0;

  /** Maximum contribution of road connectivity (corridor value). */
  private static final double W_CORRIDOR = 2.0;

  /** Contribution of the road the Police agent is already standing on. */
  private static final double W_CURRENT_ROAD = 3.0;

  /** Number of blocked agents that already gives the full agent term. */
  private static final double AGENT_REFERENCE_COUNT = 2.0;

  /** Number of casualties that already gives the full casualty term. */
  private static final double CASUALTY_REFERENCE_COUNT = 2.0;

  /** Neighbour count that already gives the full corridor term. */
  private static final double CORRIDOR_REFERENCE_DEGREE = 4.0;

  /** Distance, in world units, that halves the score of a road. */
  private static final double DISTANCE_SCALE = 50000.0;

  /** Score multiplier applied to the target chosen in the previous cycle. */
  private static final double STICKY_MULTIPLIER = 1.25;

  /** Number of highest scoring roads that are path checked. */
  private static final int PATH_CHECK_LIMIT = 4;

  /** Cycles without progress before the current target is abandoned. */
  private static final int NO_PROGRESS_LIMIT = 8;

  /** Cycles an abandoned road stays out of the candidate set. */
  private static final int COOLDOWN_DURATION = 30;

  /** Distance reduction, in world units, that counts as real progress. */
  private static final double MIN_DISTANCE_PROGRESS = 1000.0;

  /** Lowest fieryness value that still means burning. */
  private static final int FIERYNESS_BURNING_MIN = 1;

  /** Highest fieryness value that still means burning. */
  private static final int FIERYNESS_BURNING_MAX = 3;

  private final PathPlanning pathPlanning;

  private final Map<EntityID, Integer> cooldownUntil;
  private final Map<EntityID, Integer> abandonCount;
  private final Map<EntityID, Integer> blockedAgentCount;
  private final Map<EntityID, Integer> casualtyCount;
  private final Set<EntityID> burningBuildings;
  private final Set<EntityID> refugeIDs;

  private EntityID result;
  private double bestDistanceToTarget;
  private int bestRepairCostOnTarget;
  private int noProgressCount;

  private URFRoadTargetReason lastReason;
  private double lastScore;
  private int lastCandidateCount;
  private boolean lastTargetChanged;

  /**
   * @param agentInfo agent information supplied by the ADF
   * @param worldInfo world information supplied by the ADF
   * @param scenarioInfo scenario constants supplied by the ADF
   * @param moduleManager module manager used for sub module selection
   * @param developData development configuration supplied by the ADF
   */
  public URFRoadDetector(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
                         ModuleManager moduleManager, DevelopData developData) {
    super(agentInfo, worldInfo, scenarioInfo, moduleManager, developData);

    this.pathPlanning = moduleManager.getModule("URFRoadDetector.PathPlanning",
        "adf.impl.module.algorithm.DijkstraPathPlanning");
    registerModule(this.pathPlanning);

    this.cooldownUntil = new HashMap<>();
    this.abandonCount = new HashMap<>();
    this.blockedAgentCount = new HashMap<>();
    this.casualtyCount = new HashMap<>();
    this.burningBuildings = new HashSet<>();
    this.refugeIDs = new HashSet<>();

    this.result = null;
    this.bestDistanceToTarget = Double.MAX_VALUE;
    this.bestRepairCostOnTarget = Integer.MAX_VALUE;
    this.noProgressCount = 0;

    this.lastReason = URFRoadTargetReason.NO_BLOCKED_ROAD_KNOWN;
    this.lastScore = 0.0;
    this.lastCandidateCount = 0;
    this.lastTargetChanged = false;
  }

  /**
   * @return the selected road, or null when no road is worth clearing
   */
  @Override
  public EntityID getTarget() {
    return this.result;
  }

  /**
   * @return this module
   */
  @Override
  public RoadDetector calc() {

    this.lastScore = 0.0;
    this.lastCandidateCount = 0;
    this.lastTargetChanged = false;

    EntityID positionID = this.agentInfo.getPosition();
    if (positionID == null) {
      this.applySelection(null, URFRoadTargetReason.NO_POSITION, 0.0);
      return this;
    }



    EntityID door = URFRefugeDoorSweep
    .forAgent(this.agentInfo.getID())
    .nextDoor(this.agentInfo, this.worldInfo);

    if (door != null) {
      this.applySelection(door, URFRoadTargetReason.REFUGE_DOOR_SWEEP, 0.0);
      return this;
    }

    EntityID rescue = URFStuckAgentEscort
    .forAgent(this.agentInfo.getID())
    .nextTarget(this.agentInfo, this.worldInfo);

    if (rescue != null) {
      this.applySelection(rescue, URFRoadTargetReason.ESCORT_STUCK_AGENT, 0.0);
      return this;
    }


    if (this.refugeIDs.isEmpty()) {
      this.collectRefuges();
    }

    this.refreshWorldSnapshot();
    this.reviewCurrentTarget(positionID);

    List<ScoredRoad> ranked = this.rankBlockedRoads(positionID);
    this.lastCandidateCount = ranked.size();

    if (ranked.isEmpty()) {
      this.applySelection(null, URFRoadTargetReason.NO_BLOCKED_ROAD_KNOWN, 0.0);
      return this;
    }

    ScoredRoad reachable = this.selectReachable(positionID, ranked);
    if (reachable != null) {
      this.applySelection(reachable.roadID, URFRoadTargetReason.SCORED_REACHABLE,
          reachable.score);
    }
    else {
      /*
       * No path was found to any checked road.
       *
       * Keep the highest scoring road anyway. The MOVE module and
       * URFPoliceExtActionClear can still make local progress toward it.
       */
      ScoredRoad best = ranked.get(0);
      this.applySelection(best.roadID,
          URFRoadTargetReason.SCORED_UNREACHABLE_FALLBACK, best.score);
    }

    // Can Add Logging of system.out.println here

    return this;
  }


  /**
   * @param messageManager message manager supplied by the ADF
   * @return this module
   */
  @Override
  public RoadDetector updateInfo(MessageManager messageManager) {
    super.updateInfo(messageManager);
    if (this.getCountUpdateInfo() >= 2) {
      return this;
    }

    URFStuckAgentEscort
        .forAgent(this.agentInfo.getID())
        .observe(this.agentInfo, messageManager);

    return this;
  }


  /**
   * @return the reason produced by the most recent calc call
   */
  public final URFRoadTargetReason getLastReason() {
    return this.lastReason;
  }

  /**
   * @return the score of the road selected by the most recent calc call
   */
  public final double getLastScore() {
    return this.lastScore;
  }

  /**
   * @return the number of scored candidate roads in the most recent calc call
   */
  public final int getLastCandidateCount() {
    return this.lastCandidateCount;
  }

  /**
   * @return true when the most recent calc call changed the selected road
   */
  public final boolean isLastTargetChanged() {
    return this.lastTargetChanged;
  }

  /**
   * @return consecutive cycles without measurable progress on the current road
   */
  public final int getNoProgressCount() {
    return this.noProgressCount;
  }

  /**
   * @return the number of roads currently held in cooldown
   */
  public final int getCooldownCount() {
    return this.cooldownUntil.size();
  }

  /**
   * @param roadID a road entity id
   * @return the number of times this road has been abandoned
   */
  public final int getAbandonCount(EntityID roadID) {
    Integer count = this.abandonCount.get(roadID);
    return count == null ? 0 : count.intValue();
  }

  /**
   * Store the selection and reset progress tracking when the road changes.
   *
   * @param roadID the newly selected road, may be null
   * @param reason the decision reason
   * @param score the score of the selected road
   */
  private void applySelection(EntityID roadID, URFRoadTargetReason reason,
      double score) {
    boolean changed = roadID == null ? this.result != null : !roadID.equals(this.result);
    if (changed) {
      this.bestDistanceToTarget = Double.MAX_VALUE;
      this.bestRepairCostOnTarget = Integer.MAX_VALUE;
      this.noProgressCount = 0;
    }
    this.lastTargetChanged = changed;
    this.result = roadID;
    this.lastReason = reason;
    this.lastScore = score;
  }

  /**
   * Drop the current target when it is finished, invalid or stagnant.
   *
   * @param positionID the current Police agent position
   */
  private void reviewCurrentTarget(EntityID positionID) {
    if (this.result == null) {
      return;
    }

    StandardEntity entity = this.worldInfo.getEntity(this.result);
    if (!(entity instanceof Road)) {
      this.result = null;
      return;
    }

    Road road = (Road) entity;
    if (road.isBlockadesDefined() && road.getBlockades().isEmpty()) {
      /*
       * The road is now known to be open. It is finished, not abandoned,
       * so no cooldown is recorded.
       */
      this.result = null;
      return;
    }

    if (positionID.equals(this.result)) {
      /*
       * Standing on the target road. Progress is measured by repair cost,
       * because physical distance can no longer decrease.
       */
      int repairCost = this.totalRepairCost(road);
      if (repairCost >= 0 && repairCost < this.bestRepairCostOnTarget) {
        this.bestRepairCostOnTarget = repairCost;
        this.noProgressCount = 0;
      }
      else {
        this.noProgressCount++;
      }
    }
    else {
      /*
       * Travelling toward the target road. Progress is measured by
       * distance reduction.
       */
      double distance = this.worldInfo.getDistance(positionID, this.result);
      if (distance < this.bestDistanceToTarget - MIN_DISTANCE_PROGRESS) {
        this.bestDistanceToTarget = distance;
        this.noProgressCount = 0;
      }
      else {
        this.noProgressCount++;
      }
    }

    if (this.noProgressCount >= NO_PROGRESS_LIMIT) {
      int now = this.agentInfo.getTime();
      this.cooldownUntil.put(this.result, Integer.valueOf(now + COOLDOWN_DURATION));
      Integer previous = this.abandonCount.get(this.result);
      this.abandonCount.put(this.result, Integer.valueOf(previous == null ? 1 : previous.intValue() + 1));
      this.result = null;
      this.noProgressCount = 0;
    }
  }

  /**
   * Score every known blocked road that is not in cooldown.
   *
   * @param positionID the current Police agent position
   * @return candidate roads ordered by descending score
   */
  private List<ScoredRoad> rankBlockedRoads(EntityID positionID) {

    int now = this.agentInfo.getTime();
    List<ScoredRoad> scored = new ArrayList<>();

    Collection<StandardEntity> areas =
        this.worldInfo.getEntitiesOfType(StandardEntityURN.ROAD, StandardEntityURN.HYDRANT);

    for (StandardEntity entity : areas) {
      if (!(entity instanceof Road)) {
        continue;
      }

      Road road = (Road) entity;
      EntityID roadID = road.getID();

      if (!road.isBlockadesDefined() || road.getBlockades().isEmpty()) {
        continue;
      }

      Integer until = this.cooldownUntil.get(roadID);
      if (until != null && until.intValue() > now) {
        continue;
      }

      double value = this.accessValue(road, positionID);
      double distance = this.worldInfo.getDistance(positionID, roadID);
      double score = value / (1.0 + distance / DISTANCE_SCALE);

      if (roadID.equals(this.result)) {
        score = score * STICKY_MULTIPLIER;
      }

      scored.add(new ScoredRoad(roadID, score, value, distance));
    }

    Collections.sort(scored, (left, right) -> {
      int comparison = Double.compare(right.score, left.score);
      if (comparison != 0) {
        return comparison;
      }
      return Integer.compare(left.roadID.getValue(), right.roadID.getValue());
    });

    return scored;
  }

  /**
   * Compute how much mobility a road restores if it is cleared.
   *
   * @param road a blocked road
   * @param positionID the current Police agent position
   * @return the weighted access value of the road
   */
  private double accessValue(Road road, EntityID positionID) {

    boolean touchesRefuge = false;
    boolean touchesFire = false;
    int nearbyCasualties = 0;
    int degree = 0;

    if (road.isEdgesDefined()) {
      for (EntityID neighbourID : road.getNeighbours()) {
        degree++;

        if (this.refugeIDs.contains(neighbourID)) {
          touchesRefuge = true;
        }
        if (this.burningBuildings.contains(neighbourID)) {
          touchesFire = true;
        }

        Integer neighbourCasualties = this.casualtyCount.get(neighbourID);
        if (neighbourCasualties != null) {
          nearbyCasualties = nearbyCasualties + neighbourCasualties.intValue();
        }
      }
    }

    Integer ownCasualties = this.casualtyCount.get(road.getID());
    if (ownCasualties != null) {
      nearbyCasualties = nearbyCasualties + ownCasualties.intValue();
    }

    Integer agents = this.blockedAgentCount.get(road.getID());
    int blockedAgents = agents == null ? 0 : agents.intValue();

    double refugeTerm = touchesRefuge ? 1.0 : 0.0;
    double fireTerm = touchesFire ? 1.0 : 0.0;
    double casualtyTerm = Math.min(1.0, nearbyCasualties / CASUALTY_REFERENCE_COUNT);
    double agentTerm = Math.min(1.0, blockedAgents / AGENT_REFERENCE_COUNT);
    double corridorTerm = Math.min(1.0, degree / CORRIDOR_REFERENCE_DEGREE);
    double currentTerm = road.getID().equals(positionID) ? 1.0 : 0.0;

    return W_REFUGE_ACCESS * refugeTerm
        + W_BLOCKED_AGENT * agentTerm
        + W_FIRE_ACCESS * fireTerm
        + W_CASUALTY_ACCESS * casualtyTerm
        + W_CORRIDOR * corridorTerm
        + W_CURRENT_ROAD * currentTerm;
  }

  /**
   * Path check the highest scoring roads and return the first reachable one.
   *
   * @param positionID the current Police agent position
   * @param ranked candidate roads ordered by descending score
   * @return the first reachable candidate, or null when none is reachable
   */
  private ScoredRoad selectReachable(EntityID positionID, List<ScoredRoad> ranked) {

    int limit = Math.min(PATH_CHECK_LIMIT, ranked.size());

    for (int index = 0; index < limit; index++) {
      ScoredRoad candidate = ranked.get(index);

      if (candidate.roadID.equals(positionID)) {
        return candidate;
      }

      this.pathPlanning.setFrom(positionID);
      this.pathPlanning.setDestination(Collections.singleton(candidate.roadID));
      List<EntityID> path = this.pathPlanning.calc().getResult();

      if (path != null && !path.isEmpty()) {
        return candidate;
      }
    }

    return null;
  }

  /**
   * Rebuild the per cycle view of fires, casualties and blocked agents.
   */
  private void refreshWorldSnapshot() {

    this.burningBuildings.clear();
    this.casualtyCount.clear();
    this.blockedAgentCount.clear();

    Collection<StandardEntity> buildings = this.worldInfo.getEntitiesOfType(
        StandardEntityURN.BUILDING,
        StandardEntityURN.GAS_STATION,
        StandardEntityURN.REFUGE,
        StandardEntityURN.AMBULANCE_CENTRE,
        StandardEntityURN.FIRE_STATION,
        StandardEntityURN.POLICE_OFFICE);

    for (StandardEntity entity : buildings) {
      if (!(entity instanceof Building)) {
        continue;
      }
      Building building = (Building) entity;
      if (!building.isFierynessDefined()) {
        continue;
      }
      int fieryness = building.getFieryness();
      if (fieryness >= FIERYNESS_BURNING_MIN && fieryness <= FIERYNESS_BURNING_MAX) {
        this.burningBuildings.add(building.getID());
      }
    }

    EntityID selfID = this.agentInfo.getID();

    Collection<StandardEntity> humans = this.worldInfo.getEntitiesOfType(
        StandardEntityURN.CIVILIAN,
        StandardEntityURN.FIRE_BRIGADE,
        StandardEntityURN.POLICE_FORCE,
        StandardEntityURN.AMBULANCE_TEAM);

    for (StandardEntity entity : humans) {
      if (!(entity instanceof Human)) {
        continue;
      }
      Human human = (Human) entity;
      if (human.getID().equals(selfID) || !human.isPositionDefined()) {
        continue;
      }

      EntityID humanPosition = human.getPosition();

      if (this.needsHelp(human)) {
        Integer casualties = this.casualtyCount.get(humanPosition);
        this.casualtyCount.put(humanPosition,
            Integer.valueOf(casualties == null ? 1 : casualties.intValue() + 1));
      }

      /*
       * A teammate standing on a blocked road is treated as blocked.
       *
       * This is a proxy. The agent cannot observe another agent's
       * failed MOVE commands directly.
       */
      if (!(human instanceof Civilian)
          && this.worldInfo.getEntity(humanPosition) instanceof Road) {
        Integer blocked = this.blockedAgentCount.get(humanPosition);
        this.blockedAgentCount.put(humanPosition,
            Integer.valueOf(blocked == null ? 1 : blocked.intValue() + 1));
      }
    }
  }

  /**
   * @param human any known human entity
   * @return true when the human is alive and either buried or damaged
   */
  private boolean needsHelp(Human human) {
    if (human.isHPDefined() && human.getHP() <= 0) {
      return false;
    }
    if (human.isBuriednessDefined() && human.getBuriedness() > 0) {
      return true;
    }
    return human.isDamageDefined() && human.getDamage() > 0;
  }

  /**
   * @param road a blocked road
   * @return the summed repair cost, or -1 when no repair cost is known
   */
  private int totalRepairCost(Road road) {
    if (!road.isBlockadesDefined()) {
      return -1;
    }

    int total = 0;
    boolean known = false;

    for (EntityID blockadeID : road.getBlockades()) {
      StandardEntity entity = this.worldInfo.getEntity(blockadeID);
      if (!(entity instanceof rescuecore2.standard.entities.Blockade)) {
        continue;
      }
      rescuecore2.standard.entities.Blockade blockade =
          (rescuecore2.standard.entities.Blockade) entity;
      if (blockade.isRepairCostDefined()) {
        total = total + blockade.getRepairCost();
        known = true;
      }
    }

    return known ? total : -1;
  }

  /**
   * Cache the refuge entity ids once, because refuges do not move.
   */
  private void collectRefuges() {
    for (StandardEntity entity : this.worldInfo.getEntitiesOfType(StandardEntityURN.REFUGE)) {
      this.refugeIDs.add(entity.getID());
    }
  }

  /**
   * Immutable scoring result for one blocked road.
   */
  private static final class ScoredRoad {
    private final EntityID roadID;
    private final double score;
    private final double value;
    private final double distance;

    private ScoredRoad(EntityID roadID, double score, double value, double distance) {
      this.roadID = roadID;
      this.score = score;
      this.value = value;
      this.distance = distance;
    }
  }
}