# Tactics.java

```
Tactics();
Tactics(Tactics parent);
```

Subclasses you actually extend :
`TacticsAmbulanceTeam`, `TacticsFireBrigade`, `TacticsPoliceForce`
(each adds only the two constructors above).

[Attributes Stored](#attributes-stored)

**Tactics** is the **brain of a platoon agent** — the entry point of your team code. The framework builds it once, drives it through the lifecycle below, and every tick asks [think()](#think) for exactly one `Action`.

```java
public class SampleTacticsPoliceForce extends TacticsPoliceForce { ... }
```

- **Lifecycle** (you must implement all five)
  - [initialize()](#initialize)
  - [precompute()](#precompute)
  - [resume()](#resume)
  - [preparate()](#preparate)
  - [think()](#think)

- Managing **my modules**
  - [registerModule()](#registermodule)
  - [unregisterModule()](#unregistermodule)
  - [modulesPrecompute() / modulesResume() / modulesPreparate() / modulesUpdateInfo()](#modules-lifecycle)
  - [getParentTactics()](#getparenttactics)

## <a id="attributes-stored"></a>Attributes Stored
1. `private Tactics parentTactics` : Parent tactics when this one is used as a sub tactic.
2. `private List<AbstractModule> modules` : Registered modules.
3. `private List<ExtAction> modulesExtAction` : Registered extended actions.
4. `private List<CommandExecutor> modulesCommandExecutor` : Registered command executors.

## <a id="initialize"></a>initialize()

```java
abstract void initialize(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
                         ModuleManager moduleManager, MessageManager messageManager,
                         DevelopData developData);
```
Called once when the agent connects. Create your modules here with `moduleManager.getModule(...)`, register the message bundle (`messageManager.registerMessageBundle(new StandardMessageBundle())`) and call `registerModule(...)` on everything you built.

**Parameters :**
- `agentInfo`, `worldInfo`, `scenarioInfo` : The standard info objects.
- `moduleManager` : Module factory.
- `messageManager` : Communication manager.
- `developData` : Tuning parameters.

**Returns :**
- `void`

---

## <a id="precompute"></a>precompute()

```java
abstract void precompute(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
                         ModuleManager moduleManager, PrecomputeData precomputeData,
                         DevelopData developData);
```
Called during the precompute phase. Usually just forwards to `modulesPrecompute(precomputeData)`.

**Parameters :**
- `precomputeData` : The precompute save file, plus the standard info objects.

**Returns :**
- `void`

---

## <a id="resume"></a>resume()

```java
abstract void resume(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
                     ModuleManager moduleManager, PrecomputeData precomputeData,
                     DevelopData developData);
```
Called once at start up **when precomputed data exists**. Usually forwards to `modulesResume(precomputeData)`.

**Parameters :**
- `precomputeData` : The precompute save file, plus the standard info objects.

**Returns :**
- `void`

---

## <a id="preparate"></a>preparate()

```java
abstract void preparate(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
                        ModuleManager moduleManager, DevelopData developData);
```
Called once at start up **when there is no precomputed data**. Usually forwards to `modulesPreparate()`.

**Parameters :**
- The standard info objects and the module manager.

**Returns :**
- `void`

---

## <a id="think"></a>think()

```java
abstract Action think(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
                      ModuleManager moduleManager, MessageManager messageManager,
                      DevelopData developData);
```
Called **every tick**. Read messages, update modules (`modulesUpdateInfo(messageManager)`), pick a target, and return one `Action`. Returning `null` is allowed but wastes the tick — return `new ActionRest()` instead.

**Parameters :**
- `agentInfo`, `worldInfo`, `scenarioInfo` : The standard info objects.
- `moduleManager` : Module factory.
- `messageManager` : Messages heard this tick / queue for sending.
- `developData` : Tuning parameters.

**Returns :**
- `Action` : The command to execute this tick.

---

## <a id="registermodule"></a>registerModule()

```java
protected void registerModule(AbstractModule module);
protected void registerModule(ExtAction module);
protected void registerModule(CommandExecutor module);
```
Register a component so its lifecycle runs together with the tactics'.

**Parameters :**
- `module` : The module, extended action or command executor to register.

**Returns :**
- `void`

---

## <a id="unregistermodule"></a>unregisterModule()

```java
protected boolean unregisterModule(AbstractModule module);
protected boolean unregisterModule(ExtAction module);
protected boolean unregisterModule(CommandExecutor module);
```
Remove a registered component.

**Parameters :**
- `module` : The component to remove.

**Returns :**
- `boolean` : `true` if it was registered.

---

## <a id="modules-lifecycle"></a>modulesPrecompute() / modulesResume() / modulesPreparate() / modulesUpdateInfo()

```java
protected void modulesPrecompute(PrecomputeData precomputeData);
protected void modulesResume(PrecomputeData precomputeData);
protected void modulesPreparate();
protected void modulesUpdateInfo(MessageManager messageManager);
```
Forward the matching lifecycle call to every registered component. Call `modulesUpdateInfo(messageManager)` at the top of `think()`.

**Parameters :**
- `precomputeData` / `messageManager` : Passed through to the modules.

**Returns :**
- `void`

---

## <a id="getparenttactics"></a>getParentTactics()

```java
Tactics getParentTactics();
```
Get the parent tactics if this object was created as a sub tactic.

**Parameters :**
- None

**Returns :**
- `Tactics` : The parent, or `null`.

---
