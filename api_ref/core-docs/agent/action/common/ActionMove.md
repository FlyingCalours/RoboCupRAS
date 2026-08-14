# ActionMove.java

```
ActionMove(@Nonnull List<EntityID> movePath);
ActionMove(@Nonnull List<EntityID> movePath, int destinationX, int destinationY);
```

[Attributes Stored](#attributes-stored)

**ActionMove** tells the agent to **walk along a path** of areas. It is the most used action of all agent types, and is normally built from the result of `PathPlanning.getResult()`.

- How do I **build** it ?
  - [ActionMove(path)](#constructor-path)
  - [ActionMove(path, x, y)](#constructor-position)

- What is **inside** it ?
  - [getPath()](#getpath)
  - [getUsePosition()](#getuseposition)
  - [getPosX()](#getposx)
  - [getPosY()](#getposy)

## <a id="attributes-stored"></a>Attributes Stored
1. `private List<EntityID> path` : Ordered list of areas (Road/Building) to walk through.
2. `private boolean usePosition` : `true` when an exact destination point (X,Y) was given.
3. `private int posX` : Exact destination X coordinate inside the last area.
4. `private int posY` : Exact destination Y coordinate inside the last area.

## <a id="constructor-path"></a>ActionMove(path)

```java
ActionMove(@Nonnull List<EntityID> movePath);
```
Move along the given path and stop at the centre of the last area.

**Parameters :**
- `movePath` : List of area `EntityID`s from the current position to the destination.

**Returns :**
- `ActionMove` : The action object.

---

## <a id="constructor-position"></a>ActionMove(path, destinationX, destinationY)

```java
ActionMove(@Nonnull List<EntityID> movePath, int destinationX, int destinationY);
```
Move along the given path and stop at an exact coordinate inside the last area. Useful when the agent must stand next to a specific blockade or building wall.

**Parameters :**
- `movePath` : List of area `EntityID`s to walk through.
- `destinationX` : Exact X coordinate of the stop point.
- `destinationY` : Exact Y coordinate of the stop point.

**Returns :**
- `ActionMove` : The action object.

---

## <a id="getpath"></a>getPath()

```java
List<EntityID> getPath();
```
Get the list of areas this action will walk through.

**Parameters :**
- None

**Returns :**
- `List<EntityID>` : The move path.

---

## <a id="getuseposition"></a>getUsePosition()

```java
boolean getUsePosition();
```
Check whether an exact destination coordinate was supplied.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` if (X,Y) is used, otherwise `false`.

---

## <a id="getposx"></a>getPosX()

```java
int getPosX();
```
Get the exact destination X coordinate.

**Parameters :**
- None

**Returns :**
- `int` : Destination X (meaningless if `getUsePosition()` is `false`).

---

## <a id="getposy"></a>getPosY()

```java
int getPosY();
```
Get the exact destination Y coordinate.

**Parameters :**
- None

**Returns :**
- `int` : Destination Y (meaningless if `getUsePosition()` is `false`).

---
