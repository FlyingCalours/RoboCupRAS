# Edge.java

`rescuecore2.standard.entities.Edge`

```
Edge(int startX, int startY, int endX, int endY);
Edge(Point2D start, Point2D end);
Edge(int startX, int startY, int endX, int endY, EntityID neighbour);
Edge(Point2D start, Point2D end, EntityID neighbour);
```

**Edge** is one **boundary segment of an `Area`**. An edge with a neighbour is a passage between two areas; an edge without one is a wall. Police tactics use the passable edge between two areas as the exact point to clear towards, and movement tactics use it as the point to aim for when entering a building.

```java
Edge edge = currentArea.getEdgeTo(nextAreaID);
if (edge != null && edge.isPassable()) {
    int midX = (edge.getStartX() + edge.getEndX()) / 2;
    int midY = (edge.getStartY() + edge.getEndY()) / 2;
}
```

- Where does it **run** ?
  - [getStartX() / getStartY() / getEndX() / getEndY()](#coords)
  - [getStart() / getEnd()](#points)
  - [getLine()](#getline)

- Can I **walk through** ?
  - [getNeighbour()](#getneighbour)
  - [isPassable()](#ispassable)

## <a id="coords"></a>getStartX() / getStartY() / getEndX() / getEndY()

```java
int getStartX();
int getStartY();
int getEndX();
int getEndY();
```
Get the endpoints of the edge as raw coordinates.

**Parameters :**
- None

**Returns :**
- `int` : The coordinate.

---

## <a id="points"></a>getStart() / getEnd()

```java
Point2D getStart();
Point2D getEnd();
```
Get the endpoints as geometry objects, ready for `GeometryTools2D`.

**Parameters :**
- None

**Returns :**
- `Point2D` : The endpoint.

---

## <a id="getline"></a>getLine()

```java
Line2D getLine();
```
Get the edge as a `Line2D` segment — use it for intersection tests against blockades.

**Parameters :**
- None

**Returns :**
- `Line2D` : The segment.

---

## <a id="getneighbour"></a>getNeighbour()

```java
EntityID getNeighbour();
```
Get the area on the other side of this edge.

**Parameters :**
- None

**Returns :**
- `EntityID` : The neighbouring area ID, or `null` for a wall.

---

## <a id="ispassable"></a>isPassable()

```java
boolean isPassable();
```
Check whether this edge is a passage (i.e. has a neighbour). Note this is about map topology only — a passable edge can still be physically blocked by rubble.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` when the edge leads somewhere.

---
