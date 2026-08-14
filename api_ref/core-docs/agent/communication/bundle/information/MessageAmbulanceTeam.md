# MessageAmbulanceTeam.java

```
MessageAmbulanceTeam(boolean isRadio, @Nonnull AmbulanceTeam ambulanceTeam, int action, @Nullable EntityID target);
MessageAmbulanceTeam(boolean isRadio, StandardMessagePriority sendingPriority,
                     @Nonnull AmbulanceTeam ambulanceTeam, int action, @Nullable EntityID target);
MessageAmbulanceTeam(boolean isRadio, int from, int ttl, @Nonnull BitStreamReader bitStreamReader);
```

[Attributes Stored](#attributes-stored)

**MessageAmbulanceTeam** is an ambulance **telling the team what it is doing** : its position, its condition, the action it chose and the target it chose. Centres use it to avoid sending two ambulances to the same civilian.

```java
messageManager.addMessage(new MessageAmbulanceTeam(
        true, me, MessageAmbulanceTeam.ACTION_RESCUE, targetID));
```

- Action **constants**
  - `ACTION_REST = 0`, `ACTION_MOVE = 1`, `ACTION_RESCUE = 2`, `ACTION_LOAD = 3`, `ACTION_UNLOAD = 4`

- Who is it, and what is it doing ?
  - [getAgentID()](#getagentid)
  - [getAction()](#getaction)
  - [getTargetID()](#gettargetid)
  - [getPosition()](#getposition)

- What is its **condition** ?
  - [getHP()](#getcondition)
  - [getBuriedness()](#getcondition)
  - [getDamage()](#getcondition)

- Which fields are **actually filled** ?
  - [isTargetDefined() / isHPDefined() / isBuriednessDefined() / isDamageDefined() / isPositionDefined()](#isdefined)

## <a id="attributes-stored"></a>Attributes Stored
1. `agentID` : ID of the reporting ambulance.
2. `action` / `target` : What it is doing and on which entity.
3. `humanPosition`, `humanHP`, `humanBuriedness`, `humanDamage` : Its own condition.

## <a id="getagentid"></a>getAgentID()

```java
EntityID getAgentID();
```
Get the ID of the reporting ambulance team.

**Parameters :**
- None

**Returns :**
- `EntityID` : The agent ID.

---

## <a id="getaction"></a>getAction()

```java
int getAction();
```
Get the action code the agent is executing (compare with the `ACTION_*` constants).

**Parameters :**
- None

**Returns :**
- `int` : The action code.

---

## <a id="gettargetid"></a>getTargetID()

```java
EntityID getTargetID();
```
Get the entity the agent is acting on.

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
Get the reporting agent's own health, buriedness and damage — a buried ambulance is asking for help without saying so.

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
