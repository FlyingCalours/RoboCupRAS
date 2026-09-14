package urf.police.observation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import rescuecore2.worldmodel.EntityID;

/**
 * Competition-grade structured exporter for the
 * Police observation baseline.
 *
 * Observation only.
 *
 * This class never changes:
 * - target selection
 * - path planning
 * - MOVE
 * - CLEAR
 * - REST
 * - Police tactics
 */
public final class URFPoliceCsvExporter {
  private static final String SCHEMA_VERSION = "URF_POLICE_OBS_V2";
  private static final String RUN_ID = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

  /*
   * Protect against accidental duplicate export of
   * the same agent/timestep.
   */
  private static final Map<Integer, Integer> LAST_EXPORTED_TIME = new ConcurrentHashMap<>();

  private static final String HEADER = "schemaVersion," + "runId," + "time," + "agent," + "position," + "x," + "y," + "target,"
                                       + "action," + "actionSource," + "pathLength," + "stepDistance," + "totalDisplacement," 
                                       + "moveUsePosition," + "moveX," + "moveY," + "clearTarget," + "clearUseTarget," + "clearX,"
                                       + "clearY," + "clearRequestTarget," + "moveRequestTarget," + "moveCount," + "clearCount,"
                                       + "restCount," + "otherActionCount," + "targetChangeCount," + "samePositionStreak,"
                                       + "maxSamePositionStreak," + "possibleStuck," + "stuckStatus," + "failedMoveStreak," 
                                       + "confirmedStuckEvents," + "clearModuleCalls," + "clearNullResults," + "clearActionResults,"
                                       + "clearMoveResults," + "clearRestResults," + "moveModuleCalls," + "moveNullResults,"
                                       + "moveActionResults," + "moveRestResults," + "fallbackRestCount," + "clearCalcUs,"
                                       + "moveCalcUs" + System.lineSeparator();

  private URFPoliceCsvExporter() {}

  public static synchronized void export(URFPoliceMetrics metrics, URFPoliceStuckDetector stuckDetector) {
    if (metrics == null || stuckDetector == null) {
      return;
    }

    int time = metrics.getTime();
    int agent = metrics.getAgentId();

    if (time < 0) {
      return;
    }

    /*
     * Export exactly one final record for each
     * Police agent at each timestep.
     */
    Integer previousTime = LAST_EXPORTED_TIME.get(agent);

    if (previousTime != null && previousTime == time) {
      return;
    }

    try {
      Path directory = Path.of(System.getProperty("user.dir"), "logs", "urf", "police");
      Files.createDirectories(directory);
      Path file = directory.resolve("police-observation-v2-" + RUN_ID + "-agent-" + agent + ".csv");
      if (Files.notExists(file)) {
        Files.writeString(file, HEADER, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
      }

      String row = csv(SCHEMA_VERSION) + "," + csv(RUN_ID) + "," + time + "," + agent + "," + idValue(metrics.getCurrentPosition()) 
                   + "," + metrics.getCurrentX() + "," + metrics.getCurrentY() + "," + idValue(metrics.getSelectedTarget()) + ","
                   + csv(metrics.getLastActionLabel()) + "," + csv(metrics.getLastActionSource().name()) + "," + metrics.getLastPathLength()
                   + "," + metrics.getLastStepDistance() + "," + metrics.getTotalObservedDisplacement() + "," 
                   + metrics.isLastMoveUsingPosition() + "," + metrics.getLastMoveX() + "," + metrics.getLastMoveY() + ","
                   + idValue(metrics.getLastClearTarget()) + "," + metrics.isLastClearUsingBlockadeTarget() + "," 
                   + metrics.getLastClearX() + "," + metrics.getLastClearY() + "," + idValue(metrics.getLastClearRequestTarget())
                   + "," + idValue(metrics.getLastMoveRequestTarget())
                   + "," + metrics.getMoveCount() + "," + metrics.getClearCount() + "," + metrics.getRestCount() + "," 
                   + metrics.getOtherActionCount() + "," + metrics.getTargetChangeCount() + "," + metrics.getSamePositionStreak()
                   + "," + metrics.getMaxSamePositionStreak()+ "," + metrics.isPossibleStuck() + "," + csv(stuckDetector.getStatus().name())
                   + "," + stuckDetector.getConsecutiveFailedMoves() + "," + stuckDetector.getConfirmedStuckEvents() + "," 
                   + metrics.getClearModuleCallCount() + "," + metrics.getClearModuleNullResultCount() + ","
                   + metrics.getClearModuleActionClearCount() + "," + metrics.getClearModuleActionMoveCount() + ","
                   + metrics.getClearModuleActionRestCount() + "," + metrics.getMoveModuleCallCount() + "," 
                   + metrics.getMoveModuleNullResultCount() + "," + metrics.getMoveModuleActionMoveCount() + "," 
                   + metrics.getMoveModuleActionRestCount() + "," + metrics.getFallbackRestCount() + ","
                   + nanosToMicros(metrics.getLastClearCalcNanos()) + "," + nanosToMicros(metrics.getLastMoveCalcNanos())
                   + System.lineSeparator();
      Files.writeString(file, row, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
      LAST_EXPORTED_TIME.put(agent,time);
    } catch (IOException e) {
      System.err.println("URF_CSV_EXPORT_ERROR: " + e.getMessage());
    }
  }

  private static String idValue(EntityID id) {
    if (id == null) {
      return "";
    }
    return Integer.toString(id.getValue());
  }

  private static String csv(String value) {
    if (value == null) {
      return "";
    }
    return "\"" + value.replace("\"", "\"\"") + "\"";
  }

  private static double nanosToMicros(long nanos) {
    return nanos / 1000.0;
  }
}