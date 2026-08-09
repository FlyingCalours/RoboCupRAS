# HumanDetector.java

```java
HumanDetector(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

**HumanDetector** is an abstract **target detection module for Human entities** (such as civilians or injured agents requiring rescue), answering questions like :

- How to **calculate human detection results** ?
  - [calc()](#calc)

- How to manage **module lifecycle and updates** ?
  - [precompute()](#precompute)
  - [resume()](#resume)
  - [preparate()](#preparate)
  - [updateInfo()](#updateinfo)

## <a id="attributes-stored"></a>Attributes Stored
1. Inherited from `TargetDetector<Human>` / `AbstractModule`:
   - `protected ScenarioInfo scenarioInfo` : Reference to scenario configuration information.
   - `protected AgentInfo agentInfo` : Reference to agent self-awareness state.
   - `protected WorldInfo worldInfo` : Reference to world model representation.
   - `protected ModuleManager moduleManager` : Pointer to module manager.
   - `protected DevelopData developData` : Reference to development data configurations.

---

## <a id="calc"></a>calc()

```java
abstract HumanDetector calc();
```
Calculates and detects target `Human` entities.

**Parameters :**
- None

**Returns :**
- `HumanDetector` : This module instance for method chaining.

---

## <a id="precompute"></a>precompute()

```java
HumanDetector precompute(PrecomputeData precomputeData);
```
Executes precomputation tasks for human detection before simulation starts.

**Parameters :**
- `precomputeData` : Precomputation data storage container.

**Returns :**
- `HumanDetector` : This module instance for method chaining.

---

## <a id="resume"></a>resume()

```java
HumanDetector resume(PrecomputeData precomputeData);
```
Resumes human detection state using precomputed data upon startup.

**Parameters :**
- `precomputeData` : Loaded precomputation data container.

**Returns :**
- `HumanDetector` : This module instance for method chaining.

---

## <a id="preparate"></a>preparate()

```java
HumanDetector preparate();
```
Executes preparation setup tasks when precomputed data is unavailable.

**Parameters :**
- None

**Returns :**
- `HumanDetector` : This module instance for method chaining.

---

## <a id="updateinfo"></a>updateInfo()

```java
HumanDetector updateInfo(MessageManager messageManager);
```
Updates human detector state and processes incoming messages at the beginning of each simulation tick.

**Parameters :**
- `messageManager` : Communication message manager instance.

**Returns :**
- `HumanDetector` : This module instance for method chaining.

---
