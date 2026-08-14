# AmbulanceTeam.java

`rescuecore2.standard.entities.AmbulanceTeam`

```
AmbulanceTeam(EntityID id);
AmbulanceTeam(AmbulanceTeam other);
```

**AmbulanceTeam** is a `Human` — the platoon agent that rescues buried humans and transports civilians to refuges. Only ambulances can load and unload.

It adds no properties of its own; everything comes from [Human.md](Human.md).

```java
Collection<StandardEntity> list =
        worldInfo.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM);
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
- `StandardEntityURN` : `AMBULANCE_TEAM`.

---
