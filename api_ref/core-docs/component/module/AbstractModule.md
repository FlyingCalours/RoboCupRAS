# AbstractModule.java

```
AbstractModule(
    AgentInfo ai,
    WorldInfo wi,
    ScenarioInfo si,
    ModuleManager moduleManager,
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

**AbstractModule** is the **root class of every module** — `PathPlanning`, `Clustering`, `Search`, `TargetDetector`, `TargetAllocator` and all their subclasses extend it. It defines the lifecycle every module goes through each tick and each phase, plus the counters that stop the same work from being repeated twice in one tick.

Every subclass must declare the same 5 argument constructor and call `super(...)`.

- **Lifecycle** (called by the framework)
  - [precompute()](#precompute)
  - [resume()](#resume)
  - [preparate()](#preparate)
  - [updateInfo()](#updateinfo)
  - [calc()](#calc)

- Managing **sub modules**
  - [registerModule()](#registermodule)
  - [unregisterModule()](#unregistermodule)

- **Call counters** (guard against double execution)
  - [getCountPrecompute() / getCountResume() / getCountPreparate() / getCountUpdateInfo()](#counters)
  - [resetCountPrecompute() / resetCountResume() / resetCountPreparate() / resetCountUpdateInfo()](#reset-counters)

## <a id="attributes-stored"></a>Attributes Stored
1. `protected AgentInfo agentInfo` : Self awareness object.
2. `protected WorldInfo worldInfo` : Map and entity knowledge.
3. `protected ScenarioInfo scenarioInfo` : Simulation rules and constants.
4. `protected ModuleManager moduleManager` : Factory used to obtain sub modules.
5. `protected DevelopData developData` : Tuning parameters.
6. Internal counters `countPrecompute`, `countResume`, `countPreparate`, `countUpdateInfo`.
7. Internal list of registered sub modules.

## <a id="precompute"></a>precompute()

```java
AbstractModule precompute(PrecomputeData precomputeData);
```
Called during the **precompute phase**. Do the heavy offline work here and save it into `precomputeData`. Override it, call `super.precompute(precomputeData)` first, and return `this`.

**Parameters :**
- `precomputeData` : The precompute save file.

**Returns :**
- `AbstractModule` : `this`, for chaining.

---

## <a id="resume"></a>resume()

```java
AbstractModule resume(PrecomputeData precomputeData);
```
Called once at the start of a run **that has precomputed data**. Load your saved values here.

**Parameters :**
- `precomputeData` : The precompute save file.

**Returns :**
- `AbstractModule` : `this`.

---

## <a id="preparate"></a>preparate()

```java
AbstractModule preparate();
```
Called once at the start of a run **without precomputed data**. Do a cheap online version of the same setup.

**Parameters :**
- None

**Returns :**
- `AbstractModule` : `this`.

---

## <a id="updateinfo"></a>updateInfo()

```java
AbstractModule updateInfo(MessageManager messageManager);
```
Called **every tick before thinking**. Refresh the module from new perception and from the messages received this tick. Must forward the call to sub modules.

**Parameters :**
- `messageManager` : The communication manager holding this tick's messages.

**Returns :**
- `AbstractModule` : `this`.

---

## <a id="calc"></a>calc()

```java
abstract AbstractModule calc();
```
Do the actual computation of the module (find the target, build the path, ...). The result is then read through the module's own `getResult()`/`getTarget()` method.

**Parameters :**
- None

**Returns :**
- `AbstractModule` : `this`.

---

## <a id="registermodule"></a>registerModule()

```java
protected void registerModule(AbstractModule module);
```
Register a sub module so that its lifecycle methods are called automatically with the parent's.

**Parameters :**
- `module` : The sub module to register.

**Returns :**
- `void`

---

## <a id="unregistermodule"></a>unregisterModule()

```java
protected boolean unregisterModule(AbstractModule module);
```
Remove a previously registered sub module.

**Parameters :**
- `module` : The sub module to remove.

**Returns :**
- `boolean` : `true` if it was registered.

---

## <a id="counters"></a>getCountPrecompute() / getCountResume() / getCountPreparate() / getCountUpdateInfo()

```java
int getCountPrecompute();
int getCountResume();
int getCountPreparate();
int getCountUpdateInfo();
```
Number of times the matching lifecycle method has been called. The standard idiom at the top of an override is:

```java
super.updateInfo(messageManager);
if (this.getCountUpdateInfo() >= 2) { return this; }
```
so the body runs only once per tick even when several tactics share the module.

**Parameters :**
- None

**Returns :**
- `int` : The call count.

---

## <a id="reset-counters"></a>resetCountPrecompute() / resetCountResume() / resetCountPreparate() / resetCountUpdateInfo()

```java
void resetCountPrecompute();
void resetCountResume();
void resetCountPreparate();
void resetCountUpdateInfo();
```
Set the matching counter back to zero (done by the framework at the start of each tick).

**Parameters :**
- None

**Returns :**
- `void`

---
