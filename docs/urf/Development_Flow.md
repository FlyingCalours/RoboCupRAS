# Development Progress

## State 0 : API Reference preparation and Project Understanding

By exploring the project for `adf-core` and `rcrs-server`, we did not find documentation that can refer for the development later. We decide to understand and filter important API from this 2 repository with the help of LLM (Opus 5 from Claude). We use markmap as an interactive tool to connect related files(parent-child) instead of dump 100+ files in once.

Command to view the API Reference :

```
some commands
```

After exploring the project, our group decide to improve the performance of police since we get some codes for police from lecturer Dr. Mohammad Babrdel Bonab, this might be a sweet point to start. 

## State 1 : Compare Performance between URF and Default

While most of the configuration remained the same, we compare the performance for police clearing logic for `URFPoliceExtActionClear.java` from lecturer and `DefaultExtActionClear.java` from `adf.impl.extaction`.

We tried 3 maps such as Montreal, Paris and Berlin with 3 runs each.

`URFPoliceExtActionClear.java` result :

|  | Montreal | Paris | Berlin |
| -- | -- | -- | -- |
| Score 1 | score | score | score |
| Score 2 | score | score | score |
| Score 3 | score | score | score |

`DefaultExtActionClear.java` result :

|  | Montreal | Paris | Berlin |
| -- | -- | -- | -- |
| Score 1 | score | score | score |
| Score 2 | score | score | score |
| Score 3 | score | score | score |

We observed some issues during the run.

### Issue 1 : Blockade in front of Refuge Building doesnt't clear

We define the road connected with the refuge as door. The door have blockade making all the refugee was blocked and ambulance patient can't get inside the refuge making patient dead outside.

`Some Image Here`

### Issue 2 : Most of the police stuck in front of blockade and wall

This happened in both action clear file, some police stuck in front blockade. As timestep, t increased, more police stuck. 

`Some image for police in URF stuck`

`Some image for police in Default stuck`

### Issue 3 : Ambulance get blocked by blockade and police just pass by

Most of the ambulance have this issue, when a blockade blocking the door for building, they stuck, a police passby didn't give a look on the blockade that block ambulance, they keep clearing low value blockade.

`Some image for police ignore ambulance`


## State 2 : Develop Module and Solve Issue 1

We decide to solve the first issue first.

**Build :**
- `URFPoliceExtActionMove.java`
- `URFRoadDetector.java`
- `URFDoorSweep.java`

**Police Decision with Module :**
|  | `URFPoliceExtActionMove` | `URFPoliceExtActionClear` |
| -- | -- | -- |
| move | return ActionMove() | return null |
| clear | return null | return ActionClear() |


`URFRoadDetector` is the one who decide a police should move or clear. `URFDoorSweep` is the logics for **all police visit all refuge** to clean blockade. Eventhough this is inefficient at first glance, but this work and further improve.

**Result :**
|  | Motreal |
| -- | -- |
| score 1 | score |
| score 2 | score |
| score 3 | score |

## State 3 : Solve Police Stuck in URFPoliceExtActionClear

As stated in [Issue 2](#issue-2--most-of-the-police-stuck-in-front-of-blockade-and-wall), police will stuck. We decide to solve the bug in `URFPoliceExtActionClear`

The core reason of police stuck is **police only clean the blockade on the current road**. When a blockade was defined on another road at the edge. Police think there is no blockade on current road, so police decide move to another road. But police can't move since blockade on another road blocking police. Stuck happened. 

We define `findNearbyCandidate()` and a helper `searchAreas` replace `findCurrentRoadCandidate()`, so police can clear blockade surrounding.

## State 4 : Develop Communication Module

This is to solve the issue 3 where police need to collaborate with ambulance.

Build or Modify :
- `URFMessageAmbulanceTeam`
- `URFRoadDetector`

Ambulance Team will keep reporting their current position, if there is certain amount of same position, police can confirm ambulance team was stuck, police will immediately navigate to the ambulance team location and clean the blockade.

**Result :**
|  | Motreal |
| -- | -- |
| score 1 | score |
| score 2 | score |
| score 3 | score |

## State 5 : Logging

Observed Module :
- `URFObservedPoliceExtActionClear`
- `URFObservedPoliceExtActionMove`
- `URFObservedRoadDetector`

Helper :
- `URFPoliceCsvExporter`
- `URFPoliceCsvExporter`

The purpose of doing logging is collect data for future machine learning implementation.
