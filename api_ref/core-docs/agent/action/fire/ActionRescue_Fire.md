# ActionRescue.java (fire)

`adf.core.agent.action.fire.ActionRescue`

```
ActionRescue(@Nonnull EntityID targetID);
ActionRescue(@Nonnull Human human);
```

[Attributes Stored](#attributes-stored)

**ActionRescue (fire)** lets a **Fire Brigade** dig a buried human out. Fire brigades can rescue but cannot load or transport, so this is normally used only when a civilian is buried close to the fire site and no ambulance is nearby.

> Identical API to the ambulance version — see [../ambulance/ActionRescue_Ambulance.md](ActionRescue_Ambulance.md). Import the class from the package matching your agent type.

- How do I **build** it ?
  - [ActionRescue(targetID)](#constructor-id)
  - [ActionRescue(human)](#constructor-human)

- Who am I **digging out** ?
  - [getTarget()](#gettarget)

## <a id="attributes-stored"></a>Attributes Stored
1. `protected EntityID target` : The ID of the buried human being rescued.

## <a id="constructor-id"></a>ActionRescue(targetID)

```java
ActionRescue(@Nonnull EntityID targetID);
```
Rescue the human with the given ID.

**Parameters :**
- `targetID` : `EntityID` of the buried human.

**Returns :**
- `ActionRescue` : The action object.

---

## <a id="constructor-human"></a>ActionRescue(human)

```java
ActionRescue(@Nonnull Human human);
```
Convenience constructor taking the `Human` entity directly.

**Parameters :**
- `human` : The buried `Human`.

**Returns :**
- `ActionRescue` : The action object.

---

## <a id="gettarget"></a>getTarget()

```java
EntityID getTarget();
```
Get the ID of the human being rescued.

**Parameters :**
- None

**Returns :**
- `EntityID` : The target human ID.

---
