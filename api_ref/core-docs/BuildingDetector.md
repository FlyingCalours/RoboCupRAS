# BuildingDetector.java

```java
BuildingDetector(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

**BuildingDetector** is an abstract **target detection module for Building entities** that identifies and prioritizes target buildings (such as burning structures or collapsed buildings requiring attention), answering questions like :

- How to **calculate building detection results** ?
  - [calc()](#calc)

- How to manage **module lifecycle and updates** ?
  - [precompute()](#precompute)
  - [resume()](#resume)
  - [preparate()](#preparate)
  - [updateInfo()](#updateinfo)

## <a id="attributes-stored"></a>Attributes Stored
1. Inherited from `TargetDetector<Building>` / `AbstractModule`:
   - `protected ScenarioInfo scenarioInfo` : Reference to scenario configuration information.
   - `protected AgentInfo agentInfo` : Reference to agent self-awareness state.
   - `protected WorldInfo worldInfo` : Reference to world model representation.
   - `protected ModuleManager moduleManager` : Pointer to module manager.
   - `protected DevelopData developData` : Reference to development data configurations.

---

## <a id="calc"></a>calc()

```java
abstract BuildingDetector calc();
```
Calculates and detects target `Building` entities.

**Parameters :**
- None

**Returns :**
- `BuildingDetector` : This module instance for method chaining.

---

## <a id="precompute"></a>precompute()

```java
BuildingDetector precompute(PrecomputeData precomputeData);
```
Executes precomputation tasks for building detection before simulation starts.

**Parameters :**
- `precomputeData` : Precomputation data storage container.

**Returns :**
- `BuildingDetector` : This module instance for method chaining.

---

## <a id="resume"></a>resume()

```java
BuildingDetector resume(PrecomputeData precomputeData);
```
Resumes building detection state using precomputed data upon startup.

**Parameters :**
- `precomputeData` : Loaded precomputation data container.

**Returns :**
- `BuildingDetector` : This module instance for method chaining.

---

## <a id="preparate"></a>preparate()

```java
BuildingDetector preparate();
```
Executes preparation setup tasks when precomputed data is not available.

**Parameters :**
- None

**Returns :**
- `BuildingDetector` : This module instance for method chaining.

---

## <a id="updateinfo"></a>updateInfo()

```java
BuildingDetector updateInfo(MessageManager messageManager);
```
Updates building detector state and processes incoming messages at the beginning of each simulation tick.

**Parameters :**
- `messageManager` : Communication message manager instance.

**Returns :**
- `BuildingDetector` : This module instance for method chaining.

---
