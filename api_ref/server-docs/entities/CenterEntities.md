# AmbulanceCentre.java / FireStation.java / PoliceOffice.java

`rescuecore2.standard.entities.AmbulanceCentre`
`rescuecore2.standard.entities.FireStation`
`rescuecore2.standard.entities.PoliceOffice`

```
AmbulanceCentre(EntityID id);
FireStation(EntityID id);
PoliceOffice(EntityID id);
```

**The three centre entities** are `Building` subclasses that host the coordinating agents (`TacticsAmbulanceCentre`, `TacticsFireStation`, `TacticsPoliceOffice`). They add **no properties of their own** — they exist so the world model can tell you where each headquarters is and whether it has been destroyed.

They matter to team code in two ways: a centre agent's `agentInfo.me()` is one of them, and a platoon can find its headquarters by listing them.

```java
Collection<StandardEntity> offices =
        worldInfo.getEntitiesOfType(StandardEntityURN.POLICE_OFFICE);
```

- **Inherited from Building / Area**
  - `isOnFire()`, `getFieryness()`, `getBrokenness()`, `getX()`, `getY()`, `getNeighbours()`, ...

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
- `StandardEntityURN` : `AMBULANCE_CENTRE`, `FIRE_STATION` or `POLICE_OFFICE`.

---

**Note :** a centre agent has no body on the map in the sense that it cannot move, clear, rescue or extinguish. `TacticsCenter.think()` therefore returns `void` — its only outputs are messages.
