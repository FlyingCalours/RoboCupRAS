# StandardMessageBundle.java

```
StandardMessageBundle();
```

**StandardMessageBundle** is the **ready made message dictionary** of the ADF. Register it once in `Tactics.initialize()` and both your platoons and your centres can exchange every standard message type.

```java
messageManager.registerMessageBundle(new StandardMessageBundle());
```

Classes included (encoding order) :

| Group | Classes |
|---|---|
| Information | `MessageCivilian`, `MessageAmbulanceTeam`, `MessageFireBrigade`, `MessagePoliceForce`, `MessageBuilding`, `MessageRoad` |
| Centralized commands | `CommandAmbulance`, `CommandFire`, `CommandPolice`, `CommandScout`, `MessageReport` (package `...bundle.centralized`) |
| Top down commands | `CommandAmbulance`, `CommandFire`, `CommandPolice`, `CommandScout`, `MessageReport` (package `...bundle.topdown`) |

- What is **in the bundle** ?
  - [getMessageClassList()](#getmessageclasslist)

## <a id="getmessageclasslist"></a>getMessageClassList()

```java
List<Class<? extends CommunicationMessage>> getMessageClassList();
```
Return the ordered list of message classes of the standard bundle.

**Parameters :**
- None

**Returns :**
- `List<Class<? extends CommunicationMessage>>` : The class list.

---
