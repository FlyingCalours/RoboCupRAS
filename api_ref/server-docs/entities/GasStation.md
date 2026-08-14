# GasStation.java

`rescuecore2.standard.entities.GasStation`

```
GasStation(EntityID entityID);
GasStation(Building other);
```

**GasStation** is a `Building` that **explodes** when it catches fire, igniting everything around it. Treating gas stations as top priority fire targets (and keeping other agents away from them) is a standard tactic.

```java
Collection<StandardEntity> gasStations =
        worldInfo.getEntitiesOfType(StandardEntityURN.GAS_STATION);
```

- **Inherited from Building / Area**
  - all building and area getters (`isOnFire()`, `getFieryness()`, ...)

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
- `StandardEntityURN` : `GAS_STATION`.

---
