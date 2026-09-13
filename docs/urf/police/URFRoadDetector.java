# URFRoadDetector and URFObservedRoadDetector

Module family: **Police Force target selection** (assignment section 7C, "Police Force Decision Making").

| File | Package | Role |
|---|---|---|
| `URFRoadDetector.java` | `urf.police.improvement` | Independent decision module. Chooses *which blocked road is worth going to*. |
| `URFObservedRoadDetector.java` | `urf.police.observation` | Measurement wrapper. Adds metrics recording, changes no decision. |

---

## 1. Where this sits in the police cycle

`DefaultTacticsPoliceForce.think()` is a **priority fallthrough**, not a pipeline:

```
think()
  |
  +-- roadDetector.calc().getTarget()      <-- URFRoadDetector answers here
  |       |
  |       +-- target != null --> extActionClear.setTarget(target).calc().getAction()
  |                                   |
  |                                   +-- action != null --> send it, done
  |                                   +-- action == null --> fall through
  |
  +-- search.calc().getTarget() --> extActionMove ...
```

Two consequences that shape the design:

1. **Returning `null` is a legitimate answer.** It means "no blocked road is worth a trip", and tactics then falls through to the search/exploration module. The detector must not invent a target just to have one.
2. **The detector does not decide what gets cleared.** `URFPoliceExtActionClear` only ever looks at blockades on the road the agent is *standing on*. So the detector's target is effectively a **destination for movement**, and the clear module handles whatever physically blocks the agent on the way. The two modules are decoupled on purpose.

---

## 2. The scoring rule, with real numbers first

### 2.1 A concrete snapshot

Simulation time `t = 42`. The police agent stands on road `R0`. Three blocked roads are currently known:

| Road | Touches a refuge? | Touches a burning building? | Casualties on/next to it | Platoon agents standing on it | Neighbour count | Distance from agent |
|---|---|---|---|---|---|---|
| `R1` | yes (Refuge `B7`) | no | 0 | 0 | 3 | 20 000 |
| `R2` | no | yes (`B12`) | 1 buried civilian | 1 fire brigade | 5 | 60 000 |
| `R0` | no | no | 0 | 0 (itself excluded) | 2 | 0 (agent is here) |

Each raw observation is first turned into a **term between 0 and 1**:

| Term | `R1` | `R2` | `R0` | How it is computed |
|---|---|---|---|---|
| refuge `r` | 1.0 | 0.0 | 0.0 | 1 if any neighbour is a `Refuge`, else 0 |
| agents `a` | 0.0 | 0.5 | 0.0 | `min(1, agents / 2)` — 1 agent = 0.5, 2 or more = 1.0 |
| fire `f` | 0.0 | 1.0 | 0.0 | 1 if any neighbour building has fieryness 1–3 |
| casualty `c` | 0.0 | 0.5 | 0.0 | `min(1, casualties / 2)` |
| corridor `k` | 0.75 | 1.0 | 0.5 | `min(1, degree / 4)` — 3/4, 5/4 capped, 2/4 |
| here `p` | 0.0 | 0.0 | 1.0 | 1 if this road *is* the agent's current position |

Multiply by the weights (`6, 5, 4, 3, 2, 3`) and add:

```
value(R1) = 6(1.0) + 5(0.0) + 4(0.0) + 3(0.0) + 2(0.75) + 3(0.0) = 7.50
value(R2) = 6(0.0) + 5(0.5) + 4(1.0) + 3(0.5) + 2(1.00) + 3(0.0) = 10.00
value(R0) = 6(0.0) + 5(0.0) + 4(0.0) + 3(0.0) + 2(0.50) + 3(1.0) =  4.00
```

Then divide by a distance penalty with `D = 50 000`:

```
score(R1) = 7.50  / (1 + 20000/50000) = 7.50  / 1.4 = 5.36
score(R2) = 10.00 / (1 + 60000/50000) = 10.00 / 2.2 = 4.55
score(R0) = 4.00  / (1 + 0/50000)     = 4.00  / 1.0 = 4.00
```

**Result at `t = 42`: target = `R1`.** `R2` is objectively more valuable (10.00 vs 7.50) but it is three times further away, and the distance penalty flips the order. This is the intended behaviour — a police agent that walks across the map for a marginally better road achieves nothing.

### 2.2 The next cycle, and why hysteresis exists

At `t = 43` the fire at `B12` spreads and a second civilian is found, so `value(R2)` rises to 11.5 and `score(R2)` becomes 5.23. Without hysteresis the agent would now turn around and chase `R2`, then turn back the moment the numbers moved again.

The **sticky multiplier** prevents that. The road selected in the previous cycle gets its score multiplied by 1.25:

```
score(R1) = 5.36 x 1.25 = 6.70    (previous target)
score(R2) = 5.23                  (challenger)
```

`R1` is kept. A challenger must be more than 25 % better to steal the target, which turns thrashing into a deliberate, hard-to-trigger switch.

### 2.3 The general formula

For a blocked road $R$, with the agent at position $P$:

$$
\text{value}(R) \;=\;
w_{\text{ref}}\,r(R) \;+\;
w_{\text{agt}}\,a(R) \;+\;
w_{\text{fire}}\,f(R) \;+\;
w_{\text{cas}}\,c(R) \;+\;
w_{\text{cor}}\,k(R) \;+\;
w_{\text{here}}\,p(R)
$$

$$
\text{score}(R) \;=\; \frac{\text{value}(R)}{1 + \dfrac{d(P,R)}{D}} \;\cdot\; s(R),
\qquad
s(R) = \begin{cases}
1.25 & R = \text{previous target} \\
1.00 & \text{otherwise}
\end{cases}
$$

Every symbol:

| Symbol | Java name | Meaning |
|---|---|---|
| $R$ | `road` | one candidate road that is known to carry at least one blockade |
| $P$ | `positionID` | the `EntityID` of the area the police agent currently occupies |
| $r(R)$ | `refugeTerm` | 1 if a neighbour of $R$ is a refuge, else 0 |
| $a(R)$ | `agentTerm` | number of other platoon agents standing on $R$, scaled to at most 1 |
| $f(R)$ | `fireTerm` | 1 if a neighbour of $R$ is burning, else 0 |
| $c(R)$ | `casualtyTerm` | buried or damaged humans on $R$ or its neighbours, scaled to at most 1 |
| $k(R)$ | `corridorTerm` | $\min(1,\ \deg(R)/4)$ where $\deg(R)$ is the neighbour count of $R$ |
| $p(R)$ | `currentTerm` | 1 if $R = P$, else 0 |
| $w_\bullet$ | `W_REFUGE_ACCESS`, … | the six weights: 6, 5, 4, 3, 2, 3 |
| $d(P,R)$ | `worldInfo.getDistance(...)` | straight-line distance in world units |
| $D$ | `DISTANCE_SCALE` | 50 000 — the distance at which the score is halved |
| $s(R)$ | `STICKY_MULTIPLIER` | hysteresis bonus for the incumbent target |

The weights are the **maximum** contribution of each term, because every term is bounded by 1. So the ordering of the weights *is* the policy: refuge access (6) outranks trapped agents (5), which outranks fire access (4), then casualties (3) and finally raw connectivity (2). Retuning the policy means editing six numbers, not editing logic.

---

## 3. "Avoid repeatedly clearing low-value blockades"

The guideline asks for this explicitly. It is handled by **progress tracking plus cooldown**, not by a value threshold, because a road that looks cheap on paper may still be the only way out.

Each cycle, `reviewCurrentTarget()` measures progress on the current target in one of two ways:

- **Agent is not yet on the target road** → progress means the distance shrank by at least `MIN_DISTANCE_PROGRESS` (1 000 units) below the best distance seen so far.
- **Agent is standing on the target road** → distance can no longer shrink, so progress means the *summed repair cost* of the blockades on that road fell below the best value seen so far.

If neither happens, `noProgressCount` increments. Worked through:

```
t=50  target R1, distance 18 000   -> best = 18 000, noProgress = 0
t=51  distance 18 000              -> no improvement, noProgress = 1
t=52  distance 17 900              -> only 100 < 1 000, noProgress = 2
...
t=58  noProgress = 8 = NO_PROGRESS_LIMIT
      -> cooldownUntil[R1] = 58 + 30 = 88
      -> abandonCount[R1] = 1
      -> target = null, detector re-scores from scratch
```

`R1` is then invisible to the scorer until `t = 88`, so the agent commits to the next-best road instead of oscillating in front of a blockade it cannot finish. `abandonCount` is kept per road and exposed via `getAbandonCount(EntityID)` so the report can show *which* roads defeated the agent.

A road whose blockades are observed to be **gone** is dropped without a cooldown — that is success, not failure.

---

## 4. Class design and OOP justification

```
RoadDetector                        (adf.core.component.module.complex, abstract)
      ^
      |  extends
URFRoadDetector                     (urf.police.improvement)
      ^                             - owns the scoring policy
      |  extends                    - owns cooldown and progress state
URFObservedRoadDetector             (urf.police.observation)
                                    - owns measurement only
```

| Rubric item (section 10) | How it is satisfied |
|---|---|
| Inheritance only for genuine *is-a* | `URFObservedRoadDetector` **is a** `URFRoadDetector` with instrumentation. It overrides `calc()`, calls `super.calc()`, and never rewrites a decision. |
| Composition | `PathPlanning` is injected via `ModuleManager` and held as a field, not inherited. |
| Polymorphism through configuration | Tactics only knows the abstract `RoadDetector` type. Swapping `SampleRoadDetector` → `URFRoadDetector` → `URFObservedRoadDetector` is a one-line config change with no recompilation of tactics. |
| Encapsulated scoring parameters | All weights are `private static final`. No public mutable field exists; observation reads go through `getLastReason()`, `getLastScore()`, `getLastCandidateCount()`. |
| Collections used appropriately | `HashMap` for cooldown/abandon history, `HashSet` for refuge and burning-building lookup, sorted `ArrayList` for the candidate ranking. |
| Controlled error handling | Null position, non-`Road` entities, undefined blockades, undefined edges, undefined fieryness and empty paths are all guarded and degrade to "no target" instead of throwing. |
| Failure recovery (section 7H) | Unreachable target → path check falls through to `SCORED_UNREACHABLE_FALLBACK`. No progress → cooldown. Stale target → dropped when blockades disappear. |

---

## 5. Configuration

`URFRoadDetector` asks the `ModuleManager` for one sub-module:

```
URFRoadDetector.PathPlanning : adf.impl.module.algorithm.DijkstraPathPlanning
```

Because `URFObservedRoadDetector` extends `URFRoadDetector`, it reads the **same** key — the constructor chain runs the parent constructor. Wire the detector itself with whatever key your tactics class already uses for the road detector, for example:

`config/module_urf.cfg` (competition, no instrumentation):

```
DefaultTacticsPoliceForce.RoadDetector : urf.police.improvement.URFRoadDetector
URFRoadDetector.PathPlanning           : adf.impl.module.algorithm.DijkstraPathPlanning
```

`config/module_urf_observed.cfg` (experiments, CSV evidence):

```
DefaultTacticsPoliceForce.RoadDetector : urf.police.observation.URFObservedRoadDetector
URFRoadDetector.PathPlanning           : adf.impl.module.algorithm.DijkstraPathPlanning
```

`config/module_urf_astar.cfg` (ablation on the planner only):

```
DefaultTacticsPoliceForce.RoadDetector : urf.police.observation.URFObservedRoadDetector
URFRoadDetector.PathPlanning           : adf.impl.module.algorithm.AStarPathPlanning
```

Three configs, one codebase — this is the "separate configuration files" requirement (section 11, Configuration) and gives a free ablation axis for section 13.2.

---

## 6. What the observed wrapper adds

`URFObservedRoadDetector.calc()` does exactly four things after `super.calc()` returns:

1. measures the wall-clock nanoseconds of the selection itself (`getLastCalcNanos()`);
2. records `time`, area position, physical `X` and `Y` into `URFPoliceMetrics`;
3. records the selected target into `URFPoliceMetrics`;
4. returns.

It deliberately **does not print and does not export**. The CSV row for a timestep is written once, by whichever action module produced the final autonomous action — that ordering is what keeps one row per timestep instead of two. Position and X/Y are recorded here and only here, because the stuck detector needs physical displacement and the detector is the first module to run in the cycle.

---

## 7. API assumptions to verify against your `adf-core-java` build

These are the only non-obvious calls used. If one does not exist in your checkout, it will fail at compile time, not at runtime.

| Call | Used for |
|---|---|
| `moduleManager.getModule(String, String)` | sub-module selection |
| `registerModule(AbstractModule)` | lifecycle propagation to `PathPlanning` |
| `worldInfo.getEntitiesOfType(StandardEntityURN...)` | candidate and snapshot scans |
| `worldInfo.getDistance(EntityID, EntityID)` | distance penalty |
| `road.isEdgesDefined()` / `road.getNeighbours()` | corridor degree and neighbour scan |
| `road.isBlockadesDefined()` / `road.getBlockades()` | candidate filter |
| `blockade.isRepairCostDefined()` / `getRepairCost()` | on-road progress measurement |
| `building.isFierynessDefined()` / `getFieryness()` | fire term |
| `human.isPositionDefined()` / `isBuriednessDefined()` / `isDamageDefined()` / `isHPDefined()` | casualty and blocked-agent terms |
| `pathPlanning.setFrom / setDestination / calc / getResult` | reachability check |

Note: `isNeighboursDefined()` does **not** exist — `isEdgesDefined()` is the correct guard, as already recorded for this project.

---

## 8. Deliberate limitations (state these in the report)

- **No cluster ownership.** Every police agent scores the whole known map, so two agents can pick the same road. Coordination belongs in a separate module and is the natural next iteration.
- **No radio messages.** Blocked roads learned from teammates via `MessageRoad` are not merged, so the candidate set is limited to what this agent has personally seen.
- **Corridor value is a degree proxy**, not true betweenness centrality. A road with many neighbours is usually a junction, but a genuine bottleneck analysis (section 15, bonus marks) would need a precomputed centrality pass.
- **"Blocked agent" is inferred**, not observed. A teammate standing on a blocked road is *assumed* stuck; the agent cannot see another agent's failed MOVE commands.
- **Distance is straight-line**, not path length. Path length is only computed for the top four candidates, because running the planner over every blocked road each cycle is too expensive.
