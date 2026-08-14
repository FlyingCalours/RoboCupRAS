# FireBrigade.java

`rescuecore2.standard.entities.FireBrigade`

```
FireBrigade(EntityID id);
FireBrigade(FireBrigade other);
```

**FireBrigade** is a `Human` platoon agent that **extinguishes fires**. It is the only entity type with a water tank, which is the one property it adds on top of `Human`.

```java
FireBrigade me = (FireBrigade) agentInfo.me();
if (me.isWaterDefined() && me.getWater() == 0) {
    // must refill before doing anything useful
}
```

- **Inherited from Human**
  - `getPosition()`, `getX()`, `getY()`, `getHP()`, `getDamage()`, `getBuriedness()`, ...

- **Own methods**
  - [getWater()](#getwater)
  - [isWaterDefined()](#iswaterdefined)
  - [setWater()](#setwater)
  - [getStandardURN()](#getstandardurn)

## <a id="getwater"></a>getWater()

```java
int getWater();
```
Get the remaining water in the tank. Compare against `ScenarioInfo.getFireExtinguishMaxSum()` to know how many more extinguish actions are possible.

**Parameters :**
- None

**Returns :**
- `int` : Water quantity in litres.

---

## <a id="iswaterdefined"></a>isWaterDefined()

```java
boolean isWaterDefined();
```
Check whether the water property is known (it is always known for your own agent, and only known for others if they reported it).

**Parameters :**
- None

**Returns :**
- `boolean` : `true` if defined.

---

## <a id="setwater"></a>setWater()

```java
void setWater(int water);
```
Set the water quantity — used when reflecting a `MessageFireBrigade` into the world model.

**Parameters :**
- `water` : New water quantity.

**Returns :**
- `void`

---

## <a id="getstandardurn"></a>getStandardURN()

```java
StandardEntityURN getStandardURN();
```
Get the entity type.

**Parameters :**
- None

**Returns :**
- `StandardEntityURN` : `FIRE_BRIGADE`.

---
