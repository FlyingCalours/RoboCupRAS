# TargetDetector.java

```java
TargetDetector(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

**TargetDetector** is a generic abstract **target detection module** that identifies and selects a target entity of type `E` (extending `StandardEntity`) for agent tasks, answering questions like :

- How to **calculate and retrieve detected targets** ?
  - [calc()](#calc)
  - [getTarget()](#gettarget)

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
abstract TargetDetector<E> calc();
```
Calculates and detects the optimal target entity for the agent.

**Parameters :**
- None

**Returns :**
- `TargetDetector<E>` : This module instance for method chaining.

---

## <a id="gettarget"></a>getTarget()

```java
abstract EntityID getTarget();
```
Retrieves the target entity ID detected by this module.

**Parameters :**
- None

**Returns :**
- `EntityID` : The `EntityID` of the detected target, or `null` if no target was detected.

---

## <a id="precompute"></a>precompute()

```java
TargetDetector<E> precompute(PrecomputeData precomputeData);
```
Executes precomputation tasks for target detection before simulation starts.

**Parameters :**
- `precomputeData` : Precomputation data storage container.

**Returns :**
- `TargetDetector<E>` : This module instance for method chaining.

---

## <a id="resume"></a>resume()

```java
TargetDetector<E> resume(PrecomputeData precomputeData);
```
Resumes target detection state using precomputed data upon startup.

**Parameters :**
- `precomputeData` : Loaded precomputation data container.

**Returns :**
- `TargetDetector<E>` : This module instance for method chaining.

---

## <a id="preparate"></a>preparate()

```java
TargetDetector<E> preparate();
```
Executes preparation setup tasks when precomputed detection data is unavailable.

**Parameters :**
- None

**Returns :**
- `TargetDetector<E>` : This module instance for method chaining.

---

## <a id="updateinfo"></a>updateInfo()

```java
TargetDetector<E> updateInfo(MessageManager messageManager);
```
Updates internal target detection structures and processes incoming communication at the start of each simulation tick.

**Parameters :**
- `messageManager` : Communication message manager instance.

**Returns :**
- `TargetDetector<E>` : This module instance for method chaining.

---
