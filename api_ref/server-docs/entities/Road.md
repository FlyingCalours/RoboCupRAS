# Road.java

`rescuecore2.standard.entities.Road`

```
Road(EntityID id);
Road(Road other);
```

**Road** is an `Area` that agents travel along. It adds no properties of its own — everything useful comes from [Area](Area.md) : neighbours, edges, and above all `getBlockades()`.

For a police force the road is the workspace :

```java
Road road = (Road) worldInfo.getEntity(id);
if (road.isBlockadesDefined() && !road.getBlockades().isEmpty()) {
    // this road needs clearing
}
```

`Hydrant` is a subclass of `Road` — a road where fire brigades can refill water.

- **Inherited from Area**
  - `getX()`, `getY()`, `getNeighbours()`, `getEdges()`, `getEdgeTo()`, `getBlockades()`, `isBlockadesDefined()`, `getApexList()`, `getShape()`

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
- `StandardEntityURN` : `ROAD`.

---
