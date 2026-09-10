package sample_team.module.complex.observation;

import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.component.extaction.ExtAction;

import sample_team.module.complex.police.improvement.URFPoliceExtActionClear;
import sample_team.module.complex.police.observation.URFPoliceMetrics;

public class URFObservedPoliceExtActionClear extends URFPoliceExtActionClear{
    private final URFPoliceMetrics metrics;

    public URFObservedPoliceExtActionClear(
        AgentInfo ai,
        WorldInfo wi,
        ScenarioInfo si,
        ModuleManager mm,
        DevelopData dd
    ){

        super(ai,wi,si,mm,dd);
        this.metrics = URFPoliceMetrics.forAgent(ai.getID());
        this.
    }
}
