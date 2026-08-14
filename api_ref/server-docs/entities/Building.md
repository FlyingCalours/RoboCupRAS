# Building.java

`rescuecore2.standard.entities.Building`

```
Building(EntityID id);
Building(Building other);
```

**Building** is an `Area` that can **catch fire and collapse**. It is the fire brigade's target list and the main source of buried civilians. `Refuge`, `GasStation`, `FireStation`, `AmbulanceCentre` and `PoliceOffice` are all subclasses.

```java
Building b = (Building) worldInfo.getEntity(id);
if (b.isOnFire()) { ... }
```

- Is it **burning** ?
  - [isOnFire()](#isonfire)
  - [getFieryness()](#getfieryness)
  - [getFierynessEnum()](#getfierynessenum)
  - [getTemperature()](#gettemperature)
  - [getIgnition()](#getignition)

- How **damaged** is it ?
  - [getBrokenness()](#getbrokenness)

- How **big / flammable** is it ?
  - [getFloors()](#getfloors)
  - [getGroundArea()](#getgroundarea)
  - [getTotalArea()](#gettotalarea)
  - [getBuildingCode()](#getbuildingcode)
  - [getBuildingCodeEnum()](#getbuildingcodeenum)
  - [getBuildingAttributes()](#getbuildingattributes)
  - [getImportance()](#getimportance)
  - [getCapacity()](#getcapacity)

Every getter has a matching `setXxx()` and `isXxxDefined()`; always check `isXxxDefined()` on information that came from another agent.

## <a id="isonfire"></a>isOnFire()

```java
boolean isOnFire();
```
Check whether the building is currently burning (fieryness `HEATING`, `BURNING` or `INFERNO`).

**Parameters :**
- None

**Returns :**
- `boolean` : `true` when on fire.

---

## <a id="getfieryness"></a>getFieryness()

```java
int getFieryness();
```
Get the raw fire state value 0..8.

**Parameters :**
- None

**Returns :**
- `int` : Fieryness value.

---

## <a id="getfierynessenum"></a>getFierynessEnum()

```java
StandardEntityConstants.Fieryness getFierynessEnum();
```
Get the fire state as a readable enum (`UNBURNT`, `HEATING`, `BURNING`, `INFERNO`, `WATER_DAMAGE`, `MINOR_DAMAGE`, `MODERATE_DAMAGE`, `SEVERE_DAMAGE`, `BURNT_OUT`).

**Parameters :**
- None

**Returns :**
- `StandardEntityConstants.Fieryness` : The fire state.

---

## <a id="gettemperature"></a>getTemperature()

```java
int getTemperature();
```
Get the building temperature. A hot but unburnt building is about to ignite — good target for preventive extinguishing.

**Parameters :**
- None

**Returns :**
- `int` : Temperature.

---

## <a id="getignition"></a>getIgnition()

```java
boolean getIgnition();
```
Check the ignition flag set by the fire simulator.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` when ignited.

---

## <a id="getbrokenness"></a>getBrokenness()

```java
int getBrokenness();
```
Get the structural damage of the building — high brokenness means civilians inside are likely buried.

**Parameters :**
- None

**Returns :**
- `int` : Brokenness value.

---

## <a id="getfloors"></a>getFloors()

```java
int getFloors();
```
Get the number of floors.

**Parameters :**
- None

**Returns :**
- `int` : Floor count.

---

## <a id="getgroundarea"></a>getGroundArea()

```java
int getGroundArea();
```
Get the footprint area of the building.

**Parameters :**
- None

**Returns :**
- `int` : Ground area.

---

## <a id="gettotalarea"></a>getTotalArea()

```java
int getTotalArea();
```
Get the total floor area (ground area × floors). Bigger buildings burn longer and spread more fire.

**Parameters :**
- None

**Returns :**
- `int` : Total area.

---

## <a id="getbuildingcode"></a>getBuildingCode()

```java
int getBuildingCode();
```
Get the construction material code.

**Parameters :**
- None

**Returns :**
- `int` : Building code value.

---

## <a id="getbuildingcodeenum"></a>getBuildingCodeEnum()

```java
StandardEntityConstants.BuildingCode getBuildingCodeEnum();
```
Get the construction material as an enum (`WOOD`, `STEEL`, `CONCRETE`). Wooden buildings ignite fastest.

**Parameters :**
- None

**Returns :**
- `StandardEntityConstants.BuildingCode` : The material.

---

## <a id="getbuildingattributes"></a>getBuildingAttributes()

```java
int getBuildingAttributes();
```
Get the map specific attribute bit field.

**Parameters :**
- None

**Returns :**
- `int` : Attribute value.

---

## <a id="getimportance"></a>getImportance()

```java
int getImportance();
```
Get the importance weight of the building in the score function — higher means more worth saving.

**Parameters :**
- None

**Returns :**
- `int` : Importance value.

---

## <a id="getcapacity"></a>getCapacity()

```java
int getCapacity();
```
Get the building capacity value.

**Parameters :**
- None

**Returns :**
- `int` : Capacity.

---
