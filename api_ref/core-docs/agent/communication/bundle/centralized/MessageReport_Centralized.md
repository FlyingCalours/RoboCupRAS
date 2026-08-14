# MessageReport.java (centralized)

`adf.core.agent.communication.standard.bundle.centralized.MessageReport`

```
MessageReport(boolean isRadio, boolean isDone, boolean isBroadcast, @Nullable EntityID fromID);
MessageReport(boolean isRadio, StandardMessagePriority sendingPriority,
              boolean isDone, boolean isBroadcast, @Nullable EntityID fromID);
MessageReport(boolean isRadio, int from, int ttl, @Nonnull BitStreamReader bitStreamReader);
```

[Attributes Stored](#attributes-stored)

**MessageReport** is the **answer a platoon sends back to its centre** after receiving a command: "done" or "failed". Without it the centre keeps assigning the same target forever, so a centralized team must send it whenever a task finishes or becomes impossible.

```java
messageManager.addMessage(new MessageReport(true, true, false, agentInfo.getID()));
```

- What is the **outcome** ?
  - [isDone()](#isdone)
  - [isFailed()](#isfailed)

- Who **sent** it, and to whom ?
  - [getFromID()](#getfromid)
  - [isBroadcast()](#isbroadcast)
  - [isFromIDDefined()](#isfromiddefined)

## <a id="attributes-stored"></a>Attributes Stored
1. `isDone` : `true` = task completed, `false` = task failed.
2. `isBroadcast` : Whether the report goes to everybody or only to the centre.
3. `fromID` : ID of the reporting agent.

## <a id="isdone"></a>isDone()

```java
boolean isDone();
```
Check whether the reported task was completed.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` when completed.

---

## <a id="isfailed"></a>isFailed()

```java
boolean isFailed();
```
Check whether the reported task failed — the inverse of `isDone()`.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` when failed.

---

## <a id="getfromid"></a>getFromID()

```java
EntityID getFromID();
```
Get the ID of the agent that produced this report.

**Parameters :**
- None

**Returns :**
- `EntityID` : The reporting agent ID, or `null`.

---

## <a id="isbroadcast"></a>isBroadcast()

```java
boolean isBroadcast();
```
Check whether the report was broadcast to everyone.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` when broadcast.

---

## <a id="isfromiddefined"></a>isFromIDDefined()

```java
boolean isFromIDDefined();
```
Check whether the sender field is meaningful.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` if the value is meaningful.

---
