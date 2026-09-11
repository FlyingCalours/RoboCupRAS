package sample_team.module.complex.police.observation;

import adf.core.agent.action.Action;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.component.extaction.ExtAction;
import adf.impl.extaction.DefaultExtActionMove;
import rescuecore2.worldmodel.EntityID;

/**
 * Observation wrapper around DefaultExtActionMove.
 *
 * Observation responsibilities:
 * - record MOVE-module result
 * - measure MOVE calculation time
 * - evaluate stuck evidence
 * - export final autonomous decision
 *
 * Position/X/Y are intentionally NOT recorded here.
 * URFObservedRoadDetector is the single owner of
 * physical position observation.
 *
 * Police behaviour is unchanged.
 */
public class URFObservedExtActionMove extends DefaultExtActionMove {
  private final URFPoliceMetrics metrics;
  private final URFPoliceStuckDetector stuckDetector;
  private EntityID requestedTarget;

  public URFObservedExtActionMove(AgentInfo agentInfo, WorldInfo worldInfo, 
                                  ScenarioInfo scenarioInfo, ModuleManager moduleManager,
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
     * Preserve complete official MOVE target
     * processing.
     */
    super.setTarget(target);

    return this;
  }

  @Override
  public ExtAction calc() {

    long startNanos = System.nanoTime();

    /*
     * Execute complete official MOVE logic.
     */
    super.calc();

    long elapsedNanos = System.nanoTime() - startNanos;

    Action action = super.getAction();

    /*
     * Position is NOT recorded here.
     */
    this.metrics.recordMoveModuleResult(this.agentInfo.getTime(), this.requestedTarget,
                                        action, elapsedNanos);

    /*
     * MOVE is the last autonomous decision stage.
     *
     * If it returns null, Metrics preserves the
     * DefaultTacticsPoliceForce fallback REST
     * observation.
     */
    this.stuckDetector.evaluate(this.metrics);

    URFPoliceCsvExporter.export(this.metrics, this.stuckDetector);

    System.out.println(this.metrics.toLogLine() + " " + this.stuckDetector.toLogFields());
    return this;
  }
}
