# Area.java

`rescuecore2.standard.entities.Area`

```
abstract class Area extends StandardEntity
```

**Area** is anything an agent can **stand in** — `Road` and `Building` both extend it. It is the unit of movement: a path from `PathPlanning` is a list of `Area` IDs, and `AgentInfo.getPositionArea()` returns one.

The shape of an area is described by its **edges**; an edge with a neighbour is a doorway/junction you can walk through, an edge without one is a wall.

- Where is it ?
  - [getX()](#getx)
  - [getY()](#gety)

- What is it **connected** to ?
  - [getNeighbours()](#getneighbours)
  - [getEdges()](#getedges)
  - [getEdgeTo()](#getedgeto)
  - [getEdgesTo()](#getedgesto)

- Is it **blocked** ?
  - [getBlockades()](#getblockades)
  - [isBlockadesDefined()](#isblockadesdefined)

- **Geometry**
  - [getApexList()](#getapexlist)
  - [getShape()](#getshape)

## <a id="getx"></a>getX()

```java
int getX();
```
Get the X coordinate of the area's centre.

**Parameters :**
- None

**Returns :**
- `int` : X coordinate.

---

## <a id="gety"></a>getY()

```java
int getY();
```
Get the Y coordinate of the area's centre.

**Parameters :**
- None

**Returns :**
- `int` : Y coordinate.

---

## <a id="getneighbours"></a>getNeighbours()

```java
List<EntityID> getNeighbours();
```
Get every area directly reachable from this one. This is the adjacency list your own BFS/DFS or A* search walks over.

**Parameters :**
- None

**Returns :**
- `List<EntityID>` : IDs of neighbouring areas.

---

## <a id="getedges"></a>getEdges()

```java
List<Edge> getEdges();
void setEdges(List<Edge> edges);
boolean isEdgesDefined();
```
Get the boundary segments of this area. Each `Edge` has a start point, an end point and an optional neighbour.

**Parameters :**
- `edges` : (setter) The new edge list.

**Returns :**
- `List<Edge>` : The edges of the area.

---

## <a id="getedgeto"></a>getEdgeTo()

```java
Edge getEdgeTo(EntityID neighbour);
```
Get the edge leading to a given neighbour — this gives you the exact doorway coordinates to walk to or clear towards.

**Parameters :**
- `neighbour` : ID of the neighbouring area.

**Returns :**
- `Edge` : The connecting edge, or `null`.

---

## <a id="getedgesto"></a>getEdgesTo()

```java
Set<Edge> getEdgesTo(EntityID neighbour);
```
Get all edges shared with a given neighbour (some areas touch along more than one segment).

**Parameters :**
- `neighbour` : ID of the neighbouring area.

**Returns :**
- `Set<Edge>` : The connecting edges.

---

## <a id="getblockades"></a>getBlockades()

```java
List<EntityID> getBlockades();
void setBlockades(List<EntityID> blockades);
```
Get the blockades currently sitting in this area. An empty list means the area is clear.

**Parameters :**
- `blockades` : (setter) The new blockade list.

**Returns :**
- `List<EntityID>` : Blockade IDs.

---

## <a id="isblockadesdefined"></a>isBlockadesDefined()

```java
boolean isBlockadesDefined();
```
Check whether blockade information is known for this area. `false` means "never seen", which is different from "no blockades" — a police force should treat unknown areas as suspicious.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` if the property is defined.

---

## <a id="getapexlist"></a>getApexList()

```java
int[] getApexList();
```
Get the polygon vertices of this area as a flat `x0, y0, x1, y1, ...` array.

**Parameters :**
- None

**Returns :**
- `int[]` : Vertex coordinates.

---

## <a id="getshape"></a>getShape()

```java
Shape getShape();
```
Get the area outline as a Java 2D `Shape`, useful for geometric tests and debug drawing.

**Parameters :**
- None

**Returns :**
- `Shape` : The polygon.

---
