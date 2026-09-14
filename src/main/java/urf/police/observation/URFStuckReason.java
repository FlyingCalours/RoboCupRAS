package urf.police.observation;

/**
 * Justification for the current URFStuckStatus.
 *
 * OTHER_ACTION_TYPE replaces the previous behaviour of copying the raw
 * action type into the reason field. URFPoliceStuckDetector stores that
 * raw text separately so the log field is unchanged.
 */
public enum URFStuckReason {
  WAITING_FOR_FIRST_ACTION_OUTCOME,
  REPEATED_MOVE_NO_PROGRESS,
  REPEATED_MOVE_NO_PROGRESS_SAME_TARGET,
  CLEAR_ACTION_ACTIVE,
  REST_ACTION_ACTIVE,
  MOVE_MODULE_RETURNED_NULL,
  MOVE_ACTION_HAS_EMPTY_PATH,
  WAITING_FOR_NEXT_TIMESTEP_PROGRESS,
  NO_FINAL_ACTION_RECORDED,
  OTHER_ACTION_TYPE
}