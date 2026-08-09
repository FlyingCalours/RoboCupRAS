# Search.java

```java
Search(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

**Search** is an abstract **area search module** that identifies and selects target `Area` entities (such as unexplored or unsearched roads/buildings) for exploration, answering questions like :

- How to **calculate area search targets** ?
  - [calc()](#calc)

- How to manage **module lifecycle and updates** ?
  - [precompute()](#precompute)
  - [resume()](#resume)
  - [preparate()](#preparate)
  - [updateInfo()](#updateinfo)

## <a id="attributes-stored"></a>Attributes Stored
1. Inherited from `TargetDetector<Area>` / `AbstractModule`:
   - `protected ScenarioInfo scenarioInfo` : Reference to scenario configuration information.
   - `protected AgentInfo agentInfo` : Reference to agent self-awareness state.
   - `protected WorldInfo worldInfo` : Reference to world model representation.
   - `protected ModuleManager moduleManager` : Pointer to module manager.
   - `protected DevelopData developData` : Reference to development data configurations.

---

## <a id="calc"></a>calc()

```java
abstract Search calc();
```
Calculates and identifies target search `Area` entities for agent exploration.

**Parameters :**
- None

**Returns :**
- `Search` : This module instance for method chaining.

---

## <a id="precompute"></a>precompute()

```java
Search precompute(PrecomputeData precomputeData);
```
Executes precomputation tasks for area search strategy before simulation starts.

**Parameters :**
- `precomputeData` : Precomputation data storage container.

**Returns :**
- `Search` : This module instance for method chaining.

---

## <a id="resume"></a>resume()

```java
Search resume(PrecomputeData precomputeData);
```
Resumes search module state using precomputed data upon startup.

**Parameters :**
- `precomputeData` : Loaded precomputation data container.

**Returns :**
- `Search` : This module instance for method chaining.

---

## <a id="preparate"></a>preparate()

```java
Search preparate();
```
Executes preparation setup tasks when precomputed search data is unavailable.

**Parameters :**
- None

**Returns :**
- `Search` : This module instance for method chaining.

---

## <a id="updateinfo"></a>updateInfo()

```java
Search updateInfo(MessageManager messageManager);
```
Updates search module state and processes incoming messages at the beginning of each simulation tick.

**Parameters :**
- `messageManager` : Communication message manager instance.

**Returns :**
- `Search` : This module instance for method chaining.

---
