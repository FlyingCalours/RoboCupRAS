# AgentInfo.java

```
AgentInfo(
    @Nonnull Agent agent, 
    @Nonnull StandardWorldModel world
);
```

[Attributes Stored](#attributes-stored)

**AgentInfo** is a **self-awareness** module answer questions like :

- Who am **I** ?
  - [getID()](#getid)
  - [me()](#me)

- Where am I **located** ?
  - [getX()](#getx)
  - [getY()](#gety)
  - [getPosition()](#getposition)
  - [getPositionArea()](#getpositionarea)

- What do I **see and hear** ?
  - [setTime()](#settime)
  - [getTime()](#gettime)
  - [setHeard()](#setheard)
  - [getHeard()](#getheard)
  - [setChanged()](#setchanged)
  - [getChanged()](#getchanged)

- **Special Capabilities** (Water & Transport)
  - [someoneOnBoard()](#someoneonboard)
  - [isWaterDefined()](#iswaterdefined)
  - [getWater()](#getwater)

- What did I **do last tick** ?
  - [getExecutedAction()](#getexecutedaction)
  - [setExecutedAction()](#setexecutedaction)

- Am I going to **timeout** ?
  - [recordThinkStartTime()](#recordthinkstarttime)
  - [getThinkTimeMillis()](#getthinktimemillis)

## <a id="attributes-stored"></a>Attributes Stored
1. `private Agent agent` : Reference to the low-level RoboCup core agent object.
2. `private StandardWorldModel world` : Pointer to the standard world model holding all map entities.
3. `private int time` : The current simulation timestep (e.g., tick 1, 2, 3...). 
4. `private ChangeSet changed` : What the agent **sees directly** with its own eyes during the current tick
5. `private Collection<Command> heard` : Voice and radio commands/messages **received** in the current tick.
6. `private long thinkStartTime` : System timestamp (in milliseconds) recorded when the agent started thinking for this tick.

7. `private Map<Integer, Action> actionHistory` : A map storing past actions taken across all previous timesteps (Tick -> Action).


## <a id="getid"></a>getID()

```java
EntityID getID();
```
Get ID assigned to this agent by the simulation server.

**Parameters :**
- None

**Returns :**
- `EntityID` : The unique numerical ID of this agent.

---

## <a id="me"></a>me()

```java
StandardEntity me();
```
Returns the entity object representing this agent. Usually cast this to your agent type (e.g., `(PoliceForce) agentInfo.me()`).

**Parameters :**
- None

**Returns :**
- `StandardEntity` : The entity object representing this agent.

---

## <a id="getx"></a>getX()

```java
double getX();
```
Get the current X coordinate of the agent on the map.

**Parameters :**
- None

**Returns :**
- `double` : The X coordinate position.

---

## <a id="gety"></a>getY()

```java
double getY();
```
Get the current Y coordinate of the agent on the map.

**Parameters :**
- None

**Returns :**
- `double` : The Y coordinate position.

---

## <a id="getposition"></a>getPosition()

```java
EntityID getPosition();
```
Get the `EntityID` of the map location (Road or Building) where the agent is currently standing.

**Parameters :**
- None

**Returns :**
- `EntityID` : The ID of the current position entity (or human ID if inside a vehicle/agent).

---

## <a id="getpositionarea"></a>getPositionArea()

```java
Area getPositionArea();
```
Get the `Area` object (Road or Building) where the agent is currently standing.

**Parameters :**
- None

**Returns :**
- `Area` : The Area entity representing the current position.

---

## <a id="settime">setTime()

```java
void setTime(int time);
```
Sets the current simulation time step (tick).

**Parameters :**
- `time` : The current simulation timestep integer.

**Returns :**
- `void`

---

## <a id="gettime"></a>getTime()

```java
int getTime();
```
Get the current simulation time step (tick).

**Parameters :**
- None

**Returns :**
- `int` : The current simulation step number.

---

## <a id="setheard"></a>setHeard()

```java
void setHeard(Collection<Command> heard);
```
Sets the collection of commands/messages heard by the agent in the current tick.

**Parameters :**
- `heard` : A collection of received `Command` objects.

**Returns :**
- `void`

---

## <a id="getheard"></a>getHeard()

```java
Collection<Command> getHeard();
```
Get the collection of commands/messages heard by the agent in the current tick.

**Parameters :**
- None

**Returns :**
- `Collection<Command>` : A collection of voice/radio commands received, or `null`.

---

## <a id="setchanged"></a>setChanged()

```java
void setChanged(ChangeSet changed);
```
Sets the set of entity visual changes observed directly by the agent in the current tick.

**Parameters :**
- `changed` : The `ChangeSet` object containing perceived entity changes.

**Returns :**
- `void`

---

## <a id="getchanged"></a>getChanged()

```java
ChangeSet getChanged();
```
Get the visual perception changes observed directly by the agent in the current tick.

**Parameters :**
- None

**Returns :**
- `ChangeSet` : The set of changed entities perceived this tick, or `null`.

---

## <a id="someoneonboard"></a>someoneOnBoard()

```java
Human someoneOnBoard();
```
Checks if a civilian or injured agent is currently loaded inside this agent (used by Ambulance Teams).

**Parameters :**
- None

**Returns :**
- `Human` : The `Human` entity onboard, or `null` if empty.

---

## <a id="iswaterdefined"></a>isWaterDefined()

```java
boolean isWaterDefined();
```
Checks if water capacity data is defined for this agent (valid for Fire Brigade agents).

**Parameters :**
- None

**Returns :**
- `boolean` : `true` if water level is defined and agent is a Fire Brigade; otherwise `false`.

---

## <a id="getwater"></a>getWater()

```java
int getWater();
```
Get the current remaining water volume for Fire Brigade agents.

**Parameters :**
- None

**Returns :**
- `int` : Water quantity in liters (returns `0` if not a Fire Brigade).

---

## <a id="getexecutedaction"></a>getExecutedAction()

```java
Action getExecutedAction(int time);
```
Get the action executed at a specific timestep (use positive number for absolute tick, `0` or negative for relative tick relative to current time).

**Parameters :**
- `time` : Absolute tick number (e.g., `10`) or relative offset (e.g., `-1` for previous tick).

**Returns :**
- `Action` : The action executed at that timestep, or `null`.

---

## <a id="setexecutedaction"></a>setExecutedAction()

```java
void setExecutedAction(int time, Action action);
```
Record an executed action into the agent's action history for a given timestep.

**Parameters :**
- `time` : Timestep integer (absolute or relative offset).
- `action` : The `Action` object executed.

**Returns :**
- `void`

---

## <a id="recordthinkstarttime"></a>recordThinkStartTime()

```java
void recordThinkStartTime();
```
Records the current system time in milliseconds as the starting time for the agent's tick decision-making process.

**Parameters :**
- None

**Returns :**
- `void`

---

## <a id="getthinktimemillis"></a>getThinkTimeMillis()

```java
long getThinkTimeMillis();
```
Get the elapsed time in milliseconds since the start of thinking for the current tick.

**Parameters :**
- None

**Returns :**
- `long` : Elapsed time in milliseconds.

---