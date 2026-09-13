package sample_team.module.complex.police.improvement;


import adf.core.agent.action.police.ActionClear;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.component.extaction.ExtAction;

import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Edge;

import java.util.ArrayList;
import java.util.List;

import sample_team.module.complex.police.observation.URFPoliceCsvExporter;
import sample_team.module.complex.police.observation.URFPoliceMetrics;
import sample_team.module.complex.police.observation.URFPoliceStuckDetector;

/**
 * Independent URF Police CLEAR action module.
 *
 * PI-1 design goals:
 *
 * 1. Do not inherit DefaultExtActionClear.
 * 2. Do not inherit Sample decision logic.
 * 3. Clear blockades within clear range of the Police agent.
 * 4. Never generate ActionMove.
 * 5. Return null when clearing is not appropriate so that
 *    DefaultTacticsPoliceForce can continue to ExtActionMove.
 * 6. Detect stagnant clearing using blockade repair cost.
 * 7. Use deterministic directional clearing as recovery.
 * 8. Preserve the trusted URF observation/CSV pipeline.
 *
 * IMPORTANT:
 *
 * This PI-1 module intentionally does NOT perform:
 *
 * - target selection,
 * - global path planning,
 * - Fire/Ambulance prioritisation,
 * - multi-Police coordination,
 * - exploration.
 *
 * Those responsibilities belong to later independent URF modules.
 */
public class URFPoliceExtActionClear extends ExtAction {
  /**
   * Number of consecutive clear attempts with no observed
   * reduction in blockade repair cost before changing the
   * clearing method.
   */
  private static final int STAGNANT_CLEAR_LIMIT = 3;

  /**
   * Extra distance placed beyond the nearest blockade boundary
   * during directional recovery.
   *
   * RCRS coordinates are expressed in world distance units.
   */
  private static final double RECOVERY_EXTENSION = 1000.0;

  /**
   * Small value used to detect a zero-length direction vector.
   */
  private static final double MIN_VECTOR_LENGTH = 1.0;

  /**
   * Maximum Police clear distance defined by the scenario.
   */
  private final int clearDistance;

  /**
   * Trusted observation metrics from the frozen baseline.
   */
  private final URFPoliceMetrics metrics;

  /**
   * Trusted stuck detector from the frozen baseline.
   */
  private final URFPoliceStuckDetector stuckDetector;

  /**
   * Target supplied by DefaultTacticsPoliceForce.
   *
   * PI-1 does not perform target selection itself.
   */
  private EntityID target;

  /**
   * Blockade cleared during the previous clear attempt.
   */
  private EntityID previousClearBlockade;

  /**
   * Previously observed repair cost.
   */
  private int previousRepairCost;

  /**
   * Number of consecutive attempts for which the same
   * blockade did not show repair-cost improvement.
   */
  private int stagnantClearCount;

  /**
   * Number of recovery-mode directional clears.
   */
  private int recoveryClearCount;

  public URFPoliceExtActionClear(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo, ModuleManager moduleManager, DevelopData developData) {
    super(agentInfo, worldInfo, scenarioInfo, moduleManager, developData);
    this.clearDistance = scenarioInfo.getClearRepairDistance();
    this.metrics = URFPoliceMetrics.forAgent(agentInfo.getID());
    this.stuckDetector = URFPoliceStuckDetector.forAgent(agentInfo.getID());
    this.target = null;
    this.previousClearBlockade = null;
    this.previousRepairCost = -1;
    this.stagnantClearCount = 0;
    this.recoveryClearCount = 0;
  }

  /**
   * Receive the target selected by the configured RoadDetector.
   *
   * Target selection remains outside this class.
   */
  @Override
  public ExtAction setTarget(EntityID target) {
    this.target = target;
    return this;
  }

  /**
   * Calculate the Police CLEAR action.
   *
   * PI-1 rule:
   *
   * This method may return:
   *
   *   ActionClear
   *   null
   *
   * It deliberately never returns ActionMove.
   */
  @Override
  public ExtAction calc() {

    long startNanos = System.nanoTime();
    this.result = null;
    String decision = "NO_LOCAL_CLEAR";
    ClearCandidate candidate = this.findNearbyCandidate();

    if (candidate != null) {
      if (candidate.distance <= this.clearDistance) {
        this.updateClearProgress(candidate.blockade);
        if (this.stagnantClearCount >= STAGNANT_CLEAR_LIMIT) {
          this.result = this.createRecoveryClear(candidate);
          this.recoveryClearCount++;
          this.stagnantClearCount = 0;
          decision = "RECOVERY_DIRECTIONAL_CLEAR";
        } 
        else {
          /*
           * Normal clearing.
           *
           * Use the blockade EntityID directly.
           * No MOVE action is generated here.
           */
          this.result = new ActionClear(candidate.blockade);
          decision = "CLEAR_NEAREST_BLOCKADE";
        }
      } 
      else {
        /*
         * A blockade exists on the current road but is not
         * yet inside the clear range.
         *
         * Return null.
         *
         * DefaultTacticsPoliceForce can therefore continue
         * to the configured MOVE module.
         */
        this.resetPreviousClearAttempt();
        decision = "BLOCKADE_OUTSIDE_CLEAR_RANGE_HANDOFF";
      }
    } 
    else {
      this.resetPreviousClearAttempt();
      decision = "ROAD_CLEAR_OR_NO_LOCAL_BLOCKADE";
    }
    long elapsedNanos = System.nanoTime() - startNanos;

    /*
     * Preserve the trusted observation pipeline.
     *
     * If result == null, URFObservedExtActionMove will
     * record and export the final MOVE decision.
     *
     * If result != null, CLEAR is the final action and
     * must be exported here.
     */
    this.metrics.recordClearModuleResult(this.agentInfo.getTime(), this.target, this.result, elapsedNanos);
    if (this.result != null) {
      this.stuckDetector.evaluate(this.metrics);
      URFPoliceCsvExporter.export(this.metrics, this.stuckDetector);
      System.out.println(this.metrics.toLogLine() + " " + this.stuckDetector.toLogFields() + " pi1ClearDecision=" + decision + " recoveryClearCount=" + this.recoveryClearCount);
    }
    return this;
  }

  /**
   * Find the nearest valid blockade in reach of the Police agent.
   *
   * The search covers the agent's current area and every neighbouring
   * area. The kernel gates a clear command on physical distance, not on
   * road membership, so a blockade just across a shared edge is a legal
   * and often necessary target. Restricting the search to the current
   * road left the agent unable to cut the blockade that was actually
   * holding it, because MOVE reported the edge as passable and the
   * traffic simulator then refused every step.
   *
   * Range is still enforced by the caller against clearDistance.
   */
  private ClearCandidate findNearbyCandidate() {

    EntityID positionID = this.agentInfo.getPosition();
    if (positionID == null) {
      return null;
    }

    ClearCandidate best = null;

    for (EntityID areaID : this.searchAreas(positionID)) {
      StandardEntity areaEntity = this.worldInfo.getEntity(areaID);
      if (!(areaEntity instanceof Road)) {
        continue;
      }

      Road road = (Road) areaEntity;
      if (!road.isBlockadesDefined() || road.getBlockades().isEmpty()) {
        continue;
      }

      for (EntityID blockadeID : road.getBlockades()) {
        StandardEntity entity = this.worldInfo.getEntity(blockadeID);
        if (!(entity instanceof Blockade)) {
          continue;
        }
        Blockade blockade = (Blockade) entity;
        ClearCandidate candidate = this.createCandidate(blockade);

        if (candidate == null) {
          continue;
        }

        if (best == null || this.isBetterCandidate(candidate, best)) {
          best = candidate;
        }
      }
    }
    return best;
  }

  /**
   * @param positionID the agent's current area
   * @return the current area followed by every neighbouring area
   */
  private List<EntityID> searchAreas(EntityID positionID) {
    List<EntityID> areas = new ArrayList<>();
    areas.add(positionID);

    StandardEntity entity = this.worldInfo.getEntity(positionID);
    if (!(entity instanceof Area)) {
      return areas;
    }

    Area area = (Area) entity;
    if (!area.isEdgesDefined()) {
      return areas;
    }

    for (Edge edge : area.getEdges()) {
      EntityID neighbour = edge.getNeighbour();
      if (neighbour != null) {
        areas.add(neighbour);
      }
    }
    return areas;
  }

  /**
   * Build geometric information for one blockade.
   *
   * Distance is measured from the Police location to the
   * nearest point on the blockade polygon.
   */
  private ClearCandidate createCandidate(Blockade blockade) {

    double agentX = this.agentInfo.getX();
    double agentY = this.agentInfo.getY();

    /*
     * Preferred geometry:
     * blockade polygon.
     */
    if (blockade.isApexesDefined()) {
      int[] apexes = blockade.getApexes();
      if (apexes != null && apexes.length >= 6) {
        /*
         * If Police is inside the blockade polygon,
         * consider the distance zero.
         */
        if (blockade.getShape().contains(agentX, agentY)) {
          double pointX;
          double pointY;

          if (blockade.isXDefined() && blockade.isYDefined()) {
            pointX = blockade.getX();
            pointY = blockade.getY();
          } 
          else {
            pointX = apexes[0];
            pointY = apexes[1];
          }
          return new ClearCandidate(blockade, 0.0, pointX, pointY);
        }

        double bestDistance = Double.MAX_VALUE;
        double bestX = 0.0;
        double bestY = 0.0;
        int pointCount = apexes.length / 2;
        for (int i = 0; i < pointCount; i++) {
          int next = (i + 1) % pointCount;
          double x1 = apexes[i * 2];
          double y1 = apexes[i * 2 + 1];
          double x2 = apexes[next * 2];
          double y2 = apexes[next * 2 + 1];
          ClosestPoint point = this.closestPointOnSegment(agentX, agentY, x1, y1, x2, y2);
          if (point.distance < bestDistance) {
            bestDistance = point.distance;
            bestX = point.x;
            bestY = point.y;
          }
        }

        if (bestDistance < Double.MAX_VALUE) {
          return new ClearCandidate(blockade, bestDistance, bestX, bestY);
        }
      }
    }

    /*
     * Fallback:
     * use the Blockade X/Y location when polygon data is
     * not currently available.
     */
    if (blockade.isXDefined() && blockade.isYDefined()) {
      double dx = blockade.getX() - agentX;
      double dy = blockade.getY() - agentY;
      return new ClearCandidate(blockade, Math.hypot(dx, dy), blockade.getX(), blockade.getY());
    }
    return null;
  }

  /**
   * Determine whether candidate A should be preferred over B.
   *
   * Primary ordering:
   * nearest physical blockade.
   *
   * Tie-break:
   * smaller EntityID for deterministic behaviour.
   */
  private boolean isBetterCandidate(ClearCandidate candidate, ClearCandidate currentBest) {
    if (candidate.distance < currentBest.distance) {
      return true;
    }
    if (Math.abs(candidate.distance - currentBest.distance) > 0.0001) {
      return false;
    }
    return candidate.blockade.getID().getValue() < currentBest.blockade.getID().getValue();
  }

  /**
   * Compare the currently observed blockade repair cost with
   * the previous clear attempt.
   *
   * A decreasing repair cost means clearing is progressing.
   *
   * An unchanged or increased repair cost counts as stagnant.
   */
  private void updateClearProgress(Blockade blockade) {
    EntityID blockadeID = blockade.getID();
    int currentRepairCost = blockade.isRepairCostDefined() ? blockade.getRepairCost() : -1;

    if (this.previousClearBlockade != null && this.previousClearBlockade.equals(blockadeID) && this.previousRepairCost >= 0 && currentRepairCost >= 0) {
      if (currentRepairCost < this.previousRepairCost) {
        /*
         * The blockade is being repaired.
         */
        this.stagnantClearCount = 0;
      } 
      else {
        /*
         * Same blockade and no measurable progress.
         */
        this.stagnantClearCount++;
      }
    } 
    else {
      /*
       * New blockade or unavailable repair-cost history.
       */
      this.stagnantClearCount = 0;
    }
    this.previousClearBlockade = blockadeID;
    this.previousRepairCost = currentRepairCost;
  }

  /**
   * Deterministic recovery.
   *
   * Instead of issuing MOVE from the CLEAR module, change the
   * clear mode and clear direction through the nearest
   * blockade boundary.
   */
  private ActionClear createRecoveryClear(ClearCandidate candidate) {
    double agentX = this.agentInfo.getX();
    double agentY = this.agentInfo.getY();
    double dx = candidate.pointX - agentX;
    double dy = candidate.pointY - agentY;
    double vectorLength = Math.hypot(dx, dy);

    /*
     * If there is no meaningful direction vector,
     * safely fall back to target-based clearing.
     */
    if (vectorLength < MIN_VECTOR_LENGTH) {
      return new ActionClear(candidate.blockade);
    }

    /*
     * Point slightly beyond the nearest blockade boundary,
     * but never beyond the scenario clear range.
     */
    double desiredDistance = Math.min(Math.max(0.0, this.clearDistance - 100.0), vectorLength + RECOVERY_EXTENSION);
    if (desiredDistance < MIN_VECTOR_LENGTH) {
      return new ActionClear(candidate.blockade);
    }

    double scale = desiredDistance / vectorLength;
    int destinationX = (int) Math.round(agentX + dx * scale);
    int destinationY = (int) Math.round(agentY + dy * scale);

    /*
     * Still an ActionClear.
     *
     * PI-1 deliberately never creates ActionMove.
     */
    return new ActionClear(destinationX, destinationY, candidate.blockade);
  }

  /**
   * Clear previous attempt state when control is handed to
   * another action module.
   */
  private void resetPreviousClearAttempt() {
    this.previousClearBlockade = null;
    this.previousRepairCost = -1;
    this.stagnantClearCount = 0;
  }

  /**
   * Compute the closest point on line segment AB to point P.
   */
  private ClosestPoint closestPointOnSegment(double px, double py, double ax, double ay, double bx, double by) {
    double abX = bx - ax;
    double abY = by - ay;
    double lengthSquared = abX * abX + abY * abY;
    if (lengthSquared <= 0.0) {
      double distance = Math.hypot(px - ax, py - ay);
      return new ClosestPoint(ax, ay, distance);
    }
    double projection = ((px - ax) * abX + (py - ay) * abY) / lengthSquared;
    projection = Math.max(0.0, Math.min(1.0, projection));
    double closestX = ax + projection * abX;
    double closestY = ay + projection * abY;
    double distance = Math.hypot(px - closestX, py - closestY);
    return new ClosestPoint(closestX, closestY, distance);
  }

  /**
   * Immutable information about one possible blockade.
   */
  private static final class ClearCandidate {
    private final Blockade blockade;
    private final double distance;
    private final double pointX;
    private final double pointY;
    private ClearCandidate(Blockade blockade, double distance, double pointX, double pointY) {
      this.blockade = blockade;
      this.distance = distance;
      this.pointX = pointX;
      this.pointY = pointY;
    }
  }

  /**
   * Immutable closest-point calculation result.
   */
  private static final class ClosestPoint {
    private final double x;
    private final double y;
    private final double distance;
    private ClosestPoint(double x, double y, double distance) {
      this.x = x;
      this.y = y;
      this.distance = distance;
    }
  }
}
