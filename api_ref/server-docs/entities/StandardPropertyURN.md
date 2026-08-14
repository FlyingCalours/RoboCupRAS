# StandardPropertyURN.java

`rescuecore2.standard.entities.StandardPropertyURN`

```
enum StandardPropertyURN
```

**StandardPropertyURN** identifies **one property of an entity**. You need it when working with raw `Property` objects — mainly when inspecting a `ChangeSet` to find out *what* changed rather than just *which entity* changed.

```java
Property p = changed.getChangedProperty(id, StandardPropertyURN.FIERYNESS.getURNId());
if (p != null && p.isDefined()) { ... }
```

- **Common values**

| Group | Values |
|---|---|
| Position | `X`, `Y`, `POSITION`, `POSITION_HISTORY`, `DIRECTION`, `TRAVEL_DISTANCE` |
| Human condition | `HP`, `DAMAGE`, `BURIEDNESS`, `STAMINA` |
| Fire | `FIERYNESS`, `TEMPERATURE`, `IGNITION`, `BROKENNESS` |
| Building | `FLOORS`, `BUILDING_CODE`, `BUILDING_ATTRIBUTES`, `BUILDING_AREA_GROUND`, `BUILDING_AREA_TOTAL`, `IMPORTANCE`, `CAPACITY` |
| Road / blockade | `BLOCKADES`, `REPAIR_COST`, `APEXES`, `EDGES` |
| Refuge | `BED_CAPACITY`, `OCCUPIED_BEDS`, `REFILL_CAPACITY`, `WAITING_LIST_SIZE` |
| Fire brigade | `WATER_QUANTITY` |
| World | `START_TIME`, `LONGITUDE`, `LATITUDE`, `WIND_FORCE`, `WIND_DIRECTION` |

- **Methods**
  - [getURNId()](#geturnid)
  - [getURNStr()](#geturnstr)

## <a id="geturnid"></a>getURNId()

```java
int getURNId();
```
Get the numeric property URN — the form taken by `Entity.getProperty(int)` and `ChangeSet.getChangedProperty(id, int)`.

**Parameters :**
- None

**Returns :**
- `int` : Numeric property URN.

---

## <a id="geturnstr"></a>getURNStr()

```java
String getURNStr();
```
Get the textual property URN.

**Parameters :**
- None

**Returns :**
- `String` : Textual URN.

---
