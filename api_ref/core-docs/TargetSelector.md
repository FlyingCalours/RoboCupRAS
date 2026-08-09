# TargetSelector.java

```java
TargetSelector(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

> **Deprecated**: This class is deprecated in favor of **[TargetDetector](TargetDetector.md)**.

**TargetSelector** is a deprecated generic abstract class extending **[TargetDetector](TargetDetector.md)** for target entity selection and detection.

- Inherits all detection methods and lifecycle handlers from **[TargetDetector.java](TargetDetector.md)**.

## <a id="attributes-stored"></a>Attributes Stored
1. Inherited from `TargetDetector<E>` / `AbstractModule`:
   - `protected ScenarioInfo scenarioInfo` : Reference to scenario configuration information.
   - `protected AgentInfo agentInfo` : Reference to agent self-awareness state.
   - `protected WorldInfo worldInfo` : Reference to world model representation.
   - `protected ModuleManager moduleManager` : Pointer to module manager for sub-module access.
   - `protected DevelopData developData` : Reference to development data configurations.
   - Invocation counter fields (`countPrecompute`, `countResume`, `countPreparate`, `countUpdateInfo`).

---

## <a id="calc"></a>calc()

```java
abstract TargetSelector<E> calc();
```
Calculates target selection.

**Parameters :**
- None

**Returns :**
- `TargetSelector<E>` : This module instance for method chaining.

---

## <a id="gettarget"></a>getTarget()

```java
abstract EntityID getTarget();
```
Retrieves selected target entity ID.

**Parameters :**
- None

**Returns :**
- `EntityID` : Selected target entity ID, or `null`.

---

## <a id="precompute"></a>precompute()

```java
TargetSelector<E> precompute(PrecomputeData precomputeData);
```
Executes precomputation tasks.

**Parameters :**
- `precomputeData` : Precomputation data storage container.

**Returns :**
- `TargetSelector<E>` : This instance.

---

## <a id="resume"></a>resume()

```java
TargetSelector<E> resume(PrecomputeData precomputeData);
```
Resumes module state using precomputed data.

**Parameters :**
- `precomputeData` : Precomputation data storage container.

**Returns :**
- `TargetSelector<E>` : This instance.

---

## <a id="preparate"></a>preparate()

```java
TargetSelector<E> preparate();
```
Executes preparation setup tasks.

**Parameters :**
- None

**Returns :**
- `TargetSelector<E>` : This instance.

---

## <a id="updateinfo"></a>updateInfo()

```java
TargetSelector<E> updateInfo(MessageManager messageManager);
```
Updates internal state at the start of each tick.

**Parameters :**
- `messageManager` : Communication message manager instance.

**Returns :**
- `TargetSelector<E>` : This instance.

---
