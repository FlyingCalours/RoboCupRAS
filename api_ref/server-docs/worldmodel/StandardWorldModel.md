# StandardWorldModel.java

`rescuecore2.standard.entities.StandardWorldModel`

```
StandardWorldModel();
```

**StandardWorldModel** is the **raw map database** of the simulation — every road, building, blockade and agent the kernel has told you about. In ADF you normally use `WorldInfo`, which wraps this class; reach for the raw model when you need the spatial index (`getObjectsInRange`) or the true world bounds.

- What entities **exist** ?
  - [getEntity()](#getentity)
  - [getAllEntities()](#getallentities)
  - [getEntitiesOfType()](#getentitiesoftype)

- What is **near me** ?
  - [getObjectsInRange()](#getobjectsinrange)
  - [getObjectsInRectangle()](#getobjectsinrectangle)
  - [getDistance()](#getdistance)

- How **big** is the map ?
  - [getBounds()](#getbounds)
  - [getWorldBounds()](#getworldbounds)

- **Maintenance** (framework side)
  - [merge()](#merge)
  - [index()](#index)
  - [indexClass()](#indexclass)

## <a id="getentity"></a>getEntity()

```java
StandardEntity getEntity(EntityID id);
```
Look up one entity by ID. Cast the result to the concrete type you expect (`(Building) world.getEntity(id)`).

**Parameters :**
- `id` : The entity ID.

**Returns :**
- `StandardEntity` : The entity, or `null` if unknown.

---

## <a id="getallentities"></a>getAllEntities()

```java
Collection<StandardEntity> getAllEntities();
```
Get every entity currently in the model.

**Parameters :**
- None

**Returns :**
- `Collection<StandardEntity>` : All known entities.

---

## <a id="getentitiesoftype"></a>getEntitiesOfType()

```java
Collection<StandardEntity> getEntitiesOfType(StandardEntityURN urn);
Collection<StandardEntity> getEntitiesOfType(StandardEntityURN... urns);
```
Get every entity of one or more types — the usual way to list all refuges, all buildings, all civilians. The type must have been indexed (`indexClass`) first; ADF does that at start up.

**Parameters :**
- `urn` / `urns` : One or more `StandardEntityURN` values.

**Returns :**
- `Collection<StandardEntity>` : Matching entities.

---

## <a id="getobjectsinrange"></a>getObjectsInRange()

```java
Collection<StandardEntity> getObjectsInRange(EntityID entity, int range);
Collection<StandardEntity> getObjectsInRange(StandardEntity entity, int range);
Collection<StandardEntity> getObjectsInRange(int x, int y, int range);
```
Get everything within `range` map units of a point or entity. This is a spatial index lookup, far cheaper than scanning all entities — use it for "which buildings are burning near me".

**Parameters :**
- `entity` / `x`,`y` : Centre of the search.
- `range` : Radius in map units (1 metre ≈ 1000 units).

**Returns :**
- `Collection<StandardEntity>` : Entities inside the circle.

---

## <a id="getobjectsinrectangle"></a>getObjectsInRectangle()

```java
Collection<StandardEntity> getObjectsInRectangle(int x1, int y1, int x2, int y2);
```
Get every entity inside an axis aligned rectangle.

**Parameters :**
- `x1`, `y1` : One corner.
- `x2`, `y2` : The opposite corner.

**Returns :**
- `Collection<StandardEntity>` : Entities inside the rectangle.

---

## <a id="getdistance"></a>getDistance()

```java
int getDistance(EntityID first, EntityID second);
int getDistance(StandardEntity first, StandardEntity second);
```
Straight line distance between two entities. Note this is **not** travel distance — a road 10 m away may be unreachable behind a blockade, so use `PathPlanning` for real routing decisions.

**Parameters :**
- `first`, `second` : The two entities (or their IDs).

**Returns :**
- `int` : Euclidean distance in map units.

---

## <a id="getbounds"></a>getBounds()

```java
Rectangle2D getBounds();
```
Get the bounding rectangle of the whole map.

**Parameters :**
- None

**Returns :**
- `Rectangle2D` : Map bounds.

---

## <a id="getworldbounds"></a>getWorldBounds()

```java
Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> getWorldBounds();
```
Get the map bounds as `((minX, minY), (maxX, maxY))`.

**Parameters :**
- None

**Returns :**
- `Pair<Pair<Integer,Integer>, Pair<Integer,Integer>>` : Minimum and maximum coordinates.

---

## <a id="merge"></a>merge()

```java
void merge(ChangeSet changeSet);
void merge(Collection<? extends Entity> toMerge);
```
Apply perceived changes into the model. The framework calls this each tick with what the agent saw.

**Parameters :**
- `changeSet` / `toMerge` : The updates to apply.

**Returns :**
- `void`

---

## <a id="index"></a>index()

```java
void index();
void index(int meshSize);
```
(Re)build the spatial index used by `getObjectsInRange`.

**Parameters :**
- `meshSize` : Optional cell size of the index mesh.

**Returns :**
- `void`

---

## <a id="indexclass"></a>indexClass()

```java
void indexClass(StandardEntityURN... urns);
```
Declare which entity types should be kept in fast lookup lists for `getEntitiesOfType`.

**Parameters :**
- `urns` : The entity types to index.

**Returns :**
- `void`

---
