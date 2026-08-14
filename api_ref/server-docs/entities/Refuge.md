# Refuge.java

`rescuecore2.standard.entities.Refuge`

```
Refuge(EntityID id);
Refuge(Refuge other);
Refuge(Building other);
```

**Refuge** is a special `Building` that never burns and is the **destination of every transport task** : ambulances unload civilians there, fire brigades refill water there, and injured agents heal there. Listing them is normally the first thing an ambulance tactic does.

```java
Collection<StandardEntity> refuges =
        worldInfo.getEntitiesOfType(StandardEntityURN.REFUGE);
```

- How many civilians **fit** ?
  - [getBedCapacity()](#getbedcapacity)
  - [getOccupiedBeds()](#getoccupiedbeds)
  - [increaseOccupiedBeds()](#changebeds)
  - [decreaseOccupiedBeds()](#changebeds)
  - [getWaitingListSize()](#getwaitinglistsize)

- How fast can it **refill** ?
  - [getRefillCapacity()](#getrefillcapacity)

- **Inherited from Building / Area**
  - all building and area getters

## <a id="getbedcapacity"></a>getBedCapacity()

```java
int getBedCapacity();
```
Get how many civilians this refuge can treat at once. A full refuge is a bad unload target — check before driving across the map.

**Parameters :**
- None

**Returns :**
- `int` : Bed capacity.

---

## <a id="getoccupiedbeds"></a>getOccupiedBeds()

```java
int getOccupiedBeds();
void setOccupiedBeds(int capacity);
boolean isOccupiedBedsDefined();
```
Get (or set) how many beds are currently in use.

**Parameters :**
- `capacity` : (setter) New occupied bed count.

**Returns :**
- `int` : Occupied beds.

---

## <a id="changebeds"></a>increaseOccupiedBeds() / decreaseOccupiedBeds()

```java
int increaseOccupiedBeds();
int decreaseOccupiedBeds();
```
Adjust the occupied bed count by one.

**Parameters :**
- None

**Returns :**
- `int` : The new occupied bed count.

---

## <a id="getwaitinglistsize"></a>getWaitingListSize()

```java
int getWaitingListSize();
```
Get how many civilians are queued for treatment.

**Parameters :**
- None

**Returns :**
- `int` : Waiting list size.

---

## <a id="getrefillcapacity"></a>getRefillCapacity()

```java
int getRefillCapacity();
```
Get how much water this refuge can supply per tick to fire brigades.

**Parameters :**
- None

**Returns :**
- `int` : Refill capacity.

---
