# MessageManager.java

```
MessageManager();
```

[Attributes Stored](#attributes-stored)

**MessageManager** is the **communication** module. Every message your agent sends or receives this tick passes through it. In `Tactics.think()` you read what arrived with `getReceivedMessageList(...)` and queue what to send with `addMessage(...)`.

- What did I **receive** ?
  - [getReceivedMessageList()](#getreceivedmessagelist)
  - [getReceivedMessageList(classes...)](#getreceivedmessagelist-filtered)
  - [addReceivedMessage()](#addreceivedmessage)

- What do I want to **send** ?
  - [addMessage()](#addmessage)
  - [getSendMessageList()](#getsendmessagelist)

- Which **channels** am I on ?
  - [subscribeToChannels()](#subscribetochannels)
  - [getChannels()](#getchannels)
  - [getIsSubscribed()](#getissubscribed)
  - [setIsSubscribed()](#setissubscribed)
  - [subscribe()](#subscribe)
  - [setChannelSubscriber()](#setchannelsubscriber)

- How are messages **encoded / ordered** ?
  - [registerMessageBundle()](#registermessagebundle)
  - [registerMessageClass()](#registermessageclass)
  - [getMessageClass()](#getmessageclass)
  - [getMessageClassIndex()](#getmessageclassindex)
  - [setMessageCoordinator()](#setmessagecoordinator)
  - [coordinateMessages()](#coordinatemessages)

- **Help counting & cleanup**
  - [addHeardAgentHelpCount()](#addheardagenthelpcount)
  - [getHeardAgentHelpCount()](#getheardagenthelpcount)
  - [refresh()](#refresh)

## <a id="attributes-stored"></a>Attributes Stored
1. `sendMessageList` : Messages queued this tick, before channel assignment.
2. `channelSendMessageList` : Messages after the coordinator has split them per channel.
3. `receivedMessageList` : Messages decoded from radio/voice this tick.
4. `checkDuplicationCache` : Check keys used to avoid sending the same information twice.
5. `channels` / `isSubscribed` : The radio channels this agent listens to.
6. `messageCoordinator` / `channelSubscriber` : Pluggable policy objects (see `module.cfg`).
7. `heardAgentHelpCount` : Number of help calls heard this tick.

## <a id="getreceivedmessagelist"></a>getReceivedMessageList()

```java
List<CommunicationMessage> getReceivedMessageList();
```
Get every message received this tick.

**Parameters :**
- None

**Returns :**
- `List<CommunicationMessage>` : All received messages.

---

## <a id="getreceivedmessagelist-filtered"></a>getReceivedMessageList(messageClasses...)

```java
final List<CommunicationMessage> getReceivedMessageList(Class<? extends CommunicationMessage>... messageClasses);
```
Get only the received messages of the given types, e.g. `getReceivedMessageList(MessageRoad.class, MessageCivilian.class)`.

**Parameters :**
- `messageClasses` : One or more message classes to keep.

**Returns :**
- `List<CommunicationMessage>` : Received messages matching any of those classes.

---

## <a id="addreceivedmessage"></a>addReceivedMessage()

```java
void addReceivedMessage(@Nonnull CommunicationMessage message);
```
Push a message into the received list. Called by the communication module while decoding; rarely called by team code.

**Parameters :**
- `message` : The decoded message.

**Returns :**
- `void`

---

## <a id="addmessage"></a>addMessage()

```java
void addMessage(@Nonnull CommunicationMessage message);
void addMessage(@Nonnull CommunicationMessage message, boolean checkDuplication);
```
Queue a message to be transmitted at the end of this tick.

**Parameters :**
- `message` : The message to send (e.g. `new MessageRoad(true, road, blockade, false, true)`).
- `checkDuplication` : When `true`, the check key is used to filter repeated information.

**Returns :**
- `void`

---

## <a id="getsendmessagelist"></a>getSendMessageList()

```java
List<List<CommunicationMessage>> getSendMessageList();
```
Get the per channel send lists produced by the coordinator.

**Parameters :**
- None

**Returns :**
- `List<List<CommunicationMessage>>` : One list of messages per channel.

---

## <a id="subscribetochannels"></a>subscribeToChannels()

```java
void subscribeToChannels(int[] channels);
```
Request subscription to the given radio channels (takes effect next tick).

**Parameters :**
- `channels` : Array of channel numbers.

**Returns :**
- `void`

---

## <a id="getchannels"></a>getChannels()

```java
int[] getChannels();
```
Get the channels this agent asked to subscribe to.

**Parameters :**
- None

**Returns :**
- `int[]` : Channel numbers.

---

## <a id="getissubscribed"></a>getIsSubscribed()

```java
boolean getIsSubscribed();
```
Check whether the subscription request has already been sent.

**Parameters :**
- None

**Returns :**
- `boolean` : `true` if subscribed.

---

## <a id="setissubscribed"></a>setIsSubscribed()

```java
void setIsSubscribed(boolean subscribed);
```
Set the subscription flag.

**Parameters :**
- `subscribed` : New flag value.

**Returns :**
- `void`

---

## <a id="subscribe"></a>subscribe()

```java
void subscribe(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo);
```
Ask the registered `ChannelSubscriber` to decide and request channels for this tick.

**Parameters :**
- `agentInfo`, `worldInfo`, `scenarioInfo` : The standard info objects.

**Returns :**
- `void`

---

## <a id="setchannelsubscriber"></a>setChannelSubscriber()

```java
void setChannelSubscriber(ChannelSubscriber cs);
```
Install the channel subscription policy.

**Parameters :**
- `cs` : A `ChannelSubscriber` implementation.

**Returns :**
- `void`

---

## <a id="registermessagebundle"></a>registerMessageBundle()

```java
void registerMessageBundle(@Nonnull MessageBundle messageBundle);
```
Register every message class of a bundle (usually `StandardMessageBundle`) so both sender and receiver agree on the encoding indices.

**Parameters :**
- `messageBundle` : The bundle to register.

**Returns :**
- `void`

---

## <a id="registermessageclass"></a>registerMessageClass()

```java
boolean registerMessageClass(int index, @Nonnull Class<? extends CommunicationMessage> messageClass);
```
Register one custom message class at a given index (maximum index is 31).

**Parameters :**
- `index` : Encoding index `0..31`.
- `messageClass` : The message class.

**Returns :**
- `boolean` : `true` on success.

---

## <a id="getmessageclass"></a>getMessageClass()

```java
Class<? extends CommunicationMessage> getMessageClass(int index);
```
Look up the message class registered at an index.

**Parameters :**
- `index` : The encoding index.

**Returns :**
- `Class<? extends CommunicationMessage>` : The registered class, or `null`.

---

## <a id="getmessageclassindex"></a>getMessageClassIndex()

```java
int getMessageClassIndex(@Nonnull CommunicationMessage message);
```
Get the encoding index used for a message instance.

**Parameters :**
- `message` : The message.

**Returns :**
- `int` : Its registered index.

---

## <a id="setmessagecoordinator"></a>setMessageCoordinator()

```java
void setMessageCoordinator(MessageCoordinator mc);
```
Install the policy that decides which message goes on which channel.

**Parameters :**
- `mc` : A `MessageCoordinator` implementation.

**Returns :**
- `void`

---

## <a id="coordinatemessages"></a>coordinateMessages()

```java
void coordinateMessages(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo);
```
Run the coordinator, filling the per channel send lists. Called by the framework at the end of the tick.

**Parameters :**
- `agentInfo`, `worldInfo`, `scenarioInfo` : The standard info objects.

**Returns :**
- `void`

---

## <a id="addheardagenthelpcount"></a>addHeardAgentHelpCount()

```java
void addHeardAgentHelpCount();
```
Increase the counter of help messages heard this tick.

**Parameters :**
- None

**Returns :**
- `void`

---

## <a id="getheardagenthelpcount"></a>getHeardAgentHelpCount()

```java
int getHeardAgentHelpCount();
```
Get how many help messages were heard this tick.

**Parameters :**
- None

**Returns :**
- `int` : Help message count.

---

## <a id="refresh"></a>refresh()

```java
void refresh();
```
Clear the send/receive lists at the start of a new tick. Called by the framework.

**Parameters :**
- None

**Returns :**
- `void`

---
