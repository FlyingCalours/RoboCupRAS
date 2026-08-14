# PoliceTargetAllocator.java

```java
PoliceTargetAllocator(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

**PoliceTargetAllocator** is an abstract **target allocation module for Police Forces** that assigns clearance targets (such as road blockades) to specific police force entities, answering questions like :

- How to **calculate and retrieve police target allocations** ?
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
abstract PoliceTargetAllocator calc();
```
Calculates target allocations mapping police force agents to target entities or blockades.

**Parameters :**
- None

**Returns :**
- `PoliceTargetAllocator` : This module instance for method chaining.

---

## <a id="getresult"></a>getResult()

```java
abstract Map<EntityID, EntityID> getResult();
```
Retrieves the target allocation mapping where keys represent police agent IDs and values represent allocated target IDs.

**Parameters :**
- None

**Returns :**
- `Map<EntityID, EntityID>` : Map of allocated targets (`Agent ID -> Target ID`).

---

## <a id="resume"></a>resume()

```java
PoliceTargetAllocator resume(PrecomputeData precomputeData);
```
Resumes target allocator state using precomputed data upon agent startup.

**Parameters :**
- `precomputeData` : Loaded precomputation data container.

**Returns :**
- `PoliceTargetAllocator` : This module instance for method chaining.

---

## <a id="preparate"></a>preparate()

```java
PoliceTargetAllocator preparate();
```
Executes preparation setup tasks when precomputed allocation data is unavailable.

**Parameters :**
- None

**Returns :**
- `PoliceTargetAllocator` : This module instance for method chaining.

---

## <a id="updateinfo"></a>updateInfo()

```java
PoliceTargetAllocator updateInfo(MessageManager messageManager);
```
Updates internal allocation module structures and processes incoming messages at the start of each tick.

**Parameters :**
- `messageManager` : Communication message manager instance.

**Returns :**
- `PoliceTargetAllocator` : This module instance for method chaining.

---
