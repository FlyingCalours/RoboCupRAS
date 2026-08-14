# Config.java

`rescuecore2.config.Config`

```
Config();
Config(File file);
Config(Config other);
```

**Config** is the **key/value store of every simulation setting** — kernel timings, communication limits, agent counts. In ADF you rarely touch it directly: `ScenarioInfo` wraps the interesting keys with typed getters, and `ModuleConfig` (which extends `Config`) holds `module.cfg`. Reach for it when you need a key `ScenarioInfo` does not expose, via `scenarioInfo.getRawConfig()`.

```java
int channels = scenarioInfo.getRawConfig()
        .getIntValue("comms.channels.count", 2);
```

- **Read a value**
  - [getValue()](#getvalue)
  - [getIntValue()](#getintvalue)
  - [getFloatValue()](#getfloatvalue)
  - [getBooleanValue()](#getbooleanvalue)
  - [getArrayValue()](#getarrayvalue)

- **Inspect the config**
  - [isDefined()](#isdefined)
  - [getAllKeys()](#getallkeys)
  - [getRandom()](#getrandom)

- **Modify** (rarely needed in team code)
  - [setValue() / setIntValue() / setFloatValue() / setBooleanValue()](#setters)
  - [merge()](#merge)

## <a id="getvalue"></a>getValue()

```java
String getValue(String key);
String getValue(String key, String defaultValue);
```
Read a text value. The one argument form throws `NoSuchConfigOptionException` when the key is absent — always prefer the version with a default.

**Parameters :**
- `key` : Configuration key.
- `defaultValue` : Value returned when the key is missing.

**Returns :**
- `String` : The value.

---

## <a id="getintvalue"></a>getIntValue()

```java
int getIntValue(String key);
int getIntValue(String key, int defaultValue);
```
Read an integer value.

**Parameters :**
- `key` : Configuration key.
- `defaultValue` : Fallback value.

**Returns :**
- `int` : The value.

---

## <a id="getfloatvalue"></a>getFloatValue()

```java
double getFloatValue(String key);
double getFloatValue(String key, double defaultValue);
```
Read a floating point value.

**Parameters :**
- `key` : Configuration key.
- `defaultValue` : Fallback value.

**Returns :**
- `double` : The value.

---

## <a id="getbooleanvalue"></a>getBooleanValue()

```java
boolean getBooleanValue(String key);
boolean getBooleanValue(String key, boolean defaultValue);
```
Read a boolean value.

**Parameters :**
- `key` : Configuration key.
- `defaultValue` : Fallback value.

**Returns :**
- `boolean` : The value.

---

## <a id="getarrayvalue"></a>getArrayValue()

```java
List<String> getArrayValue(String key);
List<String> getArrayValue(String key, String defaultValue);
```
Read a comma separated list value.

**Parameters :**
- `key` : Configuration key.
- `defaultValue` : Fallback (as a raw string).

**Returns :**
- `List<String>` : The values.

---

## <a id="isdefined"></a>isDefined()

```java
boolean isDefined(String key);
```
Check whether a key exists.

**Parameters :**
- `key` : Configuration key.

**Returns :**
- `boolean` : `true` when defined.

---

## <a id="getallkeys"></a>getAllKeys()

```java
Set<String> getAllKeys();
```
Get every key in the configuration — useful once, to discover what the map actually provides.

**Parameters :**
- None

**Returns :**
- `Set<String>` : All keys.

---

## <a id="getrandom"></a>getRandom()

```java
Random getRandom();
```
Get the shared random number generator seeded from the configuration. Use it instead of `new Random()` so runs stay reproducible.

**Parameters :**
- None

**Returns :**
- `Random` : The generator.

---

## <a id="setters"></a>setValue() / setIntValue() / setFloatValue() / setBooleanValue()

```java
void setValue(String key, String value);
void setIntValue(String key, int value);
void setFloatValue(String key, double value);
void setBooleanValue(String key, boolean value);
```
Write a value into the in-memory configuration.

**Parameters :**
- `key` : Configuration key.
- `value` : New value.

**Returns :**
- `void`

---

## <a id="merge"></a>merge()

```java
void merge(Config other);
```
Copy every key of another configuration into this one.

**Parameters :**
- `other` : The configuration to merge in.

**Returns :**
- `void`

---
