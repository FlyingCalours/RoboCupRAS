# Clustering.java

```java
Clustering(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

**Clustering** is an abstract **spatial clustering algorithm module** that partitions entities or locations in the world model into cluster groupings, answering questions like :

- How to **query cluster numbers and entity indices** ?
  - [getClusterNumber()](#getclusternumber)
  - [getClusterIndex()](#getclusterindex)

- How to **retrieve entities and IDs within clusters** ?
  - [getClusterEntities()](#getclusterentities)
  - [getClusterEntityIDs()](#getclusterentityids)
  - [getAllClusterEntities()](#getallclusterentities)
  - [getAllClusterEntityIDs()](#getallclusterentityids)

- How to manage **module lifecycle, updates, and calculation** ?
  - [precompute()](#precompute)
  - [resume()](#resume)
  - [preparate()](#preparate)
  - [updateInfo()](#updateinfo)
  - [calc()](#calc)

## <a id="attributes-stored"></a>Attributes Stored
1. Inherited from `AbstractModule`:
   - `protected ScenarioInfo scenarioInfo` : Reference to the scenario configuration information module.
   - `protected AgentInfo agentInfo` : Reference to the self-awareness agent state module.
   - `protected WorldInfo worldInfo` : Reference to the world model representation module.
   - `protected ModuleManager moduleManager` : Pointer to the module manager for sub-module access.
   - `protected DevelopData developData` : Reference to development data configurations.
   - Invocation counter fields (`countPrecompute`, `countResume`, `countPreparate`, `countUpdateInfo`).

---

## <a id="getclusternumber"></a>getClusterNumber()

```java
abstract int getClusterNumber();
```
Get the total number of clusters computed by this clustering module.

**Parameters :**
- None

**Returns :**
- `int` : Total cluster count.

---

## <a id="getclusterindex"></a>getClusterIndex()

```java
abstract int getClusterIndex(StandardEntity entity);
abstract int getClusterIndex(EntityID id);
```
Get the cluster index assigned to a given entity or entity ID.

**Parameters :**
- `entity` : The `StandardEntity` object.
- `id` : The `EntityID` of the target entity.

**Returns :**
- `int` : Zero-based cluster index containing the entity, or `-1` if unassigned/not found.

---

## <a id="getclusterentities"></a>getClusterEntities()

```java
abstract Collection<StandardEntity> getClusterEntities(int index);
```
Retrieves all `StandardEntity` objects belonging to a specific cluster index.

**Parameters :**
- `index` : Zero-based cluster index.

**Returns :**
- `Collection<StandardEntity>` : Collection of entities assigned to the cluster.

---

## <a id="getclusterentityids"></a>getClusterEntityIDs()

```java
abstract Collection<EntityID> getClusterEntityIDs(int index);
```
Retrieves all `EntityID` identifiers belonging to a specific cluster index.

**Parameters :**
- `index` : Zero-based cluster index.

**Returns :**
- `Collection<EntityID>` : Collection of entity IDs assigned to the cluster.

---

## <a id="getallclusterentities"></a>getAllClusterEntities()

```java
List<Collection<StandardEntity>> getAllClusterEntities();
```
Gets a list of all entity clusters, indexed by cluster number.

**Parameters :**
- None

**Returns :**
- `List<Collection<StandardEntity>>` : List containing entity collections for every cluster index.

---

## <a id="getallclusterentityids"></a>getAllClusterEntityIDs()

```java
List<Collection<EntityID>> getAllClusterEntityIDs();
```
Gets a list of all entity ID clusters, indexed by cluster number.

**Parameters :**
- None

**Returns :**
- `List<Collection<EntityID>>` : List containing entity ID collections for every cluster index.

---

## <a id="precompute"></a>precompute()

```java
Clustering precompute(PrecomputeData precomputeData);
```
Executes precomputation for the clustering algorithm before simulation starts.

**Parameters :**
- `precomputeData` : Precomputation data storage container.

**Returns :**
- `Clustering` : This `Clustering` instance for method chaining.

---

## <a id="resume"></a>resume()

```java
Clustering resume(PrecomputeData precomputeData);
```
Resumes clustering state using saved precomputation data upon agent startup.

**Parameters :**
- `precomputeData` : Loaded precomputation data container.

**Returns :**
- `Clustering` : This `Clustering` instance for method chaining.

---

## <a id="preparate"></a>preparate();

```java
Clustering preparate();
```
Executes preparation setup tasks when precomputation data is not used.

**Parameters :**
- None

**Returns :**
- `Clustering` : This `Clustering` instance for method chaining.

---

## <a id="updateinfo"></a>updateInfo()

```java
Clustering updateInfo(MessageManager messageManager);
```
Updates clustering algorithm data and internal state at the start of each timestep.

**Parameters :**
- `messageManager` : Communication message manager instance.

**Returns :**
- `Clustering` : This `Clustering` instance for method chaining.

---

## <a id="calc"></a>calc()

```java
abstract Clustering calc();
```
Executes the clustering algorithm calculation.

**Parameters :**
- None

**Returns :**
- `Clustering` : This `Clustering` instance for method chaining.

---
