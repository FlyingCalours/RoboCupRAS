package sample_team.module.complex;

import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.module.algorithm.PathPlanning;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.standard.entities.Area;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
 * A* over the area-neighbour graph.
 *
 * <p>
 * Fixes over the starting version:
 * <ul>
 * <li>the termination test was {@code open.size() < 0}, which is never true — a
 * size is never negative — so an exhausted search span forever instead of
 * reporting "no path". It is now {@code open.isEmpty()}.</li>
 * <li>the open list was scanned linearly for the cheapest node; it is now a
 * {@link PriorityQueue}, which is what makes A* cheaper than Dijkstra rather
 * than more expensive.</li>
 * <li>when a cheaper route to an already-known node was found, the node map was
 * updated but the node was never re-queued, so the improvement was silently
 * discarded. Improvements are now pushed back onto the queue.</li>
 * <li>the heuristic was hard-wired to {@code targets[0]}. With a set of goals
 * (which is how {@code Search} calls this) that is not admissible and returns
 * wrong paths; it is now the minimum over the goals.</li>
 * <li>the returned path included the agent's own position, whereas
 * {@code DijkstraPathPlanning} excludes it. Callers that mix the two planners
 * were off by one. This version follows the Dijkstra convention.</li>
 * <li>null / empty inputs threw instead of returning no result.</li>
 * </ul>
 *
 * <p>
 * With many goals the heuristic costs more than it saves, so above
 * {@link #HEURISTIC_TARGET_LIMIT} goals it is dropped to zero and the search
 * degrades gracefully into Dijkstra — still correct, just uninformed.
 */
public class AStarPathPlanning extends PathPlanning {

  private static final int HEURISTIC_TARGET_LIMIT = 8;

  private Map<EntityID, Set<EntityID>> graph;
  private EntityID from;
  private Collection<EntityID> targets;
  private List<EntityID> result;

  public AStarPathPlanning(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
    super(ai, wi, si, moduleManager, developData);
    this.init();
  }


  private void init() {
    Map<EntityID, Set<EntityID>> neighbours = new LazyMap<EntityID, Set<EntityID>>() {

      @Override
      public Set<EntityID> createValue() {
        return new HashSet<>();
      }
    };
    for (Entity next : this.worldInfo) {
      if (next instanceof Area) {
        Collection<EntityID> areaNeighbours = ((Area) next).getNeighbours();
        neighbours.get(next.getID()).addAll(areaNeighbours);
      }
    }
    this.graph = neighbours;
  }


  @Override
  public List<EntityID> getResult() {
    return this.result;
  }


  @Override
  public PathPlanning setFrom(EntityID id) {
    this.from = id;
    return this;
  }


  @Override
  public PathPlanning setDestination(Collection<EntityID> targets) {
    this.targets = targets;
    return this;
  }


  @Override
  public PathPlanning updateInfo(MessageManager messageManager) {
    super.updateInfo(messageManager);
    return this;
  }


  @Override
  public PathPlanning precompute(PrecomputeData precomputeData) {
    super.precompute(precomputeData);
    return this;
  }


  @Override
  public PathPlanning resume(PrecomputeData precomputeData) {
    super.resume(precomputeData);
    return this;
  }


  @Override
  public PathPlanning preparate() {
    super.preparate();
    return this;
  }


  @Override
  public PathPlanning calc() {
    this.result = null;

    if (this.from == null || this.targets == null || this.targets.isEmpty()) {
      return this;
    }
    if (this.targets.contains(this.from)) {
      List<EntityID> here = new ArrayList<>();
      here.add(this.from);
      this.result = here;
      return this;
    }

    boolean useHeuristic = this.targets.size() <= HEURISTIC_TARGET_LIMIT;

    Map<EntityID, Node> best = new HashMap<>();
    Set<EntityID> closed = new HashSet<>();
    PriorityQueue<Node> open = new PriorityQueue<>(
        Comparator.comparingDouble(Node::estimate));

    Node start = new Node(null, this.from, 0.0,
        useHeuristic ? this.heuristic(this.from) : 0.0);
    best.put(this.from, start);
    open.add(start);

    while (!open.isEmpty()) {
      Node current = open.poll();

      // Stale queue entry: a cheaper route to this node was found after it was
      // pushed, so this copy can be discarded.
      if (closed.contains(current.getID())) {
        continue;
      }
      closed.add(current.getID());

      if (this.targets.contains(current.getID())) {
        this.result = this.buildPath(current, best);
        return this;
      }

      Collection<EntityID> neighbours = this.graph.get(current.getID());
      if (neighbours == null) {
        continue;
      }
      for (EntityID neighbour : neighbours) {
        if (closed.contains(neighbour)) {
          continue;
        }
        double cost = current.getCost()
            + this.worldInfo.getDistance(current.getID(), neighbour);
        Node known = best.get(neighbour);
        if (known == null || cost < known.getCost()) {
          Node improved = new Node(current.getID(), neighbour, cost,
              useHeuristic ? this.heuristic(neighbour) : 0.0);
          best.put(neighbour, improved);
          open.add(improved);
        }
      }
    }

    // Open list exhausted: the goals are unreachable from here.
    return this;
  }


  /**
   * Straight-line distance to the closest goal. Never overestimates the true
   * travel cost, so A* stays optimal.
   */
  private double heuristic(EntityID id) {
    double smallest = Double.MAX_VALUE;
    for (EntityID target : this.targets) {
      double distance = this.worldInfo.getDistance(id, target);
      if (distance < smallest) {
        smallest = distance;
      }
    }
    return smallest == Double.MAX_VALUE ? 0.0 : smallest;
  }


  /**
   * Walks the parent chain back to the start, then drops the start itself so
   * the result matches {@code DijkstraPathPlanning}: first element is the next
   * area to move into, last element is the goal.
   */
  private List<EntityID> buildPath(Node goal, Map<EntityID, Node> best) {
    LinkedList<EntityID> path = new LinkedList<>();
    Node cursor = goal;
    while (cursor != null) {
      path.addFirst(cursor.getID());
      EntityID parent = cursor.getParent();
      cursor = (parent == null) ? null : best.get(parent);
    }
    if (path.size() > 1 && path.getFirst().equals(this.from)) {
      path.removeFirst();
    }
    return path;
  }


  private static class Node {

    private final EntityID id;
    private final EntityID parent;
    private final double cost;
    private final double heuristic;

    Node(EntityID parent, EntityID id, double cost, double heuristic) {
      this.parent = parent;
      this.id = id;
      this.cost = cost;
      this.heuristic = heuristic;
    }


    EntityID getID() {
      return this.id;
    }


    EntityID getParent() {
      return this.parent;
    }


    double getCost() {
      return this.cost;
    }


    double estimate() {
      return this.cost + this.heuristic;
    }
  }
}