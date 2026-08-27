package urf.police.observation;

import adf.core.agent.action.Action;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.component.extaction.ExtAction;
import adf.impl.extaction.DefaultExtActionClear;
import rescuecore2.worldmodel.EntityID;

/**
 * Observation wrapper around DefaultExtActionClear.
 *
 * <p>Observation responsibilities: - record CLEAR-module result - measure CLEAR calculation time -
 * evaluate stuck evidence - export final autonomous decision
 *
 * <p>Position/X/Y are intentionally NOT recorded here. URFObservedRoadDetector is the single owner
 * of physical position observation.
 *
 * <p>Police behaviour is unchanged.
 */
public class URFObservedExtActionClear extends DefaultExtActionClear {

  private final URFPoliceMetrics metrics;

  private final URFPoliceStuckDetector stuckDetector;

  private EntityID requestedTarget;

  public URFObservedExtActionClear(
      AgentInfo agentInfo,
      WorldInfo worldInfo,
      ScenarioInfo scenarioInfo,
      ModuleManager moduleManager,
      DevelopData developData) {

    super(agentInfo, worldInfo, scenarioInfo, moduleManager, developData);

    this.metrics = URFPoliceMetrics.forAgent(agentInfo.getID());

    this.stuckDetector = URFPoliceStuckDetector.forAgent(agentInfo.getID());

    this.requestedTarget = null;
  }

  @Override
  public ExtAction setTarget(EntityID target) {

    this.requestedTarget = target;

    /*
     * Preserve the complete official target
     * processing.
     */
    super.setTarget(target);

    return this;
  }

  @Override
  public ExtAction calc() {

    long startNanos = System.nanoTime();

    /*
     * Execute the complete official CLEAR
     * implementation.
     */
    super.calc();

    long elapsedNanos = System.nanoTime() - startNanos;

    Action action = super.getAction();

    /*
     * Position is NOT recorded here.
     *
     * It was already recorded by
     * URFObservedRoadDetector for this timestep.
     */
    this.metrics.recordClearModuleResult(
        this.agentInfo.getTime(), this.requestedTarget, action, elapsedNanos);

    /*
     * A non-null CLEAR-module result becomes
     * the final autonomous Police action.
     */
    if (action != null) {

      this.stuckDetector.evaluate(this.metrics);

      URFPoliceCsvExporter.export(this.metrics, this.stuckDetector);

      System.out.println(this.metrics.toLogLine() + " " + this.stuckDetector.toLogFields());
    }

    return this;
  }
}