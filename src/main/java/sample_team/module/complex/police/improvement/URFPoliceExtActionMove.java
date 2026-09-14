package sample_team.module.complex.police.improvement;

import adf.core.agent.action.Action;
import adf.core.agent.action.common.ActionMove;
import adf.core.agent.action.police.ActionClear;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.component.extaction.ExtAction;
import adf.core.component.module.complex.RoadDetector;
import adf.impl.extaction.DefaultExtActionMove;
import java.util.ArrayList;
import java.util.List;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;
import sample_team.module.complex.police.observation.URFPoliceCsvExporter;
import sample_team.module.complex.police.observation.URFPoliceMetrics;
import sample_team.module.complex.police.observation.URFPoliceStuckDetector;

/**
 * Police MOVE action module.
 *
 * This module runs when the CLEAR module returned null. It must never
 * return null itself, because a null here lets the tactics fallthrough
 * end in ActionRest, which is indistinguishable from a frozen agent.
 *
 * The road target is preferred over the Search target. Tactics hands this
 * module only the Search target, so the road target is read straight from
 * the RoadDetector instance held by the ModuleManager. That is the same
 * object tactics uses, because getModule caches per configuration key, so
 * no shared static state is needed to carry the target across.
 *
 * When the agent is already standing on the road target it steps to a
 * neighbouring area instead, which is an attempt to push through the
 * corridor the CLEAR module has been cutting. An earlier version
 * suppressed the target in that situation; combined with scheduled yield
 * turns that made agents abandon roads they were part way through
 * opening.
 */
public class URFPoliceExtActionMove extends DefaultExtActionMove {

  /**
   * Configuration key of the RoadDetector.
   *
   * This must match the key used by the configured tactics class, because
   * the ModuleManager caches one instance per key. A different key would
   * silently create a second detector whose calc is never called.
   */
  private static final String ROAD_DETECTOR_KEY =
      "DefaultTacticsPoliceForce.RoadDetector";

  /** Used only when the key above is absent from the configuration. */
  private static final String ROAD_DETECTOR_DEFAULT =
      "sample_team.module.complex.police.improvement.URFRoadDetector";

  /** Distance held back from the clear limit when placing the aim point. */
  private static final double AIM_MARGIN = 100.0;

  /** Number of evenly spaced bearings used by the escape sweep. */
  private static final int BEARING_COUNT = 8;

  /**
   * Step between consecutive bearings.
   *
   * Coprime with BEARING_COUNT, so all eight bearings are visited before
   * any repeats, and consecutive attempts point far apart.
   */
  private static final int BEARING_STEP = 3;

  /** One full rotation in radians. */
  private static final double FULL_TURN = 2.0 * Math.PI;

  private final int clearDistance;
  private final RoadDetector roadDetector;
  private final URFPoliceMetrics metrics;
  private final URFPoliceStuckDetector stuckDetector;

  private EntityID roadTarget;
  private EntityID searchTarget;
  private EntityID requestedTarget;
  private URFMoveTargetSource targetSource;
  private boolean standingOnTarget;
  private int neighbourIndex;
  private int bearingIndex;

  /**
   * @param agentInfo agent information supplied by the ADF
   * @param worldInfo world model supplied by the ADF
   * @param scenarioInfo scenario configuration supplied by the ADF
   * @param moduleManager module registry supplied by the ADF
   * @param developData development configuration supplied by the ADF
   */
  public URFPoliceExtActionMove(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      DevelopData developData) {
    super(agentInfo, worldInfo, scenarioInfo, moduleManager, developData);
    this.clearDistance = scenarioInfo.getClearRepairDistance();

    /*
     * Read-only reference.
     *
     * It is deliberately NOT passed to registerModule. The detector's
     * lifecycle is already driven by the tactics class, and registering
     * it here would drive precompute, resume, preparate and updateInfo a
     * second time from inside the MOVE module.
     */
    this.roadDetector =
        moduleManager.getModule(ROAD_DETECTOR_KEY, ROAD_DETECTOR_DEFAULT);

    this.metrics = URFPoliceMetrics.forAgent(agentInfo.getID());
    this.stuckDetector = URFPoliceStuckDetector.forAgent(agentInfo.getID());

    this.roadTarget = null;
    this.searchTarget = null;
    this.requestedTarget = null;
    this.targetSource = URFMoveTargetSource.NO_TARGET;
    this.standingOnTarget = false;
    this.neighbourIndex = 0;
    this.bearingIndex = 0;
  }

  /**
   * @param target the Search target supplied by the tactics fallthrough
   * @return this module
   */
  @Override
  public ExtAction setTarget(EntityID target) {
    EntityID position = this.agentInfo.getPosition();

    this.searchTarget = target;
    this.roadTarget = this.readRoadTarget();
    this.standingOnTarget =
        this.roadTarget != null && this.roadTarget.equals(position);

    EntityID chosen;
    if (this.standingOnTarget) {
      // Nothing to walk towards; push through instead.
      chosen = null;
      this.targetSource = URFMoveTargetSource.PUSH_THROUGH;
    }
    else if (this.roadTarget != null) {
      chosen = this.roadTarget;
      this.targetSource = URFMoveTargetSource.ROAD_TARGET;
    }
    else if (target != null) {
      chosen = target;
      this.targetSource = URFMoveTargetSource.SEARCH_TARGET;
    }
    else {
      chosen = null;
      this.targetSource = URFMoveTargetSource.NO_TARGET;
    }

    this.requestedTarget = chosen;
    super.setTarget(chosen);
    return this;
  }

  /**
   * @return this module
   */
  @Override
  public ExtAction calc() {
    long startNanos = System.nanoTime();

    Action action = null;
    if (!this.standingOnTarget && this.requestedTarget != null) {
      super.calc();
      action = super.getAction();
    }

    if (action == null) {
      action = this.pushThrough();
    }
    this.result = action;

    long elapsedNanos = System.nanoTime() - startNanos;

    /*
     * Position is NOT recorded here.
     * URFRoadDetector is the single owner of physical position
     * observation.
     */
    this.metrics.recordMoveModuleResult(this.agentInfo.getTime(),
        this.requestedTarget, action, elapsedNanos);

    // MOVE is the last autonomous decision stage.
    this.stuckDetector.evaluate(this.metrics);
    URFPoliceCsvExporter.export(this.metrics, this.stuckDetector);
    // Can Add Logging of system.out.println here

    return this;
  }

  /**
   * @return the road target of the most recent cycle, or null
   */
  public final EntityID getRoadTarget() {
    return this.roadTarget;
  }

  /**
   * @return the Search target of the most recent cycle, or null
   */
  public final EntityID getSearchTarget() {
    return this.searchTarget;
  }

  /**
   * @return the target actually handed to the path follower, or null
   */
  public final EntityID getRequestedTarget() {
    return this.requestedTarget;
  }

  /**
   * @return which of the two targets drove the most recent cycle
   */
  public final URFMoveTargetSource getTargetSource() {
    return this.targetSource;
  }

  /**
   * @return true when the agent stands on the road target
   */
  public final boolean isStandingOnTarget() {
    return this.standingOnTarget;
  }

  /**
   * Read the target chosen by the configured RoadDetector this cycle.
   *
   * calc is deliberately not called on the detector. Tactics has already
   * run it for this timestep, and running it again would advance any
   * internal state twice per cycle.
   *
   * @return the road target, or null when the detector has none
   */
  private EntityID readRoadTarget() {
    if (this.roadDetector == null) {
      return null;
    }
    return this.roadDetector.getTarget();
  }

  /**
   * Never returns null. Each step down the chain is strictly more
   * desperate than the one above it.
   *
   * @return the fallback action
   */
  private Action pushThrough() {
    EntityID position = this.agentInfo.getPosition();

    List<EntityID> passable = this.passableNeighbours(position);
    if (!passable.isEmpty()) {
      // Rotate so repeated failures try different exits.
      return this.stepTo(position, passable);
    }

    Action clear = this.bearingClear();
    if (clear != null) {
      return clear;
    }

    // Walled in with nothing to cut: try an edge the world model says is
    // closed anyway. The traffic simulator will refuse it harmlessly.
    List<EntityID> any = this.allNeighbours(position);
    if (!any.isEmpty()) {
      return this.stepTo(position, any);
    }

    List<EntityID> path = new ArrayList<>();
    path.add(position);
    return new ActionMove(path);
  }

  /**
   * @param position the agent's current area
   * @param options the candidate neighbours
   * @return a move to the next option in rotation
   */
  private Action stepTo(EntityID position, List<EntityID> options) {
    EntityID next = options.get(this.neighbourIndex % options.size());
    this.neighbourIndex++;
    List<EntityID> path = new ArrayList<>();
    path.add(position);
    path.add(next);
    return new ActionMove(path);
  }

  /**
   * @param position the agent's current area
   * @return areas reachable across a passable edge
   */
  private List<EntityID> passableNeighbours(EntityID position) {
    List<EntityID> neighbours = new ArrayList<>();
    if (position == null) {
      return neighbours;
    }
    StandardEntity entity = this.worldInfo.getEntity(position);
    if (!(entity instanceof Area)) {
      return neighbours;
    }
    Area area = (Area) entity;
    if (!area.isEdgesDefined()) {
      return neighbours;
    }
    for (Edge edge : area.getEdges()) {
      if (edge.isPassable()) {
        neighbours.add(edge.getNeighbour());
      }
    }
    return neighbours;
  }

  /**
   * Sweeps along the next bearing when the agent is walled in.
   *
   * Only the three-argument ActionClear is used, so this compiles against
   * any ADF build that supports blockade-targeted area clearing.
   *
   * @return the clear action, or null when no blockade is in reach
   */
  private Action bearingClear() {
    Blockade blockade = this.anyNearbyBlockade();
    if (blockade == null) {
      return null;
    }
    double agentX = this.agentInfo.getX();
    double agentY = this.agentInfo.getY();
    double reach = Math.max(1.0, this.clearDistance - AIM_MARGIN);
    double bearing = this.nextBearing();
    int destinationX = (int) Math.round(agentX + Math.cos(bearing) * reach);
    int destinationY = (int) Math.round(agentY + Math.sin(bearing) * reach);
    return new ActionClear(destinationX, destinationY, blockade);
  }

  /**
   * Deterministic rotating bearing.
   *
   * With eight bearings and a step of three the order is
   * 0, 135, 270, 45, 180, 315, 90, 225 degrees, so every direction is
   * tried once before any repeat and no random source is needed.
   *
   * @return the next bearing in radians
   */
  private double nextBearing() {
    double bearing =
        (this.bearingIndex % BEARING_COUNT) * (FULL_TURN / BEARING_COUNT);
    this.bearingIndex = this.bearingIndex + BEARING_STEP;
    return bearing;
  }

  /**
   * @return any blockade in the current or a neighbouring area, or null
   */
  private Blockade anyNearbyBlockade() {
    EntityID position = this.agentInfo.getPosition();
    if (position == null) {
      return null;
    }
    List<EntityID> areas = new ArrayList<>();
    areas.add(position);
    areas.addAll(this.allNeighbours(position));

    for (EntityID areaID : areas) {
      StandardEntity entity = this.worldInfo.getEntity(areaID);
      if (!(entity instanceof Road)) {
        continue;
      }
      Road road = (Road) entity;
      if (!road.isBlockadesDefined()) {
        continue;
      }
      for (EntityID blockadeID : road.getBlockades()) {
        StandardEntity blockadeEntity = this.worldInfo.getEntity(blockadeID);
        if (blockadeEntity instanceof Blockade) {
          return (Blockade) blockadeEntity;
        }
      }
    }
    return null;
  }

  /**
   * Every neighbour, passable or not, since a blocked edge still has a
   * road behind it worth cutting towards.
   *
   * @param position the agent's current area
   * @return neighbouring area ids
   */
  private List<EntityID> allNeighbours(EntityID position) {
    List<EntityID> neighbours = new ArrayList<>();
    StandardEntity entity = this.worldInfo.getEntity(position);
    if (!(entity instanceof Area)) {
      return neighbours;
    }
    Area area = (Area) entity;
    if (!area.isEdgesDefined()) {
      return neighbours;
    }
    for (Edge edge : area.getEdges()) {
      EntityID neighbour = edge.getNeighbour();
      if (neighbour != null) {
        neighbours.add(neighbour);
      }
    }
    return neighbours;
  }
}