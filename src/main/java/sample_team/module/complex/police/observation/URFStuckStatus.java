package sample_team.module.complex.police.observation;

/**
 * Detector state published for one agent at one timestep.
 *
 * POSSIBLE_STUCK and CONFIRMED_STUCK are evidence-driven. The remaining
 * constants classify an agent that is behaving normally.
 */
public enum URFStuckStatus {
  INITIALIZING,
  POSSIBLE_STUCK,
  CONFIRMED_STUCK,
  CLEARING,
  RESTING,
  NO_MOVE_RESULT,
  MOVE_PENDING,
  MOVE_WITHOUT_PATH,
  NO_ACTION,
  OTHER_ACTION
}