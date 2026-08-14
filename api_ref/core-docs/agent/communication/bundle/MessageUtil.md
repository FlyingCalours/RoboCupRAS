# MessageUtil.java

```
class MessageUtil   // static helper, never instantiated
```

**MessageUtil** writes the **content of a received message into your world model**. Without it a received `MessageRoad` is just an object; after `reflectMessage`, `worldInfo.getEntity(roadID)` really knows the road is blocked. Call it for every received message at the start of `think()` / `updateInfo()`.

```java
for (CommunicationMessage message : messageManager.getReceivedMessageList(StandardMessage.class)) {
    MessageUtil.reflectMessage(this.worldInfo, (StandardMessage) message);
}
```

- **Generic entry point**
  - [reflectMessage(worldInfo, StandardMessage)](#reflect-standard)

- **Typed overloads**
  - [reflectMessage(worldInfo, MessageBuilding)](#reflect-typed)
  - [reflectMessage(worldInfo, MessageRoad)](#reflect-typed)
  - [reflectMessage(worldInfo, MessageCivilian)](#reflect-typed)
  - [reflectMessage(worldInfo, MessageAmbulanceTeam)](#reflect-typed)
  - [reflectMessage(worldInfo, MessageFireBrigade)](#reflect-typed)
  - [reflectMessage(worldInfo, MessagePoliceForce)](#reflect-typed)

## <a id="reflect-standard"></a>reflectMessage(worldInfo, message)

```java
static StandardEntity reflectMessage(@Nonnull WorldInfo worldInfo, @Nonnull StandardMessage message);
```
Dispatch on the real message type and apply its content to the world model. Only properties the sender marked as defined (`isHPDefined()`, `isBrokennessDefined()`, ...) are written.

**Parameters :**
- `worldInfo` : The world model to update.
- `message` : Any standard message received this tick.

**Returns :**
- `StandardEntity` : The entity that was updated, or `null` if the message carries no entity information (e.g. a command).

---

## <a id="reflect-typed"></a>Typed overloads

```java
static Building      reflectMessage(@Nonnull WorldInfo worldInfo, @Nonnull MessageBuilding message);
static Road          reflectMessage(@Nonnull WorldInfo worldInfo, @Nonnull MessageRoad message);
static Civilian      reflectMessage(@Nonnull WorldInfo worldInfo, @Nonnull MessageCivilian message);
static AmbulanceTeam reflectMessage(@Nonnull WorldInfo worldInfo, @Nonnull MessageAmbulanceTeam message);
static FireBrigade   reflectMessage(@Nonnull WorldInfo worldInfo, @Nonnull MessageFireBrigade message);
static PoliceForce   reflectMessage(@Nonnull WorldInfo worldInfo, @Nonnull MessagePoliceForce message);
```
Same behaviour, but typed: apply one specific message type and return the concrete entity. `MessageRoad` also creates or removes the reported `Blockade`.

**Parameters :**
- `worldInfo` : The world model to update.
- `message` : The typed message.

**Returns :**
- The updated entity (`Building`, `Road`, `Civilian`, `AmbulanceTeam`, `FireBrigade`, `PoliceForce`).

---
