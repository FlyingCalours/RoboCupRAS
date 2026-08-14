# DynamicClustering.java

```java
DynamicClustering(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

**DynamicClustering** is an abstract extension of **[Clustering](Clustering.md)** designed for **dynamic spatial clustering** algorithms where cluster boundaries, entity assignments, or total cluster counts dynamically change during simulation runtime based on real-time world events.

- Inherits all cluster querying and management methods from **[Clustering.java](Clustering.md)**.

## <a id="attributes-stored"></a>Attributes Stored
1. Inherited from `Clustering` / `AbstractModule`:
   - `protected ScenarioInfo scenarioInfo` : Reference to scenario configuration information.
   - `protected AgentInfo agentInfo` : Reference to agent self-awareness state.
   - `protected WorldInfo worldInfo` : Reference to world model representation.
   - `protected ModuleManager moduleManager` : Pointer to module manager.
   - `protected DevelopData developData` : Reference to development data parameters.

---
