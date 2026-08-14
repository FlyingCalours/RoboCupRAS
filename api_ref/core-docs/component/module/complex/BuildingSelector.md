# BuildingSelector.java

```java
BuildingSelector(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

> **Deprecated**: This class is deprecated in favor of **[BuildingDetector](BuildingDetector.md)**.

**BuildingSelector** is a deprecated abstract alias class extending **[BuildingDetector](BuildingDetector.md)** for building target selection and detection.

- Inherits all detection methods and lifecycle handlers from **[BuildingDetector.java](BuildingDetector.md)**.

## <a id="attributes-stored"></a>Attributes Stored
1. Inherited from `BuildingDetector` / `TargetDetector<Building>` / `AbstractModule`:
   - `protected ScenarioInfo scenarioInfo` : Reference to scenario configuration information.
   - `protected AgentInfo agentInfo` : Reference to agent self-awareness state.
   - `protected WorldInfo worldInfo` : Reference to world model representation.
   - `protected ModuleManager moduleManager` : Pointer to module manager.
   - `protected DevelopData developData` : Reference to development data parameters.

---

## <a id="calc"></a>calc()

```java
abstract BuildingSelector calc();
```
Calculates target building selection.

**Parameters :**
- None

**Returns :**
- `BuildingSelector` : This module instance for method chaining.

---

## <a id="precompute"></a>precompute()

```java
BuildingSelector precompute(PrecomputeData precomputeData);
```
Executes precomputation tasks.

**Parameters :**
- `precomputeData` : Precomputation data container.

**Returns :**
- `BuildingSelector` : This instance.

---

## <a id="resume"></a>resume()

```java
BuildingSelector resume(PrecomputeData precomputeData);
```
Resumes module state using precomputed data.

**Parameters :**
- `precomputeData` : Precomputation data container.

**Returns :**
- `BuildingSelector` : This instance.

---

## <a id="preparate"></a>preparate()

```java
BuildingSelector preparate();
```
Executes preparation setup tasks.

**Parameters :**
- None

**Returns :**
- `BuildingSelector` : This instance.

---

## <a id="updateinfo"></a>updateInfo()

```java
BuildingSelector updateInfo(MessageManager messageManager);
```
Updates internal state at the start of each tick.

**Parameters :**
- `messageManager` : Communication message manager instance.

**Returns :**
- `BuildingSelector` : This instance.

---
