# MessageCivilian.java

```
MessageCivilian(boolean isRadio, @Nonnull Civilian civilian);
MessageCivilian(boolean isRadio, StandardMessagePriority sendingPriority, @Nonnull Civilian civilian);
MessageCivilian(boolean isRadio, int from, int ttl, @Nonnull BitStreamReader bitStreamReader);
```

[Attributes Stored](#attributes-stored)

**MessageCivilian** reports a **civilian** the agent has seen : where they are, how hurt and how deeply buried. This is the most valuable message in the game — an ambulance can only rescue what somebody has reported.

```java
messageManager.addMessage(
    new MessageCivilian(true, StandardMessagePriority.HIGH, civilian));
```

- Who is it, and where ?
  - [getAgentID()](#getagentid)
  - [getPosition()](#getposition)

- How **bad** is their condition ?
  - [getHP()](#gethp)
  - [getBuriedness()](#getburiedness)
  - [getDamage()](#getdamage)

- Which fields are **actually filled** ?
  - [isHPDefined() / isBuriednessDefined() / isDamageDefined() / isPositionDefined()](#isdefined)

## <a id="attributes-stored"></a>Attributes Stored
1. `protected EntityID agentID` : ID of the reported civilian.
2. `protected EntityID humanPosition` : Area the civilian is in.
3. `protected int humanHP` : Remaining health points.
4. `protected int humanBuriedness` : How deeply buried (0 = free to load).
5. `protected int humanDamage` : Damage taken per tick.

## <a id="getagentid"></a>getAgentID()

```java
EntityID getAgentID();
```
Get the ID of the reported civilian.

**Parameters :**
- None

**Returns :**
- `EntityID` : The civilian ID.

---

## <a id="getposition"></a>getPosition()

```java
EntityID getPosition();
```
Get the area (Road or Building) where the civilian was seen.

**Parameters :**
- None

**Returns :**
- `EntityID` : The position entity ID.

---

## <a id="gethp"></a>getHP()

```java
int getHP();
```
Get the reported health points. Low HP plus high damage means the civilian dies soon — prioritise them.

**Parameters :**
- None

**Returns :**
- `int` : Health points.

---

## <a id="getburiedness"></a>getBuriedness()

```java
int getBuriedness();
```
Get the reported buriedness. Must reach `0` (through `ActionRescue`) before `ActionLoad` works.

**Parameters :**
- None

**Returns :**
- `int` : Buriedness value.

---

## <a id="getdamage"></a>getDamage()

```java
int getDamage();
```
Get the reported damage — HP lost per tick.

**Parameters :**
- None

**Returns :**
- `int` : Damage value.

---

## <a id="isdefined"></a>isHPDefined() / isBuriednessDefined() / isDamageDefined() / isPositionDefined()

```java
boolean isHPDefined();
boolean isBuriednessDefined();
boolean isDamageDefined();
boolean isPositionDefined();
```
Check whether the sender knew that property before you trust the getter.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` if the value is meaningful.

---
