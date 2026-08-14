# StaticClustering.java

```java
StaticClustering(
    AgentInfo ai, 
    WorldInfo wi, 
    ScenarioInfo si, 
    ModuleManager moduleManager, 
    DevelopData developData
);
```

[Attributes Stored](#attributes-stored)

**StaticClustering** is an abstract extension of **[Clustering](Clustering.md)** designed for **static spatial clustering** algorithms where cluster boundaries and entity partition groupings remain fixed or precomputed throughout the simulation run.

- Inherits all cluster querying and management methods from **[Clustering.java](Clustering.md)**.

## <a id="attributes-stored"></a>Attributes Stored
1. Inherited from `Clustering` / `AbstractModule`:
   - `protected ScenarioInfo scenarioInfo` : Reference to scenario configuration information.
   - `protected AgentInfo agentInfo` : Reference to agent self-awareness state.
   - `protected WorldInfo worldInfo` : Reference to world model representation.
   - `protected ModuleManager moduleManager` : Pointer to module manager.
   - `protected DevelopData developData` : Reference to development data parameters.

---
