# MessageBuilding.java

```
MessageBuilding(boolean isRadio, @Nonnull Building building);
MessageBuilding(boolean isRadio, StandardMessagePriority sendingPriority, @Nonnull Building building);
MessageBuilding(boolean isRadio, int from, int ttl, @Nonnull BitStreamReader bitStreamReader);
```

[Attributes Stored](#attributes-stored)

**MessageBuilding** shares what an agent saw about a **building** : how badly it is burning and how damaged it is. Fire brigades broadcast it constantly so the fire station can build a picture of the fire fronts.

```java
messageManager.addMessage(new MessageBuilding(true, building));
```

- What building is it ?
  - [getBuildingID()](#getbuildingid)

- What is its **state** ?
  - [getBrokenness()](#getbrokenness)
  - [getFieryness()](#getfieryness)
  - [getTemperature()](#gettemperature)

- Which fields are **actually filled** ?
  - [isBrokennessDefined()](#isdefined)
  - [isFierynessDefined()](#isdefined)
  - [isTemperatureDefined()](#isdefined)

## <a id="attributes-stored"></a>Attributes Stored
1. `protected EntityID buildingID` : The reported building.
2. `protected int buildingBrokenness` : Structural damage.
3. `protected int buildingFieryness` : Fire state (see `StandardEntityConstants.Fieryness`).
4. `protected int buildingTemperature` : Temperature of the building.

## <a id="getbuildingid"></a>getBuildingID()

```java
EntityID getBuildingID();
```
Get the ID of the reported building.

**Parameters :**
- None

**Returns :**
- `EntityID` : The building ID.

---

## <a id="getbrokenness"></a>getBrokenness()

```java
int getBrokenness();
```
Get the reported brokenness value.

**Parameters :**
- None

**Returns :**
- `int` : Brokenness (check `isBrokennessDefined()` first).

---

## <a id="getfieryness"></a>getFieryness()

```java
int getFieryness();
```
Get the reported fieryness value (0 = unburnt, 1..3 = burning, 8 = burnt out).

**Parameters :**
- None

**Returns :**
- `int` : Fieryness.

---

## <a id="gettemperature"></a>getTemperature()

```java
int getTemperature();
```
Get the reported temperature.

**Parameters :**
- None

**Returns :**
- `int` : Temperature.

---

## <a id="isdefined"></a>isBrokennessDefined() / isFierynessDefined() / isTemperatureDefined()

```java
boolean isBrokennessDefined();
boolean isFierynessDefined();
boolean isTemperatureDefined();
```
Check whether the sender actually knew that property. Always test before using the matching getter, otherwise you may store a meaningless `0` into the world model.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` if the value is meaningful.

---
