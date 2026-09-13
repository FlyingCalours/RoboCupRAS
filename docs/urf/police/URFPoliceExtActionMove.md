# URFPoliceExtActionMove and URFObservedExtActionMove

Module family: **Police Force movement and last-resort recovery** (assignment section 6 "Action Module", section 7H "Failure Recovery").

| File | Package | Role |
|---|---|---|
| `URFPoliceExtActionMove.java` | `sample_team.module.complex.police.improvement` | Independent action module. Decides *where to step, and what to do when stepping is impossible*. |
| `URFObservedExtActionMove.java` | `sample_team.module.complex.police.observation` | Measurement wrapper. Adds counters and timing, changes no decision. |

Two dependencies were removed in this revision: `URFPoliceTargetBridge` and `URFPoliceUnstuck`. Sections 2 and 3.2 explain what replaced them and why the replacements are better, not merely equivalent.

---

## 1. The one rule that defines this module

`URFPoliceExtActionMove.calc()` **must never return null.**

That is the mirror image of the CLEAR module's rule, and the reason is in the tactics fallthrough:

```
think()
  |
  +-- roadDetector.calc().getTarget() --> extActionClear.calc()
  |        +-- ActionClear --> sent, done
  |        +-- null        --> fall through
  |
  +-- search.calc().getTarget() -------> extActionMove.calc()
           +-- action  --> sent, done
           +-- null    --> ActionRest          <-- the failure case
```

`ActionRest` is indistinguishable from a crashed agent in the viewer. The agent stands still, produces no log, and a rubric row ("no persistent idle/stuck behavior", section 12.1) fails on it. MOVE is the last stage in the chain, so it must manufacture *something* legal every cycle, even when the correct answer is "this agent is trapped".

This is why the module ends with a move-to-self rather than a null. A move-to-self is legal, produces a log line, and lets the stuck detector record "acting but not displacing". A null produces silence.

---

## 2. Getting the road target without shared static state

Look again at the fallthrough above. Tactics passes the RoadDetector's target to **CLEAR** and the Search target to **MOVE**. There is no parameter that carries the road target into this module. But the road target is exactly what should drive navigation — Search is only the exploration fallback.

### 2.1 Why the previous approach failed

The earlier design published the road target into a static map (`URFPoliceTargetBridge`) and read it back here. That has two defects, and the project hit both.

**It can be half-wired and still compile.** A static accessor with readers but no writers is a valid program. `getTarget()` simply returned null forever, MOVE silently fell back to the Search target, and the refuge door sweep selected doors that nobody ever walked to. Nothing failed loudly; the agents just behaved as though the detector did not exist.

**It goes stale.** Tactics only calls `extActionClear.setTarget(...)` when the target is non-null. Any publish placed on the CLEAR side therefore cannot publish a null, so the map would keep a target the detector had already abandoned.

### 2.2 What replaced it

The `ModuleManager` already solves this. `getModule(key, defaultClass)` returns the **cached instance for that configuration key** — the same object tactics holds:

```java
this.roadDetector =
    moduleManager.getModule(ROAD_DETECTOR_KEY, ROAD_DETECTOR_DEFAULT);
```

with `ROAD_DETECTOR_KEY = "DefaultTacticsPoliceForce.RoadDetector"`, matching `module.cfg`. Then, each cycle:

```java
this.roadTarget = this.roadDetector.getTarget();
```

Four properties fall out of this, each one a defect in the bridge version:

| Property | Why it holds |
|---|---|
| No shared mutable static state | The reference is an instance field obtained through the framework's own registry. |
| Cannot be half-wired | There is no publish step to forget. The detector's own return value is the channel. |
| Never stale | A detector with no target returns null immediately; nothing has to be cleared. |
| No URF type coupling | `getTarget()` is declared on ADF's abstract `RoadDetector`. This module compiles against a baseline `SampleRoadDetector` just as well. |

**Ordering is guaranteed by the fallthrough, not by luck.** `roadDetector.calc()` runs at the top of `think()`; MOVE runs at the bottom. By the time `setTarget` executes, `getTarget()` holds this cycle's value.

`calc()` is deliberately *not* called on the detector here — tactics has already run it, and running it twice would advance the refuge sweep's state machine twice per cycle. The detector is also **not** passed to `registerModule`, because its lifecycle is already driven by the tactics class; registering it would fire `precompute`, `resume`, `preparate` and `updateInfo` a second time from inside MOVE.

### 2.3 The three sources

| Condition | `targetSource` | Passed to `super.setTarget` |
|---|---|---|
| road target exists, agent is **on** it | `PUSH_THROUGH` | `null` — nothing to walk to; the ladder takes over |
| road target exists, agent elsewhere | `ROAD_TARGET` | the road target |
| no road target | `SEARCH_TARGET` | the Search target |
| neither | `NO_TARGET` | `null` |

`PUSH_THROUGH` deserves its comment in the source. An earlier version suppressed the target when standing on it, and combined with yield turns that made agents walk away from roads they were halfway through opening. Stepping to a neighbour instead *tests* the corridor CLEAR has been cutting — is it wide enough to pass yet?

---

## 3. The fallback ladder

`pushThrough()` is a strict ordering, each rung more desperate than the one above.

| Rung | Condition | Action | Meaning |
|---|---|---|---|
| 1 | a neighbour sits behind a **passable** edge | `ActionMove(position, next)` | normal movement |
| 2 | no passable edge, a blockade within reach | `ActionClear(x, y, blockade)` on a rotating bearing | cut a way out |
| 3 | no passable edge, no blockade in reach | `ActionMove(position, any neighbour)` | try an edge the world model says is closed |
| 4 | no neighbours at all | `ActionMove(position)` | legal no-op, never null |

**Rotation, not repetition.** `stepTo()` uses `neighbourIndex % options.size()` and increments every call, so an agent that fails through exit A tries B next cycle, then C. Without this the agent retries the same failing exit forever — the classic RCRS frozen-agent bug.

**Rung 3 is intentional.** The traffic simulator refuses an impassable edge harmlessly. A refused move costs one cycle and produces a log entry; doing nothing costs one cycle and produces nothing. When the world model may be stale, trying and failing beats sitting still.

### 3.1 Bearing clear, with real numbers

`bearingClear()` handles the walled-in case. It ignores blockade geometry entirely and sweeps a compass bearing.

With `clearDistance = 10000`, `AIM_MARGIN = 100`, the agent at `P = (40000, 25000)`:

```
reach = max(1, 10000 - 100) = 9900

first attempt, bearing = 0 deg
  cos = 1.0000,  sin = 0.0000
  x = 40000 + 9900(1.0000) = 49900
  y = 25000 + 9900(0.0000) = 25000
  -> ActionClear(49900, 25000, blockade)

second attempt, bearing = 135 deg
  cos = -0.7071, sin = 0.7071
  x = 40000 + 9900(-0.7071) = 33000
  y = 25000 + 9900( 0.7071) = 32000
  -> ActionClear(33000, 32000, blockade)
```

The general form, for agent position $P$, bearing $\theta$ and clear limit $L$:

$$
\text{aim} \;=\; \Big(P_x + (L - m)\cos\theta,\;\; P_y + (L - m)\sin\theta\Big),
\qquad m = 100
$$

| Symbol | Java name | Meaning |
|---|---|---|
| $P_x, P_y$ | `agentX`, `agentY` | the agent's physical position |
| $L$ | `clearDistance` | `scenarioInfo.getClearRepairDistance()` |
| $m$ | `AIM_MARGIN` | held back so the aim point never leaves the legal clear range |
| $\theta$ | `nextBearing()` | rotating bearing, so repeated failures sweep different directions |

This is the opposite philosophy from `URFPoliceExtActionClear`, which computes the exact nearest point on the blockade polygon. Here the agent is already stuck, so precision has demonstrably failed and a blind sweep is the correct escalation. Contrasting the two in the report is worth a paragraph: same action type, opposite amounts of information.

### 3.2 The bearing sequence, now internal

`URFPoliceUnstuck` previously supplied the bearing. It is gone; `nextBearing()` is four lines:

```java
bearing = (bearingIndex % BEARING_COUNT) * (FULL_TURN / BEARING_COUNT);
bearingIndex += BEARING_STEP;
```

`BEARING_COUNT = 8` (45° apart) and `BEARING_STEP = 3`. Because 3 and 8 are **coprime**, the index cycles through all eight residues before repeating:

```
index:   0    3    6    1    4    7    2    5   -> 0 ...
bearing: 0  135  270   45  180  315   90  225   degrees
```

Every direction is tried exactly once per cycle of eight, and consecutive attempts point far apart rather than nudging a few degrees. A step of 1 would also cover all eight but would creep around the compass, wasting cycles on directions adjacent to one that just failed. No random source is involved, so a rerun of the same scenario produces the same escape sequence — which section 13.2 needs.

---

## 4. The observation wrapper

### 4.1 No parent modification required

`URFPoliceExtActionMove` is not `final`, so `URFObservedExtActionMove` extends it with no changes to the parent. This differs from the CLEAR module, which needed one word removed.

### 4.2 Why it does not export a second CSV row

The parent already closes the pipeline:

```java
this.metrics.recordMoveModuleResult(time, requestedTarget, action, elapsedNanos);
this.stuckDetector.evaluate(this.metrics);
URFPoliceCsvExporter.export(this.metrics, this.stuckDetector);
```

Repeating that in the subclass would double every row **and** evaluate the stuck detector twice per cycle, corrupting its displacement history. The wrapper adds only what the parent cannot see about itself.

### 4.3 Reading the source instead of re-deriving it

The previous wrapper recomputed `targetSource` by mirroring the parent's resolution rule. That is a drift hazard: change the rule in one file and the log quietly reports the wrong source in the other. The parent now owns `targetSource` and exposes it, and the wrapper reads it:

```java
String source = super.getTargetSource();
```

`setTarget()` no longer needs overriding at all.

| Field | Meaning |
|---|---|
| `obsMoveSource` | `ROAD_TARGET` / `SEARCH_TARGET` / `PUSH_THROUGH` / `NO_TARGET` |
| `obsMoveRoadTarget`, `obsMoveSearchTarget` | both raw inputs, so a disagreement is visible |
| `obsMoveRequested` | what actually reached the path follower |
| `obsMoveCycles` | how many times MOVE ran for this agent |
| `obsMove`, `obsMoveClear` | rung 1/3 versus rung 2 |
| `obsMoveSelfLoop` | rung 4 — the agent was completely walled in |
| `obsMoveNull` | **contract assertion, must be 0** |
| `obsMovePush`, `obsMoveRoad`, `obsMoveSearch` | target-source totals |
| `obsMoveCycleMs`, `obsMoveAvgCycleMs` | "average decision time per agent cycle", section 13.3 |

`obsMoveSelfLoop` relies on `ActionMove.getPath()` **and** `getUsePosition()`. A one-element path with `usePosition = true` is a positional move inside the current area — a real move, produced by `DefaultExtActionMove`, not rung 4. Only the plain one-element path is the walled-in case.

### 4.4 Three numbers that read the whole police agent

- **`obsMoveNull` must be zero on every run.** State it in the report as an invariant with evidence. A single non-zero value means the never-null contract broke.
- **`obsMoveSelfLoop / obsMoveCycles`** is a trapped-agent rate. High values mean the police is walled in faster than it can cut out, which points at CLEAR or the map, not at movement.
- **`obsMoveRoad / obsMoveSearch`** tells you how much of the run the detector actually drove. This is also the acceptance test for the target channel: if `obsMoveRoadTarget` is `null` on every line, the detector's target is not arriving and section 2.2 has failed somehow.

`PRINT_OBSERVATION = false` silences the extra console line while keeping the counters.

---

## 5. Class design and OOP justification

```
ExtAction                          (adf.core.component.extaction, abstract)
      ^
      |  extends
DefaultExtActionMove               (adf.impl.extaction)
      ^
      |  extends
URFPoliceExtActionMove             (...police.improvement)
      ^                            - target resolution from the RoadDetector
      |  extends                   - fallback ladder, never-null contract
URFObservedExtActionMove           (...police.observation)
                                   - measurement only
```

| Rubric item (section 10) | How it is satisfied |
|---|---|
| Inheritance only for genuine *is-a* | Two real is-a steps. The URF module **is a** default move module with recovery added, and calls `super.calc()` for the normal path rather than reimplementing path following. The observed class **is a** URF move module with instrumentation. |
| Composition over globals | The RoadDetector, metrics and stuck detector are injected fields obtained from the framework registry. The static map they replaced was the one place the design reached outside dependency injection; it is gone. |
| Polymorphism through configuration | Tactics holds the abstract `ExtAction` type; this module holds the abstract `RoadDetector` type. Default, URF and observed URF are interchangeable by config key on both sides. |
| Encapsulation | `AIM_MARGIN`, `BEARING_COUNT`, `BEARING_STEP` are `private static final`; all state is private behind getters. |
| Controlled error handling | Null position, non-`Area` entities, undefined edges, null neighbours, a missing detector and missing blockades are all guarded; each degrades one rung down the ladder instead of throwing. |
| Failure recovery (section 7H) | The ladder *is* the failure recovery: no-progress movement, empty path and walled-in states each have a defined response. |

---

## 6. Configuration

`config/module_urf.cfg` (competition):

```
DefaultTacticsPoliceForce.RoadDetector  : sample_team.module.complex.police.improvement.URFRoadDetector
DefaultTacticsPoliceForce.ExtActionMove : sample_team.module.complex.police.improvement.URFPoliceExtActionMove
```

`config/module_urf_observed.cfg` (experiments):

```
DefaultTacticsPoliceForce.RoadDetector  : sample_team.module.complex.police.observation.URFObservedRoadDetector
DefaultTacticsPoliceForce.ExtActionMove : sample_team.module.complex.police.observation.URFObservedExtActionMove
```

`config/module_baseline.cfg` (reference run):

```
DefaultTacticsPoliceForce.ExtActionMove : adf.impl.extaction.DefaultExtActionMove
```

**The `RoadDetector` key must be present and must be the key tactics uses.** MOVE looks the detector up under `DefaultTacticsPoliceForce.RoadDetector`; a typo, or a different key, produces a second detector instance whose `calc()` is never called, and `getTarget()` returns null forever. That failure is silent — it looks exactly like a police agent that ignores its targets.

---

## 7. API assumptions to verify against your build

| Call | Used in | Risk |
|---|---|---|
| `moduleManager.getModule(String, String)` caches per key | constructor | **the load-bearing assumption.** Verified at runtime by `obsMoveRoadTarget` being non-null. |
| `RoadDetector.getTarget()` | `readRoadTarget` | none — public abstract on the ADF class |
| `ActionMove.getPath()`, `getUsePosition()` | `isSelfLoop` | none — confirmed against your `ActionMove.java` |
| `super.getAction()` | observed `calc` | none — `ExtAction` exposes it |
| `this.agentInfo` | `setTarget` | none — protected in `AbstractModule` |

---

## 8. Known interactions and limitations

- **Push-through can oscillate against a refuge door.** If the door road carries a blockade *outside* clear range, CLEAR returns null, MOVE sees `standingOnTarget` and rung 1 steps the agent off the door. Next cycle the sweep still wants that door and the agent walks back. `URFRefugeDoorSweep.ATTEMPT_BUDGET` breaks the loop after 30 cycles. `obsMovePush` climbing while `sweepConfirmed` stays flat is that exact pattern, and the fix is CLEAR's aim distance, not MOVE.
- **`neighbourIndex` and `bearingIndex` never reset.** Both rotate monotonically across the whole run, so the first exit or bearing tried after a success depends on total failure count so far. Deterministic per run, but not reproducible from position alone.
- **Rung 2 clears any nearby blockade, not the blocking one.** `anyNearbyBlockade()` returns the first found in the current or a neighbouring area. Combined with a rotating bearing this is a blind sweep, justified only because the agent is already stuck.
- **MOVE reads the detector but cannot influence it.** If the detector keeps selecting a road MOVE cannot reach, MOVE has no way to say so. The detector's own no-progress cooldown is the only feedback path, and it is distance-based rather than failure-based.
- **No coordination.** Two police agents walled into the same area sweep independent bearings and may cut the same blockade.
