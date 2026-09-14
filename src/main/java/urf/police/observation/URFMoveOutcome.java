package urf.police.observation;

/**
 * Physical result of the MOVE chosen during the previous timestep.
 *
 * The outcome is only meaningful one timestep after the decision,
 * because the simulator executes an action after it is chosen.
 */
public enum URFMoveOutcome {
  NOT_EVALUATED,
  NOT_MOVE,
  NO_PATH,
  PROGRESS,
  NO_PROGRESS
}