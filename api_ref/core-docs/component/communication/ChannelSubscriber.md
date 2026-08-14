# ChannelSubscriber.java

```
ChannelSubscriber();
```

**ChannelSubscriber** decides **which radio channels the agent listens to**. The default implementation subscribes to channel 1 at time 1 and never changes; override it to split platoons across channels (e.g. police on channel 1, fire on channel 2). Selected through `module.cfg` and installed with `MessageManager.setChannelSubscriber()`.

- **Main method**
  - [subscribe()](#subscribe)

## <a id="subscribe"></a>subscribe()

```java
void subscribe(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
               MessageManager messageManager);
```
Called every tick. Decide the channel set and request it with `messageManager.subscribeToChannels(int[])`. Channel count and bandwidth come from `ScenarioInfo.getCommsChannelsCount()` / `getCommsChannelBandwidth()`.

**Parameters :**
- `agentInfo`, `worldInfo`, `scenarioInfo` : The standard info objects.
- `messageManager` : The manager used to request the subscription.

**Returns :**
- `void`

---
