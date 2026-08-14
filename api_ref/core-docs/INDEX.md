# adf-core-java — API index

Documentation of every class in `adf-core-java` that **team code actually calls**. Framework-internal classes (launcher, connectors, `Option*`, `Connector*`, `Main`, `Agent`, `Platoon*`, `Office*`, `AbstractLoader`, the debug viewer, and the `adf.impl.*` reference implementations) are deliberately **not** documented — the framework wires them up and no `sample_team` class ever invokes them.

Folder layout mirrors the Java packages, so a file's path is its import path:

`component/module/algorithm/PathPlanning.md` &rarr;
`adf.core.component.module.algorithm.PathPlanning`.

---

## agent — what the agent knows and owns

### agent/info — the three info objects

Passed into every lifecycle method; the starting point of all decisions.

| File | Class | What it answers |
|---|---|---|
| <a href="agent/info/AgentInfo.md">agent/info/AgentInfo.md</a> | `AgentInfo` | Who am I, where am I, what did I see and hear |
| <a href="agent/info/WorldInfo.md">agent/info/WorldInfo.md</a> | `WorldInfo` | What does the map look like, what entities exist |
| <a href="agent/info/ScenarioInfo.md">agent/info/ScenarioInfo.md</a> | `ScenarioInfo` | What are the rules — distances, rates, limits |

### agent/action — what an agent can do

`Tactics.think()` returns exactly one of these each tick.

| File | Class | Agent type |
|---|---|---|
| <a href="agent/action/Action.md">agent/action/Action.md</a> | `Action` | Abstract base of all actions |
| <a href="agent/action/common/ActionMove.md">agent/action/common/ActionMove.md</a> | `common.ActionMove` | All — walk along a path |
| <a href="agent/action/common/ActionRest.md">agent/action/common/ActionRest.md</a> | `common.ActionRest` | All — do nothing this tick |
| <a href="agent/action/ambulance/ActionRescue_Ambulance.md">agent/action/ambulance/ActionRescue_Ambulance.md</a> | `ambulance.ActionRescue` | Ambulance — dig a buried human out |
| <a href="agent/action/ambulance/ActionLoad.md">agent/action/ambulance/ActionLoad.md</a> | `ambulance.ActionLoad` | Ambulance — pick up a civilian |
| <a href="agent/action/ambulance/ActionUnload.md">agent/action/ambulance/ActionUnload.md</a> | `ambulance.ActionUnload` | Ambulance — put the civilian down |
| <a href="agent/action/fire/ActionExtinguish.md">agent/action/fire/ActionExtinguish.md</a> | `fire.ActionExtinguish` | Fire — spray a burning building |
| <a href="agent/action/fire/ActionRefill.md">agent/action/fire/ActionRefill.md</a> | `fire.ActionRefill` | Fire — refill the water tank |
| <a href="agent/action/fire/ActionRescue_Fire.md">agent/action/fire/ActionRescue_Fire.md</a> | `fire.ActionRescue` | Fire — dig a buried human out |
| <a href="agent/action/police/ActionClear.md">agent/action/police/ActionClear.md</a> | `police.ActionClear` | Police — remove blockades |

> `ActionRescue` exists twice, once per package. The file names carry a suffix so
> both survive in one folder tree; the package is stated at the top of each file.

### agent/communication — sending and receiving

| File | Class | What it is |
|---|---|---|
| <a href="agent/communication/MessageManager.md">agent/communication/MessageManager.md</a> | `MessageManager` | Every message in and out of the agent this tick |

**agent/communication/bundle — the standard message types**

| File | Class | What it carries |
|---|---|---|
| <a href="agent/communication/bundle/StandardMessage.md">bundle/StandardMessage.md</a> | `StandardMessage` | Base of everything below — sender, TTL, priority |
| <a href="agent/communication/bundle/StandardMessagePriority.md">bundle/StandardMessagePriority.md</a> | `StandardMessagePriority` | `LOW` / `NORMAL` / `HIGH` |
| <a href="agent/communication/bundle/StandardMessageBundle.md">bundle/StandardMessageBundle.md</a> | `StandardMessageBundle` | The ready made dictionary to register |
| <a href="agent/communication/bundle/MessageUtil.md">bundle/MessageUtil.md</a> | `MessageUtil` | Writes a received message into the world model |
| <a href="agent/communication/bundle/information/MessageCivilian.md">bundle/information/MessageCivilian.md</a> | `information.MessageCivilian` | A victim: position, HP, buriedness, damage |
| <a href="agent/communication/bundle/information/MessageBuilding.md">bundle/information/MessageBuilding.md</a> | `information.MessageBuilding` | A building: fieryness, brokenness, temperature |
| <a href="agent/communication/bundle/information/MessageRoad.md">bundle/information/MessageRoad.md</a> | `information.MessageRoad` | A road and its blockade |
| <a href="agent/communication/bundle/information/MessageAmbulanceTeam.md">bundle/information/MessageAmbulanceTeam.md</a> | `information.MessageAmbulanceTeam` | An ambulance reporting its own state |
| <a href="agent/communication/bundle/information/MessageFireBrigade.md">bundle/information/MessageFireBrigade.md</a> | `information.MessageFireBrigade` | A fire brigade reporting state + water |
| <a href="agent/communication/bundle/information/MessagePoliceForce.md">bundle/information/MessagePoliceForce.md</a> | `information.MessagePoliceForce` | A police force reporting its own state |
| <a href="agent/communication/bundle/centralized/CommandAmbulance_Centralized.md">bundle/centralized/CommandAmbulance_Centralized.md</a> | `centralized.CommandAmbulance` | Centre &rarr; ambulance order |
| <a href="agent/communication/bundle/centralized/CommandFire_Centralized.md">bundle/centralized/CommandFire_Centralized.md</a> | `centralized.CommandFire` | Centre &rarr; fire brigade order |
| <a href="agent/communication/bundle/centralized/CommandPolice_Centralized.md">bundle/centralized/CommandPolice_Centralized.md</a> | `centralized.CommandPolice` | Centre &rarr; police force order |
| <a href="agent/communication/bundle/centralized/CommandScout_Centralized.md">bundle/centralized/CommandScout_Centralized.md</a> | `centralized.CommandScout` | Centre &rarr; "go explore around here" |
| <a href="agent/communication/bundle/centralized/MessageReport_Centralized.md">bundle/centralized/MessageReport_Centralized.md</a> | `centralized.MessageReport` | Platoon &rarr; centre "done" / "failed" |
| <a href="agent/communication/bundle/TopDown_Messages.md">bundle/TopDown_Messages.md</a> | the five `topdown.*` twins | Same API, different package — pick one set per team |

### agent — managers and data

| File | Class | What it is |
|---|---|---|
| <a href="agent/module/ModuleManager.md">agent/module/ModuleManager.md</a> | `ModuleManager` | Factory for every pluggable component |
| <a href="agent/config/ModuleConfig.md">agent/config/ModuleConfig.md</a> | `ModuleConfig` | Parsed `module.cfg` |
| <a href="agent/develop/DevelopData.md">agent/develop/DevelopData.md</a> | `DevelopData` | Tuning values without recompiling |
| <a href="agent/precompute/PrecomputeData.md">agent/precompute/PrecomputeData.md</a> | `PrecomputeData` | Save file shared by precompute and run |

---

## component — what you extend

### component/tactics — the entry point of your team code

| File | Class | What it is |
|---|---|---|
| <a href="component/tactics/Tactics.md">component/tactics/Tactics.md</a> | `Tactics` (+ `TacticsAmbulanceTeam`, `TacticsFireBrigade`, `TacticsPoliceForce`) | Brain of a platoon agent — returns one `Action` per tick |
| <a href="component/tactics/TacticsCenter.md">component/tactics/TacticsCenter.md</a> | `TacticsCenter` (+ `TacticsAmbulanceCentre`, `TacticsFireStation`, `TacticsPoliceOffice`) | Brain of a centre agent — returns nothing, only messages |

### component/module — the module hierarchy

| File | Class | What it is |
|---|---|---|
| <a href="component/module/AbstractModule.md">component/module/AbstractModule.md</a> | `AbstractModule` | Root of every module — the lifecycle and its counters |

**component/module/algorithm — general purpose algorithms**

| File | Class | What it does |
|---|---|---|
| <a href="component/module/algorithm/PathPlanning.md">algorithm/PathPlanning.md</a> | `PathPlanning` | Route from here to there |
| <a href="component/module/algorithm/Clustering.md">algorithm/Clustering.md</a> | `Clustering` | Partition the map between agents |
| <a href="component/module/algorithm/StaticClustering.md">algorithm/StaticClustering.md</a> | `StaticClustering` | Clustering computed once |
| <a href="component/module/algorithm/DynamicClustering.md">algorithm/DynamicClustering.md</a> | `DynamicClustering` | Clustering recomputed as the situation changes |

**component/module/complex — task specific modules**

| File | Class | Role |
|---|---|---|
| <a href="component/module/complex/Search.md">complex/Search.md</a> | `Search` | Explore unknown areas |
| <a href="component/module/complex/TargetDetector.md">complex/TargetDetector.md</a> | `TargetDetector` | Choose a target (platoon side, base class) |
| <a href="component/module/complex/BuildingDetector.md">complex/BuildingDetector.md</a> | `BuildingDetector` | Fire brigade — pick a building |
| <a href="component/module/complex/HumanDetector.md">complex/HumanDetector.md</a> | `HumanDetector` | Ambulance — pick a victim |
| <a href="component/module/complex/RoadDetector.md">complex/RoadDetector.md</a> | `RoadDetector` | Police — pick a road to clear |
| <a href="component/module/complex/TargetSelector.md">complex/TargetSelector.md</a> | `TargetSelector` | Selector variant, base class |
| <a href="component/module/complex/BuildingSelector.md">complex/BuildingSelector.md</a> | `BuildingSelector` | Building selector variant |
| <a href="component/module/complex/HumanSelector.md">complex/HumanSelector.md</a> | `HumanSelector` | Human selector variant |
| <a href="component/module/complex/RoadSelector.md">complex/RoadSelector.md</a> | `RoadSelector` | Road selector variant |
| <a href="component/module/complex/TargetAllocator.md">complex/TargetAllocator.md</a> | `TargetAllocator` | Assign targets to agents (centre side, base class) |
| <a href="component/module/complex/AmbulanceTargetAllocator.md">complex/AmbulanceTargetAllocator.md</a> | `AmbulanceTargetAllocator` | Ambulance centre allocation |
| <a href="component/module/complex/FireTargetAllocator.md">complex/FireTargetAllocator.md</a> | `FireTargetAllocator` | Fire station allocation |
| <a href="component/module/complex/PoliceTargetAllocator.md">complex/PoliceTargetAllocator.md</a> | `PoliceTargetAllocator` | Police office allocation |

### component/extaction — extended actions

| File | Class | What it is |
|---|---|---|
| <a href="component/extaction/ExtAction.md">component/extaction/ExtAction.md</a> | `ExtAction` | Turns a chosen target into a concrete `Action` |

### component/centralized — centre &harr; platoon control

| File | Class | Side |
|---|---|---|
| <a href="component/centralized/CommandPicker.md">component/centralized/CommandPicker.md</a> | `CommandPicker` | Centre — allocation map &rarr; command messages |
| <a href="component/centralized/CommandExecutor.md">component/centralized/CommandExecutor.md</a> | `CommandExecutor` | Platoon — received command &rarr; `Action` |

### component/communication — pluggable comms policy

| File | Class | What it is |
|---|---|---|
| <a href="component/communication/CommunicationMessage.md">component/communication/CommunicationMessage.md</a> | `CommunicationMessage` | Base of every message |
| <a href="component/communication/MessageBundle.md">component/communication/MessageBundle.md</a> | `MessageBundle` | Shared dictionary of message types |
| <a href="component/communication/MessageCoordinator.md">component/communication/MessageCoordinator.md</a> | `MessageCoordinator` | Which message goes on which channel |
| <a href="component/communication/ChannelSubscriber.md">component/communication/ChannelSubscriber.md</a> | `ChannelSubscriber` | Which channels the agent listens to |
| <a href="component/communication/CommunicationModule.md">component/communication/CommunicationModule.md</a> | `CommunicationModule` | Transport layer to the rescuecore2 protocol |

---

## The tick, end to end

1. The framework calls `Tactics.think(...)`.
2. Read `messageManager.getReceivedMessageList(...)` and apply each one with
   `MessageUtil.reflectMessage(worldInfo, message)`.
3. Call `modulesUpdateInfo(messageManager)` so every registered module sees the
   new information.
4. Pick a target with a detector/selector, or execute a centre command through a
   `CommandExecutor`.
5. Turn the target into an `Action` — directly, or through an `ExtAction`.
6. Queue what the team should know with `messageManager.addMessage(...)`.
7. Return the `Action`.

Server side types used throughout (`EntityID`, `StandardEntity`, `Building`,
`Road`, `Blockade`, `Human`, ...) are documented in `server-docs/`.