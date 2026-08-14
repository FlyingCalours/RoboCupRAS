# CommandExecutor.java

```
CommandExecutor(
    AgentInfo ai,
    WorldInfo wi,
    ScenarioInfo si,
    ModuleManager moduleManager,
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

**CommandExecutor&lt;C extends CommunicationMessage&gt;** is the **platoon side of centralized control**: it takes a command message sent by a centre (`CommandPolice`, `CommandFire`, `CommandAmbulance`, `CommandScout`) and turns it into a concrete `Action`.

```java
this.commandExecutorPolice = moduleManager.getCommandExecutor(
    "TacticsPoliceForce.CommandExecutorPolice",
    "adf.impl.centralized.DefaultCommandExecutorPolice");
...
this.commandExecutorPolice.setCommand(command).calc().getAction();
```

- **Main flow**
  - [setCommand()](#setcommand)
  - [calc()](#calc)
  - [getAction()](#getaction)

- **Lifecycle**
  - [precompute()](#precompute)
  - [resume()](#resume)
  - [preparate()](#preparate)
  - [updateInfo()](#updateinfo)

- **Call counters**
  - [getCountPrecompute() / getCountResume() / getCountPreparate() / getCountUpdateInfo()](#counters)
  - [resetCountPrecompute() / resetCountResume() / resetCountPreparate() / resetCountUpdateInfo()](#reset-counters)

## <a id="attributes-stored"></a>Attributes Stored
1. `protected AgentInfo agentInfo`, `protected WorldInfo worldInfo`, `protected ScenarioInfo scenarioInfo` : Standard info objects.
2. `protected ModuleManager moduleManager` : Module factory.
3. `protected DevelopData developData` : Tuning parameters.
4. `protected Action result` : The action produced by `calc()`.

## <a id="setcommand"></a>setCommand()

```java
abstract CommandExecutor setCommand(C command);
```
Give the executor the command message it should carry out.

**Parameters :**
- `command` : The received command message (type `C`).

**Returns :**
- `CommandExecutor` : `this`, for chaining.

---

## <a id="calc"></a>calc()

```java
abstract CommandExecutor calc();
```
Compute the action that fulfils the command.

**Parameters :**
- None

**Returns :**
- `CommandExecutor` : `this`, for chaining.

---

## <a id="getaction"></a>getAction()

```java
Action getAction();
```
Get the action computed by `calc()`.

**Parameters :**
- None

**Returns :**
- `Action` : The action, or `null` if the command could not be executed.

---

## <a id="precompute"></a>precompute()

```java
CommandExecutor precompute(PrecomputeData precomputeData);
```
Precompute phase hook.

**Parameters :**
- `precomputeData` : The precompute save file.

**Returns :**
- `CommandExecutor` : `this`.

---

## <a id="resume"></a>resume()

```java
CommandExecutor resume(PrecomputeData precomputeData);
```
Start up with precomputed data.

**Parameters :**
- `precomputeData` : The precompute save file.

**Returns :**
- `CommandExecutor` : `this`.

---

## <a id="preparate"></a>preparate()

```java
CommandExecutor preparate();
```
Start up without precomputed data.

**Parameters :**
- None

**Returns :**
- `CommandExecutor` : `this`.

---

## <a id="updateinfo"></a>updateInfo()

```java
CommandExecutor updateInfo(MessageManager messageManager);
```
Per tick refresh.

**Parameters :**
- `messageManager` : This tick's communication manager.

**Returns :**
- `CommandExecutor` : `this`.

---

## <a id="counters"></a>getCountPrecompute() / getCountResume() / getCountPreparate() / getCountUpdateInfo()

```java
int getCountPrecompute();
int getCountResume();
int getCountPreparate();
int getCountUpdateInfo();
```
How many times the matching lifecycle method ran — used to skip duplicated work inside one tick.

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
Reset the matching counter.

**Parameters :**
- None

**Returns :**
- `void`

---
