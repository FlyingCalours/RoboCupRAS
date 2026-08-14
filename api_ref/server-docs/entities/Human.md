# Human.java

`rescuecore2.standard.entities.Human`

```
abstract class Human extends StandardEntity
```

**Human** is the parent of every **moving actor** : `Civilian`, `AmbulanceTeam`, `FireBrigade`, `PoliceForce`. It carries the health and position properties that all rescue decisions are built on.

```java
Human human = (Human) worldInfo.getEntity(id);
if (human.isHPDefined() && human.getHP() > 0
        && human.isBuriednessDefined() && human.getBuriedness() > 0) {
    // needs digging out
}
```

- Where is it ?
  - [getPosition()](#getposition)
  - [getX()](#getx)
  - [getY()](#gety)
  - [setPosition()](#setposition)
  - [getPositionHistory()](#getpositionhistory)
  - [getDirection()](#getdirection)
  - [getTravelDistance()](#gettraveldistance)

- How is it **doing** ?
  - [getHP()](#gethp)
  - [getDamage()](#getdamage)
  - [getBuriedness()](#getburiedness)
  - [getStamina()](#getstamina)

Every getter has a matching `setXxx()` and `isXxxDefined()` — always check `isXxxDefined()` on entities you learned about through messages.

## <a id="getposition"></a>getPosition()

```java
EntityID getPosition();
```
Get the entity the human is in. Usually an `Area`, but for a loaded civilian it is the **ambulance's ID** — that is how you detect somebody is already being carried.

**Parameters :**
- None

**Returns :**
- `EntityID` : The position entity ID.

---

## <a id="getx"></a>getX()

```java
int getX();
```
Get the exact X coordinate.

**Parameters :**
- None

**Returns :**
- `int` : X coordinate.

---

## <a id="gety"></a>getY()

```java
int getY();
```
Get the exact Y coordinate.

**Parameters :**
- None

**Returns :**
- `int` : Y coordinate.

---

## <a id="setposition"></a>setPosition()

```java
void setPosition(EntityID position);
void setPosition(EntityID newPosition, int newX, int newY);
```
Set the position (and optionally the exact coordinates). Used when reflecting a received message into the world model.

**Parameters :**
- `position` / `newPosition` : The containing entity.
- `newX`, `newY` : Exact coordinates.

**Returns :**
- `void`

---

## <a id="getpositionhistory"></a>getPositionHistory()

```java
int[] getPositionHistory();
```
Get the recent movement trace as `x0, y0, x1, y1, ...`. A trace that does not change means the agent is stuck behind a blockade.

**Parameters :**
- None

**Returns :**
- `int[]` : Coordinate history.

---

## <a id="getdirection"></a>getDirection()

```java
int getDirection();
```
Get the facing direction of the human.

**Parameters :**
- None

**Returns :**
- `int` : Direction in degrees.

---

## <a id="gettraveldistance"></a>getTravelDistance()

```java
int getTravelDistance();
```
Get the distance travelled during the last move.

**Parameters :**
- None

**Returns :**
- `int` : Distance in map units.

---

## <a id="gethp"></a>getHP()

```java
int getHP();
```
Get the remaining health points. `0` means dead — never allocate rescuers to a dead civilian.

**Parameters :**
- None

**Returns :**
- `int` : Health points.

---

## <a id="getdamage"></a>getDamage()

```java
int getDamage();
```
Get the damage per tick. `HP / damage` is the number of ticks left to live — the standard way to prioritise victims.

**Parameters :**
- None

**Returns :**
- `int` : Damage value.

---

## <a id="getburiedness"></a>getBuriedness()

```java
int getBuriedness();
```
Get how deeply the human is buried. Greater than `0` means they cannot move and must be dug out with `ActionRescue` before `ActionLoad` will work.

**Parameters :**
- None

**Returns :**
- `int` : Buriedness value.

---

## <a id="getstamina"></a>getStamina()

```java
int getStamina();
```
Get the remaining stamina.

**Parameters :**
- None

**Returns :**
- `int` : Stamina value.

---
