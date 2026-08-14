# ActionRescue.java (ambulance)

`adf.core.agent.action.ambulance.ActionRescue`

```
ActionRescue(@Nonnull EntityID targetID);
ActionRescue(@Nonnull Human human);
```

[Attributes Stored](#attributes-stored)

**ActionRescue (ambulance)** makes an **Ambulance Team** dig a buried human out of the rubble. One tick of rescue removes a fixed amount of `buriedness`; repeat until `buriedness == 0`, then use `ActionLoad`. The agent must already be in the same area as the target.

> There is a second class with the same name in `adf.core.agent.action.fire` — see [ActionRescue_Fire.md](ActionRescue_Fire.md). They behave the same way but belong to different agent types.

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
- `human` : The buried `Human` (usually a `Civilian`).

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
