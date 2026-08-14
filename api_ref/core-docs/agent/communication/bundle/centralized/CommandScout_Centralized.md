# CommandScout.java (centralized)

`adf.core.agent.communication.standard.bundle.centralized.CommandScout`

```
CommandScout(boolean isRadio, @Nullable EntityID toID, @Nullable EntityID targetID, int range);
CommandScout(boolean isRadio, StandardMessagePriority sendingPriority,
             @Nullable EntityID toID, @Nullable EntityID targetID, int range);
CommandScout(boolean isRadio, int from, int ttl, @Nonnull BitStreamReader bitStreamReader);
```

[Attributes Stored](#attributes-stored)

**CommandScout** orders a platoon to **go and look around an area**. Unlike the other commands it carries no action code — instead it carries a `range`: explore everything within that distance of `targetID`. It is the centre's tool for directed exploration when large parts of the map are still unknown.

```java
messageManager.addMessage(new CommandScout(true, agentID, areaID, 50000));
```

- Who is it **for**, and where ?
  - [getToID()](#gettoid)
  - [getTargetID()](#gettargetid)
  - [getRange()](#getrange)
  - [isBroadcast()](#isbroadcast)

- Which fields are **actually filled** ?
  - [isToIDDefined() / isTargetIDDefined()](#isdefined)

## <a id="attributes-stored"></a>Attributes Stored
1. `toID` : Receiver agent ID (`null` = broadcast).
2. `targetID` : Centre of the area to scout.
3. `range` : Radius to explore around the target.

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
Get the area the receiver should scout around.

**Parameters :**
- None

**Returns :**
- `EntityID` : The target area ID, or `null`.

---

## <a id="getrange"></a>getRange()

```java
int getRange();
```
Get the scouting radius around the target.

**Parameters :**
- None

**Returns :**
- `int` : Range in map units.

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

**Parameters :**
- None

**Returns :**
- `boolean` : `true` if the value is meaningful.

---
