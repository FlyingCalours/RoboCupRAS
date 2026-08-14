# ActionClear.java

```
ActionClear(@Nonnull EntityID targetID);
ActionClear(@Nonnull Blockade blockade);
ActionClear(@Nonnull AgentInfo agent, @Nonnull Vector2D vector);
ActionClear(int destX, int destY);
ActionClear(int destX, int destY, @Nonnull Blockade blockade);
```

[Attributes Stored](#attributes-stored)

**ActionClear** makes a **Police Force** remove blockades. It supports two server protocols:

- **old function** (`AKClear`) : target a whole `Blockade` entity by ID.
- **new function** (`AKClearArea`) : cut a corridor towards a point (X,Y) — this is the one used by modern maps and the one `getUseOldFunction()` reports as `false`.

The agent must be within `ScenarioInfo.getClearRepairDistance()` of the point it wants to clear.

- How do I **build** it ?
  - [ActionClear(targetID)](#constructor-id)
  - [ActionClear(blockade)](#constructor-blockade)
  - [ActionClear(agent, vector)](#constructor-vector)
  - [ActionClear(destX, destY)](#constructor-point)
  - [ActionClear(destX, destY, blockade)](#constructor-point-blockade)

- What is **inside** it ?
  - [getUseOldFunction()](#getuseoldfunction)
  - [getTarget()](#gettarget)
  - [getPosX()](#getposx)
  - [getPosY()](#getposy)

## <a id="attributes-stored"></a>Attributes Stored
1. `protected EntityID target` : ID of the blockade to clear (may be `null` in point mode).
2. `private boolean useOldFunction` : `true` when the entity based `AKClear` protocol is used.
3. `private int posX` : X coordinate of the clearing direction/point.
4. `private int posY` : Y coordinate of the clearing direction/point.

## <a id="constructor-id"></a>ActionClear(targetID)

```java
ActionClear(@Nonnull EntityID targetID);
```
Clear a whole blockade by ID (old function, `useOldFunction = true`).

**Parameters :**
- `targetID` : `EntityID` of the blockade.

**Returns :**
- `ActionClear` : The action object.

---

## <a id="constructor-blockade"></a>ActionClear(blockade)

```java
ActionClear(@Nonnull Blockade blockade);
```
Same as above, taking the `Blockade` entity directly.

**Parameters :**
- `blockade` : The `Blockade` to clear.

**Returns :**
- `ActionClear` : The action object.

---

## <a id="constructor-vector"></a>ActionClear(agent, vector)

```java
ActionClear(@Nonnull AgentInfo agent, @Nonnull Vector2D vector);
```
Clear towards a direction **relative to the agent**: the destination point is `agent position + vector`. Handy for clearing straight ahead along a road.

**Parameters :**
- `agent` : The `AgentInfo` of the police force (used to read its X/Y).
- `vector` : A `rescuecore2.misc.geometry.Vector2D` offset from the agent.

**Returns :**
- `ActionClear` : The action object.

---

## <a id="constructor-point"></a>ActionClear(destX, destY)

```java
ActionClear(int destX, int destY);
```
Clear a corridor towards an absolute map point (new function, `useOldFunction = false`).

**Parameters :**
- `destX` : X coordinate to clear towards.
- `destY` : Y coordinate to clear towards.

**Returns :**
- `ActionClear` : The action object.

---

## <a id="constructor-point-blockade"></a>ActionClear(destX, destY, blockade)

```java
ActionClear(int destX, int destY, @Nonnull Blockade blockade);
```
Clear towards a point and also record which blockade is being attacked (useful for reporting/target management).

**Parameters :**
- `destX` : X coordinate to clear towards.
- `destY` : Y coordinate to clear towards.
- `blockade` : The `Blockade` associated with this clearing.

**Returns :**
- `ActionClear` : The action object.

---

## <a id="getuseoldfunction"></a>getUseOldFunction()

```java
boolean getUseOldFunction();
```
Check which server protocol this action will use.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` for entity based `AKClear`, `false` for point based `AKClearArea`.

---

## <a id="gettarget"></a>getTarget()

```java
EntityID getTarget();
```
Get the blockade ID associated with this action.

**Parameters :**
- None

**Returns :**
- `EntityID` : The blockade ID, or `null` if only a point was given.

---

## <a id="getposx"></a>getPosX()

```java
int getPosX();
```
Get the X coordinate this action clears towards.

**Parameters :**
- None

**Returns :**
- `int` : X coordinate (meaningless when `getUseOldFunction()` is `true`).

---

## <a id="getposy"></a>getPosY()

```java
int getPosY();
```
Get the Y coordinate this action clears towards.

**Parameters :**
- None

**Returns :**
- `int` : Y coordinate (meaningless when `getUseOldFunction()` is `true`).

---
