package sample_team.module.complex;

import java.util.List;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

/**
 * Pure geometry used by {@link SampleExtActionClear}.
 *
 * <p>
 * Nothing in here touches {@code WorldInfo}, {@code AgentInfo} or the kernel:
 * every method takes plain numbers and returns plain numbers. That is
 * deliberate — it is the part of the clearing logic that is actually easy to
 * get wrong (issue 6, the zero-length direction vector), so it is the part that
 * has to be unit-testable without booting a simulation. See
 * {@code ClearGeometryTest}.
 */
public final class ClearGeometry {

  /**
   * Below this length (in mm) a direction vector is treated as degenerate.
   * {@code Vector2D.normalised()} divides by the length, so anything at or near
   * zero produces NaN coordinates and a clear command the kernel silently
   * discards.
   */
  public static final double EPSILON = 1.0D;

  /** Bearing step used when sweeping for an escape direction, in radians. */
  public static final double SWEEP_STEP = Math.PI / 3.0D; // 60 degrees

  private ClearGeometry() {
    // utility class
  }


  /**
   * True if the segment {@code (fromX,fromY)-(toX,toY)} crosses any edge of the
   * polygon described by {@code apexes} (x0,y0,x1,y1,...).
   *
   * @return {@code false} for a null or degenerate polygon rather than
   *   throwing, because blockade apexes are frequently undefined
   */
  public static boolean segmentHitsPolygon(double fromX, double fromY,
      double toX, double toY, int[] apexes) {
    if (!isUsablePolygon(apexes)) {
      return false;
    }
    List<Line2D> edges = GeometryTools2D
        .pointsToLines(GeometryTools2D.vertexArrayToPoints(apexes), true);
    for (Line2D edge : edges) {
      Point2D start = edge.getOrigin();
      Point2D end = edge.getEndPoint();
      if (java.awt.geom.Line2D.linesIntersect(fromX, fromY, toX, toY,
          start.getX(), start.getY(), end.getX(), end.getY())) {
        return true;
      }
    }
    return false;
  }


  /** A polygon needs at least three points, i.e. six ints. */
  public static boolean isUsablePolygon(int[] apexes) {
    return apexes != null && apexes.length >= 6 && apexes.length % 2 == 0;
  }


  /**
   * The point {@code clearDistance} away from the agent, in the direction of
   * the aim point. This is the {@code (x,y)} that goes into
   * {@code AKClearArea}.
   *
   * @return {@code null} when the agent is standing on the aim point — the
   *   direction is undefined there and the caller must pick another aim
   *   instead of normalising a zero vector (issue 6)
   */
  public static Point2D cutPoint(double agentX, double agentY, double aimX,
      double aimY, int clearDistance) {
    Vector2D direction = new Point2D(aimX, aimY)
        .minus(new Point2D(agentX, agentY));
    if (direction.getLength() < EPSILON) {
      return null;
    }
    Vector2D scaled = direction.normalised().scale(clearDistance);
    return new Point2D(agentX + scaled.getX(), agentY + scaled.getY());
  }


  /**
   * A cut point on a circle around the agent, rotated by {@code attempt}
   * steps. Used as the last-resort escape when no meaningful aim exists: each
   * successive attempt points somewhere new, so a wedged agent eventually cuts
   * in a direction that frees it instead of hammering the same bearing.
   */
  public static Point2D sweepPoint(double agentX, double agentY,
      int clearDistance, int attempt) {
    double angle = (attempt * SWEEP_STEP) % (2 * Math.PI);
    return new Point2D(agentX + clearDistance * Math.cos(angle),
        agentY + clearDistance * Math.sin(angle));
  }


  /** True when two points are within {@code tolerance} mm on both axes. */
  public static boolean samePoint(double aX, double aY, double bX, double bY,
      double tolerance) {
    return Math.abs(aX - bX) < tolerance && Math.abs(aY - bY) < tolerance;
  }


  /**
   * Index into {@code apexes} of the x-coordinate of the vertex nearest to
   * {@code (x,y)}, or {@code -1} if the polygon is unusable.
   */
  public static int nearestApexIndex(int[] apexes, double x, double y) {
    if (!isUsablePolygon(apexes)) {
      return -1;
    }
    int best = -1;
    double bestDistance = Double.MAX_VALUE;
    for (int i = 0; i + 1 < apexes.length; i += 2) {
      double distance = Math.hypot(apexes[i] - x, apexes[i + 1] - y);
      if (distance < bestDistance) {
        bestDistance = distance;
        best = i;
      }
    }
    return best;
  }
}
