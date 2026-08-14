# PathPlanning.java

```java
PathPlanning(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

**PathPlanning** is an abstract **route calculation algorithm module** that finds optimal movement paths between map entities and calculates route distances, answering questions like :

- How to **set source and destination targets** ?
  - [setFrom()](#setfrom)
  - [setDestination()](#setdestination)

- How to **calculate and retrieve path planning results** ?
  - [calc()](#calc)
  - [getResult()](#getresult)
  - [getDistance()](#getdistance)

- How to manage **module lifecycle and updates** ?
  - [precompute()](#precompute)
  - [resume()](#resume)
  - [preparate()](#preparate)
  - [updateInfo()](#updateinfo)

## <a id="attributes-stored"></a>Attributes Stored
1. Inherited from `AbstractModule`:
   - `protected ScenarioInfo scenarioInfo` : Reference to the scenario configuration information module.
   - `protected AgentInfo agentInfo` : Reference to the self-awareness agent state module.
   - `protected WorldInfo worldInfo` : Reference to the world model representation module.
   - `protected ModuleManager moduleManager` : Pointer to the module manager for sub-module access.
   - `protected DevelopData developData` : Reference to development data configurations.
   - Invocation counter fields (`countPrecompute`, `countResume`, `countPreparate`, `countUpdateInfo`).

---

## <a id="setfrom"></a>setFrom()

```java
abstract PathPlanning setFrom(EntityID id);
```
Sets the starting origin entity ID for path calculation.

**Parameters :**
- `id` : The `EntityID` of the starting location.

**Returns :**
- `PathPlanning` : This `PathPlanning` instance for method chaining.

---

## <a id="setdestination"></a>setDestination()

```java
abstract PathPlanning setDestination(Collection<EntityID> targets);
PathPlanning setDestination(EntityID... targets);
```
Sets the destination target entity ID(s) for path calculation.

**Parameters :**
- `targets` : A collection or varargs array of target destination `EntityID` objects.

**Returns :**
- `PathPlanning` : This `PathPlanning` instance for method chaining.

---

## <a id="getresult"></a>getResult()

```java
abstract List<EntityID> getResult();
List<EntityID> getResult(EntityID from, EntityID dest);
```
Retrieves the calculated path as an ordered list of `EntityID` elements from origin to target.

**Parameters :**
- `from` : (Convenience overload) Starting origin `EntityID`.
- `dest` : (Convenience overload) Destination target `EntityID`.

**Returns :**
- `List<EntityID>` : Ordered list of location entity IDs forming the route, or `null`/empty if no valid path exists.

---

## <a id="getdistance"></a>getDistance()

```java
double getDistance();
double getDistance(EntityID from, EntityID dest);
```
Calculates the total physical Euclidean route distance in millimeters across the entity waypoints in the planned path.

**Parameters :**
- `from` : (Convenience overload) Starting origin `EntityID`.
- `dest` : (Convenience overload) Destination target `EntityID`.

**Returns :**
- `double` : Total route distance in millimeters, or `0.0` if path is empty or single node.

---

## <a id="precompute"></a>precompute()

```java
PathPlanning precompute(PrecomputeData precomputeData);
```
Executes precomputation tasks for path planning (e.g., pre-calculating distance matrices) before simulation starts.

**Parameters :**
- `precomputeData` : Precomputation data storage container.

**Returns :**
- `PathPlanning` : This `PathPlanning` instance for method chaining.

---

## <a id="resume"></a>resume()

```java
PathPlanning resume(PrecomputeData precomputeData);
```
Resumes path planning state using precomputed path matrices upon startup.

**Parameters :**
- `precomputeData` : Loaded precomputation data container.

**Returns :**
- `PathPlanning` : This `PathPlanning` instance for method chaining.

---

## <a id="preparate"></a>preparate()

```java
PathPlanning preparate();
```
Executes preparation setup tasks when precomputed path data is unavailable.

**Parameters :**
- None

**Returns :**
- `PathPlanning` : This `PathPlanning` instance for method chaining.

---

## <a id="updateinfo"></a>updateInfo()

```java
PathPlanning updateInfo(MessageManager messageManager);
```
Updates internal path planning algorithm structures at the beginning of each simulation tick.

**Parameters :**
- `messageManager` : Communication message manager instance.

**Returns :**
- `PathPlanning` : This `PathPlanning` instance for method chaining.

---

## <a id="calc"></a>calc()

```java
abstract PathPlanning calc();
```
Calculates the optimal path from origin to destination.

**Parameters :**
- None

**Returns :**
- `PathPlanning` : This `PathPlanning` instance for method chaining.

---
