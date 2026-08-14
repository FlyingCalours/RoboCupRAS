# rcrs-server (`rescuecore2`) — API index

Documentation of the `rescuecore2` classes an ADF team actually calls. The rest of `rcrs-server` — the kernel, the simulators (`traffic`, `fire`, `collapse`, `clear`, `misc`), GIS/map tools, protobuf codecs, GUI components and `oldsims` — runs on the server side and is **not** callable from agent code, so it is not documented here.

Folder layout mirrors the Java packages, so a file's path is its import path:
`entities/Building.md` &rarr; `rescuecore2.standard.entities.Building`.

## worldmodel

The map database and the identifiers used to address it.

| File | Class | What it is |
|---|---|---|
| <a href="worldmodel/StandardWorldModel.md">worldmodel/StandardWorldModel.md</a> | `standard.entities.StandardWorldModel` | The raw map database — lookups, spatial range queries, distances |
| <a href="worldmodel/EntityID.md">worldmodel/EntityID.md</a> | `worldmodel.EntityID` | Unique handle of every object; safe as a map key |
| <a href="worldmodel/ChangeSet.md">worldmodel/ChangeSet.md</a> | `worldmodel.ChangeSet` | What the agent perceived this tick |

## entities

Everything that exists on the map.

**Base types**

| File | Class | What it is |
|---|---|---|
| <a href="entities/StandardEntity.md">entities/StandardEntity.md</a> | `StandardEntity` | Common parent of every map object |
| <a href="entities/StandardEntityURN.md">entities/StandardEntityURN.md</a> | `StandardEntityURN` | Entity type tag — used by `getEntitiesOfType()` |
| <a href="entities/StandardPropertyURN.md">entities/StandardPropertyURN.md</a> | `StandardPropertyURN` | Property identifier — used when reading a `ChangeSet` |
| <a href="entities/StandardEntityConstants.md">entities/StandardEntityConstants.md</a> | `Fieryness`, `BuildingCode` | Readable enums for building state |

**Areas — places an agent can stand**

| File | Class | What it is |
|---|---|---|
| <a href="entities/Area.md">entities/Area.md</a> | `Area` | Parent of Road and Building — neighbours, edges, blockades |
| <a href="entities/Road.md">entities/Road.md</a> | `Road` | Travel surface; the police force's workspace |
| <a href="entities/Building.md">entities/Building.md</a> | `Building` | Can burn and collapse; the fire brigade's target list |
| <a href="entities/Refuge.md">entities/Refuge.md</a> | `Refuge` | Unload civilians, refill water, heal agents |
| <a href="entities/Hydrant.md">entities/Hydrant.md</a> | `Hydrant` | Road where a fire brigade can refill |
| <a href="entities/GasStation.md">entities/GasStation.md</a> | `GasStation` | Building that explodes when it burns |
| <a href="entities/CenterEntities.md">entities/CenterEntities.md</a> | `AmbulanceCentre`, `FireStation`, `PoliceOffice` | The three headquarters buildings |
| <a href="entities/Blockade.md">entities/Blockade.md</a> | `Blockade` | Rubble that stops movement |
| <a href="entities/Edge.md">entities/Edge.md</a> | `Edge` | One boundary segment of an area — wall or passage |

**Humans — everything that moves**

| File | Class | What it is |
|---|---|---|
| <a href="entities/Human.md">entities/Human.md</a> | `Human` | Parent of all four below — HP, damage, buriedness, position |
| <a href="entities/Civilian.md">entities/Civilian.md</a> | `Civilian` | The victim; the score |
| <a href="entities/AmbulanceTeam.md">entities/AmbulanceTeam.md</a> | `AmbulanceTeam` | Rescues and transports |
| <a href="entities/FireBrigade.md">entities/FireBrigade.md</a> | `FireBrigade` | Extinguishes; adds the water property |
| <a href="entities/PoliceForce.md">entities/PoliceForce.md</a> | `PoliceForce` | Clears blockades |

## misc

Geometry and small helpers.

| File | Class | What it is |
|---|---|---|
| <a href="misc/geometry/Point2D.md">misc/geometry/Point2D.md</a> | `misc.geometry.Point2D` | Immutable position |
| <a href="misc/geometry/Vector2D.md">misc/geometry/Vector2D.md</a> | `misc.geometry.Vector2D` | Direction and length — used with `ActionClear` |
| <a href="misc/geometry/Line2D.md">misc/geometry/Line2D.md</a> | `misc.geometry.Line2D` | Line segment; returned by `Edge.getLine()` |
| <a href="misc/geometry/GeometryTools2D.md">misc/geometry/GeometryTools2D.md</a> | `misc.geometry.GeometryTools2D` | Intersections, distances, polygon area and centroid |
| <a href="misc/Pair.md">misc/Pair.md</a> | `misc.Pair` | Two value container; returned by `getLocation()` |

## messages, config, log

| File | Class | What it is |
|---|---|---|
| <a href="messages/Command.md">messages/Command.md</a> | `messages.Command` (+ the `AK*` classes) | Protocol level order; what `AgentInfo.getHeard()` returns |
| <a href="config/Config.md">config/Config.md</a> | `config.Config` | Key/value store of simulation settings |
| <a href="log/Logger.md">log/Logger.md</a> | `log.Logger` | Logging with agent context |

## Reading these files

- **Units :** distances are in map units, roughly **1 metre = 1000 units**.
- **Defined checks :** every `getXxx()` on an entity has a matching `isXxxDefined()`.
  For entities learned through radio messages rather than seen directly, check it
  before trusting the value — an undefined property reads as `0`, which looks like
  a healthy civilian or a clear road.
- **Casting :** `getEntity()` and `getEntitiesOfType()` return `StandardEntity`;
  cast to the concrete type, or branch on `getStandardURN()`.