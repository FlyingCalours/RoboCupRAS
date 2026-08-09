# AmbulanceTargetAllocator.java

```java
AmbulanceTargetAllocator(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

**AmbulanceTargetAllocator** is an abstract **target allocation module for Ambulance Teams** that assigns rescue/transport targets (e.g. buried or injured civilians/agents) to specific ambulance agent entities, answering questions like :

- How to **calculate and retrieve target allocations** ?
  - [calc()](#calc)
  - [getResult()](#getresult)

- How to manage **module lifecycle and updates** ?
  - [resume()](#resume)
  - [preparate()](#preparate)
  - [updateInfo()](#updateinfo)

## <a id="attributes-stored"></a>Attributes Stored
1. Inherited from `TargetAllocator` / `AbstractModule`:
   - `protected ScenarioInfo scenarioInfo` : Reference to scenario configuration parameters.
   - `protected AgentInfo agentInfo` : Reference to agent self-awareness state.
   - `protected WorldInfo worldInfo` : Reference to world model representation.
   - `protected ModuleManager moduleManager` : Pointer to module manager.
   - `protected DevelopData developData` : Reference to development data settings.

---

## <a id="calc"></a>calc()

```java
abstract AmbulanceTargetAllocator calc();
```
Calculates the target allocations mapping ambulance agents to target entities.

**Parameters :**
- None

**Returns :**
- `AmbulanceTargetAllocator` : This module instance for method chaining.

---

## <a id="getresult"></a>getResult()

```java
abstract Map<EntityID, EntityID> getResult();
```
Retrieves the target allocation mapping where keys represent agent IDs (e.g., Ambulance Teams) and values represent allocated target IDs.

**Parameters :**
- None

**Returns :**
- `Map<EntityID, EntityID>` : Map of allocated targets (`Agent ID -> Target ID`).

---

## <a id="resume"></a>resume()

```java
AmbulanceTargetAllocator resume(PrecomputeData precomputeData);
```
Resumes target allocator state using precomputed data upon agent startup.

**Parameters :**
- `precomputeData` : Loaded precomputation data container.

**Returns :**
- `AmbulanceTargetAllocator` : This module instance for method chaining.

---

## <a id="preparate"></a>preparate()

```java
AmbulanceTargetAllocator preparate();
```
Executes preparation setup tasks when precomputed allocation data is unavailable.

**Parameters :**
- None

**Returns :**
- `AmbulanceTargetAllocator` : This module instance for method chaining.

---

## <a id="updateinfo"></a>updateInfo()

```java
AmbulanceTargetAllocator updateInfo(MessageManager messageManager);
```
Updates internal allocation module structures and processes incoming communication at the start of each simulation tick.

**Parameters :**
- `messageManager` : Communication message manager instance.

**Returns :**
- `AmbulanceTargetAllocator` : This module instance for method chaining.

---
