# StandardMessagePriority.java

```
enum StandardMessagePriority { LOW, NORMAL, HIGH }
```

**StandardMessagePriority** tells the `MessageCoordinator` **how important a message is** when bandwidth is scarce. Every `StandardMessage` constructor has an overload taking a priority; the overload without it uses `NORMAL`.

| Value | Typical use |
|---|---|
| `HIGH` | Life critical or command traffic — a buried civilian with low HP, a centre command, a "task done" report. |
| `NORMAL` | Ordinary information sharing — building on fire, road blocked. |
| `LOW` | Nice to have updates that can be dropped — already known or stale information. |

```java
messageManager.addMessage(
    new MessageCivilian(true, StandardMessagePriority.HIGH, civilian));
```

**Values :**
- `LOW` : Send only if bandwidth remains.
- `NORMAL` : Default priority.
- `HIGH` : Send first.

---
