# RoadDetector.java

```java
RoadDetector(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

**RoadDetector** is an abstract **target detection module for Road entities** (such as blocked roads requiring clearance), answering questions like :

- How to **calculate road detection results** ?
  - [calc()](#calc)

- How to manage **module lifecycle and updates** ?
  - [precompute()](#precompute)
  - [resume()](#resume)
  - [preparate()](#preparate)
  - [updateInfo()](#updateinfo)

## <a id="attributes-stored"></a>Attributes Stored
1. Inherited from `TargetDetector<Road>` / `AbstractModule`:
   - `protected ScenarioInfo scenarioInfo` : Reference to scenario configuration information.
   - `protected AgentInfo agentInfo` : Reference to agent self-awareness state.
   - `protected WorldInfo worldInfo` : Reference to world model representation.
   - `protected ModuleManager moduleManager` : Pointer to module manager.
   - `protected DevelopData developData` : Reference to development data configurations.

---

## <a id="calc"></a>calc()

```java
abstract RoadDetector calc();
```
Calculates and detects target `Road` entities.

**Parameters :**
- None

**Returns :**
- `RoadDetector` : This module instance for method chaining.

---

## <a id="precompute"></a>precompute()

```java
RoadDetector precompute(PrecomputeData precomputeData);
```
Executes precomputation tasks for road detection before simulation starts.

**Parameters :**
- `precomputeData` : Precomputation data storage container.

**Returns :**
- `RoadDetector` : This module instance for method chaining.

---

## <a id="resume"></a>resume()

```java
RoadDetector resume(PrecomputeData precomputeData);
```
Resumes road detection state using precomputed data upon startup.

**Parameters :**
- `precomputeData` : Loaded precomputation data container.

**Returns :**
- `RoadDetector` : This module instance for method chaining.

---

## <a id="preparate"></a>preparate()

```java
RoadDetector preparate();
```
Executes preparation setup tasks when precomputed data is unavailable.

**Parameters :**
- None

**Returns :**
- `RoadDetector` : This module instance for method chaining.

---

## <a id="updateinfo"></a>updateInfo()

```java
RoadDetector updateInfo(MessageManager messageManager);
```
Updates road detector state and processes incoming messages at the beginning of each simulation tick.

**Parameters :**
- `messageManager` : Communication message manager instance.

**Returns :**
- `RoadDetector` : This module instance for method chaining.

---
