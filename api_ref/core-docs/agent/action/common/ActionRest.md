# ActionRest.java

```
ActionRest();
```

**ActionRest** makes the agent **do nothing** this tick. It is the standard fallback when no target is found, and it also lets a buried/tired agent recover stamina.

- How do I **build** it ?
  - [ActionRest()](#constructor)

## <a id="constructor"></a>ActionRest()

```java
ActionRest();
```
Create a rest action. Sent to the server as `AKRest`.

**Parameters :**
- None

**Returns :**
- `ActionRest` : The action object.

---
