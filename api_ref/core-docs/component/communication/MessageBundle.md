# MessageBundle.java

```
abstract class MessageBundle
```

**MessageBundle** is the **shared dictionary of message types**. Sender and receiver encode messages as small indices, so every agent in the team must register the same bundle in the same order — otherwise decoding produces garbage. The ready made bundle is `StandardMessageBundle`.

```java
messageManager.registerMessageBundle(new StandardMessageBundle());
```

- What types are **in the bundle** ?
  - [getMessageClassList()](#getmessageclasslist)

## <a id="getmessageclasslist"></a>getMessageClassList()

```java
abstract List<Class<? extends CommunicationMessage>> getMessageClassList();
```
Return every message class of this bundle, in a fixed order. Position in the list is the encoding index (maximum 32 classes).

**Parameters :**
- None

**Returns :**
- `List<Class<? extends CommunicationMessage>>` : The ordered class list.

---
