# PoliceForce.java

`rescuecore2.standard.entities.PoliceForce`

```
PoliceForce(EntityID id);
PoliceForce(PoliceForce other);
```

**PoliceForce** is a `Human` — the platoon agent that clears blockades. It cannot rescue or extinguish, but without it the other agents cannot move.

It adds no properties of its own; everything comes from [Human.md](Human.md).

```java
Collection<StandardEntity> list =
        worldInfo.getEntitiesOfType(StandardEntityURN.POLICE_FORCE);
```

- **Inherited from Human**
  - `getPosition()`, `getX()`, `getY()`, `getHP()`, `getDamage()`, `getBuriedness()`, `getStamina()`, `getDirection()`, `getPositionHistory()`, `getTravelDistance()` and their `isXxxDefined()` companions

- **Own methods**
  - [getStandardURN()](#getstandardurn)

## <a id="getstandardurn"></a>getStandardURN()

```java
StandardEntityURN getStandardURN();
```
Get the entity type.

**Parameters :**
- None

**Returns :**
- `StandardEntityURN` : `POLICE_FORCE`.

---
