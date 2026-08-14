# StandardEntityURN.java

`rescuecore2.standard.entities.StandardEntityURN`

```
enum StandardEntityURN
```

**StandardEntityURN** is the **type tag of every map entity**. You use it to ask the world model for all entities of a kind, and to branch on what an entity is.

```java
Collection<StandardEntity> refuges =
        worldInfo.getEntitiesOfType(StandardEntityURN.REFUGE);
```

- **Values**

| Value | Meaning |
|---|---|
| `WORLD` | The world entity itself. |
| `ROAD` | A road segment. |
| `BLOCKADE` | Rubble blocking part of a road. |
| `BUILDING` | An ordinary building. |
| `REFUGE` | Safe building : heal civilians, unload, refill water. |
| `HYDRANT` | Road where a fire brigade can refill. |
| `GAS_STATION` | Building that explodes when it catches fire. |
| `FIRE_STATION` | Fire brigade centre. |
| `AMBULANCE_CENTRE` | Ambulance centre. |
| `POLICE_OFFICE` | Police centre. |
| `CIVILIAN` | A civilian to be rescued. |
| `FIRE_BRIGADE` | Fire brigade platoon agent. |
| `AMBULANCE_TEAM` | Ambulance team platoon agent. |
| `POLICE_FORCE` | Police force platoon agent. |

- **Methods**
  - [getURNId()](#geturnid)
  - [getURNStr()](#geturnstr)
  - [fromInt()](#fromint)
  - [fromString()](#fromstring)

## <a id="geturnid"></a>getURNId()

```java
int getURNId();
```
Get the numeric id of this URN.

**Parameters :**
- None

**Returns :**
- `int` : Numeric URN id.

---

## <a id="geturnstr"></a>getURNStr()

```java
String getURNStr();
```
Get the textual URN, e.g. `...:entity:road`.

**Parameters :**
- None

**Returns :**
- `String` : Textual URN.

---

## <a id="fromint"></a>fromInt()

```java
static StandardEntityURN fromInt(int urn);
```
Convert a numeric URN back into the enum value.

**Parameters :**
- `urn` : Numeric URN id.

**Returns :**
- `StandardEntityURN` : The matching value.

---

## <a id="fromstring"></a>fromString()

```java
static StandardEntityURN fromString(String urn);
```
Convert a textual URN back into the enum value.

**Parameters :**
- `urn` : Textual URN.

**Returns :**
- `StandardEntityURN` : The matching value.

---
