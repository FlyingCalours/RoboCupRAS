# ActionLoad.java

```
ActionLoad(@Nonnull EntityID targetID);
ActionLoad(@Nonnull Civilian civilian);
```

[Attributes Stored](#attributes-stored)

**ActionLoad** makes an **Ambulance Team** pick up a civilian standing in the same area. The civilian must have `buriedness == 0` (already dug out) and the ambulance must not already carry someone — check with `AgentInfo.someoneOnBoard()`.

- How do I **build** it ?
  - [ActionLoad(targetID)](#constructor-id)
  - [ActionLoad(civilian)](#constructor-civilian)

- Who am I **loading** ?
  - [getTarget()](#gettarget)

## <a id="attributes-stored"></a>Attributes Stored
1. `protected EntityID target` : The ID of the civilian to load.

## <a id="constructor-id"></a>ActionLoad(targetID)

```java
ActionLoad(@Nonnull EntityID targetID);
```
Load the civilian with the given ID.

**Parameters :**
- `targetID` : `EntityID` of the civilian.

**Returns :**
- `ActionLoad` : The action object.

---

## <a id="constructor-civilian"></a>ActionLoad(civilian)

```java
ActionLoad(@Nonnull Civilian civilian);
```
Convenience constructor taking the `Civilian` entity directly.

**Parameters :**
- `civilian` : The `Civilian` object to load.

**Returns :**
- `ActionLoad` : The action object.

---

## <a id="gettarget"></a>getTarget()

```java
EntityID getTarget();
```
Get the ID of the civilian this action will load.

**Parameters :**
- None

**Returns :**
- `EntityID` : The target civilian ID.

---
