package sample_team.module.complex.police.improvement;

/**
 * Branch taken by URFPoliceExtActionClear during one cycle.
 *
 * These values were previously bare string literals assigned to a local
 * variable, so they were never declared as constants anywhere.
 *
 * The last two constants describe a null result, which hands control to
 * the configured MOVE module.
 */
public enum URFClearDecision {
  CLEAR_NEAREST_BLOCKADE,
  RECOVERY_DIRECTIONAL_CLEAR,
  BLOCKADE_OUTSIDE_CLEAR_RANGE_HANDOFF,
  ROAD_CLEAR_OR_NO_LOCAL_BLOCKADE,
  NO_LOCAL_CLEAR
}