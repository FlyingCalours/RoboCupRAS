# Line2D.java

`rescuecore2.misc.geometry.Line2D`

```
Line2D(Point2D origin, Vector2D direction);
Line2D(Point2D origin, Point2D end);
Line2D(double x, double y, double dx, double dy);
```

**Line2D** is a **line segment** from an origin along a direction. `Edge.getLine()` returns one, and the standard use in team code is checking whether the straight path to a target crosses a blockade edge.

- Where does it **run** ?
  - [getOrigin() / getEndPoint() / getMidpoint()](#points)
  - [getDirection()](#getdirection)
  - [getLength()](#getlength)
  - [getPoint()](#getpoint)

- Does it **cross** something ?
  - [getIntersection()](#getintersection)
  - [isGeometricallyEquivalent()](#isgeometricallyequivalent)

## <a id="points"></a>getOrigin() / getEndPoint() / getMidpoint()

```java
Point2D getOrigin();
Point2D getEndPoint();
Point2D getMidpoint();
```
Get the start, end or middle of the segment. The midpoint of a passable `Edge` is the usual "walk to the doorway" target.

**Parameters :**
- None

**Returns :**
- `Point2D` : The point.

---

## <a id="getdirection"></a>getDirection()

```java
Vector2D getDirection();
```
Get the direction vector of the line.

**Parameters :**
- None

**Returns :**
- `Vector2D` : The direction.

---

## <a id="getlength"></a>getLength()

```java
double getLength();
```
Get the length of the segment.

**Parameters :**
- None

**Returns :**
- `double` : The length.

---

## <a id="getpoint"></a>getPoint()

```java
Point2D getPoint(double t);
```
Get the point at parameter `t` along the line (`0` = origin, `1` = end).

**Parameters :**
- `t` : Position along the line.

**Returns :**
- `Point2D` : The point.

---

## <a id="getintersection"></a>getIntersection()

```java
double getIntersection(Line2D other);
```
Get the parameter `t` at which this line meets another. Combine with `getPoint(t)` to obtain the crossing point; `GeometryTools2D.getSegmentIntersectionPoint` does both in one call.

**Parameters :**
- `other` : The other line.

**Returns :**
- `double` : The intersection parameter (`NaN` when parallel).

---

## <a id="isgeometricallyequivalent"></a>isGeometricallyEquivalent()

```java
boolean isGeometricallyEquivalent(Line2D other);
```
Check whether two lines describe the same segment.

**Parameters :**
- `other` : The other line.

**Returns :**
- `boolean` : `true` when equivalent.

---
