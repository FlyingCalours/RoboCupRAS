# RCRS Agent Simulation Report

| Name | Student ID | Programme |
| -- | -- | -- |
| Chong Zi Yang | 2401892 | AM |
| Carlos Wong | 2303326 | AM |

> **HOW TO USE THIS DRAFT**
>
> Every block marked **▢ NEEDS YOU** is something only you can supply — a number from a run, a screenshot, a decision, or a fact I could not verify from the code I have seen. A complete checklist of them is in Appendix A. Delete every one of these blocks before submitting.
>
> Placeholders `[score]`, `[n]`, `[figure]` are for data you will fill in.

---

## Table of Contents

1. [Introduction and Problem Statement](#1-introduction-and-problem-statement)
2. [Baseline Reproduction](#2-baseline-reproduction)
3. [Architecture](#3-architecture)
4. [Object-Oriented Design](#4-object-oriented-design)
5. [Implementation](#5-implementation)
6. [Testing](#6-testing)
7. [Experiments and Results](#7-experiments-and-results)
8. [Discussion](#8-discussion)
9. [Limitations and Future Work](#9-limitations-and-future-work)
10. [Conclusion](#10-conclusion)
11. [Individual Contributions](#11-individual-contributions)
12. [References](#12-references)
13. [Appendix A — Submission Checklist](#appendix-a--submission-checklist)

---

## 1. Introduction and Problem Statement

### 1.1 Context

RoboCup Rescue Agent Simulation (RCRS) is a disaster-response simulation in which autonomous agents operate in a virtual city after an earthquake. Buildings burn, roads are blocked by debris, and civilians are injured or trapped. Agents perceive only part of the world, communicate over a bandwidth-limited radio channel, and must act within a fixed number of timesteps.

The three platoon roles are interdependent. Ambulance Teams cannot reach victims along blocked roads; Fire Brigades cannot reach fires. **Police Force is therefore a multiplier role**: its own score contribution is indirect, but its failure caps what every other role can achieve.

### 1.2 Scope decision

Our group focused development on the Police Force role. Two reasons:

1. We received a partial Police implementation (`URFPoliceExtActionClear`) from Dr. Mohammad Babrdel Bonab, which gave us a concrete, non-trivial starting point rather than a blank file.
2. Early observation runs showed Police failing in ways that visibly blocked the other two roles, making it the highest-leverage target.

### 1.3 API preparation

Neither `adf-core-java` nor `rcrs-server` ships developer documentation. Before writing code we built our own API reference by reading both repositories and extracting the classes and methods relevant to agent development. We used **markmap** to render the class relationships as an interactive parent–child map rather than working through 100+ files linearly. A large language model (Claude Opus 5) was used as a reading aid during this extraction.

> **▢ NEEDS YOU** — The markmap command in `Development_Flow.md` is a placeholder (`some commands`). Paste the real command, and state where the generated reference lives in the ZIP. Also state plainly how the LLM was used (reading aid / code review / drafting), since rubric 13.7 penalises undocumented copied code and an explicit, honest statement protects you.

### 1.4 Problems identified

Three concrete failures were observed in baseline runs, each reproducible across maps.

**Issue 1 — Refuge entrances never cleared.** We define a *door* as a road sharing an edge with a refuge. When a door carried a blockade, ambulances could not deliver patients and civilians accumulated outside the refuge and died. No police agent ever cleared a door.

**Issue 2 — Police agents freeze.** Police stopped making progress and accumulated in one location. The count of frozen agents grew monotonically with timestep.

**Issue 3 — Blocked ambulances ignored.** An ambulance stuck behind a blockade was passed by police agents that continued clearing low-value blockades elsewhere.

### 1.5 Measurable objectives

| # | Objective | Metric | Source |
| -- | -- | -- | -- |
| 1 | Refuge doors are inspected and cleared | `sweepConfirmed` reaches `sweepDoors` | escort/sweep log fields |
| 2 | Police do not freeze | `obsMoveSelfLoop / obsMoveCycles`, `confirmedStuckEvents` | CSV |
| 3 | Blocked ambulances are freed | `escortFreed / escortStarted` | escort log fields |
| 4 | No agent ever idles involuntarily | `obsMoveNull = 0` | CSV invariant |

---

## 2. Baseline Reproduction

### 2.1 Environment

| Item | Assignment recommendation | Our environment |
| -- | -- | -- |
| Operating system | Windows 11 | Arch Linux |
| Shell | Git Bash | bash |
| Java | OpenJDK Temurin 21 | Openjdk version "26.0.2.1" 2026-08-18 |
| Root folder | `C:/Users/<username>/Desktop/RoboCupRescue/` | `~/Documents/java/robocup/` |
| Repositories | rcrs-server, adf-core-java, adf-sample-agent-java | same |

We developed on Linux rather than the recommended Windows 11 setup. Because the build is driven entirely by the supplied Gradle wrapper and the launch scripts are POSIX shell, no source change was required for the platform difference. The README supplies both command sets so the submission reproduces on either platform.

### 2.2 Build and run

```bash
# Clone urls
git clone https://github.com/roborescue/rcrs-server.git
git clone https://github.com/roborescue/adf-core-java.git

# Optional to clone our repo, unzip is fine
git clone https://github.com/FlyingCalours/RoboCupRAS.git

# Compile server
cd rcrs-server && ./gradlew completeBuild

cd ..

# Compile ADF core
cd adf-core-java && ./gradlew clean && ./gradlew build

cd ..

# One Tab Run Server
cd rcrs-server/scripts && ./start.sh

# One Tab Run Our Repo, Depends the name, we have 2
cd G3_CS_UECS1044_GA && ./launch -all

# Run Specific Map on Server
cd rcrs-server/scripts && ./start.sh -m ../maps/montreal/map -c ../maps/montreal/config
```

### 2.3 Checkpoint 0 evidence

| Evidence | Status |
| -- | -- |
| `java -version` and `which java` | ![](img/java_version_and_which_java.png) |
| BUILD SUCCESSFUL for all three repositories | ![](img/success_build_3_repo.png) |
| Server GUI and viewer running | ![](img/test_basic_map_can_work.png) |
| Three platoon agent types connected | ![](img/connect_3_agent.png) |

---

## 3. Architecture

### 3.1 Layers

```
rcrs-server          kernel, simulators, viewer         not modified
      |
adf-core-java        lifecycle, modules, messaging      not modified
      |
adf-sample-agent-java  reference team                   used as baseline
      |
URF (ours)           sample_team.module.complex.police  our contribution
```

We modified neither the server nor the ADF core. All URF code lives under `sample_team.module.complex.police`, split into two packages:

| Package | Responsibility |
| -- | -- |
| `...police.improvement` | decision logic — what the agent does |
| `...police.observation` | measurement — what we record about it |

This split is deliberate. Every observation class is a subclass of the corresponding improvement class and overrides only measurement, never a decision. Switching between the two is a configuration change, so an experiment and a competition run execute identical decision code.

### 3.2 The tactics fallthrough

The single most important structural fact about the Police role is that `DefaultTacticsPoliceForce.think()` is a **priority fallthrough**, not a pipeline:

```
think()
  |
  +-- roadDetector.calc().getTarget()
  |        |
  |        +-- target != null --> extActionClear.setTarget(target).calc()
  |                                    +-- ActionClear --> send, cycle ends
  |                                    +-- null        --> fall through
  |
  +-- search.calc().getTarget() -----> extActionMove.setTarget(target).calc()
                                           +-- action --> send, cycle ends
                                           +-- null   --> ActionRest
```

Three consequences shaped the entire design:

1. **`null` from CLEAR is a handoff signal, not a failure.** It is how control reaches the MOVE module.
2. **`null` from MOVE is a real failure.** It ends in `ActionRest`, which is indistinguishable from a crashed agent in the viewer. MOVE therefore carries a never-null contract.
3. **Tactics hands MOVE the *Search* target, not the RoadDetector's target.** Making the detector drive navigation required an explicit channel, discussed in §5.3.

> **▢ NEEDS YOU** — Confirm this flow against the `DefaultTacticsPoliceForce` source in your `adf-core-java` checkout and cite the file path. The report should reference the real code, not our reconstruction of it.

### 3.3 Module responsibilities

| Module | Class | Decides |
| -- | -- | -- |
| Target selection | `URFRoadDetector` | *which road is worth going to* |
| Action (clear) | `URFPoliceExtActionClear` | *what to clear within reach, right now* |
| Action (move) | `URFPoliceExtActionMove` | *where to step, and what to do when stepping fails* |

Note the division: the detector chooses a **destination**; the CLEAR module ignores that destination entirely and cuts whatever lies within clear range of the agent's current position. A police agent walking to a refuge door therefore clears every blockade it steps over on the way, at no cost to the sweep.

### 3.4 Workflow diagram

![](img/A_rcrs-overall-sequence.png)

---

## 4. Object-Oriented Design

### 4.1 Class structure

![](img/overall-class-diagram.png)

### 4.2 How the design satisfies Section 10

| Requirement | How it is met |
| -- | -- |
| **Inheritance only for genuine is-a** | Each observed class **is a** URF module with instrumentation added. It overrides `calc()`, calls `super.calc()`, and never rewrites a decision. `URFPoliceExtActionMove` **is a** default move module with recovery added, and delegates the normal path to `super.calc()` rather than reimplementing path following. |
| **No one large class holding all role logic** | Target selection, clearing and movement are three separate classes with no shared state. Door sweeping and ambulance escort are two further classes that hold state only and issue no actions. |
| **Composition** | `PathPlanning` is injected into `URFRoadDetector` via `ModuleManager`. `URFPoliceExtActionMove` composes a `RoadDetector` reference the same way. Metrics and the stuck detector are per-agent objects obtained from registries, not inherited. |
| **Polymorphism through configuration** | Tactics holds only the abstract `ExtAction` and `RoadDetector` types. Baseline, URF and observed URF are interchangeable by editing one line of a `.cfg` file, with no recompilation. |
| **Encapsulated parameters, no public mutable fields** | All scoring weights and thresholds are `private static final`. All state is private behind getters. |
| **Collections used appropriately** | `HashMap` for cooldown and abandon history, `HashSet` for refuge and burning-building lookup, sorted `ArrayList` for candidate ranking, `ConcurrentHashMap` for per-agent registries, immutable private nested classes (`ClearCandidate`, `ClosestPoint`, `Sighting`) as value objects. |
| **Controlled error handling** | Null position, non-`Area` entities, undefined edges, undefined blockades, undefined fieryness, missing repair cost and empty paths are each guarded. Every guard degrades to a defined fallback rather than throwing — the CLEAR module hands off, the MOVE module drops one rung down its ladder. |
| **Enums** | *See gap note below.* |

> **▢ NEEDS YOU — RUBRIC GAP** — Section 10 asks for **enums** for agent states, task types or target statuses. Our decision reasons are currently `public static final String` constants (`REASON_REFUGE_DOOR_SWEEP`, `SOURCE_ROAD_TARGET`, and so on). Converting these to two enums — say `TargetReason` and `MoveSource` — is a mechanical refactor of perhaps thirty minutes, and it converts a visible rubric miss into a hit. Strongly recommended before submission. If you do it, delete this block and the row above.

> **▢ NEEDS YOU — RUBRIC GAP** — Section 10 also asks for **interfaces** for interchangeable strategies (`PathPlanner`, `TargetSelector`, and so on). We currently rely on ADF's abstract classes rather than declaring our own interfaces. This is defensible — extending the framework's abstractions is the idiomatic ADF approach — but it should be argued explicitly in the report rather than left unmentioned. Decide whether to argue it or to extract one interface, and say which here.

### 4.3 UML class diagram

![](img/urf_police_class_diagram.png)

---

## 5. Implementation

### 5.1 `URFRoadDetector` — police target selection

The detector answers one question per cycle: which road is worth travelling to? It runs a three-level priority ladder.

| Priority | Source | Rationale |
| -- | -- | -- |
| 1 | stuck ambulance escort | a blocked rescue vehicle is costing lives now |
| 2 | refuge door sweep | unobserved refuge entrances, which scoring cannot reach |
| 3 | access-value scoring | everything else |

Returning `null` is legitimate and means "nothing is worth a trip"; tactics then falls through to the Search module.

#### 5.1.1 Access-value scoring

Each known blocked road is scored on six terms, every term normalised to $[0, 1]$:

$$
\text{value}(R) = w_{\text{ref}}r + w_{\text{agt}}a + w_{\text{fire}}f + w_{\text{cas}}c + w_{\text{cor}}k + w_{\text{here}}p
$$

$$
\text{score}(R) = \frac{\text{value}(R)}{1 + \dfrac{d(P, R)}{D}} \cdot s(R)
$$

| Symbol | Meaning | Weight |
| -- | -- | -- |
| $r$ | road touches a refuge | 6.0 |
| $a$ | other platoon agents standing on it, scaled | 5.0 |
| $f$ | road touches a burning building | 4.0 |
| $c$ | buried or damaged humans on it or adjacent, scaled | 3.0 |
| $k$ | connectivity, $\min(1, \deg(R)/4)$ | 2.0 |
| $p$ | the agent is already standing on it | 3.0 |
| $d(P,R)$ | straight-line distance from the agent | — |
| $D$ | `DISTANCE_SCALE`, 50 000 world units | — |
| $s(R)$ | 1.25 if $R$ was the previous target, else 1.0 | — |

Because every term is bounded by 1, each weight *is* the maximum contribution of its term, so the ordering of the weights is the policy. Retuning means editing six numbers, not editing logic.

The hysteresis factor $s(R)$ is what prevents target thrashing: a challenger must beat the incumbent by more than 25 % to take over.

**Worked example.** Agent at road `R0`. Three blocked roads known:

| Road | $r$ | $a$ | $f$ | $c$ | $k$ | $p$ | value | distance | score |
| -- | -- | -- | -- | -- | -- | -- | -- | -- | -- |
| R1 | 1.0 | 0 | 0 | 0 | 0.75 | 0 | 7.50 | 20 000 | 5.36 |
| R2 | 0 | 0.5 | 1.0 | 0.5 | 1.0 | 0 | 10.00 | 60 000 | 4.55 |
| R0 | 0 | 0 | 0 | 0 | 0.50 | 1.0 | 4.00 | 0 | 4.00 |

R2 is objectively more valuable but three times further away; the distance penalty flips the order and R1 wins. This is intended — a police agent that crosses the map for a marginally better road achieves nothing.

#### 5.1.2 Avoiding repeated low-value clearing

Section 7C of the assignment requires that agents avoid repeatedly clearing low-value blockades. We handle this with **progress tracking and cooldown** rather than a value threshold, because a cheap-looking road may be the only way out.

- Off the target road, progress means the distance shrank by at least 1 000 units below the best seen.
- On the target road, distance cannot shrink, so progress means the summed blockade repair cost fell below the best seen.

Eight consecutive cycles without progress puts the road in a 30-cycle cooldown and records an abandonment. `getAbandonCount(EntityID)` exposes which roads defeated the agent.

### 5.2 `URFPoliceExtActionClear` — clearing

The module returns either an `ActionClear` or `null`, never an `ActionMove`. Keeping the return type narrow is what lets each module be ablated independently.

#### 5.2.1 Polygon distance, not centre distance

A blockade is a polygon. Measuring to its centre overestimates distance and makes the agent refuse to clear something it is touching. `createCandidate()` walks each polygon edge and finds the closest point on each segment:

$$
t^{*} = \operatorname{clamp}_{[0,1]}\left(\frac{(P-A)\cdot(B-A)}{\lVert B-A\rVert^{2}}\right), \quad Q = A + t^{*}(B-A), \quad d = \lVert P - Q\rVert
$$

The clamp is the essential part: without it the "closest point" can land outside the segment and the distance is wrong.

**Worked example.** Agent at $(40000, 25000)$, blockade a 2000×2000 square with corner $A = (41000, 25000)$. Polygon distance gives 1000; centre distance gives 2236. With a clear range of 1500 the centre method would refuse a blockade the agent is standing beside.

#### 5.2.2 Stagnation and directional recovery

`updateClearProgress()` compares blockade repair cost cycle to cycle. A falling cost proves the command is landing; unchanged cost for `STAGNANT_CLEAR_LIMIT = 3` cycles triggers recovery, which switches from the entity form `ActionClear(blockade)` to the directional form `ActionClear(x, y, blockade)`, aiming a point beyond the blockade boundary but inside the legal clear range. Recovery is still an `ActionClear` — the module solves its own failure rather than escaping into `ActionMove`.

#### 5.2.3 Widening the search beyond the current road (Issue 2 fix)

The original module searched only the agent's current road. This was the root cause of Issue 2:

```
police on road A, blockade on road B covering the passage
  CLEAR:  no blockade near me on road A     -> null
  MOVE:   edge A->B reports passable        -> ActionMove(A, B)
  traffic simulator: refused, blockade in the way
  next cycle: rotate to another exit, refused, ... indefinitely
```

`Edge.isPassable()` means "not a wall" — it says nothing about blockades. So the agent was permanently unable to reach the one action that would free it.

The kernel gates a clear command on **distance**, not on road membership. We therefore replaced `findCurrentRoadCandidate()` with `findNearbyCandidate()` and a helper `searchAreas()`, which collects the agent's area plus every neighbouring area, passable or not. All downstream logic — geometry, ordering, tie-break, stagnation, recovery, observation — is unchanged, and the existing `candidate.distance <= clearDistance` guard still enforces legality.

> **▢ NEEDS YOU** — Verify empirically and state the result: after the change, does a blockade on a *neighbouring* road actually show a falling `repairCost` over successive cycles? If yes, the distance-gate reasoning is confirmed and you can state it as a verified finding. If the cost never moves, the kernel is rejecting cross-road clears and this section must be rewritten.

### 5.3 `URFPoliceExtActionMove` — movement and last-resort recovery

#### 5.3.1 The never-null contract

`ActionRest` is indistinguishable from a crashed agent. MOVE must produce something legal every cycle, so the chain ends with a move-to-self rather than a null: a legal action that produces a log line and lets the stuck detector record "acting but not displacing".

#### 5.3.2 Reaching the road target without shared static state

Tactics hands MOVE the Search target, so the road target needs another channel. Our first design published it into a static map. That design failed twice, and both failures are worth reporting:

- **It can be half-wired and still compile.** A static accessor with readers but no writer is a valid program. The map returned `null` forever, MOVE silently fell back to the Search target, and the refuge sweep selected doors nobody walked to — with no error anywhere.
- **It goes stale.** Tactics only calls `extActionClear.setTarget(...)` when the target is non-null, so a publish on the CLEAR side could never publish a `null`, and the map would retain a target the detector had already abandoned.

We replaced it by asking the framework for the detector itself:

```java
this.roadDetector = moduleManager.getModule(
    "DefaultTacticsPoliceForce.RoadDetector",
    "sample_team.module.complex.police.improvement.URFRoadDetector");
```

`ModuleManager` caches one instance per configuration key, so this is the same object tactics uses. Each cycle MOVE reads `roadDetector.getTarget()`. No shared mutable static, nothing to forget to publish, never stale, and no URF type coupling — `getTarget()` is declared on ADF's abstract `RoadDetector`, so the module also works against a baseline detector.

Ordering is guaranteed by the fallthrough: the detector runs at the top of `think()`, MOVE at the bottom. The detector's `calc()` is deliberately *not* called here, and the detector is deliberately *not* passed to `registerModule`, since its lifecycle is already driven by tactics.

#### 5.3.3 The fallback ladder

| Rung | Condition | Action |
| -- | -- | -- |
| 1 | a neighbour behind a passable edge | `ActionMove(position, next)` |
| 2 | no passable edge, blockade in reach | `ActionClear(x, y, blockade)` on a rotating bearing |
| 3 | no passable edge, no blockade in reach | `ActionMove(position, any neighbour)` |
| 4 | no neighbours at all | `ActionMove(position)` — legal no-op |

Exit choice rotates (`neighbourIndex % options.size()`), so an agent that fails through one exit tries a different one next cycle. Rung 3 is intentional: a refused move costs one cycle and produces a log entry, while doing nothing costs one cycle and produces nothing.

Rung 2 sweeps a deterministic rotating bearing. With `BEARING_COUNT = 8` and `BEARING_STEP = 3` — coprime — the sequence is 0°, 135°, 270°, 45°, 180°, 315°, 90°, 225°, covering every direction once before repeating, with consecutive attempts far apart. No random source is used, so the escape sequence is reproducible across runs.

This is the opposite philosophy from §5.2.1, where the geometry is computed exactly. Here the agent is already stuck, so precision has demonstrably failed and a blind sweep is the correct escalation.

### 5.4 `URFRefugeDoorSweep` — Issue 1

#### 5.4.1 Why scoring alone cannot fix it

`URFRoadDetector` gives refuge-adjacent roads the highest weight in the policy, and it still never sent anyone to a door. The reason is fog of war:

```
road.isBlockadesDefined()  ==  "has this agent ever looked at this road?"
```

An unobserved blockade is not in `worldInfo`, so the road is not a candidate, so it scores nothing, so nobody goes, so nobody observes it. The loop closes on itself. Raising the weight to 600 would change nothing. **The only way out is to go and look** — active perception, not better scoring.

#### 5.4.2 Mechanism

A door is a `Road` sharing an edge with a `Refuge`. Doors are collected once, deduplicated and sorted by entity ID so every agent walks them in the same order and runs stay comparable. Each cycle:

| Step | Rule |
| -- | -- |
| 1. Opportunistic confirm | standing on any door that is observed clear → mark confirmed, suppress 50 cycles |
| 2. Give up | current door held ≥ 30 cycles → mark abandoned, suppress 50 cycles |
| 3. Commit | a door is held → return it unchanged |
| 4. Choose | otherwise, nearest non-suppressed door, ties by entity ID |

Step 1 shortens the sweep considerably, since doors often sit on the route between refuges. Step 2 is the failure recovery: without it an agent walks at an unreachable entrance for the rest of the scenario. The 50-cycle recheck exists because doors go stale — a building collapsing at t=90 can drop fresh debris on a door confirmed at t=20.

`SWEEP_DEADLINE = 150` stops the sweep issuing doors late in the run. This is a safety valve: worst case the sweep costs `doorCount × 30` cycles, which on a map with many refuges can consume the whole scenario and starve the scoring logic.

### 5.5 `URFStuckAgentEscort` — Issue 3

#### 5.5.1 No new message type was needed

The obvious design is a custom ambulance-side "help me" broadcast. A probe run showed this unnecessary. Every police agent already receives the full ambulance roster every cycle:

```
ambulance=24   on 660 of 768 logged cycles
ambulance=0    only at t=1, t=2, t=3 (comms warm-up)
```

24 is the entire ambulance team and the count is flat, meaning **map-wide range, not line of sight**. So the feature is implemented entirely inside the Police role by consuming traffic already on the channel: zero ambulance-side code, zero configuration change, zero extra bandwidth.

The same probe showed `MessageRoad` running at 48–452 messages per cycle against a single channel, so adding another broadcast type would have pushed at a real limit. We record this as a design decision made on measured evidence rather than assumption.

#### 5.5.2 The stall signal

`MessageAmbulanceTeam` carries id, position, action, buriedness and HP. The action constants are what make the rule clean:

> An ambulance is stuck when it reports `ACTION_MOVE` from an unchanged position for 3 consecutive cycles.

An ambulance that is resting, rescuing, loading or unloading is stationary *on purpose*; position stagnation alone cannot tell those apart from being blocked. Buried and dead ambulances are excluded — police clearing cannot help either.

The release condition is the elegant part: when the ambulance reports a **new position**, it is travelling again. That is direct observable evidence the escort worked, so `escortFreed / escortStarted` is a causal measure rather than a correlation with final score. `ESCORT_BUDGET = 20` covers only the failure path.

As with the door sweep, the candidate test deliberately does **not** require the ambulance's road to carry a known blockade — that would be the same fog-of-war trap, rejecting exactly the cases worth investigating.

### 5.6 Observation layer

| Class | Records |
| -- | -- |
| `URFObservedRoadDetector` | area position, physical X/Y, selected target, selection time |
| `URFObservedExtActionClear` | clear-vs-handoff counts, cycle time |
| `URFObservedExtActionMove` | target source, ladder rung reached, never-null assertion, cycle time |
| `URFPoliceMetrics` | per-agent action history, streaks, module call counts |
| `URFPoliceStuckDetector` | displacement-based stuck status and confirmed stuck events |
| `URFPoliceCsvExporter` | one CSV row per agent per timestep |

Each observed class adds only what its parent cannot see about itself; none re-records or re-exports, because a duplicate export would double every row *and* evaluate the stuck detector twice per cycle, corrupting its displacement history. `URFObservedRoadDetector` is the single owner of position recording.

The exporter writes to `logs/urf/police/police-observation-v2-<runId>-agent-<id>.csv` with schema version `URF_POLICE_OBS_V2` and 44 columns, guarded against duplicate export of the same agent-timestep.

---

## 6. Testing

### 6.1 Required test levels (§12.1)

| Level | Method | Result |
| -- | -- | -- |
| Build | `./gradlew clean build -x test` on all three repositories | [pass/fail] |
| Connection | all three platoon types connect via `./launch.sh -local -all` | [pass/fail] |
| Module | each strategy selectable by editing one `.cfg` line | [pass/fail] |
| Behaviour | viewer shows intended actions, no persistent idle | [pass/fail] |
| Exception | missing/undefined entity data does not terminate the team | [pass/fail] |
| Regression | baseline configuration still runs after URF code added | [pass/fail] |
| Performance | baseline vs URF under identical scenario conditions | see §7 |

### 6.2 Unit-testable logic

The following are pure functions of their inputs and can be tested without a running server:

| Function | Test |
| -- | -- |
| `closestPointOnSegment` | the §5.2.1 worked example; projection clamping at both ends; zero-length segment |
| `accessValue` | the §5.1.1 table; each term in isolation |
| `isBetterCandidate` | distance ordering; entity-ID tie-break within 0.0001 |
| `nextBearing` | all eight bearings visited before repeat |
| `isSelfLoop` | one-element path with and without `usePosition` |

> **▢ NEEDS YOU** — Rubric 4 rewards systematic testing. Even three or four JUnit tests over the functions above would be disproportionately valuable, because they are the only parts of the system testable without a full simulation. State whether you wrote them; if not, say so honestly in §9 rather than leaving it unmentioned.

### 6.3 Runtime invariants

Two assertions checked from the logs of every run:

1. `obsMoveNull = 0` — the MOVE never-null contract held.
2. Exactly one CSV row per agent per timestep — no duplicate export.

---

## 7. Experiments and Results

### 7.1 Protocol

Three maps, three runs each, identical scenario, server version and time limit. Only the `.cfg` file differs between configurations.

> **▢ NEEDS YOU — RUBRIC GAP** — Section 11 requires baseline and improved strategies to be selectable through **separate configuration files**, and §7I requires independent configurations so the sample config is not overwritten. You currently have a single `module.cfg`. Create at minimum:
>
> - `module_baseline.cfg` — all sample/default modules
> - `module_urf.cfg` — URF improvement classes, no observation
> - `module_urf_observed.cfg` — URF observation classes
>
> plus one ablation config per row in §7.4. This is a Pass/Fail row and takes fifteen minutes.

### 7.2 Baseline: `URFPoliceExtActionClear` vs `DefaultExtActionClear`

Comparing the lecturer-supplied clearing logic against the ADF default, all else equal.

`URFPoliceExtActionClear`:

| | Montreal | Paris | Berlin |
| -- | -- | -- | -- |
| Run 1 | [score] | [score] | [score] |
| Run 2 | [score] | [score] | [score] |
| Run 3 | [score] | [score] | [score] |
| Mean ± SD | [x ± y] | [x ± y] | [x ± y] |

`DefaultExtActionClear`:

| | Montreal | Paris | Berlin |
| -- | -- | -- | -- |
| Run 1 | [score] | [score] | [score] |
| Run 2 | [score] | [score] | [score] |
| Run 3 | [score] | [score] | [score] |
| Mean ± SD | [x ± y] | [x ± y] | [x ± y] |

### 7.3 Full URF vs baseline

| Configuration | Montreal | Paris | Berlin | Mean |
| -- | -- | -- | -- | -- |
| Official baseline | [score] | [score] | [score] | [score] |
| Improved URF | [score] | [score] | [score] | [score] |

Supporting behaviour metrics (URF only):

| Metric | Montreal | Paris | Berlin |
| -- | -- | -- | -- |
| `sweepConfirmed / sweepDoors` | [n/n] | [n/n] | [n/n] |
| `escortFreed / escortStarted` | [n/n] | [n/n] | [n/n] |
| `obsMoveSelfLoop / obsMoveCycles` | [n] | [n] | [n] |
| `confirmedStuckEvents` | [n] | [n] | [n] |
| `obsMoveRoad / obsMoveSearch` | [n] | [n] | [n] |
| `obsMoveNull` (must be 0) | [n] | [n] | [n] |
| Mean decision time per cycle (µs) | [n] | [n] | [n] |

### 7.4 Ablation

Each row disables exactly one module, two runs per map.

| Variant | What is removed | Montreal | Paris | Berlin |
| -- | -- | -- | -- | -- |
| Full URF | — | [score] | [score] | [score] |
| No door sweep | refuge sweep block in `calc()` | [score] | [score] | [score] |
| No escort | escort block in `calc()` | [score] | [score] | [score] |
| Current-road clear only | revert to `findCurrentRoadCandidate()` | [score] | [score] | [score] |
| Priority swapped | door sweep above escort | [score] | [score] | [score] |
| Sweep deadline disabled | `SWEEP_DEADLINE = 100000` | [score] | [score] | [score] |

> **▢ NEEDS YOU** — The "current-road clear only" row is the most valuable in the table, because it isolates the fix for Issue 2 — the one change we believe explains the largest behavioural difference. Prioritise it if time is short.

### 7.5 Evidence figures

| Figure | Content |
| -- | -- |
| [figure] | Issue 1 — civilians accumulated outside a blocked refuge door |
| [figure] | Issue 2 — police clumped at one location (URF clear) |
| [figure] | Issue 2 — same failure under `DefaultExtActionClear` |
| [figure] | Issue 3 — police passing a blocked ambulance |
| [figure] | After fixes — refuge door open, police dispersed |

> **▢ NEEDS YOU** — You have the "before" screenshots already. The "after" one matters most: a side-by-side of the same map and timestep before and after the `findNearbyCandidate` change is the single most persuasive figure in the report.

---

## 8. Discussion

### 8.1 Fog of war is a different problem from prioritisation

The most transferable lesson from this project is that **a scoring function cannot prioritise what has never been perceived**. Both Issue 1 and Issue 3 look like prioritisation failures and are not. In each case the correct fix was to send an agent to look, then let the existing scoring take over. We initially tried to solve Issue 1 by raising the refuge weight, which had no effect whatsoever, because the road was never in the candidate set to be weighted.

### 8.2 Silent failure modes dominated our debugging time

Three separate defects produced identical visible behaviour — police ignoring their targets — with no exception, no log line and no compilation error:

1. The static target bridge had readers but no writer.
2. `Edge.isPassable()` reports wall geometry, not blockade state, so MOVE kept choosing a blocked exit.
3. A module-key mismatch would silently construct a second detector whose `calc()` never runs.

Each was found by instrumentation, not by reading code. This is the strongest argument for the observation layer: `obsMoveRoadTarget=null` on every line identified defect 1 in seconds after days of speculation.

### 8.3 What measurement changed about our design

Two design decisions were reversed by data rather than argument:

- We planned a custom ambulance→police message. A probe showed the needed data was already arriving at 24 messages per cycle, and that the channel was carrying up to 452 `MessageRoad` messages per cycle. The custom message was dropped.
- We assumed perception-range escort would be adequate. The same probe showed message range is map-wide, which made the feature far more useful than the perception-only version we had designed.

> **▢ NEEDS YOU** — Add a paragraph here on what the §7 numbers actually show. If the URF configuration does not beat the baseline on some map, say so and explain why; rubric 4's top band explicitly rewards failure analysis and evidence-based conclusions, not only positive results.

---

## 9. Limitations and Future Work

### 9.1 Scope

**Only the Police role was improved.** Fire Brigade and Ambulance Team run the unmodified sample modules.

> **▢ NEEDS YOU — RUBRIC GAP, MOST SERIOUS** — Section 11 requires "at least one role-specific target-selection strategy … **for each of the three platoon roles**". This is a Pass/Fail row and as things stand it fails. Two options:
>
> - **Cheap fix:** write a minimal `URFHumanDetector` for Ambulance Team (score victims by buriedness, damage and distance) and a minimal `URFBuildingDetector` for Fire Brigade (score fires by fieryness and proximity to refuges/civilians). Each can be 80–120 lines reusing the scoring pattern from §5.1. This turns a fail into a pass and also strengthens rubric 3.
> - **Honest fallback:** state the gap explicitly here and justify the depth-over-breadth choice. Some marks are lost, but far fewer than for an unacknowledged gap that the lecturer discovers.
>
> Decide which, and rewrite this section accordingly. Do not leave it silent.

> **▢ NEEDS YOU — RUBRIC GAP** — Section 11 also requires "at least one improved or alternative planner is implemented **and evaluated**". `URFRoadDetector.PathPlanning` is configurable, so the cheapest route to a pass is an evaluation rather than an implementation: run the same maps with `DijkstraPathPlanning` and `AStarPathPlanning` and report the score difference as an ablation row. If `sample_team.module.complex.AStarPathPlanning` is sample code rather than yours, say so — claiming it would be worse than the gap.

### 9.2 Known technical limitations

| Limitation | Detail |
| -- | -- |
| **Police clump** | All agents run identical logic and often select the same target, so they converge and duplicate work. A distributed assignment — sort police IDs, take `myIndex % doors.size()` — would disperse them with no communication, since every agent knows the full police roster from `worldInfo` at startup. |
| **Escort may starve the sweep** | The escort sits above the door sweep and returns non-null whenever any ambulance is stalled. With 24 ambulances this can prevent the sweep running at all. Measured by the priority-swap ablation row in §7.4. |
| **Oscillating agents are not detected** | The escort detects *frozen* ambulances. An ambulance ping-ponging between two roads resets its stall clock every cycle and is never treated as stuck, despite being equally blocked. Tracking the last 3–4 reported positions would close this. |
| **No inter-police coordination** | Two police agents on the same road both clear the nearest blockade. No target reservation exists. |
| **Corridor value is a degree proxy** | `min(1, deg/4)` approximates centrality. True betweenness centrality (a §15 bonus item) would need a precomputed pass. |
| **Blocked-agent term is inferred** | A teammate standing on a blocked road is *assumed* stuck; the agent cannot observe another agent's failed MOVE commands. |
| **Fire brigades not escorted** | `MessageFireBrigade` arrives at 36 per cycle with the same data shape; extending the escort is largely a copy of the observe loop. |
| **Rotation counters never reset** | `neighbourIndex` and `bearingIndex` rotate monotonically, so exit choice depends on total failure count rather than position. Deterministic per run, but not reproducible from position alone. |
| **Constants are untuned** | `STALL_LIMIT`, `STAGNANT_CLEAR_LIMIT`, `RECOVERY_EXTENSION`, `SWEEP_DEADLINE` were chosen by reasoning, not measurement. |

### 9.3 Future work

1. **Distributed door and target assignment** — the single highest-value next change, addressing the clumping that currently wastes most of the police fleet.
2. **Target reservation via `MessagePoliceForce`** — the messages already arrive at 22 per cycle and are currently unused.
3. **Bottleneck and centrality analysis** (§15 bonus) — precompute true corridor importance instead of the degree proxy.
4. **Learning-based ranking** (§15 bonus) — the CSV schema was designed with this in mind. Forty-four columns per agent per timestep across multiple maps is a usable dataset for learning the scoring weights rather than hand-setting them. This is the long-term motivation for the observation layer, and any such model must be compared fairly against the hand-tuned baseline reported here.

---

## 10. Conclusion

> **▢ NEEDS YOU** — Write this last, once §7 has numbers. It should state: what was built, what measurably improved, what did not, and the one lesson you would carry to another multi-agent project. Keep it to three or four short paragraphs and do not introduce anything not already evidenced above.

---

## 11. Individual Contributions

| Student Name | Student ID | Contribution |
| -- | -- | -- |
| Chong Zi Yang | 2401892 | [contribution] |
| Carlos Wong | 2303326 | [contribution] |

> **▢ NEEDS YOU** — Be specific: name the classes each person wrote, the experiments each ran, and who produced the diagrams, README and video. Rubric 6 also requires both members to participate in the demonstration and answer questions, so divide the explaining as well as the coding.

---

## 12. References

1. RoboCup Rescue Simulation. `rcrs-server`. https://github.com/roborescue/rcrs-server
2. RoboCup Rescue Simulation. `adf-core-java`. https://github.com/roborescue/adf-core-java
3. RoboCup Rescue Simulation. `adf-sample-agent-java`. https://github.com/roborescue/adf-sample-agent-java
4. Bonab, M. B. (2026). *UECS1044/UECS1144 Group Assignment: RCRS Agent Simulation*. Universiti Tunku Abdul Rahman.

> **▢ NEEDS YOU** — Add the markmap tool reference, any RoboCup Rescue papers you read, and a citation for the LLM assistance. Rubric 5's top band requires proper references.

---

## Appendix A — Submission Checklist

### Rubric gaps to close (ordered by risk)

| # | Gap | Section | Cost |
| -- | -- | -- | -- |
| 1 | No target-selection strategy for Fire Brigade or Ambulance Team | §9.1 | ~2 × 100 lines, or an honest statement |
| 2 | No separate configuration files | §7.1 | ~15 min |
| 3 | Path planner not evaluated | §9.1 | one ablation row |
| 4 | No enums for states/reasons | §4.2 | ~30 min refactor |
| 5 | Interfaces not declared | §4.2 | argue it, or extract one |

### Deliverables

| Item | Status |
| -- | -- |
| A. Complete Java source, config, Gradle, no build artifacts | ▢ |
| B. Final report PDF | ▢ |
| C. UML class diagram | ▢ |
| C. Workflow diagram | ▢ |
| README with exact build/run instructions | ▢ |
| E. Individual contributions | ▢ |
| F. Demonstration video, 15–30 min | ▢ |
| ZIP named `G3_CS_UECS1044_GA.zip` | ▢ |
| SHA/checksum or timestamp submitted before deadline | ▢ |

### Data to collect

| Item | Section |
| -- | -- |
| Baseline vs URF clear comparison, 3 maps × 3 runs | §7.2 |
| Full URF vs official baseline, 3 maps × 3 runs | §7.3 |
| Behaviour metrics from CSV | §7.3 |
| Ablation, 6 variants × 2 runs | §7.4 |
| Checkpoint 0 screenshots | §2.3 |
| Before/after evidence figures | §7.5 |
| Neighbour-road `repairCost` verification | §5.2.3 |

### Naming corrections applied from `Development_Flow.md`

Your brainstorm file uses several names that differ from the code. This report uses the code's names. Rename in `Development_Flow.md` too if you submit it, and make sure the UML matches.

| In `Development_Flow.md` | Actual class |
| -- | -- |
| `URFDoorSweep` | `URFRefugeDoorSweep` |
| `URFMessageAmbulanceTeam` | `URFStuckAgentEscort` |
| `URFObservedPoliceExtActionClear` | `URFObservedExtActionClear` |
| `URFObservedPoliceExtActionMove` | `URFObservedExtActionMove` |
| `URFPoliceCsvExporter` listed twice under "Helper" | `URFPoliceMetrics`, `URFPoliceStuckDetector`, `URFPoliceCsvExporter` |

One factual correction: State 2 of `Development_Flow.md` says `URFRoadDetector` "decides a police should move or clear". It does not. It selects a *target*; the move-or-clear decision is made by the tactics fallthrough depending on whether the CLEAR module returns `null`. §3.2 states this correctly and the distinction matters — a marker who reads the ADF source will notice.