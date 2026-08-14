# ModuleConfig.java

```
ModuleConfig(
    @Nonnull String fileName,
    @Nonnull List<String> rawData
);
```

[Attributes Stored](#attributes-stored)

**ModuleConfig** is the parsed form of **module.cfg** — the JSON file that maps module keys to implementation class names. It extends `rescuecore2.config.Config`, so all `Config` getters are available. Obtain it with `ModuleManager.getModuleConfig()`; you normally read it only when you add your own custom keys.

```java
String impl = moduleManager.getModuleConfig()
        .getValue("SampleTacticsPoliceForce.PathPlanning");
```

- Inherited from **Config** (see `server-docs/Config.md`)
  - `getValue(key)` / `getValue(key, default)`
  - `getIntValue(key, default)` / `getFloatValue(key, default)` / `getBooleanValue(key, default)`
  - `getArrayValue(key, default)`
  - `isDefined(key)` / `getAllKeys()`

## <a id="attributes-stored"></a>Attributes Stored
1. `public static final String DEFAULT_CONFIG_FILE_NAME` : Default configuration file name.
2. Inherited key → value map from `rescuecore2.config.Config`.

## <a id="constructor"></a>ModuleConfig(fileName, rawData)

```java
ModuleConfig(@Nonnull String fileName, @Nonnull List<String> rawData);
```
Parse the raw JSON lines of a module configuration file. Called by the launcher, not by team code.

**Parameters :**
- `fileName` : Name of the configuration file (for error messages).
- `rawData` : The file content, one entry per line.

**Returns :**
- `ModuleConfig` : The parsed configuration.

---

**Note :** a missing key makes `getValue` throw `NoSuchConfigOptionException`. `ModuleManager` catches it and falls back to the default class name you passed to `getModule()`, which is why every `getModule()` call in the sample code supplies one.
