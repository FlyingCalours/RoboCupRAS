# MessagePoliceForce.java

```
MessagePoliceForce(boolean isRadio, @Nonnull PoliceForce policeForce, int action, @Nullable EntityID target);
MessagePoliceForce(boolean isRadio, StandardMessagePriority sendingPriority,
                   @Nonnull PoliceForce policeForce, int action, @Nullable EntityID target);
MessagePoliceForce(boolean isRadio, int from, int ttl, @Nonnull BitStreamReader bitStreamReader);
```

[Attributes Stored](#attributes-stored)

**MessagePoliceForce** is a police force **reporting its own state** : position, condition, and which road/blockade it is clearing. Sending it every tick is what stops two police agents from clearing the same blockade.

```java
messageManager.addMessage(new MessagePoliceForce(
        true, me, MessagePoliceForce.ACTION_CLEAR, targetID));
```

- Action **constants**
  - `ACTION_REST = 0`, `ACTION_MOVE = 1`, `ACTION_CLEAR = 2`

- Who is it, and what is it doing ?
  - [getAgentID()](#getagentid)
  - [getAction()](#getaction)
  - [getTargetID()](#gettargetid)
  - [getPosition()](#getposition)

- What is its **condition** ?
  - [getHP() / getBuriedness() / getDamage()](#getcondition)

- Which fields are **actually filled** ?
  - [isTargetDefined() / isHPDefined() / isBuriednessDefined() / isDamageDefined() / isPositionDefined()](#isdefined)

## <a id="attributes-stored"></a>Attributes Stored
1. `agentID` : ID of the reporting police force.
2. `action` / `target` : What it is doing and on which entity.
3. `humanPosition`, `humanHP`, `humanBuriedness`, `humanDamage` : Its own condition.

## <a id="getagentid"></a>getAgentID()

```java
EntityID getAgentID();
```
Get the ID of the reporting police force.

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
Get the entity being acted on — usually the road or blockade being cleared.

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

## <a id="isdefined"></a>isTargetDefined() / isHPDefined() / isBuriednessDefined() / isDamageDefined() / isPositionDefined()

```java
boolean isTargetDefined();
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
