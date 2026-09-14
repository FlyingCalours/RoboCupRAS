package sample_team.module.complex.police.improvement;

import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.WorldInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

/**
 * Refuge door sweep.
 *
 * Scoring cannot prioritise a blockade that has never been observed. A
 * refuge door road stays outside the candidate set until some agent
 * physically stands there and looks at it, so the highest scoring weight
 * in the world never fires. This class removes the perception gap by
 * sending every Police agent to every refuge door in turn.
 *
 * A door is a Road that shares an edge with a Refuge.
 *
 * This is deliberate redundancy. Every agent sweeps every door, so a
 * refuge entrance is confirmed open several times rather than once.
 *
 * This class holds state only. It issues no actions and knows nothing
 * about ADF modules, so it can be consulted by the RoadDetector, by the
 * MOVE module, or by both.
 */
public final class URFRefugeDoorSweep {

  /** Per agent instances, matching the URFPoliceMetrics registry style. */
  private static final Map<EntityID, URFRefugeDoorSweep> INSTANCES =
      new ConcurrentHashMap<>();

  /** Cycles before a confirmed or abandoned door becomes pending again. */
  private static final int RECHECK_INTERVAL = 200;

  /** Cycles spent targeting one door before giving up on it. */
  private static final int ATTEMPT_BUDGET = 30;

  /**
   * Simulation time after which the sweep stops issuing doors.
   *
   * Safety valve only. Worst case the sweep costs
   * doorCount x ATTEMPT_BUDGET cycles, which on a map with many refuges
   * can consume an entire run and starve the scoring logic.
   *
   * Set to Integer.MAX_VALUE to sweep for the whole scenario.
   */
  private static final int SWEEP_DEADLINE = 40;

  private final List<EntityID> doors;
  private final Map<EntityID, Integer> suppressUntil;

  private boolean doorsCollected;
  private EntityID currentDoor;
  private int attemptStartTime;
  private int confirmedCount;
  private int abandonedCount;
  private int lastTime;

  private URFRefugeDoorSweep() {
    this.doors = new ArrayList<>();
    this.suppressUntil = new HashMap<>();
    this.doorsCollected = false;
    this.currentDoor = null;
    this.attemptStartTime = 0;
    this.confirmedCount = 0;
    this.abandonedCount = 0;
    this.lastTime = 0;
  }

  /**
   * @param agentID the Police agent entity id
   * @return the sweep state belonging to that agent
   */
  public static URFRefugeDoorSweep forAgent(EntityID agentID) {
    return INSTANCES.computeIfAbsent(agentID, id -> new URFRefugeDoorSweep());
  }

  /**
   * Advance the sweep by one cycle and report the door to head for.
   *
   * Call this once per cycle, before any scoring logic. A non-null result
   * must be treated as the road target for this cycle.
   *
   * @param agentInfo agent information supplied by the ADF
   * @param worldInfo world model supplied by the ADF
   * @return the door road to target, or null when the sweep has nothing
   *     left to do and normal target selection should resume
   */
  public EntityID nextDoor(AgentInfo agentInfo, WorldInfo worldInfo) {

    if (!this.doorsCollected) {
      this.collectDoors(worldInfo);
      this.doorsCollected = true;
    }

    if (this.doors.isEmpty()) {
      return null;
    }

    int now = agentInfo.getTime();
    this.lastTime = now;

    if (now > SWEEP_DEADLINE) {
      this.currentDoor = null;
      return null;
    }

    EntityID position = agentInfo.getPosition();

    /*
     * Opportunistic confirmation.
     *
     * A door the agent happens to be standing on counts as checked even
     * when it was not the door being targeted. This shortens the sweep
     * considerably on maps where doors sit on the route between refuges.
     */
    if (position != null
        && this.doors.contains(position)
        && !this.isSuppressed(position, now)
        && this.isObservedClear(position, worldInfo)) {
      this.suppressUntil.put(position, Integer.valueOf(now + RECHECK_INTERVAL));
      this.confirmedCount++;
      if (position.equals(this.currentDoor)) {
        this.currentDoor = null;
      }
    }

    /*
     * Give up on a door the agent cannot reach or cannot open.
     *
     * Without this the agent walks at an unreachable refuge entrance for
     * the rest of the scenario.
     */
    if (this.currentDoor != null && now - this.attemptStartTime >= ATTEMPT_BUDGET) {
      this.suppressUntil.put(this.currentDoor, Integer.valueOf(now + RECHECK_INTERVAL));
      this.abandonedCount++;
      this.currentDoor = null;
    }

    /*
     * Commitment.
     *
     * Once a door is chosen it is kept until confirmed or abandoned, so
     * the agent does not re-pick a nearer door every time it walks a few
     * hundred units.
     */
    if (this.currentDoor != null) {
      return this.currentDoor;
    }

    EntityID nearest = this.nearestPendingDoor(position, worldInfo, now);
    if (nearest == null) {
      return null;
    }

    this.currentDoor = nearest;
    this.attemptStartTime = now;

    // Can Add Logging of system.out.println here

    return this.currentDoor;
  }

  /**
   * @return the number of refuge doors found on this map
   */
  public int getDoorCount() {
    return this.doors.size();
  }

  /**
   * @return the number of doors this agent has observed to be clear
   */
  public int getConfirmedCount() {
    return this.confirmedCount;
  }

  /**
   * @return the number of doors this agent gave up on
   */
  public int getAbandonedCount() {
    return this.abandonedCount;
  }

  /**
   * @return the door currently being targeted, or null
   */
  public EntityID getCurrentDoor() {
    return this.currentDoor;
  }

  /**
   * @return the number of doors still waiting to be checked
   */
  public int getPendingCount() {
    int pending = 0;
    for (EntityID door : this.doors) {
      if (!this.isSuppressed(door, this.lastTime)) {
        pending++;
      }
    }
    return pending;
  }

  /**
   * @return the sweep state as log fields, matching the URF log style
   */
  public String toLogFields() {
    return "sweepDoor=" + this.currentDoor
        + " sweepDoors=" + this.doors.size()
        + " sweepConfirmed=" + this.confirmedCount
        + " sweepAbandoned=" + this.abandonedCount
        + " sweepPending=" + this.getPendingCount();
  }

  /**
   * @param position the agent's current area, may be null
   * @param worldInfo world model supplied by the ADF
   * @param now the current simulation time
   * @return the nearest door that is not suppressed, or null
   */
  private EntityID nearestPendingDoor(EntityID position, WorldInfo worldInfo, int now) {
    EntityID best = null;
    double bestDistance = Double.MAX_VALUE;

    for (EntityID door : this.doors) {
      if (this.isSuppressed(door, now)) {
        continue;
      }
      double distance = position == null ? 0.0 : worldInfo.getDistance(position, door);
      if (best == null || distance < bestDistance) {
        best = door;
        bestDistance = distance;
      }
    }
    return best;
  }

  /**
   * @param door a door road id
   * @param now the current simulation time
   * @return true while the door is inside its recheck interval
   */
  private boolean isSuppressed(EntityID door, int now) {
    Integer until = this.suppressUntil.get(door);
    return until != null && until.intValue() > now;
  }

  /**
   * @param roadID a road id
   * @param worldInfo world model supplied by the ADF
   * @return true when the road is known to carry no blockade
   */
  private boolean isObservedClear(EntityID roadID, WorldInfo worldInfo) {
    StandardEntity entity = worldInfo.getEntity(roadID);
    if (!(entity instanceof Road)) {
      return false;
    }
    Road road = (Road) entity;
    return road.isBlockadesDefined() && road.getBlockades().isEmpty();
  }

  /**
   * Collect every Road that shares an edge with a Refuge.
   *
   * Edges are used rather than getNeighbours so that wall edges, which
   * carry no neighbour, are skipped. The result is sorted by entity id so
   * that every agent walks the doors in the same order and repeated runs
   * stay comparable.
   *
   * @param worldInfo world model supplied by the ADF
   */
  private void collectDoors(WorldInfo worldInfo) {
    Set<EntityID> unique = new LinkedHashSet<>();

    for (StandardEntity entity : worldInfo.getEntitiesOfType(StandardEntityURN.REFUGE)) {
      if (!(entity instanceof Area)) {
        continue;
      }
      Area refuge = (Area) entity;
      if (!refuge.isEdgesDefined()) {
        continue;
      }
      for (Edge edge : refuge.getEdges()) {
        EntityID neighbour = edge.getNeighbour();
        if (neighbour == null) {
          continue;
        }
        if (worldInfo.getEntity(neighbour) instanceof Road) {
          unique.add(neighbour);
        }
      }
    }

    this.doors.addAll(unique);
    Collections.sort(this.doors, (left, right) ->
        Integer.compare(left.getValue(), right.getValue()));
  }
}