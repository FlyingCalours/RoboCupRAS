---
markmap:
  initialExpandLevel: 2
---

# adf-core

## (A) [Action][action]

### ambulance
#### (C) [ActionLoad][actionload]
#### (C) [ActionRescue][actionrescue_ambulance]
#### (C) [ActionUnload][actionunload]

### fire
#### (C) [ActionExtinguish][actionextinguish]
#### (C) [ActionRefill][actionrefill]
#### (C) [ActionRescue][actionrescue_fire]

### police
#### (C) [ActionClear][actionclear]

### common
#### (C) [ActionMove][actionmove]
#### (C) [ActionRest][actionrest]


## (A) [CommunicationMessage][communicationmessage]
### (A) [StandardMessage][standardmessage]
#### (C) [CommandAmbulance][commandambulance]
#### (C) [CommandFire][commandfire]
#### (C) [CommandPolice][commandpolice]
#### (C) [CommandScout][commandscout]
#### (C) [MessageReport][messagereport]
#### (C) [MessageAmbulanceTeam][messageambulanceteam]
#### (C) [MessageBuilding][messagebuilding]
#### (C) [MessageCivilian][messagecivilian]
#### (C) [MessageFireBrigade][messagefirebrigade]
#### (C) [MessagePoliceForce][messagepoliceforce]
#### (C) [MessageRoad][messageroad]


## (A) [AbstractModule][abstractmodule]
### algorithm
#### (A) [Clustering][clustering]
##### (A) [DynamicClustering][dynamicclustering]
##### (A) [StaticClustering][staticclustering]
#### (A) [PathPlanning][pathplanning]

### (A) [TargetDetector \<E extends StandardEntity\>][targetdetector]
#### \<Area\>
##### (A) [Search][search]

#### \<Road\>
##### (A) [RoadDetector][roaddetector]
###### (A) [RoadSelector][roadselector]

#### \<Human\>
##### (A) [HumanDetector][humandetector]
###### (A) [HumanSelector][humanselector]

#### \<Building\>
##### (A) [BuildingDetector][buildingdetector]
###### (A) [BuildingSelector][buildingselector]

#### (A) [TargetSelector \<E extends StandardEntity\>][targetselector]


### (A) [TargetAllocator][targetallocator]
#### (A) [AmbulanceTargetAllocator][ambulancetargetallocator]
#### (A) [FireTargetAllocator][firetargetallocator]
#### (A) [PoliceTargetAllocator][policetargetallocator]


## (A) [MessageBundle][messagebundle]
### (C) [StandardMessageBundle][standardmessagebundle]


## (Enum) [StandardMessagePriority][standardmessagepriority]


## (C) [DevelopData][developdata]
## (C) [ModuleManager][modulemanager]
## (C) [PrecomputeData][precomputedata]


## info
### (C) [AgentInfo][agentinfo]
### (C) [ScenarioInfo][scenarioinfo]
### (C) [WorldInfo][worldinfo]

## (A) [Tactics][tactics]
## (A) [TacticsCenter][tacticscenter]
## (A) [CommandExecutor \<C extends CommunicationMessage\>][commandexecutor]
## (A) [CommandPicker][commandpicker]
## (C) [ChannelSubscriber][channelsubscriber]
## (A) [CommunicationModule][communicationmodule]
## (A) [MessageCoordinator][messagecoordinator]
## (A) [ExtAction][extaction]

## FROM_SERVER
### (RCRS) (C) [Config][config]
#### (ADF-CORE) (C) [ModuleConfig][moduleconfig]




<!-- ============================================================
     LINK DEFINITIONS
     Prefix: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/
     ============================================================ -->

<!-- agent/action -->
[action]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/action/Action.md
[actionload]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/action/ambulance/ActionLoad.md
[actionrescue_ambulance]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/action/ambulance/ActionRescue_Ambulance.md
[actionunload]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/action/ambulance/ActionUnload.md
[actionextinguish]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/action/fire/ActionExtinguish.md
[actionrefill]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/action/fire/ActionRefill.md
[actionrescue_fire]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/action/fire/ActionRescue_Fire.md
[actionclear]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/action/police/ActionClear.md
[actionmove]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/action/common/ActionMove.md
[actionrest]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/action/common/ActionRest.md

<!-- agent/communication/bundle -->
[standardmessage]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/communication/bundle/StandardMessage.md
[standardmessagebundle]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/communication/bundle/StandardMessageBundle.md
[standardmessagepriority]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/communication/bundle/StandardMessagePriority.md
[commandambulance]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/communication/bundle/centralized/CommandAmbulance_Centralized.md
[commandfire]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/communication/bundle/centralized/CommandFire_Centralized.md
[commandpolice]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/communication/bundle/centralized/CommandPolice_Centralized.md
[commandscout]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/communication/bundle/centralized/CommandScout_Centralized.md
[messagereport]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/communication/bundle/centralized/MessageReport_Centralized.md
[messageambulanceteam]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/communication/bundle/information/MessageAmbulanceTeam.md
[messagebuilding]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/communication/bundle/information/MessageBuilding.md
[messagecivilian]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/communication/bundle/information/MessageCivilian.md
[messagefirebrigade]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/communication/bundle/information/MessageFireBrigade.md
[messagepoliceforce]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/communication/bundle/information/MessagePoliceForce.md
[messageroad]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/communication/bundle/information/MessageRoad.md

<!-- agent (misc) -->
[developdata]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/develop/DevelopData.md
[modulemanager]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/module/ModuleManager.md
[precomputedata]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/precompute/PrecomputeData.md
[moduleconfig]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/config/ModuleConfig.md

<!-- agent/info -->
[agentinfo]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/info/AgentInfo.md
[scenarioinfo]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/info/ScenarioInfo.md
[worldinfo]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/agent/info/WorldInfo.md

<!-- component/communication -->
[communicationmessage]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/communication/CommunicationMessage.md
[messagebundle]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/communication/MessageBundle.md
[channelsubscriber]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/communication/ChannelSubscriber.md
[communicationmodule]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/communication/CommunicationModule.md
[messagecoordinator]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/communication/MessageCoordinator.md

<!-- component/module -->
[abstractmodule]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/AbstractModule.md
[clustering]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/algorithm/Clustering.md
[dynamicclustering]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/algorithm/DynamicClustering.md
[staticclustering]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/algorithm/StaticClustering.md
[pathplanning]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/algorithm/PathPlanning.md

<!-- component/module/complex -->
[targetdetector]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/complex/TargetDetector.md
[targetselector]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/complex/TargetSelector.md
[search]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/complex/Search.md
[roaddetector]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/complex/RoadDetector.md
[roadselector]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/complex/RoadSelector.md
[humandetector]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/complex/HumanDetector.md
[humanselector]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/complex/HumanSelector.md
[buildingdetector]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/complex/BuildingDetector.md
[buildingselector]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/complex/BuildingSelector.md
[targetallocator]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/complex/TargetAllocator.md
[ambulancetargetallocator]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/complex/AmbulanceTargetAllocator.md
[firetargetallocator]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/complex/FireTargetAllocator.md
[policetargetallocator]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/module/complex/PoliceTargetAllocator.md

<!-- component (misc) -->
[tactics]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/tactics/Tactics.md
[tacticscenter]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/tactics/TacticsCenter.md
[commandexecutor]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/centralized/CommandExecutor.md
[commandpicker]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/centralized/CommandPicker.md
[extaction]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/core-docs/component/extaction/ExtAction.md

<!-- server-docs (outside core-docs) -->
[config]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/config/Config.md













# rcrs-server (`rescuecore2`)

## (A) [StandardEntity][standardentity]

### (A) [Area][area]
#### (C) [Road][road]
##### (C) [Hydrant][hydrant]
#### (C) [Building][building]
##### (C) [Refuge][refuge]
##### (C) [GasStation][gasstation]
##### (C) [AmbulanceCentre][centerentities]
##### (C) [FireStation][centerentities]
##### (C) [PoliceOffice][centerentities]

### (A) [Human][human]
#### (C) [Civilian][civilian]
#### (C) [AmbulanceTeam][ambulanceteam]
#### (C) [FireBrigade][firebrigade]
#### (C) [PoliceForce][policeforce]

### (C) [Blockade][blockade]


## (I) [Command][command]
### (A) AbstractCommand
#### (C) AKMove
#### (C) AKRescue
#### (C) AKLoad
#### (C) AKUnload
#### (C) AKExtinguish
#### (C) AKClear
#### (C) AKRest
#### (C) AKSpeak
#### (C) AKSubscribe


## (C) [StandardWorldModel][standardworldmodel]
## (C) [EntityID][entityid]
## (C) [ChangeSet][changeset]
## (C) [Edge][edge]


## (Enum) [StandardEntityURN][standardentityurn]
## (Enum) [StandardPropertyURN][standardpropertyurn]
## (Enum) [Fieryness][standardentityconstants]
## (Enum) [BuildingCode][standardentityconstants]


## (C) [Point2D][point2d]
## (C) [Vector2D][vector2d]
## (C) [Line2D][line2d]
## (C) [GeometryTools2D][geometrytools2d]
## (C) [Pair][pair]


## (C) [Config][config]
## (C) [Logger][logger]




<!-- ============================================================
     LINK DEFINITIONS
     Prefix: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/
     ============================================================ -->

<!-- entities -->
[standardentity]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/StandardEntity.md
[area]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/Area.md
[road]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/Road.md
[hydrant]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/Hydrant.md
[building]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/Building.md
[refuge]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/Refuge.md
[gasstation]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/GasStation.md
[centerentities]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/CenterEntities.md
[blockade]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/Blockade.md
[edge]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/Edge.md

<!-- entities / humans -->
[human]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/Human.md
[civilian]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/Civilian.md
[ambulanceteam]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/AmbulanceTeam.md
[firebrigade]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/FireBrigade.md
[policeforce]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/PoliceForce.md

<!-- enums / type tags -->
[standardentityurn]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/StandardEntityURN.md
[standardpropertyurn]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/StandardPropertyURN.md
[standardentityconstants]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/entities/StandardEntityConstants.md

<!-- worldmodel -->
[standardworldmodel]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/worldmodel/StandardWorldModel.md
[entityid]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/worldmodel/EntityID.md
[changeset]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/worldmodel/ChangeSet.md

<!-- geometry / helpers -->
[point2d]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/misc/geometry/Point2D.md
[vector2d]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/misc/geometry/Vector2D.md
[line2d]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/misc/geometry/Line2D.md
[geometrytools2d]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/misc/geometry/GeometryTools2D.md
[pair]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/misc/Pair.md

<!-- messages, config, log -->
[command]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/messages/Command.md
[config]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/config/Config.md
[logger]: https://github.com/FlyingCalours/RoboCupRAS/blob/main/api_ref/server-docs/log/Logger.md