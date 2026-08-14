# MessageCoordinator.java

```
abstract class MessageCoordinator
```

**MessageCoordinator** decides **which queued message goes on which channel** and which ones get dropped when the bandwidth is not enough. It is the place to implement priority policies (e.g. send `MessageCivilian` before `MessageBuilding`). Selected through `module.cfg` and installed with `MessageManager.setMessageCoordinator()`; the ready made implementation is `adf.impl.module.comm.DefaultMessageCoordinator`.

- **Main method**
  - [coordinate()](#coordinate)

## <a id="coordinate"></a>coordinate()

```java
abstract void coordinate(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
                         MessageManager messageManager,
                         ArrayList<CommunicationMessage> sendMessageList,
                         List<List<CommunicationMessage>> channelSendMessageList);
```
Distribute the messages of `sendMessageList` into `channelSendMessageList`, respecting each channel's bandwidth (`ScenarioInfo.getCommsChannelBandwidth(channel)`). Index `0` of `channelSendMessageList` is the voice channel.

**Parameters :**
- `agentInfo`, `worldInfo`, `scenarioInfo` : The standard info objects.
- `messageManager` : The calling manager.
- `sendMessageList` : All messages queued this tick.
- `channelSendMessageList` : Output — one list per channel, to be filled by this method.

**Returns :**
- `void`

---
