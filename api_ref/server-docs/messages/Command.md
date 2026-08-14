# Command.java

`rescuecore2.messages.Command`

```
interface Command extends Message
```

**Command** is the **protocol level order** sent between agents and the kernel. Two places bring it into team code:

1. `Action.getCommand(agentID, time)` builds one (`AKMove`, `AKClear`, `AKRescue`, `AKExtinguish`, `AKLoad`, `AKUnload`, `AKRest`, `AKSpeak`, ...) — the framework does this for you.
2. `AgentInfo.getHeard()` returns `Collection<Command>` : the raw voice/radio traffic of this tick, which the communication module decodes into `CommunicationMessage` objects.

```java
for (Command command : agentInfo.getHeard()) {
    EntityID sender = command.getAgentID();
    ...
}
```

- **Methods**
  - [getAgentID()](#getagentid)
  - [getTime()](#gettime)

- **Related concrete classes** (`rescuecore2.standard.messages`)
  - `AKMove`, `AKClear`, `AKClearArea`, `AKRescue`, `AKExtinguish`, `AKLoad`, `AKUnload`, `AKRest`, `AKSay`, `AKSpeak`, `AKSubscribe`

## <a id="getagentid"></a>getAgentID()

```java
EntityID getAgentID();
```
Get the agent that issued this command — for a heard `AKSpeak`, this is who spoke.

**Parameters :**
- None

**Returns :**
- `EntityID` : The issuing agent's ID.

---

## <a id="gettime"></a>getTime()

```java
int getTime();
```
Get the timestep the command belongs to.

**Parameters :**
- None

**Returns :**
- `int` : The timestep.

---

## <a id="akspeak"></a>AKSpeak (most relevant subclass)

```java
AKSpeak(EntityID agent, int time, int channel, byte[] data);
int    getChannel();
byte[] getContent();
```
The message carrying agent-to-agent communication. `getChannel()` is `0` for voice (shouting) and `1..n` for radio channels; `getContent()` is the encoded `CommunicationMessage` payload.

**Returns :**
- `int` / `byte[]` : Channel number and raw payload.

---
