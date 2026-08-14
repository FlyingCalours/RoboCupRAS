# RoadSelector.java

```java
RoadSelector(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

> **Deprecated**: This class is deprecated in favor of **[RoadDetector](RoadDetector.md)**.

**RoadSelector** is a deprecated abstract alias class extending **[RoadDetector](RoadDetector.md)** for road target selection and detection.

- Inherits all detection methods and lifecycle handlers from **[RoadDetector.java](RoadDetector.md)**.

## <a id="attributes-stored"></a>Attributes Stored
1. Inherited from `RoadDetector` / `TargetDetector<Road>` / `AbstractModule`:
   - `protected ScenarioInfo scenarioInfo` : Reference to scenario configuration information.
   - `protected AgentInfo agentInfo` : Reference to agent self-awareness state.
   - `protected WorldInfo worldInfo` : Reference to world model representation.
   - `protected ModuleManager moduleManager` : Pointer to module manager.
   - `protected DevelopData developData` : Reference to development data parameters.

---

## <a id="calc"></a>calc()

```java
abstract RoadSelector calc();
```
Calculates road target selection.

**Parameters :**
- None

**Returns :**
- `RoadSelector` : This module instance for method chaining.

---

## <a id="precompute"></a>precompute()

```java
RoadSelector precompute(PrecomputeData precomputeData);
```
Executes precomputation tasks.

**Parameters :**
- `precomputeData` : Precomputation data container.

**Returns :**
- `RoadSelector` : This instance.

---

## <a id="resume"></a>resume()

```java
RoadSelector resume(PrecomputeData precomputeData);
```
Resumes module state using precomputed data.

**Parameters :**
- `precomputeData` : Precomputation data container.

**Returns :**
- `RoadSelector` : This instance.

---

## <a id="preparate"></a>preparate()

```java
RoadSelector preparate();
```
Executes preparation setup tasks.

**Parameters :**
- None

**Returns :**
- `RoadSelector` : This instance.

---

## <a id="updateinfo"></a>updateInfo()

```java
RoadSelector updateInfo(MessageManager messageManager);
```
Updates internal state at the start of each tick.

**Parameters :**
- `messageManager` : Communication message manager instance.

**Returns :**
- `RoadSelector` : This instance.

---
