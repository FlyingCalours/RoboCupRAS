# CommandPicker.java

```
CommandPicker(
    AgentInfo ai,
    WorldInfo wi,
    ScenarioInfo si,
    ModuleManager moduleManager,
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

**CommandPicker** is the **centre side of centralized control**: it takes the allocation map produced by a `TargetAllocator` (agent ID → target ID) and converts it into the command messages the centre broadcasts.

```java
this.commandPicker = moduleManager.getCommandPicker(
    "TacticsPoliceOffice.CommandPicker",
    "adf.impl.centralized.DefaultCommandPickerPolice");
...
for (CommunicationMessage msg :
        this.commandPicker.setAllocatorResult(allocation).calc().getResult()) {
    messageManager.addMessage(msg);
}
```

- **Main flow**
  - [setAllocatorResult()](#setallocatorresult)
  - [calc()](#calc)
  - [getResult()](#getresult)

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

## <a id="setallocatorresult"></a>setAllocatorResult()

```java
abstract CommandPicker setAllocatorResult(Map<EntityID, EntityID> allocationData);
```
Provide the allocation to convert into commands.

**Parameters :**
- `allocationData` : Map of agent `EntityID` → assigned target `EntityID`.

**Returns :**
- `CommandPicker` : `this`, for chaining.

---

## <a id="calc"></a>calc()

```java
abstract CommandPicker calc();
```
Build the command messages.

**Parameters :**
- None

**Returns :**
- `CommandPicker` : `this`, for chaining.

---

## <a id="getresult"></a>getResult()

```java
abstract Collection<CommunicationMessage> getResult();
```
Get the command messages to hand to `MessageManager.addMessage(...)`.

**Parameters :**
- None

**Returns :**
- `Collection<CommunicationMessage>` : The commands to broadcast.

---

## <a id="precompute"></a>precompute()

```java
CommandPicker precompute(PrecomputeData precomputeData);
```
Precompute phase hook.

**Parameters :**
- `precomputeData` : The precompute save file.

**Returns :**
- `CommandPicker` : `this`.

---

## <a id="resume"></a>resume()

```java
CommandPicker resume(PrecomputeData precomputeData);
```
Start up with precomputed data.

**Parameters :**
- `precomputeData` : The precompute save file.

**Returns :**
- `CommandPicker` : `this`.

---

## <a id="preparate"></a>preparate()

```java
CommandPicker preparate();
```
Start up without precomputed data.

**Parameters :**
- None

**Returns :**
- `CommandPicker` : `this`.

---

## <a id="updateinfo"></a>updateInfo()

```java
CommandPicker updateInfo(MessageManager messageManager);
```
Per tick refresh.

**Parameters :**
- `messageManager` : This tick's communication manager.

**Returns :**
- `CommandPicker` : `this`.

---

## <a id="counters"></a>getCountPrecompute() / getCountResume() / getCountPreparate() / getCountUpdateInfo()

```java
int getCountPrecompute();
int getCountResume();
int getCountPreparate();
int getCountUpdateInfo();
```
Lifecycle call counters.

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
