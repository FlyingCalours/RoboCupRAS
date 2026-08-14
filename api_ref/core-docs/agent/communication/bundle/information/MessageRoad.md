# MessageRoad.java

```
MessageRoad(boolean isRadio, @Nonnull Road road, @Nullable Blockade blockade,
            @Nullable Boolean isPassable, boolean isSendBlockadeLocation);
MessageRoad(boolean isRadio, StandardMessagePriority sendingPriority, @Nonnull Road road,
            @Nullable Blockade blockade, @Nullable Boolean isPassable, boolean isSendBlockadeLocation);
MessageRoad(boolean isRadio, int from, int ttl, @Nonnull BitStreamReader bitStreamReader);
```

[Attributes Stored](#attributes-stored)

**MessageRoad** reports a **road and its blockade**. Police forces send it to tell the team a road is blocked (or cleared) — pass `isPassable = true` and `blockade = null` to announce a road is now open.

```java
messageManager.addMessage(new MessageRoad(true, road, blockade, false, true));
```

- Which road / blockade ?
  - [getRoadID()](#getroadid)
  - [getBlockadeID()](#getblockadeid)

- How **bad** is the blockade ?
  - [getRepairCost()](#getrepaircost)
  - [getBlockadeX()](#getblockadex)
  - [getBlockadeY()](#getblockadey)
  - [isPassable()](#ispassable)

- Which fields are **actually filled** ?
  - [isBlockadeDefined() / isRepairCostDefined() / isXDefined() / isYDefined()](#isdefined)

## <a id="attributes-stored"></a>Attributes Stored
1. `protected EntityID roadID` : The reported road.
2. `protected EntityID roadBlockadeID` : The blockade on it, if any.
3. `protected int blockadeRepairCost` : Work needed to clear it.
4. `protected Boolean roadPassable` : Whether the road can be traversed.
5. `protected Integer blockadeX` / `blockadeY` : Blockade centre coordinates.
6. `protected boolean isSendBlockadeLocation` : Whether coordinates are included in the encoding.

## <a id="getroadid"></a>getRoadID()

```java
EntityID getRoadID();
```
Get the ID of the reported road.

**Parameters :**
- None

**Returns :**
- `EntityID` : The road ID.

---

## <a id="getblockadeid"></a>getBlockadeID()

```java
EntityID getBlockadeID();
```
Get the ID of the reported blockade.

**Parameters :**
- None

**Returns :**
- `EntityID` : The blockade ID, or `null` when none was reported.

---

## <a id="getrepaircost"></a>getRepairCost()

```java
int getRepairCost();
```
Get the reported repair cost of the blockade — how much clearing work it needs.

**Parameters :**
- None

**Returns :**
- `int` : Repair cost.

---

## <a id="getblockadex"></a>getBlockadeX()

```java
Integer getBlockadeX();
```
Get the X coordinate of the blockade, when the sender included it.

**Parameters :**
- None

**Returns :**
- `Integer` : X coordinate, or `null`.

---

## <a id="getblockadey"></a>getBlockadeY()

```java
Integer getBlockadeY();
```
Get the Y coordinate of the blockade, when the sender included it.

**Parameters :**
- None

**Returns :**
- `Integer` : Y coordinate, or `null`.

---

## <a id="ispassable"></a>isPassable()

```java
Boolean isPassable();
```
Whether the sender considers the road traversable.

**Parameters :**
- None

**Returns :**
- `Boolean` : `true`/`false`, or `null` when unknown.

---

## <a id="isdefined"></a>isBlockadeDefined() / isRepairCostDefined() / isXDefined() / isYDefined()

```java
boolean isBlockadeDefined();
boolean isRepairCostDefined();
boolean isXDefined();
boolean isYDefined();
```
Check whether each field is meaningful before reading it.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` if the value is meaningful.

---
