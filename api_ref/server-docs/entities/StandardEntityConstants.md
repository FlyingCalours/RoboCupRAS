# StandardEntityConstants.java

`rescuecore2.standard.entities.StandardEntityConstants`

```
final class StandardEntityConstants   // holder of enums, never instantiated
```

**StandardEntityConstants** holds the **readable enums for building properties**. Use them instead of raw integers so your fire logic stays understandable.

```java
if (building.getFierynessEnum() == StandardEntityConstants.Fieryness.INFERNO) { ... }
```

## <a id="fieryness"></a>Fieryness

```java
enum Fieryness {
    UNBURNT, HEATING, BURNING, INFERNO,
    WATER_DAMAGE, MINOR_DAMAGE, MODERATE_DAMAGE, SEVERE_DAMAGE, BURNT_OUT
}
```

| Value | Meaning |
|---|---|
| `UNBURNT` | Not burnt at all. |
| `HEATING` | Starting to burn — cheapest moment to stop the fire. |
| `BURNING` | Burning properly. |
| `INFERNO` | Burning fiercely; hard to extinguish and spreads to neighbours. |
| `WATER_DAMAGE` | Never burnt, but wet from spraying. |
| `MINOR_DAMAGE` | Extinguished, small damage. |
| `MODERATE_DAMAGE` | Extinguished, medium damage. |
| `SEVERE_DAMAGE` | Extinguished, heavy damage. |
| `BURNT_OUT` | Completely destroyed — no point spraying it. |

---

## <a id="buildingcode"></a>BuildingCode

```java
enum BuildingCode { WOOD, STEEL, CONCRETE }
```

| Value | Meaning |
|---|---|
| `WOOD` | Wooden construction — ignites fastest, spreads fire fastest. |
| `STEEL` | Steel frame construction. |
| `CONCRETE` | Reinforced concrete — most fire resistant. |

---
