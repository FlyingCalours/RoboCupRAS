package sample_team.module.complex.police.improvement;

/**
 * Why URFRoadDetector settled on the road it did.
 *
 * Replaces the six REASON_ String constants previously declared on
 * URFRoadDetector. NO_BLOCKED_ROAD_KNOWN and
 * SCORED_UNREACHABLE_FALLBACK carry the old REASON_NO_CANDIDATE and
 * REASON_SCORED_UNREACHABLE values.
 */
public enum URFRoadTargetReason {
  NO_POSITION,
  REFUGE_DOOR_SWEEP,
  ESCORT_STUCK_AGENT,
  NO_BLOCKED_ROAD_KNOWN,
  SCORED_REACHABLE,
  SCORED_UNREACHABLE_FALLBACK
}