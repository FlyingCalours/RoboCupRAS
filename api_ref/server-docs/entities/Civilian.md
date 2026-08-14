# Civilian.java

`rescuecore2.standard.entities.Civilian`

```
Civilian(EntityID id);
Civilian(Civilian other);
```

**Civilian** is a `Human` — a victim to be found, dug out, loaded and carried to a refuge. Civilians are the score: everything the team does is ultimately about keeping them alive.

It adds no properties of its own; everything comes from [Human.md](Human.md).

```java
Collection<StandardEntity> list =
        worldInfo.getEntitiesOfType(StandardEntityURN.CIVILIAN);
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
- `StandardEntityURN` : `CIVILIAN`.

---
