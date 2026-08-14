# ActionExtinguish.java

```
ActionExtinguish(@Nonnull EntityID targetID, int maxPower);
ActionExtinguish(@Nonnull Building building, int maxPower);
```

[Attributes Stored](#attributes-stored)

**ActionExtinguish** makes a **Fire Brigade** spray water on a burning building. The agent must be within `ScenarioInfo.getFireExtinguishMaxDistance()` of the target and must still have water (`AgentInfo.getWater()`); the usual power value is `ScenarioInfo.getFireExtinguishMaxSum()`.

- How do I **build** it ?
  - [ActionExtinguish(targetID, maxPower)](#constructor-id)
  - [ActionExtinguish(building, maxPower)](#constructor-building)

- What is **inside** it ?
  - [getTarget()](#gettarget)
  - [getPower()](#getpower)

## <a id="attributes-stored"></a>Attributes Stored
1. `protected EntityID target` : ID of the burning building to extinguish.
2. `private int power` : Amount of water (litres) to spray this tick.

## <a id="constructor-id"></a>ActionExtinguish(targetID, maxPower)

```java
ActionExtinguish(@Nonnull EntityID targetID, int maxPower);
```
Spray the building with the given ID.

**Parameters :**
- `targetID` : `EntityID` of the burning building.
- `maxPower` : Water quantity to use this tick.

**Returns :**
- `ActionExtinguish` : The action object.

---

## <a id="constructor-building"></a>ActionExtinguish(building, maxPower)

```java
ActionExtinguish(@Nonnull Building building, int maxPower);
```
Convenience constructor taking the `Building` entity directly.

**Parameters :**
- `building` : The burning `Building`.
- `maxPower` : Water quantity to use this tick.

**Returns :**
- `ActionExtinguish` : The action object.

---

## <a id="gettarget"></a>getTarget()

```java
EntityID getTarget();
```
Get the ID of the building being extinguished.

**Parameters :**
- None

**Returns :**
- `EntityID` : The target building ID.

---

## <a id="getpower"></a>getPower()

```java
int getPower();
```
Get the water quantity this action will spray.

**Parameters :**
- None

**Returns :**
- `int` : Water quantity in litres.

---
