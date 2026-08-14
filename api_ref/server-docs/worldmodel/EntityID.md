# EntityID.java

`rescuecore2.worldmodel.EntityID`

```
EntityID(int id);
```

**EntityID** is the **unique handle of every object** in the simulation — agents, roads, buildings, blockades. Almost every ADF API takes or returns one. It is immutable and implements `equals`/`hashCode`, so it is safe as a `HashMap` key or `HashSet` element (which is how allocation maps and visited sets are built).

```java
Map<EntityID, EntityID> allocation = new HashMap<>();
allocation.put(agentInfo.getID(), targetID);
```

- **Methods**
  - [getValue()](#getvalue)
  - [equals()](#equals)
  - [hashCode()](#hashcode)
  - [toString()](#tostring)

## <a id="getvalue"></a>getValue()

```java
int getValue();
```
Get the raw integer id. Useful for logging, sorting, or a stable tie break between agents (e.g. "lowest ID takes the target").

**Parameters :**
- None

**Returns :**
- `int` : The numeric ID.

---

## <a id="equals"></a>equals()

```java
boolean equals(Object o);
```
Two `EntityID` objects are equal when their integer values match. Always compare with `equals()`, never `==`.

**Parameters :**
- `o` : The object to compare with.

**Returns :**
- `boolean` : `true` when equal.

---

## <a id="hashcode"></a>hashCode()

```java
int hashCode();
```
Hash code based on the integer value.

**Parameters :**
- None

**Returns :**
- `int` : The hash code.

---

## <a id="tostring"></a>toString()

```java
String toString();
```
Text form of the ID, for debugging.

**Parameters :**
- None

**Returns :**
- `String` : The ID as text.

---
