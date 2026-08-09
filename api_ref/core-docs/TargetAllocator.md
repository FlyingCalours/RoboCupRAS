# TargetAllocator.java

```java
TargetAllocator(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

**TargetAllocator** is an abstract **multi-agent target allocation module** that assigns target entities across multiple agents, answering questions like :

- How to **calculate and retrieve target allocation mappings** ?
  - [calc()](#calc)
  - [getResult()](#getresult)

- How to manage **module lifecycle and updates** ?
  - [precompute()](#precompute)
  - [resume()](#resume)
  - [preparate()](#preparate)
  - [updateInfo()](#updateinfo)

## <a id="attributes-stored"></a>Attributes Stored
1. Inherited from `AbstractModule`:
   - `protected ScenarioInfo scenarioInfo` : Reference to scenario configuration information.
   - `protected AgentInfo agentInfo` : Reference to agent self-awareness state.
   - `protected WorldInfo worldInfo` : Reference to world model representation.
   - `protected ModuleManager moduleManager` : Pointer to module manager for sub-module access.
   - `protected DevelopData developData` : Reference to development data configurations.
   - Invocation counter fields (`countPrecompute`, `countResume`, `countPreparate`, `countUpdateInfo`).

---

## <a id="calc"></a>calc()

```java
abstract TargetAllocator calc();
```
Calculates target allocations across agents and targets.

**Parameters :**
- None

**Returns :**
- `TargetAllocator` : This `TargetAllocator` instance for method chaining.

---

## <a id="getresult"></a>getResult()

```java
abstract Map<EntityID, EntityID> getResult();
```
Retrieves the target allocation map where keys represent agent entity IDs and values represent allocated target entity IDs.

**Parameters :**
- None

**Returns :**
- `Map<EntityID, EntityID>` : Allocation mapping (`Agent ID -> Target ID`).

---

## <a id="precompute"></a>precompute()

```java
final TargetAllocator precompute(PrecomputeData precomputeData);
```
Executes precomputation tasks for target allocation before simulation starts.

**Parameters :**
- `precomputeData` : Precomputation data storage container.

**Returns :**
- `TargetAllocator` : This `TargetAllocator` instance for method chaining.

---

## <a id="resume"></a>resume()

```java
TargetAllocator resume(PrecomputeData precomputeData);
```
Resumes target allocation module state using precomputed data upon agent startup.

**Parameters :**
- `precomputeData` : Loaded precomputation data container.

**Returns :**
- `TargetAllocator` : This `TargetAllocator` instance for method chaining.

---

## <a id="preparate"></a>preparate()

```java
TargetAllocator preparate();
```
Executes preparation setup tasks when precomputed data is unavailable.

**Parameters :**
- None

**Returns :**
- `TargetAllocator` : This `TargetAllocator` instance for method chaining.

---

## <a id="updateinfo"></a>updateInfo()

```java
TargetAllocator updateInfo(MessageManager messageManager);
```
Updates internal target allocator structures and processes incoming communication at the start of each simulation tick.

**Parameters :**
- `messageManager` : Communication message manager instance.

**Returns :**
- `TargetAllocator` : This `TargetAllocator` instance for method chaining.

---
