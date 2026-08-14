# ActionUnload.java

```
ActionUnload();
```

**ActionUnload** makes an **Ambulance Team** put down the civilian it is carrying. It should only be issued when the agent is standing inside a `Refuge` (otherwise the civilian is simply dropped on the road).

- How do I **build** it ?
  - [ActionUnload()](#constructor)

## <a id="constructor"></a>ActionUnload()

```java
ActionUnload();
```
Create an unload action. Sent to the server as `AKUnload`.

**Parameters :**
- None

**Returns :**
- `ActionUnload` : The action object.

---
