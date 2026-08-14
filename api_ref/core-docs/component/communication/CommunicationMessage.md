# CommunicationMessage.java

```
CommunicationMessage(boolean isRadio);
```

[Attributes Stored](#attributes-stored)

**CommunicationMessage** is the **abstract base of every message** an agent can send. `StandardMessage` (and through it all `Message*` / `Command*` classes) extends it. You only implement it directly when you invent a new message type; then you must also register the class with `MessageManager.registerMessageClass()`.

- How is it **sent** ?
  - [isRadio()](#isradio)
  - [getByteArraySize()](#getbytearraysize)
  - [toByteArray()](#tobytearray)
  - [toBitOutputStream()](#tobitoutputstream)

- How are **duplicates** filtered ?
  - [getCheckKey()](#getcheckkey)

## <a id="attributes-stored"></a>Attributes Stored
1. `private boolean isRadio` : `true` when the message goes on a radio channel, `false` for voice (shouting).

## <a id="isradio"></a>isRadio()

```java
boolean isRadio();
```
Check the transmission medium of this message.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` for radio, `false` for voice.

---

## <a id="getbytearraysize"></a>getByteArraySize()

```java
abstract int getByteArraySize();
```
Size in bytes of the encoded message. Used to check the channel bandwidth limit (`ScenarioInfo.getCommsChannelBandwidth()`).

**Parameters :**
- None

**Returns :**
- `int` : Encoded size in bytes.

---

## <a id="tobytearray"></a>toByteArray()

```java
abstract byte[] toByteArray();
```
Encode the message into raw bytes.

**Parameters :**
- None

**Returns :**
- `byte[]` : The encoded message.

---

## <a id="tobitoutputstream"></a>toBitOutputStream()

```java
abstract BitOutputStream toBitOutputStream();
```
Encode the message bit by bit — this is where the compact ADF encoding is built.

**Parameters :**
- None

**Returns :**
- `BitOutputStream` : The bit level encoding.

---

## <a id="getcheckkey"></a>getCheckKey()

```java
abstract String getCheckKey();
```
A string identifying the *information content* of the message. `MessageManager.addMessage(msg, true)` uses it so the same fact is not broadcast twice.

**Parameters :**
- None

**Returns :**
- `String` : The duplication check key.

---
