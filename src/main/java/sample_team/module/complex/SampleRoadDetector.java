package sample_team.module.complex;

import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.module.algorithm.PathPlanning;
import adf.core.component.module.complex.RoadDetector;
import adf.core.debug.DefaultLogger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
 * Whole-map, recursive blockade sweep for the Police Force.
 *
 * <p>
 * The police agent's only job in this team is to open every road on the map.
 * This module decides <em>which</em> road to attack next;
 * {@link SampleExtActionClear} decides how to get there and how to cut through.
 *
 * <p>
 * <b>Why recursion.</b> The map is an undirected graph whose nodes are
 * {@link Area}s (roads, hydrants, buildings) and whose edges are the passages
 * between neighbouring areas. Finding the closest road that still needs work is
 * therefore a graph search from the agent's current position. It is implemented
 * here as a recursive breadth-first flood fill: {@link #sweep(List, Set)} takes
 * one "ring" of areas, tests every area in it, and then calls itself with the
 * next ring outwards. Recursion stops when a candidate is found or when the
 * frontier runs dry, which is guaranteed to happen because every area is added
 * to {@code visited} exactly once.
 *
 * <p>
 * Note that the recursion depth equals the number of <em>rings</em> (the graph
 * eccentricity of the agent, typically a few dozen on a competition map), not
 * the number of areas. A plain recursive DFS flood fill would recurse once per
 * area and could overflow the JVM stack on a large map; this version cannot.
 *
 * <p>
 * <b>Coverage.</b> Two kinds of road are accepted as a target:
 * <ol>
 * <li>a road we have <em>seen</em> and that still holds blockades — work we
 * know about;</li>
 * <li>a road we have <em>never</em> seen — exploration, because a blockade we
 * have not looked at yet is still a blockade.</li>
 * </ol>
 * Since ring 1 is checked before ring 2 and so on, the agent always deals with
 * the nearest outstanding road first and the frontier grows steadily outward
 * until every road in the connected component has been visited and opened. When
 * the flood fill finds nothing (all reachable roads are done, or the agent is
 * stranded in a disconnected component) the module falls back on the path
 * planner over every remaining road in the world.
 *
 * <p>
 * Deliberately <b>no clustering</b>: the stock sample restricts each agent to
 * its own K-means cluster, which by construction can never cover the whole map.
 * Agents are spread out instead by the tie-break in
 * {@link #pickFromRing(List)}, which is a pure function of the agent's own ID
 * and so needs no radio communication.
 */
public class SampleRoadDetector extends RoadDetector {

  /**
   * Cycles the agent will chase one road before writing it off. Without this a
   * road that turns out to be unreachable (walled off by a collapsed building,
   * or across a gap the planner cannot bridge) would hold the agent forever.
   */
  private static final int TARGET_PATIENCE = 30;

  private PathPlanning pathPlanning;
  private Logger logger;

  /** The road the agent is currently committed to. */
  private EntityID result;

  /** Adjacency list of the whole area graph. Built once, never changes. */
  private Map<EntityID, Collection<EntityID>> neighbourGraph;

  /** Every road-like area in the map (Road + Hydrant). Built once. */
  private Set<EntityID> allRoads;

  /** Roads we have observed at least once. */
  private Set<EntityID> seenRoads;

  /** Roads observed to still contain at least one blockade. */
  private Set<EntityID> blockedRoads;

  /** Roads observed to be completely open. */
  private Set<EntityID> clearedRoads;

  private boolean initialised;

  /** Simulation time at which the current target was chosen. */
  private int targetChosenAt;

  public SampleRoadDetector(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
    super(ai, wi, si, moduleManager, developData);
    this.logger = DefaultLogger.getLogger(ai.me());

    this.pathPlanning = moduleManager.getModule(
        "SampleRoadDetector.PathPlanning",
        "adf.impl.module.algorithm.DijkstraPathPlanning");
    registerModule(this.pathPlanning);

    this.neighbourGraph = new HashMap<>();
    this.allRoads = new HashSet<>();
    this.seenRoads = new HashSet<>();
    this.blockedRoads = new HashSet<>();
    this.clearedRoads = new HashSet<>();
    this.initialised = false;
    this.result = null;
    this.targetChosenAt = 0;
  }


  // ---------------------------------------------------------------- lifecycle

  @Override
  public RoadDetector precompute(PrecomputeData precomputeData) {
    super.precompute(precomputeData);
    if (this.getCountPrecompute() >= 2) {
      return this;
    }
    this.buildStaticMap();
    return this;
  }


  @Override
  public RoadDetector resume(PrecomputeData precomputeData) {
    super.resume(precomputeData);
    if (this.getCountResume() >= 2) {
      return this;
    }
    this.buildStaticMap();
    return this;
  }


  @Override
  public RoadDetector preparate() {
    super.preparate();
    if (this.getCountPreparate() >= 2) {
      return this;
    }
    this.buildStaticMap();
    return this;
  }


  /**
   * The static geometry of the map (which area touches which, and which of them
   * are roads) is known from the very first cycle and never changes, so it is
   * cached exactly once.
   */
  private void buildStaticMap() {
    if (this.initialised) {
      return;
    }
    for (Entity entity : this.worldInfo) {
      if (!(entity instanceof Area)) {
        continue;
      }
      Area area = (Area) entity;
      this.neighbourGraph.put(area.getID(),
          new ArrayList<>(area.getNeighbours()));
      if (area instanceof Road) {
        this.allRoads.add(area.getID());
      }
    }
    this.initialised = true;
    logger.debug("Map cached: " + this.allRoads.size() + " roads, "
        + this.neighbourGraph.size() + " areas");
  }


  // ------------------------------------------------------------- perception

  @Override
  public RoadDetector updateInfo(MessageManager messageManager) {
    super.updateInfo(messageManager);
    if (this.getCountUpdateInfo() >= 2) {
      return this;
    }
    this.buildStaticMap();

    // Everything the agent perceived (or was told about) this cycle.
    for (EntityID id : this.worldInfo.getChanged().getChangedEntities()) {
      StandardEntity entity = this.worldInfo.getEntity(id);
      if (entity instanceof Road) {
        this.observeRoad((Road) entity);
      } else if (entity instanceof Blockade) {
        // A blockade sighting immediately condemns the road it sits on, even if
        // that road itself was not in the change set this cycle.
        Blockade blockade = (Blockade) entity;
        if (blockade.isPositionDefined()) {
          EntityID roadID = blockade.getPosition();
          this.seenRoads.add(roadID);
          this.clearedRoads.remove(roadID);
          this.blockedRoads.add(roadID);
        }
      }
    }

    // The road under our feet and the ones next to it are the most reliable
    // observations we have, so refresh them explicitly.
    StandardEntity here = this.worldInfo
        .getEntity(this.agentInfo.getPosition());
    if (here instanceof Area) {
      this.refresh(here);
      for (EntityID neighbourID : ((Area) here).getNeighbours()) {
        this.refresh(this.worldInfo.getEntity(neighbourID));
      }
    }
    return this;
  }


  private void refresh(StandardEntity entity) {
    if (entity instanceof Road) {
      this.observeRoad((Road) entity);
    }
  }


  /**
   * Files a road into exactly one of the three buckets. A road whose blockade
   * list is undefined is recorded as "seen" but is put in neither the blocked
   * nor the cleared bucket, because we honestly do not know yet.
   */
  private void observeRoad(Road road) {
    EntityID id = road.getID();
    this.seenRoads.add(id);
    if (!road.isBlockadesDefined()) {
      return;
    }
    if (road.getBlockades().isEmpty()) {
      this.blockedRoads.remove(id);
      this.clearedRoads.add(id);
    } else {
      this.clearedRoads.remove(id);
      this.blockedRoads.add(id);
    }
  }


  // ------------------------------------------------------------ target choice

  @Override
  public RoadDetector calc() {
    this.buildStaticMap();
    EntityID position = this.agentInfo.getPosition();

    // 1. A blocked road under our own feet always wins: the agent physically
    //    cannot leave until it has cut its way out.
    StandardEntity here = this.worldInfo.getEntity(position);
    if (here instanceof Road && this.isStillBlocked((Road) here)) {
      this.result = position;
      return this;
    }

    // 2. Keep the current commitment unless it is finished or invalid. Picking
    //    a fresh target every cycle makes the agent oscillate between two roads
    //    and clear neither of them.
    if (this.result != null) {
      boolean expired = (this.agentInfo.getTime()
          - this.targetChosenAt) > TARGET_PATIENCE;
      if (!this.isDone(this.result) && !expired) {
        return this;
      }
      if (expired) {
        // Give up on it and do not come back: treat it as handled so the sweep
        // moves on instead of re-selecting the same unreachable road.
        logger.debug("Giving up on unreachable road " + this.result);
        this.blockedRoads.remove(this.result);
        this.seenRoads.add(this.result);
      } else {
        logger.debug("Road " + this.result + " opened, choosing a new one");
      }
      this.result = null;
    }

    // 3. Recursive ring-by-ring flood fill outwards from the agent.
    Set<EntityID> visited = new HashSet<>();
    visited.add(position);
    this.result = this.sweep(Collections.singletonList(position), visited);

    // 4. Nothing reachable through the flood fill: ask the path planner for the
    //    nearest of every road still outstanding anywhere in the world.
    if (this.result == null) {
      this.result = this.farTarget(position);
    }

    if (this.result == null) {
      logger.debug("Every known road is open - map complete");
    } else {
      this.targetChosenAt = this.agentInfo.getTime();
      logger.debug("New target road: " + this.result + " (" + progress() + ")");
    }
    return this;
  }


  /**
   * One level of the recursive breadth-first sweep.
   *
   * @param ring
   *   every area at the current distance from the agent
   * @param visited
   *   every area already enqueued, so no area is ever processed twice
   *
   * @return the chosen road, or {@code null} if this branch of the search is
   *   exhausted
   */
  private EntityID sweep(List<EntityID> ring, Set<EntityID> visited) {
    // Base case 1: the frontier is empty, the connected component is exhausted.
    if (ring.isEmpty()) {
      return null;
    }
    List<EntityID> blockedHere = new ArrayList<>();
    List<EntityID> unknownHere = new ArrayList<>();
    List<EntityID> nextRing = new ArrayList<>();

    for (EntityID areaID : ring) {
      if (this.allRoads.contains(areaID)) {
        if (this.blockedRoads.contains(areaID)) {
          blockedHere.add(areaID);
        } else if (!this.seenRoads.contains(areaID)) {
          unknownHere.add(areaID);
        }
      }
      Collection<EntityID> neighbours = this.neighbourGraph.get(areaID);
      if (neighbours == null) {
        continue;
      }
      for (EntityID neighbourID : neighbours) {
        if (visited.add(neighbourID)) {
          nextRing.add(neighbourID);
        }
      }
    }

    // Known work beats exploration at the same distance.
    if (!blockedHere.isEmpty()) {
      return this.pickFromRing(blockedHere);
    }
    if (!unknownHere.isEmpty()) {
      return this.pickFromRing(unknownHere);
    }

    // Recursive case: nothing in this ring, try the next one outwards.
    return this.sweep(nextRing, visited);
  }


  /**
   * Deterministic tie-break inside one ring. Every police agent runs the same
   * code, so if they all took {@code get(0)} they would pile onto the same road
   * and waste most of the force. Offsetting by the agent's own ID spreads them
   * across the candidates without a single radio message.
   */
  private EntityID pickFromRing(List<EntityID> candidates) {
    if (candidates.size() == 1) {
      return candidates.get(0);
    }
    List<EntityID> sorted = new ArrayList<>(candidates);
    sorted.sort((a, b) -> Integer.compare(a.getValue(), b.getValue()));
    int offset = Math.abs(this.agentInfo.getID().getValue()) % sorted.size();
    return sorted.get(offset);
  }


  /**
   * Fallback used when the flood fill comes up empty: hand every outstanding
   * road to the path planner at once and take whichever one it reaches first.
   * This also rescues an agent whose local component really is finished.
   */
  private EntityID farTarget(EntityID position) {
    Collection<EntityID> remaining = new HashSet<>(this.allRoads);
    remaining.removeAll(this.clearedRoads);
    remaining.remove(position);
    if (remaining.isEmpty()) {
      return null;
    }
    this.pathPlanning.setFrom(position);
    this.pathPlanning.setDestination(remaining);
    List<EntityID> path = this.pathPlanning.calc().getResult();
    if (path == null || path.isEmpty()) {
      return null;
    }
    return path.get(path.size() - 1);
  }


  /**
   * True once the road no longer needs the agent's attention. An unexplored
   * road is never "done": the whole point of targeting it was to go and look.
   */
  private boolean isDone(EntityID roadID) {
    StandardEntity entity = this.worldInfo.getEntity(roadID);
    if (!(entity instanceof Road)) {
      return true;
    }
    Road road = (Road) entity;
    if (!road.isBlockadesDefined()) {
      return false;
    }
    return road.getBlockades().isEmpty();
  }


  private boolean isStillBlocked(Road road) {
    return road.isBlockadesDefined() && !road.getBlockades().isEmpty();
  }


  @Override
  public EntityID getTarget() {
    return this.result;
  }


  /** Exposed for debugging: how much of the map is provably open. */
  public String progress() {
    return this.clearedRoads.size() + "/" + this.allRoads.size()
        + " roads open, " + this.blockedRoads.size() + " known blocked";
  }
}