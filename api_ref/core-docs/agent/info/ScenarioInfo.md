# ScenarioInfo.java

```java
ScenarioInfo(
    @Nonnull Config config, 
    @Nonnull Mode mode
);

ScenarioInfo(
    @Nonnull Config config
);
```

[Attributes Stored](#attributes-stored)

**ScenarioInfo** is a **scenario configuration** module that provides convenient accessors for simulation settings, server parameters, and agent counts, answering questions like :

- What is the **agent run mode** ?
  - [getMode()](#getmode)

- How to access or set the **raw configuration** ?
  - [getRawConfig()](#getrawconfig)
  - [setConfig()](#setconfig)

- What are the **Firefighting & Water** parameters ?
  - [getFireExtinguishMaxSum()](#getfireextinguishmaxsum)
  - [getFireExtinguishMaxDistance()](#getfireextinguishmaxdistance)
  - [getFireTankMaximum()](#getfiretankmaximum)
  - [getFireTankRefillRate()](#getfiretankrefillrate)
  - [getFireTankRefillHydrantRate()](#getfiretankrefillhydrantrate)

- What are the **Clear & Repair** parameters ?
  - [getClearRepairRate()](#getclearrepairrate)
  - [getClearRepairDistance()](#getclearrepairdistance)
  - [getClearRepairRad()](#getclearrepairrad)

- What are the **Perception & Vision** parameters ?
  - [getPerceptionLosMaxDistance()](#getperceptionlosmaxdistance)
  - [getPerceptionLosPrecisionDamage()](#getperceptionlosprecisiondamage)
  - [getPerceptionLosPrecisionHp()](#getperceptionlosprecisionhp)

- What are the **Communication Channels** parameters ?
  - [getCommsChannelsCount()](#getcommschannelscount)
  - [getCommsChannelBandwidth()](#getcommschannelbandwidth)
  - [getCommsChannelsMaxPlatoon()](#getcommschannelsmaxplatoon)
  - [getCommsChannelsMaxOffice()](#getcommschannelsmaxoffice)
  - [getVoiceMessagesSize()](#getvoicemessagessize)

- What are the **Kernel & Simulation** parameters ?
  - [getKernelHost()](#getkernelhost)
  - [getKernelTimesteps()](#getkerneltimesteps)
  - [getKernelAgentsThinkTime()](#getkernelagentsthinktime)
  - [getKernelStartupConnectTime()](#getkernelstartupconnecttime)
  - [getKernelAgentsIgnoreuntil()](#getkernelagentsignoreuntil)
  - [getKernelCommunicationModel()](#getkernelcommunicationmodel)
  - [getKernelPerception()](#getkernelperception)

- How many **Agents & Centres** exist in the scenario ?
  - [getScenarioAgentsAt()](#getscenarioagentsat)
  - [getScenarioAgentsFb()](#getscenarioagentsfb)
  - [getScenarioAgentsPf()](#getscenarioagentspf)
  - [getScenarioAgentsAc()](#getscenarioagentsac)
  - [getScenarioAgentsFs()](#getscenarioagentsfs)
  - [getScenarioAgentsPo()](#getscenarioagentspo)

- Is the framework running in **Debug or Development** mode ?
  - [isDebugMode()](#isdebugmode)
  - [isDevelopMode()](#isdevelopmode)

## <a id="attributes-stored"></a>Attributes Stored
1. `private Config config` : The base RoboCup Rescue simulation configuration object (`rescuecore2.config.Config`).
2. `private Mode mode` : The execution mode of the agent (`NON_PRECOMPUTE`, `PRECOMPUTED`, or `PRECOMPUTATION_PHASE`).

---

## <a id="getmode"></a>getMode()

```java
Mode getMode();
```
Get the current execution run mode of the agent.

**Parameters :**
- None

**Returns :**
- `Mode` : The agent run mode (`NON_PRECOMPUTE`, `PRECOMPUTED`, or `PRECOMPUTATION_PHASE`).

---

## <a id="getrawconfig"></a>getRawConfig()

```java
Config getRawConfig();
```
Get the raw configuration object holding all key-value simulation options.

**Parameters :**
- None

**Returns :**
- `Config` : The underlying `rescuecore2.config.Config` object.

---

## <a id="setconfig"></a>setConfig()

```java
void setConfig(Config config);
```
Sets or updates the base configuration object.

**Parameters :**
- `config` : The `Config` instance to assign.

**Returns :**
- `void`

---

## <a id="getfireextinguishmaxsum"></a>getFireExtinguishMaxSum()

```java
int getFireExtinguishMaxSum();
```
Get the maximum cumulative extinguish power allowed across all Fire Brigades in a cycle (`fire.extinguish.max-sum`).

**Parameters :**
- None

**Returns :**
- `int` : The maximum extinguish sum value.

---

## <a id="getfireextinguishmaxdistance"></a>getFireExtinguishMaxDistance()

```java
int getFireExtinguishMaxDistance();
```
Get the maximum effective range in millimeters for fire extinguishing actions (`fire.extinguish.max-distance`).

**Parameters :**
- None

**Returns :**
- `int` : Maximum extinguish distance in millimeters.

---

## <a id="getfiretankmaximum"></a>getFireTankMaximum()

```java
int getFireTankMaximum();
```
Get the maximum water capacity in liters of a Fire Brigade's water tank (`fire.tank.maximum`).

**Parameters :**
- None

**Returns :**
- `int` : Maximum water capacity in liters.

---

## <a id="getfiretankrefillrate"></a>getFireTankRefillRate()

```java
int getFireTankRefillRate();
```
Get the rate in liters per tick at which a Fire Brigade's tank refills when positioned at a refuge (`fire.tank.refill_rate`). Defaults to `500` if not explicitly specified.

**Parameters :**
- None

**Returns :**
- `int` : Water refill rate at refuges (default `500`).

---

## <a id="getfiretankrefillhydrantrate"></a>getFireTankRefillHydrantRate()

```java
int getFireTankRefillHydrantRate();
```
Get the rate in liters per tick at which a Fire Brigade's tank refills when connected to a hydrant (`fire.tank.refill_hydrant_rate`).

**Parameters :**
- None

**Returns :**
- `int` : Water refill rate at hydrants.

---

## <a id="getclearrepairrate"></a>getClearRepairRate()

```java
int getClearRepairRate();
```
Get the rate at which Police Force agents clear/repair blockages per cycle (`clear.repair.rate`).

**Parameters :**
- None

**Returns :**
- `int` : Road repair/clear rate.

---

## <a id="getclearrepairdistance"></a>getClearRepairDistance()

```java
int getClearRepairDistance();
```
Get the maximum distance in millimeters from which a Police Force agent can perform clear actions (`clear.repair.distance`).

**Parameters :**
- None

**Returns :**
- `int` : Maximum clear action distance in millimeters.

---

## <a id="getclearrepairrad"></a>getClearRepairRad()

```java
int getClearRepairRad();
```
Get the effective clearance radius in millimeters surrounding a clear operation (`clear.repair.rad`).

**Parameters :**
- None

**Returns :**
- `int` : Repair radius in millimeters.

---

## <a id="getperceptionlosmaxdistance"></a>getPerceptionLosMaxDistance()

```java
int getPerceptionLosMaxDistance();
```
Get the maximum Line-of-Sight perception distance in millimeters (`perception.los.max-distance`).

**Parameters :**
- None

**Returns :**
- `int` : Maximum perception distance in millimeters.

---

## <a id="getperceptionlosprecisiondamage"></a>getPerceptionLosPrecisionDamage()

```java
int getPerceptionLosPrecisionDamage();
```
Get the calculation precision factor for damage perception under Line-of-Sight (`perception.los.precision.damage`).

**Parameters :**
- None

**Returns :**
- `int` : Precision level for damage calculation.

---

## <a id="getperceptionlosprecisionhp"></a>getPerceptionLosPrecisionHp()

```java
int getPerceptionLosPrecisionHp();
```
Get the calculation precision factor for Health Points (HP) perception under Line-of-Sight (`perception.los.precision.hp`).

**Parameters :**
- None

**Returns :**
- `int` : Precision level for HP calculation.

---

## <a id="getcommschannelscount"></a>getCommsChannelsCount()

```java
int getCommsChannelsCount();
```
Get the total number of communication channels configured in the scenario (`comms.channels.count`).

**Parameters :**
- None

**Returns :**
- `int` : Number of available communication channels.

---

## <a id="getcommschannelbandwidth"></a>getCommsChannelBandwidth()

```java
int getCommsChannelBandwidth(int channel);
```
Get the maximum bandwidth capacity in bytes per cycle for a specific communication channel (`comms.channels.<channel>.bandwidth`).

**Parameters :**
- `channel` : The channel index integer.

**Returns :**
- `int` : Bandwidth limit for the specified channel, or `0` if channel index is invalid.

---

## <a id="getcommschannelsmaxplatoon"></a>getCommsChannelsMaxPlatoon()

```java
int getCommsChannelsMaxPlatoon();
```
Get the maximum number of channels a field agent (Platoon: Fire Brigade, Police Force, Ambulance Team) can concurrently subscribe to (`comms.channels.max.platoon`).

**Parameters :**
- None

**Returns :**
- `int` : Maximum channels allowed for platoon agents.

---

## <a id="getcommschannelsmaxoffice"></a>getCommsChannelsMaxOffice()

```java
int getCommsChannelsMaxOffice();
```
Get the maximum number of channels a centre/office agent (Fire Station, Police Office, Ambulance Centre) can concurrently subscribe to (`comms.channels.max.centre`).

**Parameters :**
- None

**Returns :**
- `int` : Maximum channels allowed for centre entities.

---

## <a id="getvoicemessagessize"></a>getVoiceMessagesSize()

```java
int getVoiceMessagesSize();
```
Get the maximum allowed message size in bytes for acoustic voice messages on channel 0 (`comms.channels.0.messages.size`).

**Parameters :**
- None

**Returns :**
- `int` : Maximum voice message size in bytes.

---

## <a id="getkernelhost"></a>getKernelHost()

```java
String getKernelHost();
```
Get the hostname or IP address of the simulation kernel server (`kernel.host`).

**Parameters :**
- None

**Returns :**
- `String` : Server host address, or `null` if unspecified.

---

## <a id="getkerneltimesteps"></a>getKernelTimesteps();

```java
int getKernelTimesteps();
```
Get the total number of timesteps configured for the simulation run (`kernel.timesteps`).

**Parameters :**
- None

**Returns :**
- `int` : Total simulation timesteps.

---

## <a id="getkernelagentsthinktime"></a>getKernelAgentsThinkTime()

```java
int getKernelAgentsThinkTime();
```
Get the maximum decision time limit in milliseconds given to agents during each tick (`kernel.agents.think-time`).

**Parameters :**
- None

**Returns :**
- `int` : Agent thinking time limit in milliseconds.

---

## <a id="getkernelstartupconnecttime"></a>getKernelStartupConnectTime()

```java
int getKernelStartupConnectTime();
```
Get the timeout duration in milliseconds allocated for agents and simulators to connect to the kernel on startup (`kernel.startup.connect-time`).

**Parameters :**
- None

**Returns :**
- `int` : Connection window duration in milliseconds.

---

## <a id="getkernelagentsignoreuntil"></a>getKernelAgentsIgnoreuntil()

```java
int getKernelAgentsIgnoreuntil();
```
Get the initial simulation timestep up to which agent action commands are ignored by the kernel (`kernel.agents.ignoreuntil`).

**Parameters :**
- None

**Returns :**
- `int` : Timestep threshold before command processing starts.

---

## <a id="getkernelcommunicationmodel"></a>getKernelCommunicationModel()

```java
String getKernelCommunicationModel();
```
Get the fully-qualified class name of the communication model utilized by the kernel (`kernel.communication-model`).

**Parameters :**
- None

**Returns :**
- `String` : Class name of the communication model, or `null`.

---

## <a id="getkernelperception"></a>getKernelPerception()

```java
String getKernelPerception();
```
Get the fully-qualified class name of the perception module utilized by the kernel (`kernel.perception`).

**Parameters :**
- None

**Returns :**
- `String` : Class name of the perception implementation, or `null`.

---

## <a id="getscenarioagentsat"></a>getScenarioAgentsAt()

```java
int getScenarioAgentsAt();
```
Get the total number of Ambulance Team (`AT`) agents present in the scenario (`scenario.agents.at`).

**Parameters :**
- None

**Returns :**
- `int` : Count of Ambulance Team agents.

---

## <a id="getscenarioagentsfb"></a>getScenarioAgentsFb()

```java
int getScenarioAgentsFb();
```
Get the total number of Fire Brigade (`FB`) agents present in the scenario (`scenario.agents.fb`).

**Parameters :**
- None

**Returns :**
- `int` : Count of Fire Brigade agents.

---

## <a id="getscenarioagentspf"></a>getScenarioAgentsPf()

```java
int getScenarioAgentsPf();
```
Get the total number of Police Force (`PF`) agents present in the scenario (`scenario.agents.pf`).

**Parameters :**
- None

**Returns :**
- `int` : Count of Police Force agents.

---

## <a id="getscenarioagentsac"></a>getScenarioAgentsAc()

```java
int getScenarioAgentsAc();
```
Get the total number of Ambulance Centre (`AC`) entities present in the scenario (`scenario.agents.ac`).

**Parameters :**
- None

**Returns :**
- `int` : Count of Ambulance Centre entities.

---

## <a id="getscenarioagentsfs"></a>getScenarioAgentsFs()

```java
int getScenarioAgentsFs();
```
Get the total number of Fire Station (`FS`) entities present in the scenario (`scenario.agents.fs`).

**Parameters :**
- None

**Returns :**
- `int` : Count of Fire Station entities.

---

## <a id="getscenarioagentspo"></a>getScenarioAgentsPo()

```java
int getScenarioAgentsPo();
```
Get the total number of Police Office (`PO`) entities present in the scenario (`scenario.agents.po`).

**Parameters :**
- None

**Returns :**
- `int` : Count of Police Office entities.

---

## <a id="isdebugmode"></a>isDebugMode()

```java
boolean isDebugMode();
```
Check if the framework is executing with the debug flag enabled (`ConfigKey.KEY_DEBUG_FLAG`).

**Parameters :**
- None

**Returns :**
- `boolean` : `true` if debug mode is active; otherwise `false`.

---

## <a id="isdevelopmode"></a>isDevelopMode()

```java
boolean isDevelopMode();
```
Check if the framework is executing with the development flag enabled (`ConfigKey.KEY_DEVELOP_FLAG`).

**Parameters :**
- None

**Returns :**
- `boolean` : `true` if development mode is active; otherwise `false`.

---