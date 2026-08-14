# StandardMessage.java

```
StandardMessage(boolean isRadio, StandardMessagePriority sendingPriority);
StandardMessage(boolean isRadio, int senderID, int ttl, BitStreamReader bsr);
```

[Attributes Stored](#attributes-stored)

**StandardMessage** is the **base class of every message in the standard bundle** (`MessageBuilding`, `MessageCivilian`, `MessageRoad`, the agent messages, and all `Command*` / `MessageReport` classes). It adds sender, time to live and priority on top of `CommunicationMessage`.

The second constructor is the **decoding** constructor: it is called by the framework when a received bit stream is turned back into an object.

- Who **sent** it, and how old is it ?
  - [getSenderID()](#getsenderid)
  - [getTTL()](#getttl)

- How **urgent** is it ?
  - [getSendingPriority()](#getsendingpriority)

- **Helper**
  - [getBitSize()](#getbitsize)

## <a id="attributes-stored"></a>Attributes Stored
1. `senderID` : ID of the agent that created the message.
2. `ttl` : Time to live — how many ticks the message may be relayed.
3. `sendingPriority` : `LOW`, `NORMAL` or `HIGH` (see `StandardMessagePriority`).
4. `isRadio` : Inherited from `CommunicationMessage`.

## <a id="getsenderid"></a>getSenderID()

```java
EntityID getSenderID();
```
Get the agent that sent this message. Useful to know who reported a blockade or asked for help.

**Parameters :**
- None

**Returns :**
- `EntityID` : The sender's ID.

---

## <a id="getttl"></a>getTTL()

```java
int getTTL();
```
Get the remaining time to live of the message.

**Parameters :**
- None

**Returns :**
- `int` : Remaining hops/ticks.

---

## <a id="getsendingpriority"></a>getSendingPriority()

```java
StandardMessagePriority getSendingPriority();
```
Get the priority the coordinator should use for this message.

**Parameters :**
- None

**Returns :**
- `StandardMessagePriority` : `LOW`, `NORMAL` or `HIGH`.

---

## <a id="getbitsize"></a>getBitSize()

```java
protected int getBitSize(int value);
```
Number of bits needed to encode a value — used by subclasses when building their bit stream.

**Parameters :**
- `value` : The value to measure.

**Returns :**
- `int` : Bit count.

---
