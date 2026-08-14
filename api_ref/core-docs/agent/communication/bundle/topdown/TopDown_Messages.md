# topdown package (CommandAmbulance / CommandFire / CommandPolice / CommandScout / MessageReport)

`adf.core.agent.communication.standard.bundle.topdown.*`

```
CommandAmbulance(boolean isRadio, EntityID toID, EntityID targetID, int action);
CommandFire(boolean isRadio, EntityID toID, EntityID targetID, int action);
CommandPolice(boolean isRadio, EntityID toID, EntityID targetID, int action);
CommandScout(boolean isRadio, EntityID toID, EntityID targetID, int range);
MessageReport(boolean isRadio, boolean isDone, boolean isBroadcast, EntityID fromID);
```

**The `topdown` classes are thin subclasses of the `centralized` ones.** They carry exactly the same fields, constants and getters — the package is what differs, and it is used to separate a *strict top-down chain of command* (centre → platoon, no negotiation) from ordinary centralized coordination. Both sets are registered by `StandardMessageBundle`, so both can be exchanged.

**Practical rule :** pick one package for your whole team and import consistently. Mixing them silently breaks command handling, because a `CommandExecutor` typed on the centralized `CommandPolice` will never match a topdown `CommandPolice` in `getReceivedMessageList(...)`.

Full method documentation is identical to the centralized versions :

- [CommandAmbulance_Centralized.md](CommandAmbulance_Centralized.md)
- [CommandFire_Centralized.md](CommandFire_Centralized.md)
- [CommandPolice_Centralized.md](CommandPolice_Centralized.md)
- [CommandScout_Centralized.md](CommandScout_Centralized.md)
- [MessageReport_Centralized.md](MessageReport_Centralized.md)

---
