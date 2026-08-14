# ExtAction.java

```java
ExtAction(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

**ExtAction** is an abstract **external action execution** module that serves as a base class for complex agent behaviors, managing target setting, action calculation, lifecycle callbacks, and invocation counts, answering questions like :

- How to **set targets, calculate actions, and retrieve results** ?
  - [setTarget()](#settarget)
  - [calc()](#calc)
  - [getAction()](#getaction)

- How to handle **agent lifecycle phases and updates** ?
  - [precompute()](#precompute)
  - [resume()](#resume)
  - [preparate()](#preparate)
  - [updateInfo()](#updateinfo)

- How to query and reset **invocation counters** ?
  - [getCountPrecompute()](#getcountprecompute)
  - [getCountResume()](#getcountresume)
  - [getCountPreparate()](#getcountpreparate)
  - [getCountUpdateInfo()](#getcountupdateinfo)
  - [resetCountPrecompute()](#resetcountprecompute)
  - [resetCountResume()](#resetcountresume)
  - [resetCountPreparate()](#resetcountpreparate)
  - [resetCountUpdateInfo()](#resetcountupdateinfo)

## <a id="attributes-stored"></a>Attributes Stored
1. `protected ScenarioInfo scenarioInfo` : Reference to the scenario configuration information module.
2. `protected AgentInfo agentInfo` : Reference to the self-awareness agent state module.
3. `protected WorldInfo worldInfo` : Reference to the world model representation module.
4. `protected ModuleManager moduleManager` : Pointer to the module manager for sub-module access.
5. `protected DevelopData developData` : Reference to development data configurations.
6. `private int countPrecompute` : Invocation counter for the precomputation phase.
7. `private int countResume` : Invocation counter for the precompute-resume phase.
8. `private int countPreparate` : Invocation counter for the preparation phase.
9. `private int countUpdateInfo` : Invocation counter for information update phase per timestep.
10. `private int countUpdateInfoCurrentTime` : The last simulation timestep recorded during an information update.
11. `protected Action result` : The calculated action result produced by this module.

---

## <a id="settarget"></a>setTarget()

```java
abstract ExtAction setTarget(EntityID targets);
@Deprecated ExtAction setTarget(EntityID... targets);
```
Sets the target entity ID for this external action execution module.

**Parameters :**
- `targets` : The target `EntityID` (or array/varargs of `EntityID` for the deprecated variant).

**Returns :**
- `ExtAction` : This `ExtAction` instance for method chaining.

---

## <a id="calc"></a>calc()

```java
abstract ExtAction calc();
```
Calculates and determines the action to be taken by the agent.

**Parameters :**
- None

**Returns :**
- `ExtAction` : This `ExtAction` instance for method chaining.

---

## <a id="getaction"></a>getAction()

```java
Action getAction();
```
Gets the action calculated by this module.

**Parameters :**
- None

**Returns :**
- `Action` : The calculated `Action` object, or `null` if no action was decided.

---

## <a id="precompute"></a>precompute()

```java
ExtAction precompute(PrecomputeData precomputeData);
```
Executes precomputation tasks before the simulation starts and increments the precompute invocation counter.

**Parameters :**
- `precomputeData` : Precomputation data storage container.

**Returns :**
- `ExtAction` : This `ExtAction` instance.

---

## <a id="resume"></a>resume()

```java
ExtAction resume(PrecomputeData precomputeData);
```
Resumes precomputed data processing upon agent startup and increments the resume invocation counter.

**Parameters :**
- `precomputeData` : Loaded precomputation data container.

**Returns :**
- `ExtAction` : This `ExtAction` instance.

---

## <a id="preparate"></a>preparate()

```java
ExtAction preparate();
```
Executes preparation setup tasks when precomputed data is not used, and increments the preparate invocation counter.

**Parameters :**
- None

**Returns :**
- `ExtAction` : This `ExtAction` instance.

---

## <a id="updateinfo"></a>updateInfo()

```java
ExtAction updateInfo(MessageManager messageManager);
```
Updates internal module information at the beginning of each simulation tick and increments the update counter for the current tick.

**Parameters :**
- `messageManager` : Communication message manager instance.

**Returns :**
- `ExtAction` : This `ExtAction` instance.

---

## <a id="getcountprecompute"></a>getCountPrecompute()

```java
int getCountPrecompute();
```
Gets the total number of times `precompute()` was called.

**Parameters :**
- None

**Returns :**
- `int` : Precompute invocation count.

---

## <a id="getcountresume"></a>getCountResume()

```java
int getCountResume();
```
Gets the total number of times `resume()` was called.

**Parameters :**
- None

**Returns :**
- `int` : Resume invocation count.

---

## <a id="getcountpreparate"></a>getCountPreparate()

```java
int getCountPreparate();
```
Gets the total number of times `preparate()` was called.

**Parameters :**
- None

**Returns :**
- `int` : Preparate invocation count.

---

## <a id="getcountupdateinfo"></a>getCountUpdateInfo()

```java
int getCountUpdateInfo();
```
Gets the number of times `updateInfo()` was called during the current simulation timestep.

**Parameters :**
- None

**Returns :**
- `int` : Update invocation count for the current tick.

---

## <a id="resetcountprecompute"></a>resetCountPrecompute()

```java
void resetCountPrecompute();
```
Resets the precompute invocation counter to zero.

**Parameters :**
- None

**Returns :**
- `void`

---

## <a id="resetcountresume"></a>resetCountResume()

```java
void resetCountResume();
```
Resets the resume invocation counter to zero.

**Parameters :**
- None

**Returns :**
- `void`

---

## <a id="resetcountpreparate"></a>resetCountPreparate()

```java
void resetCountPreparate();
```
Resets the preparate invocation counter to zero.

**Parameters :**
- None

**Returns :**
- `void`

---

## <a id="resetcountupdateinfo"></a>resetCountUpdateInfo()

```java
void resetCountUpdateInfo();
```
Resets the update info invocation counter to zero.

**Parameters :**
- None

**Returns :**
- `void`

---