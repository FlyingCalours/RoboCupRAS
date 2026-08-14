# Action.java

```
Action();
```

**Action** is the **abstract base class** of every command your agent can return from `think()`.
A `Tactics` class must always return an `Action` object; the framework converts it into a
real server command through [getCommand()](#getcommand).

- What can I **return** ?
  - Common : `ActionMove`, `ActionRest`
  - Ambulance : `ActionLoad`, `ActionUnload`, `ActionRescue` (ambulance)
  - Fire : `ActionExtinguish`, `ActionRefill`, `ActionRescue` (fire)
  - Police : `ActionClear`

- How is it **sent to the server** ?
  - [getCommand()](#getcommand)

## <a id="getcommand"></a>getCommand()

```java
abstract Message getCommand(@Nonnull EntityID agentID, int time);
```
Converts this action object into the raw `rescuecore2` command message (`AKMove`, `AKClear`, ...) that is sent to the kernel. Called by the framework, **not** by your team code.

**Parameters :**
- `agentID` : The `EntityID` of the agent issuing the command.
- `time` : The current simulation timestep.

**Returns :**
- `Message` : The protocol level command message for the server.

---
