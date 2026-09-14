package sample_team.module.complex.police.observation;

/**
 * Which decision stage produced the final action.
 *
 * UNSPECIFIED marks the backward-compatible recordAction entry point.
 * UNKNOWN marks a null source, which should never occur in normal runs.
 */
public enum URFActionSource {
  NONE,
  UNSPECIFIED,
  UNKNOWN,
  CLEAR_MODULE,
  MOVE_MODULE,
  TACTICS_FALLBACK_REST
}