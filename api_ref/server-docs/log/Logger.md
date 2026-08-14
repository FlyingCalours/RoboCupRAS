# Logger.java

`rescuecore2.log.Logger`

```
class Logger   // static methods, never instantiated
```

**Logger** is the **logging facility** used across the server and the ADF. Prefer it to `System.out.println` — output is tagged with the agent context, so you can tell which of the twenty police forces printed a line, and levels can be filtered instead of deleted.

```java
Logger.debug("PoliceForce " + agentInfo.getID() + " clearing " + targetID);
```

- **Log a message**
  - [trace() / debug() / info() / warn() / error() / fatal()](#levels)

- **Context**
  - [setLogContext()](#setlogcontext)
  - [pushLogContext() / popLogContext()](#pushpop)

## <a id="levels"></a>trace() / debug() / info() / warn() / error() / fatal()

```java
static void trace(String msg);   static void trace(String msg, Throwable t);
static void debug(String msg);   static void debug(String msg, Throwable t);
static void info(String msg);    static void info(String msg, Throwable t);
static void warn(String msg);    static void warn(String msg, Throwable t);
static void error(String msg);   static void error(String msg, Throwable t);
static void fatal(String msg);   static void fatal(String msg, Throwable t);
```
Write a message at the given severity. Use `debug` for tactic tracing and `error` for genuine failures; `trace` is very noisy on a full map.

**Parameters :**
- `msg` : The message text.
- `t` : Optional exception to log with its stack trace.

**Returns :**
- `void`

---

## <a id="setlogcontext"></a>setLogContext()

```java
static void setLogContext(String context);
```
Set the context label attached to every following message from this thread — normally the agent name and ID.

**Parameters :**
- `context` : The context label.

**Returns :**
- `void`

---

## <a id="pushpop"></a>pushLogContext() / popLogContext()

```java
static void pushLogContext(String context);
static void popLogContext();
```
Push a nested context and pop it again, so a block of logging is tagged without losing the outer label.

**Parameters :**
- `context` : The context label to push.

**Returns :**
- `void`

---
