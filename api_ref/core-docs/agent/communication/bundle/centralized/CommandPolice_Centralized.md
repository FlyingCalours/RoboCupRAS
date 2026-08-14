# CommandPolice.java (centralized)

`adf.core.agent.communication.standard.bundle.centralized.CommandPolice`

```
CommandPolice(boolean isRadio, @Nullable EntityID toID, @Nullable EntityID targetID, int action);
CommandPolice(boolean isRadio, StandardMessagePriority sendingPriority,
      @Nullable EntityID toID, @Nullable EntityID targetID, int action);
CommandPolice(boolean isRadio, int from, int ttl, @Nonnull BitStreamReader bitStreamReader);
```

[Attributes Stored](#attributes-stored)

**CommandPolice** is an order sent by a **Police centre** to one Police platoon (or to all of them). The platoon reads it in `think()`, checks that `getToID()` is its own ID (or that `isBroadcast()` is true), and hands it to a `CommandExecutor`.

Pass `toID = null` to broadcast the order to every Police agent.

```java
messageManager.addMessage(new CommandPolice(
        true, policeID, blockedRoadID, CommandPolice.ACTION_CLEAR));
```

- Action **constants**
  - `ACTION_REST = 0`, `ACTION_MOVE = 1`, `ACTION_CLEAR = 2`, `ACTION_AUTONOMY = 3`

- Who is it **for**, and about **what** ?
  - [getToID()](#gettoid)
  - [getTargetID()](#gettargetid)
  - [getAction()](#getaction)
  - [isBroadcast()](#isbroadcast)

- Which fields are **actually filled** ?
  - [isToIDDefined() / isTargetIDDefined()](#isdefined)

## <a id="attributes-stored"></a>Attributes Stored
1. `toID` : Receiver agent ID (`null` = broadcast).
2. `targetID` : Entity the receiver should act on.
3. `action` : Which action to perform (see constants).

## <a id="gettoid"></a>getToID()

```java
EntityID getToID();
```
Get the agent this command is addressed to.

**Parameters :**
- None

**Returns :**
- `EntityID` : The receiver ID, or `null` when broadcast.

---

## <a id="gettargetid"></a>getTargetID()

```java
EntityID getTargetID();
```
Get the entity the receiver is ordered to act on.

**Parameters :**
- None

**Returns :**
- `EntityID` : The target ID, or `null`.

---

## <a id="getaction"></a>getAction()

```java
int getAction();
```
Get the ordered action code (compare with the `ACTION_*` constants). `ACTION_AUTONOMY` means "decide for yourself".

**Parameters :**
- None

**Returns :**
- `int` : The action code.

---

## <a id="isbroadcast"></a>isBroadcast()

```java
boolean isBroadcast();
```
Check whether this command is addressed to every agent of the type.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` when broadcast.

---

## <a id="isdefined"></a>isToIDDefined() / isTargetIDDefined()

```java
boolean isToIDDefined();
boolean isTargetIDDefined();
```
Check whether the receiver / target fields are meaningful.
(`idTargetIDDefined()` also exists — it is an older, misspelled alias of `isTargetIDDefined()`.)

**Parameters :**
- None

**Returns :**
- `boolean` : `true` if the value is meaningful.

---
