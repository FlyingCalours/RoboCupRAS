# URFPoliceExtActionClear and URFObservedExtActionClear

Module family: **Police Force action selection** (assignment section 6, "Action Module" and section 7H, "Failure Recovery").

| File | Package | Role |
|---|---|---|
| `URFPoliceExtActionClear.java` | `urf.police.improvement` | Independent action module. Decides *what to clear right here, right now*. |
| `URFObservedExtActionClear.java` | `urf.police.observation` | Measurement wrapper. Adds cycle counters and timing, changes no decision. |

---

## 1. The one rule that defines this module

`URFPoliceExtActionClear.calc()` may return exactly two things:

- an **`ActionClear`**, or
- **`null`**.

It never returns `ActionMove`. That single constraint is what makes the police architecture clean, and it is worth stating plainly in the report:

```
think()
  |
  +-- roadDetector target ---> extActionClear.calc()
                                    |
                                    +-- ActionClear --> sent to the kernel, cycle ends
                                    +-- null        --> tactics falls through to extActionMove
```

Because tactics is a **priority fallthrough**, `null` is not a failure — it is the handoff signal. A clear module that returned `ActionMove` would silently take over the movement decision and make the MOVE module untestable. Keeping the return type narrow means each module can be ablated independently in section 13.2 experiments.

Note the division of labour with the detector: `URFRoadDetector` picks a *destination road*; this module only ever looks at blockades on the road the agent is **currently standing on**. The target passed to `setTarget()` is stored for logging and is deliberately not used for blockade selection.

---

## 2. Choosing the blockade, with real numbers

### 2.1 Distance to a polygon, not to a centre point

A blockade is a polygon, so measuring from the agent to the blockade's `(x, y)` centre would overestimate the distance and the agent would refuse to clear something it is practically touching.

Concrete case. The agent is at `P = (40000, 25000)`. One blockade has four apexes forming a 2000 x 2000 square:

```
A = (41000, 25000)   B = (43000, 25000)
D = (41000, 27000)   C = (43000, 27000)
```

`createCandidate()` walks each edge and calls `closestPointOnSegment()`. Take edge `A -> B`:

```
AB        = B - A          = (2000, 0)
|AB|²     = 2000² + 0²     = 4 000 000
P - A     = (-1000, 0)

projection = ((P-A) . AB) / |AB|²
           = ((-1000)(2000) + (0)(0)) / 4 000 000
           = -2 000 000 / 4 000 000
           = -0.5          -> clamped into [0, 1] -> 0.0

closest   = A + 0.0 * AB   = (41000, 25000)
distance  = hypot(40000-41000, 25000-25000) = 1000
```

The clamp is the important line. Without it the "closest point" would land *outside* the segment, behind vertex `A`, and the distance would be wrong. The same computation runs for all four edges and the smallest result wins.

Compare: the blockade centre is at `(42000, 26000)`, which is `hypot(2000, 1000) = 2236` away. Polygon distance gives 1000. With a scenario `clearRepairDistance` of, say, 1500, the centre-based method would refuse to clear a blockade the agent is standing right next to.

The general form, for a point $P$ and segment $A \to B$:

$$
t^{*} = \operatorname{clamp}_{[0,1]}\!\left(\frac{(P-A)\cdot(B-A)}{\lVert B-A\rVert^{2}}\right),
\qquad
Q = A + t^{*}(B-A),
\qquad
d = \lVert P-Q\rVert
$$

| Symbol | Java name | Meaning |
|---|---|---|
| $P$ | `(px, py)` | the police agent's physical position |
| $A, B$ | `(ax, ay)`, `(bx, by)` | the two endpoints of one polygon edge |
| $t^{*}$ | `projection` | how far along the edge the foot of the perpendicular falls, clamped so it stays on the segment |
| $Q$ | `(closestX, closestY)` | the nearest point of that edge |
| $d$ | `distance` | what is compared against `clearDistance` |

Two guards surround this: if the agent is **inside** the polygon the distance is forced to 0, and if apex data is not yet observed the module falls back to the blockade's `(x, y)`.

### 2.2 Tie-breaking

`isBetterCandidate()` prefers the smaller distance, and when two distances differ by less than `0.0001` it prefers the **smaller `EntityID`**. That is not cosmetic — floating-point ties would otherwise make the agent's choice depend on `HashSet` iteration order, and the run would not be reproducible. Section 13.2 requires repeated runs to be comparable, so determinism is a testing requirement, not a style preference.

---

## 3. Failure recovery: stagnation and directional clearing

### 3.1 Detecting stagnation

`updateClearProgress()` compares the blockade's repair cost with the previous cycle:

```
t=30  blockade b9, repairCost 5000   (new blockade)  -> stagnantClearCount = 0
t=31  blockade b9, repairCost 4200   (decreasing)    -> stagnantClearCount = 0
t=32  blockade b9, repairCost 4200   (no change)     -> stagnantClearCount = 1
t=33  blockade b9, repairCost 4200   (no change)     -> stagnantClearCount = 2
t=34  blockade b9, repairCost 4200   (no change)     -> stagnantClearCount = 3 = STAGNANT_CLEAR_LIMIT
      -> switch to directional recovery
```

A *decreasing* repair cost proves the clear command is landing. Unchanged or increasing cost means the command is being issued but nothing is happening — typically because the agent is aimed at a part of the blockade that the clear geometry cannot reach.

### 3.2 The recovery clear

`createRecoveryClear()` switches from the **entity form** `ActionClear(blockade)` to the **directional form** `ActionClear(x, y, blockade)`, aiming a point past the blockade boundary along the ray from the agent.

Same numbers as above, with `clearDistance = 10000`:

```
agent      P = (40000, 25000)
nearest    Q = (41000, 25000)        (from the polygon walk)
dx, dy       = 1000, 0
vectorLength = hypot(1000, 0) = 1000

desiredDistance = min( max(0, clearDistance - 100), vectorLength + RECOVERY_EXTENSION )
                = min( max(0, 9900),                1000 + 1000 )
                = min( 9900, 2000 )
                = 2000

scale       = 2000 / 1000 = 2.0
destination = (40000 + 1000(2.0),  25000 + 0(2.0)) = (42000, 25000)

-> ActionClear(42000, 25000, b9)
```

The aim point sits 1000 units *beyond* the blockade edge, so the swept clear region covers the boundary instead of stopping on it, while `clearDistance - 100` guarantees the point never falls outside the scenario's legal clear range. Two degenerate cases fall back to the plain entity form: a direction vector shorter than 1 unit (agent effectively on top of the blockade) and a desired distance shorter than 1 unit.

Crucially, recovery is still an `ActionClear`. The module solves its own failure rather than escaping into `ActionMove` and breaking the module contract.

---

## 4. The observation wrapper

### 4.1 Required parent edit — one word

`URFPoliceExtActionClear` is currently declared `final`, which makes extension impossible:

```java
// before
public final class URFPoliceExtActionClear extends ExtAction {

// after
public class URFPoliceExtActionClear extends ExtAction {
```

That is the **only** change needed. Nothing else in the parent moves.

### 4.2 Why the observed class does not export a second CSV row

This differs from the old `URFObservedExtActionClear extends DefaultExtActionClear`. `DefaultExtActionClear` knew nothing about metrics, so the wrapper owned the entire pipeline. `URFPoliceExtActionClear` already does all of it inside `calc()`:

```java
this.metrics.recordClearModuleResult(time, target, result, elapsedNanos);
if (this.result != null) {
  this.stuckDetector.evaluate(this.metrics);
  URFPoliceCsvExporter.export(this.metrics, this.stuckDetector);
  System.out.println(...);
}
```

If the subclass repeated that after `super.calc()`, every timestep would produce **two CSV rows and two stuck-detector evaluations**, and the experiment data would be silently wrong. So `URFObservedExtActionClear` adds only what the parent cannot see about itself:

| Field | Meaning |
|---|---|
| `obsClearTarget` | the target as handed over by tactics, captured in the overridden `setTarget()` |
| `obsCycles` | how many times the CLEAR module ran for this agent |
| `obsClear` | how many cycles produced an `ActionClear` |
| `obsHandoff` | how many cycles returned `null` and fell through to MOVE |
| `obsCycleMs` | wall-clock for this cycle, **including** the parent's CSV export cost |
| `obsAvgCycleMs` | running mean, which is the "average decision time per agent cycle" metric from section 13.3 |

`obsClear / obsCycles` is a directly reportable behaviour ratio: an agent at 5 % clear and 95 % handoff is wandering, one at 95 % clear is probably stuck on one road. Read it together with the detector's `abandonCount` and the picture is complete.

Set `PRINT_OBSERVATION = false` to keep the counters without the extra console line.

### 4.3 Optional iteration 2 — decision strings in the CSV

The parent's local `decision` variable (`CLEAR_NEAREST_BLOCKADE`, `RECOVERY_DIRECTIONAL_CLEAR`, `BLOCKADE_OUTSIDE_CLEAR_RANGE_HANDOFF`, `ROAD_CLEAR_OR_NO_LOCAL_BLOCKADE`) is not visible to the subclass. If the report needs a per-row decision trace — which is a bonus-mark item (section 15, "decision trace explaining why targets were selected") — the smallest change is to extract the publish block in the parent into a protected hook:

```java
protected void publishClearObservation(String decision, long elapsedNanos) { ... existing body ... }
```

and have the subclass override it, add its fields, and **not** call `super`. Leave this until the current version is measured; it is a second iteration, not a prerequisite.

---

## 5. Class design and OOP justification

```
ExtAction                           (adf.core.component.extaction, abstract)
      ^
      |  extends
URFPoliceExtActionClear             (urf.police.improvement)
      ^                             - owns blockade geometry
      |  extends                    - owns stagnation + recovery
URFObservedExtActionClear           (urf.police.observation)
                                    - owns measurement only
```

| Rubric item (section 10) | How it is satisfied |
|---|---|
| Inheritance only for genuine *is-a* | The observed class **is a** URF clear module with instrumentation. It overrides `calc()` and `setTarget()`, delegates to `super`, and never rewrites a decision. |
| No one large class holding all role logic | Target selection lives in `URFRoadDetector`, movement in the MOVE module, clearing here. Three files, three responsibilities. |
| Polymorphism through configuration | Tactics holds the abstract `ExtAction` type. Baseline, URF and observed URF are interchangeable by config key. |
| Encapsulation | `STAGNANT_CLEAR_LIMIT`, `RECOVERY_EXTENSION`, `MIN_VECTOR_LENGTH` are `private static final`. Observation state is private with getters. |
| Collections and immutable models | `ClearCandidate` and `ClosestPoint` are immutable private static nested classes — value objects, not public mutable structs. |
| Controlled error handling | Null position, non-`Road` position, undefined blockade list, undefined apexes, undefined `(x, y)` and zero-length vectors are all guarded; each degrades to `null` (handoff) rather than throwing. |
| Failure recovery (section 7H) | Stagnation detection plus directional recovery, with the fallback chain: directional clear → entity clear → `null` handoff. |

---

## 6. Configuration

`config/module_urf.cfg` (competition):

```
DefaultTacticsPoliceForce.ExtActionClear : urf.police.improvement.URFPoliceExtActionClear
```

`config/module_urf_observed.cfg` (experiments):

```
DefaultTacticsPoliceForce.ExtActionClear : urf.police.observation.URFObservedExtActionClear
```

`config/module_baseline.cfg` (reference run, section 13.2 row 1):

```
DefaultTacticsPoliceForce.ExtActionClear : adf.impl.extaction.DefaultExtActionClear
```

Three rows of the experiment matrix, zero code changes between them. Pair each with the matching `RoadDetector` line from the detector document so that "baseline", "improved URF" and "ablation" are each a single file the lecturer can inspect.

---

## 7. Deliberate limitations (state these in the report)

- **No target-driven clearing.** The module clears only on the current road. A blockade sitting between the agent and its target road is handled opportunistically as the agent walks over it, not planned for.
- **Stagnation state is per agent, not per road.** `previousClearBlockade` holds one blockade. Switching roads resets the history, so a road that stalled the agent earlier is not remembered here — the detector's cooldown covers that instead.
- **Repair cost may be undefined.** When `isRepairCostDefined()` is false the progress check is skipped and stagnation cannot be detected for that blockade.
- **`RECOVERY_EXTENSION = 1000` and `STAGNANT_CLEAR_LIMIT = 3` are untuned constants.** They are good ablation candidates: run the same map with limit 2 / 3 / 5 and report the score difference.
- **No coordination.** Two police agents on the same road both clear the nearest blockade, which duplicates effort. That belongs in a coordination module.
