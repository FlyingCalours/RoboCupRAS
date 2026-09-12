package sample_team.module.complex.police.improvement;

import adf.core.agent.communication.MessageManager;
import adf.core.agent.communication.standard.bundle.information.MessageAmbulanceTeam;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.WorldInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

/**
 * Stuck agent escort.
 *
 * Ambulance teams broadcast their id, position and current action every
 * cycle, and those reports reach every Police agent regardless of
 * distance. An ambulance that keeps reporting ACTION_MOVE from an
 * unchanged position is trying to travel and failing, which on this map
 * almost always means a blockade.
 *
 * The Police agent targets that ambulance's road. Clearing is then
 * handled by URFPoliceExtActionClear, which always cuts whatever lies
 * under the agent's feet.
 *
 * The stall signal is used directly. Whether the road carries a known
 * blockade is deliberately NOT checked, because an unobserved road has
 * no blockade data at all and requiring it would reject exactly the
 * cases worth investigating.
 *
 * This class holds state only. It issues no actions and knows nothing
 * about ADF modules.
 */
public final class URFStuckAgentEscort {

  /** Per agent instances, matching the URFPoliceMetrics registry style. */
  private static final Map<EntityID, URFStuckAgentEscort> INSTANCES =
      new ConcurrentHashMap<>();

  /**
   * Cycles an ambulance must report ACTION_MOVE from one position before
   * it counts as stuck.
   */
  private static final int STALL_LIMIT = 3;

  /** Cycles spent escorting one ambulance before giving up. */
  private static final int ESCORT_BUDGET = 20;

  /** Cycles an ambulance stays unavailable after an escort ends. */
  private static final int RELEASE_COOLDOWN = 20;

  /** Cycles without a report before a sighting is discarded. */
  private static final int SIGHTING_LIFETIME = 5;

  private final Map<EntityID, Sighting> sightings;
  private final Map<EntityID, Integer> suppressUntil;

  private EntityID escortAmbulance;
  private EntityID escortPosition;
  private EntityID escortRoad;
  private int escortStartTime;
  private int lastTime;

  private int escortStartedCount;
  private int escortFreedCount;
  private int escortExpiredCount;

  private URFStuckAgentEscort() {
    this.sightings = new HashMap<>();
    this.suppressUntil = new HashMap<>();
    this.escortAmbulance = null;
    this.escortPosition = null;
    this.escortRoad = null;
    this.escortStartTime = 0;
    this.lastTime = 0;
    this.escortStartedCount = 0;
    this.escortFreedCount = 0;
    this.escortExpiredCount = 0;
  }

  /**
   * @param agentID the Police agent entity id
   * @return the escort state belonging to that agent
   */
  public static URFStuckAgentEscort forAgent(EntityID agentID) {
    return INSTANCES.computeIfAbsent(agentID, id -> new URFStuckAgentEscort());
  }

  /**
   * Fold this cycle's ambulance reports into the sighting table.
   *
   * Call this once per cycle from updateInfo, before nextTarget.
   *
   * @param agentInfo agent information supplied by the ADF
   * @param messageManager message manager supplied by the ADF
   */
  public void observe(AgentInfo agentInfo, MessageManager messageManager) {

    int now = agentInfo.getTime();

    for (var message
        : messageManager.getReceivedMessageList(MessageAmbulanceTeam.class)) {

      if (!(message instanceof MessageAmbulanceTeam report)) {
        continue;
      }
      if (!report.isPositionDefined()) {
        continue;
      }

      EntityID ambulanceID = report.getAgentID();
      EntityID position = report.getPosition();
      if (position == null) {
        continue;
      }

      Sighting sighting = this.sightings.get(ambulanceID);

      if (sighting == null || !position.equals(sighting.position)) {
        /*
         * First report, or the ambulance has physically moved. Either
         * way the stall clock restarts.
         */
        sighting = new Sighting();
        sighting.position = position;
        sighting.stallStartTime = now;
        this.sightings.put(ambulanceID, sighting);
      }

      sighting.action = report.getAction();
      sighting.lastSeenTime = now;
      sighting.dead = report.isHPDefined() && report.getHP() <= 0;

      /*
       * A buried ambulance is not blocked. Police clearing cannot free
       * it, so it is never an escort candidate.
       */
      sighting.buried =
          report.isBuriednessDefined() && report.getBuriedness() > 0;
    }
  }

  /**
   * Report the road to head for in order to free a stuck ambulance.
   *
   * @param agentInfo agent information supplied by the ADF
   * @param worldInfo world model supplied by the ADF
   * @return the road to target, or null when no ambulance needs help
   */
  public EntityID nextTarget(AgentInfo agentInfo, WorldInfo worldInfo) {

    int now = agentInfo.getTime();
    this.lastTime = now;
    this.discardStaleSightings(now);

    if (this.escortAmbulance != null) {
      Sighting sighting = this.sightings.get(this.escortAmbulance);

      if (sighting == null) {
        this.releaseEscort(now);
        this.escortExpiredCount++;
      }
      else if (!sighting.position.equals(this.escortPosition)) {
        /*
         * The ambulance reported a new position, so it is travelling
         * again. This is the success case.
         */
        this.releaseEscort(now);
        this.escortFreedCount++;
      }
      else if (now - this.escortStartTime >= ESCORT_BUDGET) {
        this.releaseEscort(now);
        this.escortExpiredCount++;
      }
      else {
        return this.escortRoad;
      }
    }

    return this.chooseEscort(agentInfo, worldInfo, now);
  }

  /**
   * @return the ambulance currently being escorted, or null
   */
  public EntityID getEscortAmbulance() {
    return this.escortAmbulance;
  }

  /**
   * @return the road currently targeted for an escort, or null
   */
  public EntityID getEscortRoad() {
    return this.escortRoad;
  }

  /**
   * @return the number of ambulances known to this agent
   */
  public int getSightingCount() {
    return this.sightings.size();
  }

  /**
   * @return the number of escorts started so far
   */
  public int getEscortStartedCount() {
    return this.escortStartedCount;
  }

  /**
   * @return the number of escorts that ended with the ambulance moving
   */
  public int getEscortFreedCount() {
    return this.escortFreedCount;
  }

  /**
   * @return the number of escorts that ran out of budget or went stale
   */
  public int getEscortExpiredCount() {
    return this.escortExpiredCount;
  }

  /**
   * @return the number of ambulances currently reporting a stall
   */
  public int getStalledCount() {
    int stalled = 0;
    for (Sighting sighting : this.sightings.values()) {
      if (this.isStalled(sighting, this.lastTime)) {
        stalled++;
      }
    }
    return stalled;
  }

  /**
   * @return the escort state as log fields, matching the URF log style
   */
  public String toLogFields() {
    return "escortAmbulance=" + this.escortAmbulance
        + " escortRoad=" + this.escortRoad
        + " escortSeen=" + this.sightings.size()
        + " escortStalled=" + this.getStalledCount()
        + " escortStarted=" + this.escortStartedCount
        + " escortFreed=" + this.escortFreedCount
        + " escortExpired=" + this.escortExpiredCount;
  }

  /**
   * Choose the nearest stalled ambulance that is not in cooldown.
   *
   * @param agentInfo agent information supplied by the ADF
   * @param worldInfo world model supplied by the ADF
   * @param now the current simulation time
   * @return the road to target, or null
   */
  private EntityID chooseEscort(AgentInfo agentInfo, WorldInfo worldInfo, int now) {

    EntityID police = agentInfo.getPosition();

    EntityID bestAmbulance = null;
    EntityID bestPosition = null;
    EntityID bestRoad = null;
    double bestDistance = Double.MAX_VALUE;

    for (Map.Entry<EntityID, Sighting> entry : this.sightings.entrySet()) {

      EntityID ambulanceID = entry.getKey();
      Sighting sighting = entry.getValue();

      if (this.isSuppressed(ambulanceID, now)) {
        continue;
      }
      if (!this.isStalled(sighting, now)) {
        continue;
      }

      EntityID road = this.resolveRoad(sighting.position, worldInfo);
      if (road == null) {
        continue;
      }

      double distance = police == null ? 0.0 : worldInfo.getDistance(police, road);

      boolean better;
      if (bestAmbulance == null) {
        better = true;
      }
      else if (distance < bestDistance) {
        better = true;
      }
      else if (distance > bestDistance) {
        better = false;
      }
      else {
        /*
         * Sighting iteration order is not stable, so equal distances are
         * broken by entity id to keep repeated runs comparable.
         */
        better = ambulanceID.getValue() < bestAmbulance.getValue();
      }

      if (better) {
        bestAmbulance = ambulanceID;
        bestPosition = sighting.position;
        bestRoad = road;
        bestDistance = distance;
      }
    }

    if (bestAmbulance == null) {
      return null;
    }

    this.escortAmbulance = bestAmbulance;
    this.escortPosition = bestPosition;
    this.escortRoad = bestRoad;
    this.escortStartTime = now;
    this.escortStartedCount++;

    // Can Add Logging of system.out.println here

    return this.escortRoad;
  }

  /**
   * @param sighting one ambulance sighting
   * @param now the current simulation time
   * @return true when the ambulance is trying to move and failing
   */
  private boolean isStalled(Sighting sighting, int now) {
    if (sighting.buried || sighting.dead) {
      return false;
    }
    if (sighting.action != MessageAmbulanceTeam.ACTION_MOVE) {
      return false;
    }
    return now - sighting.stallStartTime >= STALL_LIMIT;
  }

  /**
   * End the current escort and hold that ambulance back for a while.
   *
   * @param now the current simulation time
   */
  private void releaseEscort(int now) {
    if (this.escortAmbulance != null) {
      this.suppressUntil.put(this.escortAmbulance,
          Integer.valueOf(now + RELEASE_COOLDOWN));
    }
    this.escortAmbulance = null;
    this.escortPosition = null;
    this.escortRoad = null;
  }

  /**
   * @param ambulanceID an ambulance entity id
   * @param now the current simulation time
   * @return true while the ambulance is inside its release cooldown
   */
  private boolean isSuppressed(EntityID ambulanceID, int now) {
    Integer until = this.suppressUntil.get(ambulanceID);
    return until != null && until.intValue() > now;
  }

  /**
   * Forget ambulances that have stopped reporting.
   *
   * @param now the current simulation time
   */
  private void discardStaleSightings(int now) {
    List<EntityID> expired = new ArrayList<>();
    for (Map.Entry<EntityID, Sighting> entry : this.sightings.entrySet()) {
      if (now - entry.getValue().lastSeenTime > SIGHTING_LIFETIME) {
        expired.add(entry.getKey());
      }
    }
    for (EntityID ambulanceID : expired) {
      this.sightings.remove(ambulanceID);
    }
  }

  /**
   * Resolve a reported position to a road the Police agent can stand on.
   *
   * An ambulance reporting a Building position is stuck at an entrance,
   * so the lowest numbered neighbouring road is used instead.
   *
   * @param areaID the reported position
   * @param worldInfo world model supplied by the ADF
   * @return a road entity id, or null
   */
  private EntityID resolveRoad(EntityID areaID, WorldInfo worldInfo) {

    StandardEntity entity = worldInfo.getEntity(areaID);
    if (entity instanceof Road) {
      return areaID;
    }
    if (!(entity instanceof Area)) {
      return null;
    }

    Area area = (Area) entity;
    if (!area.isEdgesDefined()) {
      return null;
    }

    EntityID best = null;
    for (Edge edge : area.getEdges()) {
      EntityID neighbour = edge.getNeighbour();
      if (neighbour == null) {
        continue;
      }
      if (!(worldInfo.getEntity(neighbour) instanceof Road)) {
        continue;
      }
      if (best == null || neighbour.getValue() < best.getValue()) {
        best = neighbour;
      }
    }
    return best;
  }

  /**
   * Latest broadcast state of one ambulance team.
   */
  private static final class Sighting {
    private EntityID position;
    private int action;
    private int stallStartTime;
    private int lastSeenTime;
    private boolean buried;
    private boolean dead;
  }
}