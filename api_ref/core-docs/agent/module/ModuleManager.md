# ModuleManager.java

```
ModuleManager(
    @Nonnull AgentInfo agentInfo,
    @Nonnull WorldInfo worldInfo,
    @Nonnull ScenarioInfo scenarioInfo,
    @Nonnull ModuleConfig moduleConfig,
    @Nonnull DevelopData developData
);
```

**ModuleManager** is the **factory / registry** for every pluggable component. You never call `new SampleSearch(...)` directly — you ask the manager for a module name, and `module.cfg` decides which class is actually created. The same instance is reused for the same key, so two tactics asking for the same module share one object.

```java
this.pathPlanning = moduleManager.getModule(
    "SampleTacticsPoliceForce.PathPlanning",
    "adf.impl.module.algorithm.AStarPathPlanning");
```

- How do I get a **module** ?
  - [getModule()](#getmodule)

- How do I get an **ExtAction** ?
  - [getExtAction()](#getextaction)

- How do I get **centralized** components ?
  - [getCommandExecutor()](#getcommandexecutor)
  - [getCommandPicker()](#getcommandpicker)

- How do I get **communication** components ?
  - [getChannelSubscriber()](#getchannelsubscriber)
  - [getMessageCoordinator()](#getmessagecoordinator)

- What **config** is in use ?
  - [getModuleConfig()](#getmoduleconfig)

## <a id="getmodule"></a>getModule()

```java
final <T extends AbstractModule> T getModule(@Nonnull String moduleName, @Nullable String defaultClassName);
final <T extends AbstractModule> T getModule(@Nonnull String moduleName);
```
Create (or reuse) the module registered under `moduleName` in `module.cfg`. If the key is missing, `defaultClassName` is instantiated instead. The return type is inferred from the variable you assign to, e.g. `PathPlanning p = moduleManager.getModule(...)`.

**Parameters :**
- `moduleName` : Key in `module.cfg`, e.g. `"SampleTacticsPoliceForce.PathPlanning"`.
- `defaultClassName` : Fully qualified class name used when the key is not configured.

**Returns :**
- `T extends AbstractModule` : The module instance.

---

## <a id="getextaction"></a>getExtAction()

```java
final ExtAction getExtAction(String actionName, String defaultClassName);
final ExtAction getExtAction(String actionName);
```
Same idea for extended actions (`ExtActionClear`, `ExtActionMove`, `ExtActionTransport`, ...).

**Parameters :**
- `actionName` : Key in `module.cfg`.
- `defaultClassName` : Fallback class name.

**Returns :**
- `ExtAction` : The extended action instance.

---

## <a id="getcommandexecutor"></a>getCommandExecutor()

```java
final <E extends CommandExecutor<? extends CommunicationMessage>> E getCommandExecutor(String executorName, String defaultClassName);
final <E extends CommandExecutor<? extends CommunicationMessage>> E getCommandExecutor(String executorName);
```
Get the component that turns a received centre command into an action (platoon side of centralized control).

**Parameters :**
- `executorName` : Key in `module.cfg`.
- `defaultClassName` : Fallback class name.

**Returns :**
- `E extends CommandExecutor` : The executor instance.

---

## <a id="getcommandpicker"></a>getCommandPicker()

```java
final CommandPicker getCommandPicker(String pickerName, String defaultClassName);
final CommandPicker getCommandPicker(String pickerName);
```
Get the component that converts an allocation map into command messages (centre side of centralized control).

**Parameters :**
- `pickerName` : Key in `module.cfg`.
- `defaultClassName` : Fallback class name.

**Returns :**
- `CommandPicker` : The picker instance.

---

## <a id="getchannelsubscriber"></a>getChannelSubscriber()

```java
final ChannelSubscriber getChannelSubscriber(String subscriberName, String defaultClassName);
final ChannelSubscriber getChannelSubscriber(String subscriberName);
final ChannelSubscriber getChannelSubscriber(Class<ChannelSubscriber> subsClass);
```
Get the channel subscription policy, by config key or directly by class.

**Parameters :**
- `subscriberName` : Key in `module.cfg`.
- `defaultClassName` : Fallback class name.
- `subsClass` : Class object to instantiate directly.

**Returns :**
- `ChannelSubscriber` : The subscriber instance.

---

## <a id="getmessagecoordinator"></a>getMessageCoordinator()

```java
final MessageCoordinator getMessageCoordinator(String coordinatorName, String defaultClassName);
final MessageCoordinator getMessageCoordinator(String coordinatorName);
final MessageCoordinator getMessageCoordinator(Class<MessageCoordinator> subsClass);
```
Get the message coordination policy, by config key or directly by class.

**Parameters :**
- `coordinatorName` : Key in `module.cfg`.
- `defaultClassName` : Fallback class name.
- `subsClass` : Class object to instantiate directly.

**Returns :**
- `MessageCoordinator` : The coordinator instance.

---

## <a id="getmoduleconfig"></a>getModuleConfig()

```java
ModuleConfig getModuleConfig();
```
Get the parsed `module.cfg` so you can read your own custom keys.

**Parameters :**
- None

**Returns :**
- `ModuleConfig` : The module configuration object.

---
