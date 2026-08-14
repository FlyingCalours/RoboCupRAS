# StandardEntity.java

`rescuecore2.standard.entities.StandardEntity`

```
abstract class StandardEntity extends AbstractEntity
```

**StandardEntity** is the **common parent of every object on the map** : `Area` (→ `Road`, `Building`), `Human` (→ `Civilian`, `AmbulanceTeam`, `FireBrigade`, `PoliceForce`), `Blockade`, and the centre buildings. Everything `WorldInfo.getEntity()` returns is one of these, so you almost always cast the result.

```java
StandardEntity entity = worldInfo.getEntity(id);
if (entity instanceof Building) { ... }
```

- Who is this entity ?
  - [getID()](#getid)
  - [getStandardURN()](#getstandardurn)
  - [getURN()](#geturn)

- Where is it ?
  - [getLocation()](#getlocation)

- **Inherited from AbstractEntity**
  - [getProperties() / getProperty()](#properties)
  - [copy()](#copy)
  - [getFullDescription()](#getfulldescription)

## <a id="getid"></a>getID()

```java
EntityID getID();
```
Get the unique ID of this entity.

**Parameters :**
- None

**Returns :**
- `EntityID` : The entity ID.

---

## <a id="getstandardurn"></a>getStandardURN()

```java
abstract StandardEntityURN getStandardURN();
```
Get the type of this entity as an enum — the cleanest way to branch on entity type.

```java
switch (entity.getStandardURN()) {
    case ROAD:     ... break;
    case BUILDING: ... break;
}
```

**Parameters :**
- None

**Returns :**
- `StandardEntityURN` : The entity type.

---

## <a id="geturn"></a>getURN()

```java
final int getURN();
```
Get the numeric URN id of this entity type.

**Parameters :**
- None

**Returns :**
- `int` : Numeric type id.

---

## <a id="getlocation"></a>getLocation()

```java
Pair<Integer, Integer> getLocation(WorldModel<? extends StandardEntity> world);
```
Get the (X, Y) position of this entity. For a human inside a building or a vehicle, this resolves through to the container's coordinates.

**Parameters :**
- `world` : The world model used to resolve nested positions.

**Returns :**
- `Pair<Integer, Integer>` : `first()` = X, `second()` = Y, or `null` if unknown.

---

## <a id="properties"></a>getProperties() / getProperty()

```java
Set<Property> getProperties();
Property getProperty(int urn);
```
Low level access to the raw property objects of the entity. Prefer the typed getters (`getHP()`, `getFieryness()`, ...) in normal code.

**Parameters :**
- `urn` : Numeric property URN (see `StandardPropertyURN`).

**Returns :**
- `Set<Property>` / `Property` : The properties.

---

## <a id="copy"></a>copy()

```java
Entity copy();
```
Create an independent copy of this entity — useful when you want to remember an old state.

**Parameters :**
- None

**Returns :**
- `Entity` : The copy.

---

## <a id="getfulldescription"></a>getFullDescription()

```java
String getFullDescription();
```
Human readable dump of the entity and all its properties. Very handy for debugging with `Logger.debug(...)`.

**Parameters :**
- None

**Returns :**
- `String` : The description.

---
