# HumanSelector.java

```java
HumanSelector(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

> **Deprecated**: This class is deprecated in favor of **[HumanDetector](HumanDetector.md)**.

**HumanSelector** is a deprecated abstract alias class extending **[HumanDetector](HumanDetector.md)** for human target selection and detection.

- Inherits all detection methods and lifecycle handlers from **[HumanDetector.java](HumanDetector.md)**.

## <a id="attributes-stored"></a>Attributes Stored
1. Inherited from `HumanDetector` / `TargetDetector<Human>` / `AbstractModule`:
   - `protected ScenarioInfo scenarioInfo` : Reference to scenario configuration information.
   - `protected AgentInfo agentInfo` : Reference to agent self-awareness state.
   - `protected WorldInfo worldInfo` : Reference to world model representation.
   - `protected ModuleManager moduleManager` : Pointer to module manager.
   - `protected DevelopData developData` : Reference to development data parameters.

---

## <a id="calc"></a>calc()

```java
abstract HumanSelector calc();
```
Calculates human target selection.

**Parameters :**
- None

**Returns :**
- `HumanSelector` : This module instance for method chaining.

---

## <a id="precompute"></a>precompute()

```java
HumanSelector precompute(PrecomputeData precomputeData);
```
Executes precomputation tasks.

**Parameters :**
- `precomputeData` : Precomputation data container.

**Returns :**
- `HumanSelector` : This instance.

---

## <a id="resume"></a>resume()

```java
HumanSelector resume(PrecomputeData precomputeData);
```
Resumes module state using precomputed data.

**Parameters :**
- `precomputeData` : Precomputation data container.

**Returns :**
- `HumanSelector` : This instance.

---

## <a id="preparate"></a>preparate()

```java
HumanSelector preparate();
```
Executes preparation setup tasks.

**Parameters :**
- None

**Returns :**
- `HumanSelector` : This instance.

---

## <a id="updateinfo"></a>updateInfo()

```java
HumanSelector updateInfo(MessageManager messageManager);
```
Updates internal state at the start of each tick.

**Parameters :**
- `messageManager` : Communication message manager instance.

**Returns :**
- `HumanSelector` : This instance.

---
