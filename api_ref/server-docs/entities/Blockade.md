# Blockade.java

`rescuecore2.standard.entities.Blockade`

```
Blockade(EntityID id);
Blockade(Blockade other);
```

**Blockade** is the rubble that **stops agents moving along a road**. It is the police force's only real target: find it, get within `ScenarioInfo.getClearRepairDistance()`, and issue `ActionClear` until it disappears.

```java
for (EntityID blockadeID : road.getBlockades()) {
    Blockade blockade = (Blockade) worldInfo.getEntity(blockadeID);
    if (blockade.isRepairCostDefined()) { ... }
}
```

- Where is it ?
  - [getX()](#getx)
  - [getY()](#gety)
  - [getPosition()](#getposition)

- How much **work** does it need ?
  - [getRepairCost()](#getrepaircost)

- What **shape** is it ?
  - [getApexes()](#getapexes)
  - [getShape()](#getshape)

Every getter has a matching `setXxx()` and `isXxxDefined()`.

## <a id="getx"></a>getX()

```java
int getX();
```
Get the X coordinate of the blockade centre — pass it to `new ActionClear(x, y)`.

**Parameters :**
- None

**Returns :**
- `int` : X coordinate.

---

## <a id="gety"></a>getY()

```java
int getY();
```
Get the Y coordinate of the blockade centre.

**Parameters :**
- None

**Returns :**
- `int` : Y coordinate.

---

## <a id="getposition"></a>getPosition()

```java
EntityID getPosition();
```
Get the road this blockade sits on.

**Parameters :**
- None

**Returns :**
- `EntityID` : The containing area ID.

---

## <a id="getrepaircost"></a>getRepairCost()

```java
int getRepairCost();
```
Get the remaining clearing work. It drops each tick a police force clears it; when it reaches zero the blockade entity is removed. Comparing repair cost to `ScenarioInfo.getClearRepairRate()` tells you how many ticks the job will take.

**Parameters :**
- None

**Returns :**
- `int` : Repair cost.

---

## <a id="getapexes"></a>getApexes()

```java
int[] getApexes();
```
Get the polygon vertices of the blockade as `x0, y0, x1, y1, ...`.

**Parameters :**
- None

**Returns :**
- `int[]` : Vertex coordinates.

---

## <a id="getshape"></a>getShape()

```java
Shape getShape();
```
Get the blockade outline as a Java 2D `Shape` — use it to test whether a straight path crosses the rubble.

**Parameters :**
- None

**Returns :**
- `Shape` : The polygon.

---
