# CommunicationModule.java

```
abstract class CommunicationModule
```

**CommunicationModule** is the **transport layer** between your `MessageManager` and the rescuecore2 protocol. The default implementation, `StandardCommunicationModule`, encodes queued messages into `AKSpeak` commands and decodes what the agent heard into `CommunicationMessage` objects. You normally never touch it — it is selected in the launcher, not in `module.cfg`.

- **Receiving**
  - [receive()](#receive)

- **Sending**
  - [send()](#send)

## <a id="receive"></a>receive()

```java
abstract void receive(Agent agent, MessageManager messageManager);
```
Decode everything the agent heard this tick (`AgentInfo.getHeard()`) and push it into the message manager's received list.

**Parameters :**
- `agent` : The low level agent object.
- `messageManager` : The manager to fill.

**Returns :**
- `void`

---

## <a id="send"></a>send()

```java
abstract void send(Agent agent, MessageManager messageManager);
```
Encode the queued send list and transmit it on the subscribed channels.

**Parameters :**
- `agent` : The low level agent object.
- `messageManager` : The manager holding the queued messages.

**Returns :**
- `void`

---
