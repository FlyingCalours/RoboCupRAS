# DevelopData.java

```
DevelopData(
    boolean developFlag,
    @Nonnull String developDataFileName,
    @Nonnull List<String> rawData
);
```

**DevelopData** gives read only access to a **JSON tuning file** passed on the command line (`-dev`). Use it to change magic numbers (thresholds, distances, weights) without recompiling. Every getter takes a default value, so the code still runs when the file is absent.

```java
int maxDistance = developData.getInteger("SampleRoadDetector.maxDistance", 30000);
```

- Am I in **develop mode** ?
  - [isDevelopMode()](#isdevelopmode)

- Read a **single value**
  - [getInteger()](#getinteger)
  - [getDouble()](#getdouble)
  - [getBoolean()](#getboolean)
  - [getString()](#getstring)

- Read a **list**
  - [getIntegerList()](#getintegerlist)
  - [getDoubleList()](#getdoublelist)
  - [getBooleanList()](#getbooleanlist)
  - [getStringList()](#getstringlist)

- **Cleanup**
  - [clear()](#clear)

## <a id="isdevelopmode"></a>isDevelopMode()

```java
boolean isDevelopMode();
```
Check whether the agent was launched with develop data enabled.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` when develop mode is on.

---

## <a id="getinteger"></a>getInteger()

```java
Integer getInteger(@Nonnull String name, int defaultValue);
```
Read an integer value.

**Parameters :**
- `name` : Key in the develop data file.
- `defaultValue` : Value returned when the key is missing or develop mode is off.

**Returns :**
- `Integer` : The configured value or the default.

---

## <a id="getdouble"></a>getDouble()

```java
Double getDouble(@Nonnull String name, double defaultValue);
```
Read a floating point value.

**Parameters :**
- `name` : Key in the develop data file.
- `defaultValue` : Fallback value.

**Returns :**
- `Double` : The configured value or the default.

---

## <a id="getboolean"></a>getBoolean()

```java
Boolean getBoolean(@Nonnull String name, boolean defaultValue);
```
Read a boolean flag.

**Parameters :**
- `name` : Key in the develop data file.
- `defaultValue` : Fallback value.

**Returns :**
- `Boolean` : The configured value or the default.

---

## <a id="getstring"></a>getString()

```java
String getString(@Nonnull String name, @Nullable String defaultValue);
```
Read a text value.

**Parameters :**
- `name` : Key in the develop data file.
- `defaultValue` : Fallback value (may be `null`).

**Returns :**
- `String` : The configured value or the default.

---

## <a id="getintegerlist"></a>getIntegerList()

```java
List<Integer> getIntegerList(@Nonnull String name, List<Integer> defaultValue);
```
Read an array of integers.

**Parameters :**
- `name` : Key in the develop data file.
- `defaultValue` : Fallback list.

**Returns :**
- `List<Integer>` : The configured list or the default.

---

## <a id="getdoublelist"></a>getDoubleList()

```java
List<Double> getDoubleList(@Nonnull String name, List<Double> defaultValue);
```
Read an array of doubles.

**Parameters :**
- `name` : Key in the develop data file.
- `defaultValue` : Fallback list.

**Returns :**
- `List<Double>` : The configured list or the default.

---

## <a id="getbooleanlist"></a>getBooleanList()

```java
List<Boolean> getBooleanList(@Nonnull String name, List<Boolean> defaultValue);
```
Read an array of booleans.

**Parameters :**
- `name` : Key in the develop data file.
- `defaultValue` : Fallback list.

**Returns :**
- `List<Boolean>` : The configured list or the default.

---

## <a id="getstringlist"></a>getStringList()

```java
List<String> getStringList(@Nonnull String name, List<String> defaultValue);
```
Read an array of strings.

**Parameters :**
- `name` : Key in the develop data file.
- `defaultValue` : Fallback list.

**Returns :**
- `List<String>` : The configured list or the default.

---

## <a id="clear"></a>clear()

```java
void clear();
```
Drop all cached develop values.

**Parameters :**
- None

**Returns :**
- `void`

---
