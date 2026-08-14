# TacticsCenter.java

```
TacticsCenter();
TacticsCenter(TacticsCenter parent);
```

Subclasses you actually extend :
`TacticsAmbulanceCentre`, `TacticsFireStation`, `TacticsPoliceOffice`

**TacticsCenter** is the **brain of a centre agent** (Ambulance Centre / Fire Station / Police Office). Centres cannot move or act on the map, so `think()` returns nothing — their job is to receive information, run the `TargetAllocator` modules, and broadcast commands through a `CommandPicker`.

- **Lifecycle** (you must implement all four)
  - [initialize()](#initialize)
  - [resume()](#resume)
  - [preparate()](#preparate)
  - [think()](#think)

- Managing **my modules**
  - [registerModule()](#registermodule)
  - [unregisterModule()](#unregistermodule)
  - [modulesPrecompute() / modulesResume() / modulesPreparate() / modulesUpdateInfo()](#modules-lifecycle)
  - [getParentControl()](#getparentcontrol)

## <a id="initialize"></a>initialize()

```java
abstract void initialize(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
                         ModuleManager moduleManager, MessageManager messageManager,
                         DevelopData developData);
```
Called once at connection. Build the allocator and picker modules here.

**Parameters :**
- The standard info objects, module manager, message manager and develop data.

**Returns :**
- `void`

---

## <a id="resume"></a>resume()

```java
abstract void resume(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
                     ModuleManager moduleManager, PrecomputeData precomputeData,
                     DevelopData developData);
```
Start up with precomputed data available.

**Parameters :**
- `precomputeData` plus the standard objects.

**Returns :**
- `void`

---

## <a id="preparate"></a>preparate()

```java
abstract void preparate(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
                        ModuleManager moduleManager, DevelopData developData);
```
Start up without precomputed data.

**Parameters :**
- The standard objects and module manager.

**Returns :**
- `void`

---

## <a id="think"></a>think()

```java
abstract void think(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
                    ModuleManager moduleManager, MessageManager messageManager,
                    DevelopData developData);
```
Called every tick. Update modules, run the allocator, then push the resulting command messages with `messageManager.addMessage(...)`. Note the **`void`** return type — a centre issues no map action.

**Parameters :**
- The standard objects, module manager, message manager and develop data.

**Returns :**
- `void`

---

## <a id="registermodule"></a>registerModule()

```java
protected void registerModule(AbstractModule module);
protected void registerModule(CommandPicker module);
```
Register a module or a command picker with this centre.

**Parameters :**
- `module` : The component to register.

**Returns :**
- `void`

---

## <a id="unregistermodule"></a>unregisterModule()

```java
protected boolean unregisterModule(AbstractModule module);
protected boolean unregisterModule(CommandPicker module);
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
Forward the matching lifecycle call to all registered components.

**Parameters :**
- `precomputeData` / `messageManager` as appropriate.

**Returns :**
- `void`

---

## <a id="getparentcontrol"></a>getParentControl()

```java
TacticsCenter getParentControl();
```
Get the parent centre tactics if this one was created as a sub tactic.

**Parameters :**
- None

**Returns :**
- `TacticsCenter` : The parent, or `null`.

---
