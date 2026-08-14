# GeometryTools2D.java

`rescuecore2.misc.geometry.GeometryTools2D`

```
class GeometryTools2D   // static helpers, never instantiated
```

**GeometryTools2D** is the **geometry toolbox** of the simulator. Police tactics use it to decide whether a blockade actually sits between the agent and its destination; fire and ambulance tactics use it for distance and centroid computations.

```java
Point2D hit = GeometryTools2D.getSegmentIntersectionPoint(pathLine, blockadeEdge);
if (hit != null) { /* the rubble really is in the way */ }
```

- **Intersections**
  - [getIntersectionPoint()](#intersections)
  - [getSegmentIntersectionPoint()](#intersections)
  - [intersects() / overlaps() / parallel() / collinear() / contains()](#tests)

- **Distances and closest points**
  - [getDistance()](#getdistance)
  - [getClosestPoint() / getClosestPointOnSegment()](#closest)

- **Polygons**
  - [computeArea() / computeSignedArea() / computeCentroid()](#polygons)
  - [isPointInsidePolygon()](#ispointinsidepolygon)
  - [vertexArrayToPoints() / pointsToLines()](#conversion)

- **Angles**
  - [getAngleBetweenVectors() / getRightAngleBetweenVectors() / getLeftAngleBetweenVectors()](#angles)
  - [getRightAngleBetweenLines() / getLeftAngleBetweenLines()](#angles)
  - [isRightTurn()](#angles)

## <a id="intersections"></a>getIntersectionPoint() / getSegmentIntersectionPoint()

```java
static Point2D getIntersectionPoint(Line2D l1, Line2D l2);
static Point2D getSegmentIntersectionPoint(Line2D l1, Line2D l2);
```
Get where two lines meet. The `Segment` version only counts crossings that lie **inside both segments** — that is the one you want for "does this wall block my path".

**Parameters :**
- `l1`, `l2` : The two lines.

**Returns :**
- `Point2D` : The crossing point, or `null` when there is none.

---

## <a id="tests"></a>intersects() / overlaps() / parallel() / collinear() / contains()

```java
static boolean intersects(Line2D segment1, Line2D segment2);
static boolean overlaps(Line2D segment1, Line2D segment2);
static boolean parallel(Line2D l1, Line2D l2);
static boolean collinear(Line2D line1, Line2D line2);
static boolean contains(Line2D line, Point2D point);
```
Boolean geometric tests between lines and points.

**Parameters :**
- The lines / point to test.

**Returns :**
- `boolean` : Result of the test.

---

## <a id="getdistance"></a>getDistance()

```java
static double getDistance(Point2D p1, Point2D p2);
static double getDistance(Line2D segment, Point2D point);
static double getDistance(Line2D segment1, Line2D segment2);
```
Distance between points, a point and a segment, or two segments. Use the point-to-segment version to check whether the agent is close enough to clear a blockade edge.

**Parameters :**
- The points / segments to measure.

**Returns :**
- `double` : The distance in map units.

---

## <a id="closest"></a>getClosestPoint() / getClosestPointOnSegment()

```java
static Point2D getClosestPoint(Line2D line, Point2D point);
static Point2D getClosestPointOnSegment(Line2D line, Point2D point);
```
Get the point on a line (or inside a segment) nearest to a given point — the natural "stand here to work on that edge" position.

**Parameters :**
- `line` : The line or segment.
- `point` : The reference point.

**Returns :**
- `Point2D` : The closest point.

---

## <a id="polygons"></a>computeArea() / computeSignedArea() / computeCentroid()

```java
static double  computeArea(List<Point2D> vertices);
static double  computeSignedArea(List<Point2D> vertices);
static Point2D computeCentroid(List<Point2D> vertices);
```
Area and centre of a polygon — apply them to `Blockade.getApexes()` or `Area.getApexList()` to get the size and centre of rubble.

**Parameters :**
- `vertices` : The polygon vertices in order.

**Returns :**
- `double` / `Point2D` : Area, signed area, or centroid.

---

## <a id="ispointinsidepolygon"></a>isPointInsidePolygon()

```java
static boolean isPointInsidePolygon(Point2D p, List<Point2D> vertices);
```
Check whether a point lies inside a polygon — e.g. is the agent standing on the rubble.

**Parameters :**
- `p` : The point.
- `vertices` : The polygon vertices.

**Returns :**
- `boolean` : `true` when inside.

---

## <a id="conversion"></a>vertexArrayToPoints() / pointsToLines()

```java
static List<Point2D> vertexArrayToPoints(int[] vertices);
static List<Point2D> vertexArrayToPoints(double[] vertices);
static List<Line2D>  pointsToLines(List<Point2D> points);
static List<Line2D>  pointsToLines(List<Point2D> points, boolean close);
```
Convert the flat apex arrays returned by `Area`/`Blockade` into points, and points into segments.

**Parameters :**
- `vertices` : Flat `x0, y0, x1, y1, ...` array.
- `points` : Ordered points.
- `close` : Whether to add the closing segment.

**Returns :**
- `List<Point2D>` / `List<Line2D>` : The converted geometry.

---

## <a id="angles"></a>Angle helpers

```java
static double  getAngleBetweenVectors(Vector2D first, Vector2D second);
static double  getRightAngleBetweenVectors(Vector2D first, Vector2D second);
static double  getLeftAngleBetweenVectors(Vector2D first, Vector2D second);
static double  getRightAngleBetweenLines(Line2D first, Line2D second);
static double  getLeftAngleBetweenLines(Line2D first, Line2D second);
static boolean isRightTurn(Vector2D first, Vector2D second);
static boolean isRightTurn(Line2D first, Line2D second);
```
Angle measurements between vectors and lines, in degrees.

**Parameters :**
- The vectors / lines to compare.

**Returns :**
- `double` : The angle, or `boolean` for `isRightTurn`.

---
