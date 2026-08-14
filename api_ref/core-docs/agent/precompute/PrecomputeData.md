# PrecomputeData.java

```
PrecomputeData();
PrecomputeData(String name);
```

[Attributes Stored](#attributes-stored)

**PrecomputeData** is the **save file** shared between the precompute run and the real run. During `precompute()` you `set*` values (clusters, distance tables, refuge lists); during `resume()` you `get*` them back instead of recomputing. Data is written to `precomp_data/`.

- Is the data **usable** ?
  - [isReady()](#isready)
  - [setReady()](#setready)
  - [write()](#write)
  - [copy()](#copy)
  - [removeData()](#removedata)

- **Store** a value
  - [setInteger() / setDouble() / setBoolean() / setString() / setEntityID()](#setters)
  - [setIntegerList() / setDoubleList() / setBooleanList() / setStringList() / setEntityIDList()](#list-setters)

- **Load** a value
  - [getInteger() / getDouble() / getBoolean() / getString() / getEntityID()](#getters)
  - [getIntegerList() / getDoubleList() / getBooleanList() / getStringList() / getEntityIDList()](#list-getters)

## <a id="attributes-stored"></a>Attributes Stored
1. `public static final File PRECOMP_DATA_DIR` : Directory (`precomp_data`) holding the saved files.
2. Internal maps of name → value for each supported type.

## <a id="isready"></a>isReady()

```java
boolean isReady(WorldInfo worldInfo);
```
Check that precomputed data exists **and** matches the current map. Always test this at the start of `resume()`; if it returns `false`, fall back to computing at runtime.

**Parameters :**
- `worldInfo` : The world info, used to verify the map signature.

**Returns :**
- `boolean` : `true` when the data can be trusted.

---

## <a id="setready"></a>setReady()

```java
boolean setReady(boolean isReady, WorldInfo worldInfo);
```
Mark the data as complete and stamp it with the current map.

**Parameters :**
- `isReady` : Ready flag.
- `worldInfo` : The world info used for the map stamp.

**Returns :**
- `boolean` : `true` on success.

---

## <a id="write"></a>write()

```java
boolean write();
```
Flush everything to disk. Called at the end of the precompute phase.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` if the file was written.

---

## <a id="copy"></a>copy()

```java
PrecomputeData copy();
```
Create a deep copy of this data set.

**Parameters :**
- None

**Returns :**
- `PrecomputeData` : The copy.

---

## <a id="removedata"></a>removeData()

```java
static void removeData(String name);
static void removeData();
```
Delete a named precompute file, or all of them.

**Parameters :**
- `name` : File name to delete (omit to delete everything).

**Returns :**
- `void`

---

## <a id="setters"></a>setInteger() / setDouble() / setBoolean() / setString() / setEntityID()

```java
Integer  setInteger(String name, int value);
Double   setDouble(String name, double value);
Boolean  setBoolean(String name, boolean value);
String   setString(String name, String value);
EntityID setEntityID(String name, EntityID value);
```
Store one value under a key.

**Parameters :**
- `name` : Key used later by the matching getter.
- `value` : The value to store.

**Returns :**
- The stored value (previous value semantics of the underlying map).

---

## <a id="list-setters"></a>setIntegerList() / setDoubleList() / setBooleanList() / setStringList() / setEntityIDList()

```java
List<Integer>  setIntegerList(String name, List<Integer> list);
List<Double>   setDoubleList(String name, List<Double> list);
List<Boolean>  setBooleanList(String name, List<Boolean> list);
List<String>   setStringList(String name, List<String> list);
List<EntityID> setEntityIDList(String name, List<EntityID> list);
```
Store a list under a key — this is how clusters, refuge lists and precomputed routes are saved.

**Parameters :**
- `name` : Key used later by the matching getter.
- `list` : The list to store.

**Returns :**
- The stored list.

---

## <a id="getters"></a>getInteger() / getDouble() / getBoolean() / getString() / getEntityID()

```java
Integer  getInteger(String name);
Double   getDouble(String name);
Boolean  getBoolean(String name);
String   getString(String name);
EntityID getEntityID(String name);
```
Load one value.

**Parameters :**
- `name` : The key used when storing.

**Returns :**
- The stored value, or a null/zero default when the key is absent.

---

## <a id="list-getters"></a>getIntegerList() / getDoubleList() / getBooleanList() / getStringList() / getEntityIDList()

```java
List<Integer>  getIntegerList(String name);
List<Double>   getDoubleList(String name);
List<Boolean>  getBooleanList(String name);
List<String>   getStringList(String name);
List<EntityID> getEntityIDList(String name);
```
Load a stored list.

**Parameters :**
- `name` : The key used when storing.

**Returns :**
- The stored list, or an empty list when the key is absent.

---
