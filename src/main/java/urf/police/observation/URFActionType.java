package urf.police.observation;

/**
 * Final action recorded for one Police decision cycle.
 *
 * Every constant name is the exact string this value previously had, so
 * name() reproduces the old "action=" log field and the old CSV action
 * column without a label table.
 *
 * OTHER covers any Action subclass the observation layer does not model.
 * URFPoliceMetrics keeps the class simple name beside it so that detail
 * is not lost.
 */
public enum URFActionType {
  NONE,
  MOVE,
  CLEAR,
  REST,
  OTHER
}