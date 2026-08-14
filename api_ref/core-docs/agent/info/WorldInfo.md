# WorldInfo.java

```java
WorldInfo(
    @Nonnull StandardWorldModel world
);
```

[Attributes Stored](#attributes-stored)

**WorldInfo** is a **world model representation** module that provides access to world entities, spatial queries, historical state rollback calculations, and specialized query utilities for burning buildings, buried humans, and road blockades, answering questions like :

- How to **retrieve entities** and their historical state ?
  - [getEntity()](#getentity)
  - [getEntitiesOfType()](#getentitiesoftype)
  - [getEntityIDsOfType()](#getentityidsoftype)
  - [getAllEntities()](#getallentities)

- How to perform **spatial and range queries** ?
  - [getObjectsInRange()](#getobjectsinrange)
  - [getObjectsInRectangle()](#getobjectsinrectangle)
  - [getObjectIDsInRange()](#getobjectidsinrange)
  - [getObjectIDsInRectangle()](#getobjectidsinrectangle)

- How to query **burning buildings, buried humans, and blockades** ?
  - [getFireBuildings()](#getfirebuildings)
  - [getFireBuildingIDs()](#getfirebuildingids)
  - [getNumberOfBuried()](#getnumberofburied)
  - [getBuriedHumans()](#getburiedhumans)
  - [getBlockades()](#getblockades)

- How to find **positions, locations, coordinates, and distances** ?
  - [getPosition()](#getposition)
  - [getLocation()](#getlocation)
  - [getDistance()](#getdistance)
  - [getBounds()](#getbounds)
  - [getWorldBounds()](#getworldbounds)

- How to manage **world state, indexing, updates, and rollback** ?
  - [indexClass()](#indexclass)
  - [index()](#index)
  - [requestRollback()](#requestrollback)
  - [isRequestedRollback()](#isrequestedrollback)
  - [getChanged()](#getchanged)
  - [setChanged()](#setchanged)
  - [setTime()](#settime)
  - [getRawWorld()](#getrawworld)
  - [setWorld()](#setworld)
  - [addEntity()](#addentity)
  - [addEntities()](#addentities)
  - [removeEntity()](#removeentity)
  - [removeAllEntities()](#removeallentities)
  - [merge()](#merge)
  - [registerEntityListener()](#registerentitylistener)
  - [registerWorldListener()](#registerworldlistener)
  - [registerRollbackListener()](#registerrollbacklistener)

## <a id="attributes-stored"></a>Attributes Stored
1. `private StandardWorldModel world` : The underlying RoboCup Rescue standard world model storing map objects and agent entities.
2. `private ChangeSet changed` : The set of entity visual property updates perceived in the current tick.
3. `private int time` : The current simulation timestep.
4. `private Map<EntityID, Map<Integer, Map<Integer, Object>>> rollback` : Multi-level map caching property mutation history for historical entity state rollback calculations (`EntityID -> Time -> PropertyURN -> Value`).
5. `private boolean runRollback` : Flag indicating whether state rollback tracking and entity listeners are enabled.

---

## <a id="indexclass"></a>indexClass()

```java
void indexClass(StandardEntityURN... urns);
```
Indexes entity types in the world model by their URNs for fast type-based lookup.

**Parameters :**
- `urns` : Varargs of `StandardEntityURN` types to index.

**Returns :**
- `void`

---

## <a id="index"></a>index()

```java
void index();
```
Indexes all entity types in the world model for optimized lookups.

**Parameters :**
- None

**Returns :**
- `void`

---

## <a id="requestrollback"></a>requestRollback()

```java
WorldInfo requestRollback();
```
Enables property rollback history tracking for this world model instance.

**Parameters :**
- None

**Returns :**
- `WorldInfo` : This `WorldInfo` object instance for method chaining.

---

## <a id="isrequestedrollback"></a>isRequestedRollback()

```java
boolean isRequestedRollback();
```
Checks if historical state rollback tracking has been requested.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` if rollback is enabled; otherwise `false`.

---

## <a id="getentity"></a>getEntity()

```java
StandardEntity getEntity(@Nonnull EntityID entityID);
StandardEntity getEntity(int targetTime, @Nonnull EntityID entityID);
StandardEntity getEntity(int targetTime, @Nonnull StandardEntity entity);
```
Retrieves an entity from the world model by its `EntityID`, optionally rolled back to a historical timestep.

**Parameters :**
- `entityID` : The unique `EntityID` of the entity.
- `entity` : The current `StandardEntity` object.
- `targetTime` : Absolute tick number (or relative negative offset from current time) for state rollback.

**Returns :**
- `StandardEntity` : The requested entity instance (or rolled back copy), or `null` if not found or non-existent at that target time.

---

## <a id="getentitiesoftype"></a>getEntitiesOfType()

```java
Collection<StandardEntity> getEntitiesOfType(@Nonnull StandardEntityURN urn);
Collection<StandardEntity> getEntitiesOfType(StandardEntityURN... urns);
Collection<StandardEntity> getEntitiesOfType(int targetTime, @Nonnull StandardEntityURN urn);
Collection<StandardEntity> getEntitiesOfType(int targetTime, StandardEntityURN... urns);
```
Gets all entities in the world matching specified URN type(s), optionally evaluated at a target rollback timestep.

**Parameters :**
- `urn` / `urns` : Standard URN type identifier(s) (e.g., `BUILDING`, `CIVILIAN`).
- `targetTime` : Absolute or relative offset timestep for rollback evaluation.

**Returns :**
- `Collection<StandardEntity>` : Collection of matching entities.

---

## <a id="getentityidsoftype"></a>getEntityIDsOfType()

```java
Collection<EntityID> getEntityIDsOfType(@Nonnull StandardEntityURN urn);
Collection<EntityID> getEntityIDsOfType(StandardEntityURN... urns);
Collection<EntityID> getEntityIDsOfType(int targetTime, @Nonnull StandardEntityURN urn);
Collection<EntityID> getEntityIDsOfType(int targetTime, StandardEntityURN... urns);
```
Gets the IDs of all entities matching specified URN type(s), optionally evaluated at a target rollback timestep.

**Parameters :**
- `urn` / `urns` : Standard URN type identifier(s).
- `targetTime` : Absolute or relative offset timestep for rollback evaluation.

**Returns :**
- `Collection<EntityID>` : Collection of entity IDs matching the query.

---

## <a id="getallentities"></a>getAllEntities()

```java
Collection<StandardEntity> getAllEntities();
Collection<StandardEntity> getAllEntities(int targetTime);
```
Retrieves all entities present in the world model, optionally rolled back to a target timestep state.

**Parameters :**
- `targetTime` : Optional absolute or relative offset timestep for rollback evaluation.

**Returns :**
- `Collection<StandardEntity>` : Collection of all standard entities in the world.

---

## <a id="getobjectsinrange"></a>getObjectsInRange()

```java
Collection<StandardEntity> getObjectsInRange(@Nonnull EntityID entityID, @Nonnegative int range);
Collection<StandardEntity> getObjectsInRange(@Nonnull StandardEntity entity, @Nonnegative int range);
Collection<StandardEntity> getObjectsInRange(int x, int y, @Nonnegative int range);
Collection<StandardEntity> getObjectsInRange(int targetTime, @Nonnull EntityID entityID, @Nonnegative int range);
Collection<StandardEntity> getObjectsInRange(int targetTime, @Nonnull EntityID entityID, @Nonnegative int range, boolean ignoreHuman);
Collection<StandardEntity> getObjectsInRange(int targetTime, @Nonnull StandardEntity entity, @Nonnegative int range);
Collection<StandardEntity> getObjectsInRange(int targetTime, @Nonnull StandardEntity entity, @Nonnegative int range, boolean ignoreHuman);
Collection<StandardEntity> getObjectsInRange(int targetTime, int x, int y, @Nonnegative int range);
Collection<StandardEntity> getObjectsInRange(int targetTime, int x, int y, @Nonnegative int range, boolean ignoreHuman);
```
Finds all world entities within a given radius distance in millimeters of an entity or coordinate point, with optional rollback time and human-exclusion filtering.

**Parameters :**
- `entityID` / `entity` : The reference origin entity.
- `x`, `y` : Map coordinates in millimeters.
- `range` : Radial distance limit in millimeters.
- `targetTime` : Target rollback timestep.
- `ignoreHuman` : If `true`, skips evaluating human positions.

**Returns :**
- `Collection<StandardEntity>` : Entities residing within the specified radius.

---

## <a id="getobjectsinrectangle"></a>getObjectsInRectangle()

```java
Collection<StandardEntity> getObjectsInRectangle(int x1, int y1, int x2, int y2);
Collection<StandardEntity> getObjectsInRectangle(int targetTime, int x1, int y1, int x2, int y2);
Collection<StandardEntity> getObjectsInRectangle(int targetTime, int x1, int y1, int x2, int y2, boolean ignoreHuman);
```
Finds all entities located within a rectangular bounding box region, with optional rollback time and human-exclusion filtering.

**Parameters :**
- `x1`, `y1` : Top-left corner coordinates in millimeters.
- `x2`, `y2` : Bottom-right corner coordinates in millimeters.
- `targetTime` : Target rollback timestep.
- `ignoreHuman` : If `true`, ignores dynamic human positions.

**Returns :**
- `Collection<StandardEntity>` : Entities falling inside the specified bounding box.

---

## <a id="getobjectidsinrange"></a>getObjectIDsInRange()

```java
Collection<EntityID> getObjectIDsInRange(@Nonnull EntityID entity, int range);
Collection<EntityID> getObjectIDsInRange(@Nonnull StandardEntity entity, int range);
Collection<EntityID> getObjectIDsInRange(int x, int y, int range);
Collection<EntityID> getObjectIDsInRange(int targetTime, @Nonnull EntityID entity, int range);
Collection<EntityID> getObjectIDsInRange(int targetTime, @Nonnull EntityID entity, int range, boolean ignoreHuman);
Collection<EntityID> getObjectIDsInRange(int targetTime, @Nonnull StandardEntity entity, int range);
Collection<EntityID> getObjectIDsInRange(int targetTime, @Nonnull StandardEntity entity, int range, boolean ignoreHuman);
Collection<EntityID> getObjectIDsInRange(int targetTime, int x, int y, int range);
Collection<EntityID> getObjectIDsInRange(int targetTime, int x, int y, int range, boolean ignoreHuman);
```
Gets the IDs of entities located within a given radius distance, supporting optional rollback evaluation and human filtering.

**Parameters :**
- `entity` / `entityID` : Origin entity.
- `x`, `y` : Map coordinates in millimeters.
- `range` : Distance threshold in millimeters.
- `targetTime` : Target rollback timestep.
- `ignoreHuman` : Whether to skip checking human entity locations.

**Returns :**
- `Collection<EntityID>` : Collection of matching entity IDs.

---

## <a id="getobjectidsinrectangle"></a>getObjectIDsInRectangle()

```java
Collection<EntityID> getObjectIDsInRectangle(int x1, int y1, int x2, int y2);
Collection<EntityID> getObjectIDsInRectangle(int targetTime, int x1, int y1, int x2, int y2);
Collection<EntityID> getObjectIDsInRectangle(int targetTime, int x1, int y1, int x2, int y2, boolean ignoreHuman);
```
Gets the IDs of entities within a rectangular region, with optional rollback evaluation and human filtering.

**Parameters :**
- `x1`, `y1` : Bounding box coordinate 1.
- `x2`, `y2` : Bounding box coordinate 2.
- `targetTime` : Target rollback timestep.
- `ignoreHuman` : Whether to ignore human entity positions.

**Returns :**
- `Collection<EntityID>` : Collection of entity IDs inside the rectangle.

---

## <a id="getfirebuildings"></a>getFireBuildings()

```java
Collection<Building> getFireBuildings();
```
Retrieves all buildings currently on fire in the world model (includes ordinary buildings, gas stations, and offices/centres).

**Parameters :**
- None

**Returns :**
- `Collection<Building>` : Collection of burning `Building` objects.

---

## <a id="getfirebuildingids"></a>getFireBuildingIDs()

```java
Collection<EntityID> getFireBuildingIDs();
```
Retrieves the entity IDs of all currently burning buildings.

**Parameters :**
- None

**Returns :**
- `Collection<EntityID>` : Entity IDs of burning buildings.

---

## <a id="getnumberofburied"></a>getNumberOfBuried()

```java
int getNumberOfBuried(@Nonnull Building building);
int getNumberOfBuried(@Nonnull EntityID entityID);
```
Calculates the count of human agents (civilians or platoon agents) buried under debris inside a specified building entity.

**Parameters :**
- `building` / `entityID` : The target building or building ID.

**Returns :**
- `int` : Count of trapped humans with buriedness greater than 0.

---

## <a id="getburiedhumans"></a>getBuriedHumans()

```java
Collection<Human> getBuriedHumans(@Nonnull Building building);
Collection<Human> getBuriedHumans(@Nonnull EntityID entityID);
```
Retrieves all buried human entities trapped inside a given building.

**Parameters :**
- `building` / `entityID` : The target building object or building ID.

**Returns :**
- `Collection<Human>` : Trapped human agents in the building.

---

## <a id="getblockades"></a>getBlockades()

```java
Collection<Blockade> getBlockades(@Nonnull EntityID entityID);
Collection<Blockade> getBlockades(@Nonnull Area area);
```
Retrieves all active `Blockade` objects located inside a given map area or area ID.

**Parameters :**
- `entityID` / `area` : Target area entity or area ID.

**Returns :**
- `Collection<Blockade>` : Set of blockade objects within the area.

---

## <a id="getposition"></a>getPosition()

```java
StandardEntity getPosition(@Nonnull Human human);
StandardEntity getPosition(@Nonnull Blockade blockade);
StandardEntity getPosition(@Nonnull EntityID entityID);
StandardEntity getPosition(int targetTime, @Nonnull Human human);
StandardEntity getPosition(int targetTime, @Nonnull Blockade blockade);
StandardEntity getPosition(int targetTime, @Nonnull EntityID entityID);
```
Retrieves the entity position (Road/Building/Area) where a human or blockade is located, with optional historical rollback.

**Parameters :**
- `human` / `blockade` / `entityID` : Target entity.
- `targetTime` : Target rollback timestep.

**Returns :**
- `StandardEntity` : The position entity, or `null`.

---

## <a id="getlocation"></a>getLocation()

```java
Pair<Integer, Integer> getLocation(@Nonnull StandardEntity entity);
Pair<Integer, Integer> getLocation(@Nonnull EntityID entityID);
Pair<Integer, Integer> getLocation(int targetTime, @Nonnull StandardEntity entity);
Pair<Integer, Integer> getLocation(int targetTime, @Nonnull EntityID entityID);
```
Gets the absolute `(X, Y)` spatial coordinates in millimeters for an entity, with optional historical rollback evaluation.

**Parameters :**
- `entity` / `entityID` : Target entity.
- `targetTime` : Target rollback timestep.

**Returns :**
- `Pair<Integer, Integer>` : Pair representing `(x, y)` position coordinates, or `null`.

---

## <a id="getdistance"></a>getDistance()

```java
int getDistance(@Nonnull EntityID first, @Nonnull EntityID second);
int getDistance(@Nonnull StandardEntity first, @Nonnull StandardEntity second);
int getDistance(int targetTime, @Nonnull EntityID first, @Nonnull EntityID second);
int getDistance(int targetTime, @Nonnull StandardEntity first, @Nonnull StandardEntity second);
```
Calculates the straight-line distance in millimeters between two entities in the world, with optional rollback calculation.

**Parameters :**
- `first`, `second` : Entities or entity IDs.
- `targetTime` : Target rollback timestep.

**Returns :**
- `int` : Distance value in millimeters.

---

## <a id="getbounds"></a>getBounds()

```java
Rectangle2D getBounds();
```
Get the 2D spatial bounding rectangle containing the entire world map.

**Parameters :**
- None

**Returns :**
- `Rectangle2D` : Map bounding rectangle.

---

## <a id="getworldbounds"></a>getWorldBounds()

```java
Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> getWorldBounds();
```
Get the minimum and maximum `(X, Y)` bounds of the world coordinate system.

**Parameters :**
- None

**Returns :**
- `Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>` : Coordinate bounds `((minX, minY), (maxX, maxY))`.

---

## <a id="addentity"></a>addEntity()

```java
void addEntity(@Nonnull Entity entity);
void addEntity(@Nonnull Entity entity, @Nonnull Class<? extends EntityListener> listener, Class<? extends EntityListener>... otherListeners);
```
Adds a new entity to the world model, optionally attaching custom entity listeners.

**Parameters :**
- `entity` : Entity object to insert into the world.
- `listener` / `otherListeners` : Entity listener classes to instantiate and register.

**Returns :**
- `void`

---

## <a id="addentities"></a>addEntities()

```java
void addEntities(@Nonnull Collection<? extends Entity> entities);
void addEntities(@Nonnull Collection<? extends Entity> entities, @Nonnull Class<? extends EntityListener> listener, Class<? extends EntityListener>... otherListeners);
```
Adds a collection of entities to the world model, optionally attaching listener instances.

**Parameters :**
- `entities` : Collection of entities to add.
- `listener` / `otherListeners` : Optional entity listener classes.

**Returns :**
- `void`

---

## <a id="removeentity"></a>removeEntity()

```java
void removeEntity(@Nonnull StandardEntity e);
void removeEntity(@Nonnull EntityID id);
```
Removes an entity from the world model by instance reference or entity ID.

**Parameters :**
- `e` / `id` : Target entity or ID to remove.

**Returns :**
- `void`

---

## <a id="removeallentities"></a>removeAllEntities()

```java
void removeAllEntities();
```
Removes all entities currently stored in the world model.

**Parameters :**
- None

**Returns :**
- `void`

---

## <a id="merge"></a>merge()

```java
void merge(@Nonnull ChangeSet changeSet);
```
Merges a perceived `ChangeSet` into the world model to update entity properties based on recent perception data.

**Parameters :**
- `changeSet` : The set of property changes received from perception.

**Returns :**
- `void`

---

## <a id="registerentitylistener"></a>registerEntityListener()

```java
void registerEntityListener(@Nonnull Class<? extends EntityListener> listener);
```
Instantiates and registers an entity listener to all entities currently existing in the world model.

**Parameters :**
- `listener` : Listener class implementing `EntityListener`.

**Returns :**
- `void`

---

## <a id="registerworldlistener"></a>registerWorldListener()

```java
void registerWorldListener(@Nonnull Class<? extends WorldModelListener<StandardEntity>> listener);
```
Instantiates and registers a world model listener to observe entity addition and removal events.

**Parameters :**
- `listener` : World listener class implementing `WorldModelListener<StandardEntity>`.

**Returns :**
- `void`

---

## <a id="registerrollbacklistener"></a>registerRollbackListener()

```java
void registerRollbackListener();
```
Registers internal property and world listeners required for state rollback tracking if rollback is requested.

**Parameters :**
- None

**Returns :**
- `void`

---

## <a id="getchanged"></a>getChanged()

```java
ChangeSet getChanged();
```
Get the perception `ChangeSet` object for the current simulation tick.

**Parameters :**
- None

**Returns :**
- `ChangeSet` : The current changeset.

---

## <a id="setchanged"></a>setChanged()

```java
void setChanged(@Nonnull ChangeSet changed);
```
Sets the perception changeset for the current simulation tick.

**Parameters :**
- `changed` : Perception updates `ChangeSet`.

**Returns :**
- `void`

---

## <a id="settime"></a>setTime()

```java
void setTime(int time);
```
Sets the current simulation timestep.

**Parameters :**
- `time` : Current time integer.

**Returns :**
- `void`

---

## <a id="setworld"></a>setWorld()

```java
void setWorld(@Nonnull StandardWorldModel world);
```
Sets or updates the underlying `StandardWorldModel` instance.

**Parameters :**
- `world` : Target world model.

**Returns :**
- `void`

---

## <a id="getrawworld"></a>getRawWorld()

```java
StandardWorldModel getRawWorld();
```
Get the underlying raw `StandardWorldModel` instance.

**Parameters :**
- None

**Returns :**
- `StandardWorldModel` : The raw world model reference.

---