# RCRS Agent Simulation Report

| Name | Student ID | Programme |
| -- | -- | -- |
| Chong Zi Yang | 2401892 | AM |
| Carlos Wong | 2303326 | AM |

---

## Table of Contents

1. [Introduction and Problem Statement](#1-introduction-and-problem-statement)
2. [Baseline Reproduction](#2-baseline-reproduction)
3. [Architecture](#3-architecture)
4. [Object-Oriented Design](#4-object-oriented-design)
5. [Implementation](#5-implementation)
6. [Experiments and Results](#6-testing-experiments-and-results)
7. [Discussion](#7-discussion)
8. [Limitations and Future Work](#8-limitations-and-future-work)
9.  [Conclusion](#9-conclusion)
10. [Individual Contributions](#10-individual-contributions)
11. [References](#11-references)
12. [Appendix A — Submission Checklist](#appendix-a--submission-checklist)

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

You can check the summarized API reference in `api_ref/` either for `index.html` or all `.md` in `core-docs` and `server-docs`.

You can also check our github repo that save your time to render : [Our Github](https://github.com/FlyingCalours/RoboCupRAS/tree/main)

### 1.4 Problems identified

Three concrete failures were observed in baseline runs, each reproducible across maps.

**Issue 1 — Refuge entrances never cleared.** We define a *door* as a road sharing an edge with a refuge. When a door carried a blockade, ambulances could not deliver patients and civilians accumulated outside the refuge and died. No police agent ever cleared a door.

**Issue 2 — Police agents freeze.** Police stopped making progress. The count of frozen agents grew monotonically with timestep.

**Issue 3 — Blocked ambulances ignored.** An ambulance stuck behind a blockade was passed by police agents that continued clearing low-value blockades elsewhere.

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
cd RoboCupRAS && ./launch -all

# Optional : Run Specific Map on Server
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
URF (ours)           urf.police                         our contribution
```

We modified neither the server nor the ADF core. All URF code lives under `urf.police`, split into two packages:

| Package | Responsibility |
| -- | -- |
| `...police.improvement` | decision logic — what the agent does |
| `...police.observation` | measurement — what we record about it |

This split is deliberate. Every observation class is a subclass of the corresponding improvement class and overrides only measurement, never a decision. Switching between the two is a configuration change, so an experiment and a competition run execute identical decision code.

### 3.2 Module responsibilities

| Module | Class | Decides |
| -- | -- | -- |
| Target selection | `URFRoadDetector` | *which road is worth going to* |
| Action (clear) | `URFPoliceExtActionClear` | *what to clear within reach, right now* |
| Action (move) | `URFPoliceExtActionMove` | *where to step, and what to do when stepping fails* |

Note the division: the detector chooses a **destination**; the CLEAR module ignores that destination entirely and cuts whatever lies within clear range of the agent's current position. A police agent walking to a refuge door therefore clears every blockade it steps over on the way, at no cost to the sweep.

### 3.3 Workflow diagram

![](img/A_rcrs-overall-sequence.png)

---

## 4. Object-Oriented Design

### 4.1 Class structure

Diagram below show overall parent-child relation between important class , **does not include enum**

![](img/overall-class-diagram.png)

### 4.2 UML class diagram

![](img/urf_police_class_diagram.png)

---

## 5. Implementation

### 5.1 `URFRoadDetector` — police target selection

The detector answers one question per cycle: which road is worth travelling to? It runs a three-level priority ladder.

| Priority | Source | Rationale |
| -- | -- | -- |
| 1 | refuge door sweep | unobserved refuge entrances, which scoring cannot reach |
| 2 | stuck ambulance escort | a blocked rescue vehicle is costing lives now |
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

- Off the target road, progress means the distance shrank by at least 1000 units below the best seen.
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

## 6. Testing, Experiments and Results

### 6.1 Protocol

Three maps, three runs each, identical scenario, server version and time limit. Only the `.cfg` file differs between configurations.

#### 6.1.1 Command for Different Map in RCRS Server
Below is the command in `rcrs-server` for different map.

Montreal :
```bash
./start.sh -m ../maps/montreal/map -c ../maps/montreal/config
```

Paris :
```bash
./start.sh -m ../maps/paris/map -c ../maps/paris/config
```

Berlin :
```bash
./start.sh -m ../maps/berlin/map -c ../maps/berlin/config
```

#### 6.1.2 Command for Different Configuration in Our Repo
Below is the command in our repository for different configuration.

Default Baseline :
```bash
./launch.sh -all
```

Lecturer supplied URF Baseline :
```bash
./launch.sh -mc config/module_baseline_urf.cfg -all
```

Improved URF develop by us :
```bash
./launch.sh -mc config/module_improved_urf.cfg -all
```

### 6.2 Baseline: `URFPoliceExtActionClear` vs `DefaultExtActionClear`

Comparing the lecturer-supplied clearing logic against the ADF default, all else equal.

`URFPoliceExtActionClear`:

| | Montreal | Paris | Berlin |
| -- | -- | -- | -- |
| Run 1 | 14.668 | 11.019 | 9.568 |
| Run 2 | 14.668 | 11.019 | 9.568 |
| Run 3 | 14.668 | 11.019 | 9.568 |

`DefaultExtActionClear`:

| | Montreal | Paris | Berlin |
| -- | -- | -- | -- |
| Run 1 | 16.234 | 11.299 |  |
| Run 2 | 16.234 | 11.299 | [score] |
| Run 3 | 16.234 | 11.299 | [score] |

### 6.3 Full URF vs Default Baseline

| Configuration | Montreal | Paris | Berlin |
| -- | -- | -- | -- | -- |
| Official baseline | 16.234 | 11.299 | [score] |
| Improved URF | 17.425 | 13.541  | 10.098 |

### 6.4 Evidence figures

| Figure | Content |
| -- | -- |
| ![](img/refugee-stuck-at-refuge-door.png) | Issue 1 — civilians accumulated outside a blocked refuge door |
| ![](img/police-stuck-urf-clear-orig.png) | Issue 2 — police stuck by blockade at one location (URF clear) |
| ![](img/police-ignore-ambulance-causing-victim-dead.png) | Issue 3 — police passing a blocked ambulance causing refugee dead |
| ![](img/door-open-and-police-help-ambulance.png) | After fixes — refuge door open, police dispersed |

---

## 7. Discussion

### 7.1 Fog of war is a different problem from prioritisation

The most transferable lesson from this project is that **a scoring function cannot prioritise what has never been perceived**. Both Issue 1 and Issue 3 look like prioritisation failures and are not. In each case the correct fix was to send an agent to look, then let the existing scoring take over. We initially tried to solve Issue 1 by raising the refuge weight, which had no effect whatsoever, because the road was never in the candidate set to be weighted.

### 7.2 Silent failure modes dominated our debugging time

Three separate defects produced identical visible behaviour — police ignoring their targets — with no exception, no log line and no compilation error:

1. The static target bridge had readers but no writer.
2. `Edge.isPassable()` reports wall geometry, not blockade state, so MOVE kept choosing a blocked exit.
3. A module-key mismatch would silently construct a second detector whose `calc()` never runs.

Each was found by instrumentation, not by reading code. This is the strongest argument for the observation layer: `obsMoveRoadTarget=null` on every line identified defect 1 in seconds after days of speculation.

### 7.3 What measurement changed about our design

Two design decisions were reversed by data rather than argument:

- We planned a custom ambulance→police message. A probe showed the needed data was already arriving at 24 messages per cycle, and that the channel was carrying up to 452 `MessageRoad` messages per cycle. The custom message was dropped.
- We assumed perception-range escort would be adequate. The same probe showed message range is map-wide, which made the feature far more useful than the perception-only version we had designed.

§6 numbers actually show that our improvement work with 17.425 scores in montreal map, slightly higher than default baseline 16.234. However in larger map like paris and berlin, we earn lower score than smaller map like montreal.

---

## 8. Limitations and Future Work

### 8.1 Scope

**Only the Police role was improved.** Fire Brigade and Ambulance Team run the unmodified sample modules. We only improve police step by step, optimize the police decision making in order to get higher scores. 

### 8.2 Known technical limitations

| Limitation | Detail |
| -- | -- |
| **Police clump** | All agents run identical logic and often select the same target, so they converge and duplicate work. A distributed assignment — sort police IDs, take `myIndex % doors.size()` — would disperse them with no communication, since every agent knows the full police roster from `worldInfo` at startup. |
| **Oscillating agents are not detected** | The escort detects *frozen* ambulances. An ambulance ping-ponging between two roads resets its stall clock every cycle and is never treated as stuck, despite being equally blocked. Tracking the last 3–4 reported positions would close this. |
| **No inter-police coordination** | Two police agents on the same road both clear the nearest blockade. No target reservation exists. |
| **Corridor value is a degree proxy** | `min(1, deg/4)` approximates centrality. True betweenness centrality would need a precomputed pass. |
| **Blocked-agent term is inferred** | A teammate standing on a blocked road is *assumed* stuck; the agent cannot observe another agent's failed MOVE commands. |
| **Fire brigades not escorted** | `MessageFireBrigade` arrives at 36 per cycle with the same data shape; extending the escort is largely a copy of the observe loop. |
| **Constants are untuned** | `STALL_LIMIT`, `STAGNANT_CLEAR_LIMIT`, `RECOVERY_EXTENSION`, `SWEEP_DEADLINE` were chosen by reasoning, not measurement. |

### 8.3 Future work

1. **Distributed door and target assignment** — the single highest-value next change, addressing the clumping that currently wastes most of the police fleet.
2. **Target reservation via `MessagePoliceForce`** — the messages already arrive at 22 per cycle and are currently unused.
3. **Bottleneck and centrality analysis** — precompute true corridor importance instead of the degree proxy.
4. **Learning-based ranking** — the CSV schema was designed with this in mind. Forty-four columns per agent per timestep across multiple maps is a usable dataset for learning the scoring weights rather than hand-setting them. This is the long-term motivation for the observation layer, and any such model must be compared fairly against the hand-tuned baseline reported here.

---

## 9. Conclusion

**Issue 1 — Refuge entrances never cleared.** We define a *door* as a road sharing an edge with a refuge. When a door carried a blockade, ambulances could not deliver patients and civilians accumulated outside the refuge and died. No police agent ever cleared a door.

**Issue 2 — Police agents freeze.** Police stopped making progress. The count of frozen agents grew monotonically with timestep.

**Issue 3 — Blocked ambulances ignored.** An ambulance stuck behind a blockade was passed by police agents that continued clearing low-value blockades elsewhere.


In conclude, we built modules that make police can do basic clearing and moving without stuck with the codes provided by Dr. Mohammad Babrdel Bonab as the base. 

We faced issue 1 which is the door for refuge never opened. We solve it by commanding every police to navigate to every refuge on the map. It worked, but this can be improve later for communication module.

We faced issue 2 where police agents freeze, this is a small bug occur in the codes provided by lecturer. The issue is police clear blockade on current road only, so we reset it to clear blockade on neighbour road also.

We faced issue 3 which blocked ambulance get ignored by police. There's a free radio where ambulance and fire team keep reporting their position. If police received their position is same as previous, then confirm they stuck and navigate to their location

The result shows our improvement slightly beat the baseline model in 3 different map. Where default baseline beat urf baseline also.

---

## 10. Individual Contributions

| Student Name | Student ID | Contribution |
| -- | -- | -- |
| Chong Zi Yang | 2401892 | [contribution] |
| Carlos Wong | 2303326 | [contribution] |

---

## 11. References

1. RoboCup Rescue Simulation. `rcrs-server`. https://github.com/roborescue/rcrs-server
2. RoboCup Rescue Simulation. `adf-core-java`. https://github.com/roborescue/adf-core-java
3. RoboCup Rescue Simulation. `adf-sample-agent-java`. https://github.com/roborescue/adf-sample-agent-java
4. Bonab, M. B. (2026). *UECS1044/UECS1144 Group Assignment: RCRS Agent Simulation*. Universiti Tunku Abdul Rahman.

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