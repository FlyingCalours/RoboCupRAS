# Pair.java

`rescuecore2.misc.Pair`

```
Pair(S first, T second);
```

**Pair&lt;S, T&gt;** is a tiny **two value container**. You meet it mainly as the return type of `StandardEntity.getLocation()` and `WorldInfo.getLocation()`, which give back an (X, Y) coordinate pair.

```java
Pair<Integer, Integer> location = worldInfo.getLocation(entity);
int x = location.first();
int y = location.second();
```

- **Methods**
  - [first()](#first)
  - [second()](#second)

## <a id="first"></a>first()

```java
S first();
```
Get the first element — the X coordinate in location pairs.

**Parameters :**
- None

**Returns :**
- `S` : The first value.

---

## <a id="second"></a>second()

```java
T second();
```
Get the second element — the Y coordinate in location pairs.

**Parameters :**
- None

**Returns :**
- `T` : The second value.

---
