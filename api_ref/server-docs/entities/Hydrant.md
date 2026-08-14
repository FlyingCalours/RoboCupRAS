# Hydrant.java

`rescuecore2.standard.entities.Hydrant`

```
Hydrant(EntityID id);
Hydrant(Hydrant other);
Hydrant(Road other);
```

**Hydrant** is a `Road` where a **fire brigade can refill water** without going back to a refuge. Hydrants are usually much closer to the fire, so a good fire tactic prefers them; the refill rate is `ScenarioInfo.getFireHydrantRefillRate()`.

```java
Collection<StandardEntity> hydrants =
        worldInfo.getEntitiesOfType(StandardEntityURN.HYDRANT);
```

- **Inherited from Road / Area**
  - `getX()`, `getY()`, `getNeighbours()`, `getEdges()`, `getBlockades()`, ...

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
- `StandardEntityURN` : `HYDRANT`.

---
