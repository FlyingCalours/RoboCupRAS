# URFRefugeDoorSweep — issue 1, refuge doors never open

Package: `sample_team.module.complex.police.improvement`

---

## 1. Why scoring alone can never fix this

`URFRoadDetector` gives refuge-adjacent roads the highest weight in the whole policy (`W_REFUGE_ACCESS = 6.0`). It still never sends anyone to a refuge door. The reason is not the weights — it is **fog of war**.

```
road.isBlockadesDefined()  ==  "has this agent ever looked at this road?"
```

A blockade the agent has never observed is not in `worldInfo`, so the road is not in the candidate set, so it scores nothing, so nobody goes, so nobody observes it. The loop closes on itself. Raising the weight to 600 would change nothing.

The only way out is to **go and look**, which is exactly the "stupid method" requested. It is not stupid; it is active perception, and it is worth framing that way in the report.

---

## 2. What a door is

A door is a `Road` that shares an edge with a `Refuge`.

Collection walks refuge edges rather than `getNeighbours()`, because a wall edge carries a null neighbour and must be skipped — the same pattern already proven in `URFPoliceExtActionMove.allNeighbours()`. Results are deduplicated and **sorted by entity id**, so every agent walks the doors in the same order and repeated runs stay comparable (section 13.2 needs that).

Typical map: 1–4 refuges, 2–4 doors each, so 4–12 doors total.

---

## 3. The state machine

One door at a time, per agent. Each cycle `nextDoor()` runs four steps in order:

| Step | Rule | Effect |
|---|---|---|
| 1. Opportunistic confirm | agent stands on *any* door and that road is observed clear | mark confirmed, suppress 50 cycles |
| 2. Give up | current door targeted for ≥ `ATTEMPT_BUDGET` cycles | mark abandoned, suppress 50 cycles |
| 3. Commit | a current door still stands | return it unchanged |
| 4. Choose | none held | nearest non-suppressed door, ties by entity id |

Returns `null` when every door is suppressed — the sweep is quiet and normal scoring resumes.

Worked through, with `ATTEMPT_BUDGET = 30`, `RECHECK_INTERVAL = 50`:

```
t=5   no current door -> nearest pending = R41 (distance 34 000)  -> target R41
t=6   committed                                                   -> target R41
...
t=19  agent arrives on R41, blockades defined and non-empty
      CLEAR module clears it (detector target is irrelevant to CLEAR)
t=24  blockades now empty -> confirmed, suppressUntil[R41] = 74
      current door cleared -> nearest pending = R58                -> target R58
t=30  agent walks past R12, which is a door and is observed clear
      opportunistic confirm -> suppressUntil[R12] = 80             (never visited deliberately)
...
t=60  R58 has been targeted for 30 cycles and is not reachable
      abandoned, suppressUntil[R58] = 110                          -> target R77
t=74  R41 becomes pending again — a building may have collapsed onto it since
```

Step 1 matters more than it looks. On most maps several doors sit on the route between refuges, so they get confirmed for free and the sweep finishes in a fraction of the worst case.

Step 2 is the failure recovery. Without it an agent walks at an unreachable entrance for the rest of the scenario.

`RECHECK_INTERVAL` exists because "checked once" goes stale — buildings collapse mid-run and drop fresh blockades onto roads that were open.

---

## 4. Wiring — three lines in `URFRoadDetector`

Add one reason constant:

```java
/** Returned while the refuge door sweep is driving the target. */
public static final String REASON_REFUGE_DOOR_SWEEP = "REFUGE_DOOR_SWEEP";
```

Then in `calc()`, immediately after the null-position guard and **before** `refreshWorldSnapshot()`:

```java
EntityID door = URFRefugeDoorSweep
    .forAgent(this.agentInfo.getID())
    .nextDoor(this.agentInfo, this.worldInfo);

if (door != null) {
  this.applySelection(door, REASON_REFUGE_DOOR_SWEEP, 0.0);
  return this;
}
```

That is the whole integration. Nothing else in the detector changes.

Three consequences worth understanding before running it:

1. **The sweep outranks scoring.** While a door is pending the scoring logic does not run at all. That is the requested behaviour, not a bug.
2. **CLEAR is unaffected.** `URFPoliceExtActionClear` only looks at the road underfoot and ignores the target entirely, so a police agent walking to a door still clears everything it steps on along the way. The sweep costs nothing in clearing throughput.
3. **The detector's own cooldown does not apply to doors.** `reviewCurrentTarget()` is skipped on the sweep path, so `ATTEMPT_BUDGET` is the only give-up mechanism for a door. That is deliberate — two competing timers on one target would fight.

---

## 5. The constant you should decide about

`SWEEP_DEADLINE = 150` is the one thing here that was **not** requested. It stops the sweep from issuing doors after simulation time 150.

The arithmetic behind it: worst case the sweep costs `doorCount × ATTEMPT_BUDGET` cycles. Twelve doors × 30 = 360 cycles, against a default scenario length of 300. In that worst case the police agent spends the entire run walking to refuge entrances and the scoring logic never executes once.

The deadline is a safety valve against that, not a disagreement with the design. Two ways to go:

- **Keep it.** Sweep early, score late. Defensible: refuge access matters most in the opening minutes, when ambulances start transporting.
- **Remove it** — set to `Integer.MAX_VALUE`. Gives literally "all police check all doors" as asked. Check `obsMoveRoad` and `sweepConfirmed` afterwards to see what it cost.

Either way, run it both ways once. The difference is a free ablation row for section 13.2, and "we measured the cost of our own design decision" reads well in section 4 of the rubric.

---

## 6. Observation

The sweep exposes `toLogFields()` in the URF log style:

```
sweepDoor=<current> sweepDoors=<total> sweepConfirmed=<n> sweepAbandoned=<n> sweepPending=<n>
```

Add it wherever the detector observer prints. The before/after evidence for issue 1 is the pair:

- `sweepConfirmed` rising from 0 to `sweepDoors` — the sweep is completing;
- `obsMoveRoad / obsMoveSearch` from `URFObservedExtActionMove` shifting towards road targets — the detector is now driving navigation instead of falling through to Search.

If `sweepAbandoned` is high, doors are unreachable and the problem has moved upstream to path planning.

---

## 7. Limitations

- **Full redundancy is the point and the cost.** Every agent sweeps every door, so a door gets confirmed once per police agent. With shared knowledge one confirmation would serve the team, but that needs a communication module.
- **All agents may target the same door.** They pick the nearest pending door independently, and early in a run they often start near each other. Cluster ownership would split the door list; that is a coordination module, not this one.
- **No deliberate re-look after `RECHECK_INTERVAL`.** A door re-arms after 50 cycles, but the agent only returns if that door is now the nearest pending one. A collapse on the far side of the map may go unnoticed for a long time.
- **`doors.contains(position)` is a linear scan.** Fine for 12 doors, once per cycle. If a map ever produces hundreds of doors, swap in a `HashSet`.
- **Refuge list assumes refuges are known at first call.** They are static map entities present from `preparate()`, so the lazy one-time collection is safe — but it does mean the door list is never rebuilt if the world model changes shape.
