# MessageFireBrigade.java

```
MessageFireBrigade(boolean isRadio, @Nonnull FireBrigade fireBrigade, int action, @Nullable EntityID target);
MessageFireBrigade(boolean isRadio, StandardMessagePriority sendingPriority,
                   @Nonnull FireBrigade fireBrigade, int action, @Nullable EntityID target);
MessageFireBrigade(boolean isRadio, int from, int ttl, @Nonnull BitStreamReader bitStreamReader);
```

[Attributes Stored](#attributes-stored)

**MessageFireBrigade** is a fire brigade **reporting its own state** : position, condition, remaining water, and which building it is attacking. The extra field compared with the other agent messages is **water**.

```java
messageManager.addMessage(new MessageFireBrigade(
        true, me, MessageFireBrigade.ACTION_EXTINGUISH, targetID));
```

- Action **constants**
  - `ACTION_REST = 0`, `ACTION_MOVE = 1`, `ACTION_EXTINGUISH = 2`, `ACTION_REFILL = 3`, `ACTION_RESCUE = 4`

- Who is it, and what is it doing ?
  - [getAgentID()](#getagentid)
  - [getAction()](#getaction)
  - [getTargetID()](#gettargetid)
  - [getPosition()](#getposition)

- Can it still **fight fire** ?
  - [getWater()](#getwater)

- What is its **condition** ?
  - [getHP() / getBuriedness() / getDamage()](#getcondition)

- Which fields are **actually filled** ?
  - [isTargetDefined() / isWaterDefined() / isHPDefined() / isBuriednessDefined() / isDamageDefined() / isPositionDefined()](#isdefined)

## <a id="attributes-stored"></a>Attributes Stored
1. `agentID` : ID of the reporting fire brigade.
2. `action` / `target` : What it is doing and on which entity.
3. `fireBrigadeWater` : Remaining water in the tank.
4. `humanPosition`, `humanHP`, `humanBuriedness`, `humanDamage` : Its own condition.

## <a id="getagentid"></a>getAgentID()

```java
EntityID getAgentID();
```
Get the ID of the reporting fire brigade.

**Parameters :**
- None

**Returns :**
- `EntityID` : The agent ID.

---

## <a id="getaction"></a>getAction()

```java
int getAction();
```
Get the action code being executed (compare with the `ACTION_*` constants).

**Parameters :**
- None

**Returns :**
- `int` : The action code.

---

## <a id="gettargetid"></a>getTargetID()

```java
EntityID getTargetID();
```
Get the entity being acted on — usually the burning building.

**Parameters :**
- None

**Returns :**
- `EntityID` : The target ID, or `null`.

---

## <a id="getposition"></a>getPosition()

```java
EntityID getPosition();
```
Get the area the agent is standing in.

**Parameters :**
- None

**Returns :**
- `EntityID` : The position entity ID.

---

## <a id="getwater"></a>getWater()

```java
int getWater();
```
Get the reported water quantity. A brigade near zero must head for a refuge or hydrant, so a centre can re-plan around it.

**Parameters :**
- None

**Returns :**
- `int` : Water in litres.

---

## <a id="getcondition"></a>getHP() / getBuriedness() / getDamage()

```java
int getHP();
int getBuriedness();
int getDamage();
```
Get the reporting agent's own health, buriedness and damage.

**Parameters :**
- None

**Returns :**
- `int` : The value.

---

## <a id="isdefined"></a>isTargetDefined() / isWaterDefined() / isHPDefined() / isBuriednessDefined() / isDamageDefined() / isPositionDefined()

```java
boolean isTargetDefined();
boolean isWaterDefined();
boolean isHPDefined();
boolean isBuriednessDefined();
boolean isDamageDefined();
boolean isPositionDefined();
```
Check whether each field is meaningful before reading it.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` if the value is meaningful.

---
