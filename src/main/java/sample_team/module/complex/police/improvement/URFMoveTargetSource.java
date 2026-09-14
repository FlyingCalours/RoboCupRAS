package sample_team.module.complex.police.improvement;

/**
 * Which target drove the MOVE module during one cycle.
 *
 * Replaces the four SOURCE_ String constants previously declared on
 * URFPoliceExtActionMove. The constant names match the old string
 * values, so existing log output is unchanged.
 *
 * NO_TARGET carries the old SOURCE_NONE value.
 */
public enum URFMoveTargetSource {
  ROAD_TARGET,
  SEARCH_TARGET,
  PUSH_THROUGH,
  NO_TARGET
}