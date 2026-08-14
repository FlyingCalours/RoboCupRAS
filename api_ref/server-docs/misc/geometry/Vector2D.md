# Vector2D.java

`rescuecore2.misc.geometry.Vector2D`

```
Vector2D(double dx, double dy);
```

**Vector2D** is a **direction and length**. Police tactics use it constantly with `ActionClear(AgentInfo, Vector2D)`: build a vector towards the blockade, scale it to the clearing distance, and clear along it.

```java
Vector2D v = new Vector2D(targetX - agentX, targetY - agentY)
                 .normalised()
                 .scale(scenarioInfo.getClearRepairDistance());
return new ActionClear(agentInfo, v);
```

- **Methods**
  - [getX() / getY()](#coords)
  - [getLength()](#getlength)
  - [normalised()](#normalised)
  - [scale()](#scale)
  - [add()](#add)
  - [negate()](#negate)
  - [getNormal()](#getnormal)
  - [dot()](#dot)
  - [cross()](#cross)

## <a id="coords"></a>getX() / getY()

```java
double getX();
double getY();
```
Get the components of the vector.

**Parameters :**
- None

**Returns :**
- `double` : The component.

---

## <a id="getlength"></a>getLength()

```java
double getLength();
```
Get the length (magnitude) of the vector.

**Parameters :**
- None

**Returns :**
- `double` : The length.

---

## <a id="normalised"></a>normalised()

```java
Vector2D normalised();
```
Get a vector with the same direction and length 1 — combine with `scale()` to get an exact distance.

**Parameters :**
- None

**Returns :**
- `Vector2D` : The unit vector.

---

## <a id="scale"></a>scale()

```java
Vector2D scale(double amount);
```
Get this vector multiplied by a factor.

**Parameters :**
- `amount` : The scale factor.

**Returns :**
- `Vector2D` : The scaled vector.

---

## <a id="add"></a>add()

```java
Vector2D add(Vector2D v);
```
Get the sum of two vectors.

**Parameters :**
- `v` : The vector to add.

**Returns :**
- `Vector2D` : The sum.

---

## <a id="negate"></a>negate()

```java
Vector2D negate();
```
Get the opposite vector — handy for backing away from a target.

**Parameters :**
- None

**Returns :**
- `Vector2D` : The negated vector.

---

## <a id="getnormal"></a>getNormal()

```java
Vector2D getNormal();
```
Get a vector perpendicular to this one — useful for sidestepping a blockade.

**Parameters :**
- None

**Returns :**
- `Vector2D` : The normal vector.

---

## <a id="dot"></a>dot()

```java
double dot(Vector2D v);
```
Dot product — positive when the two vectors point roughly the same way.

**Parameters :**
- `v` : The other vector.

**Returns :**
- `double` : The dot product.

---

## <a id="cross"></a>cross()

```java
double cross(Vector2D v);
```
Cross product — its sign tells you whether the turn is left or right.

**Parameters :**
- `v` : The other vector.

**Returns :**
- `double` : The cross product.

---
