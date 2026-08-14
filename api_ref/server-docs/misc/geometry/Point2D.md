# Point2D.java

`rescuecore2.misc.geometry.Point2D`

```
Point2D(double x, double y);
```

**Point2D** is an **immutable position** in map coordinates. It is the currency of the geometry helpers: `Edge` endpoints, blockade vertices and clearing targets are all expressed as points.

```java
Point2D agentPos = new Point2D(agentInfo.getX(), agentInfo.getY());
Vector2D toTarget = targetPoint.minus(agentPos);
```

- **Methods**
  - [getX() / getY()](#coords)
  - [translate()](#translate)
  - [minus()](#minus)
  - [plus()](#plus)
  - [toVector()](#tovector)

## <a id="coords"></a>getX() / getY()

```java
double getX();
double getY();
```
Get the coordinates of the point.

**Parameters :**
- None

**Returns :**
- `double` : The coordinate.

---

## <a id="translate"></a>translate()

```java
Point2D translate(double dx, double dy);
```
Get a new point moved by an offset.

**Parameters :**
- `dx`, `dy` : The offset.

**Returns :**
- `Point2D` : The moved point.

---

## <a id="minus"></a>minus()

```java
Vector2D minus(Point2D p);
```
Get the vector from `p` to this point — the usual way to build a direction to clear or move towards.

**Parameters :**
- `p` : The other point.

**Returns :**
- `Vector2D` : The difference vector.

---

## <a id="plus"></a>plus()

```java
Point2D plus(Vector2D v);
```
Get the point reached by following a vector from this point.

**Parameters :**
- `v` : The vector to add.

**Returns :**
- `Point2D` : The resulting point.

---

## <a id="tovector"></a>toVector()

```java
Vector2D toVector();
```
Interpret this point as a vector from the origin.

**Parameters :**
- None

**Returns :**
- `Vector2D` : The vector.

---
