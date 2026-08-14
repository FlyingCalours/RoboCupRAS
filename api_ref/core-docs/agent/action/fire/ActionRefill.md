# ActionRefill.java

```
ActionRefill();
```

**ActionRefill** makes a **Fire Brigade** refill its water tank. The agent must be standing inside a `Refuge` or on a `Hydrant`; the action is otherwise ignored by the server. Refill rate comes from `ScenarioInfo.getFireTankRefillRate()` / `getFireHydrantRefillRate()`.

- How do I **build** it ?
  - [ActionRefill()](#constructor)

## <a id="constructor"></a>ActionRefill()

```java
ActionRefill();
```
Create a refill action. Internally sent as a "rest at refill point" command, so the agent stays where it is this tick.

**Parameters :**
- None

**Returns :**
- `ActionRefill` : The action object.

---
