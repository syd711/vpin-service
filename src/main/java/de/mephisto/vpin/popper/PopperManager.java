package de.mephisto.vpin.popper;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.highscores.HighscoreManager;
import de.mephisto.vpin.http.HttpServer;
import de.mephisto.vpin.util.SqliteConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PopperManager {
  private final static Logger LOG = LoggerFactory.getLogger(PopperManager.class);

  private final static String CURL_COMMAND_POPPER_START = "curl -X POST --data-urlencode \"system=\" http://localhost:" + HttpServer.PORT + "/service/popperLaunch";
  private final static String CURL_COMMAND_TABLE_START = "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + HttpServer.PORT + "/service/gameLaunch";
  private final static String CURL_COMMAND_TABLE_EXIT = "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + HttpServer.PORT + "/service/gameExit";

  private final SqliteConnector connector;
  private final HighscoreManager highscoreManager;

  private final List<TableStatusChangeListener> listeners = new ArrayList<>();
  private final List<PopperLaunchListener> launchListeners = new ArrayList<>();

  public PopperManager(SqliteConnector connector, HighscoreManager highscoreManager) {
    this.connector = connector;
    this.highscoreManager = highscoreManager;
    this.runConfigCheck();
  }

  public void notifyTableStatusChange(final GameInfo game, final boolean started) {
    new Thread(() -> {
      if (started) {
        this.executeTableLaunchCommands(game);
      }
      else {
        this.executeTableExitCommands(game);
      }

      TableStatusChangedEvent event = () -> game;
      for (TableStatusChangeListener listener : this.listeners) {
        if (started) {
          listener.tableLaunched(event);
        }
        else {
          listener.tableExited(event);
        }
      }
    }).start();
  }

  @SuppressWarnings("unused")
  public void addPopperLaunchListener(PopperLaunchListener listener) {
    this.launchListeners.add(listener);
  }

  public void addTableStatusChangeListener(TableStatusChangeListener listener) {
    this.listeners.add(listener);
  }

  @SuppressWarnings("unused")
  public void removeTableStatusChangeListener(TableStatusChangeListener listener) {
    this.listeners.remove(listener);
  }


  public void executeTableLaunchCommands(GameInfo game) {
    LOG.info("Executing table launch commands for '" + game + "'");
  }

  public void executeTableExitCommands(GameInfo game) {
    LOG.info("Executing table exit commands for '" + game + "'");
    highscoreManager.invalidateHighscore(game);
  }

  private void runConfigCheck() {
    Emulators[] values = Emulators.values();
    for (Emulators value : values) {
      String emulatorName = Emulators.getEmulatorName(value);
      String emulatorLaunchScript = this.connector.getEmulatorStartupScript(emulatorName);
      if (!emulatorLaunchScript.contains(CURL_COMMAND_TABLE_START)) {
        emulatorLaunchScript = emulatorLaunchScript + "\n" + CURL_COMMAND_TABLE_START + "\n";
        this.connector.updateScript(emulatorName, "LaunchScript", emulatorLaunchScript);
      }
      String emulatorExitScript = this.connector.getEmulatorExitScript(Emulators.getEmulatorName(value));
      if (!emulatorExitScript.contains(CURL_COMMAND_TABLE_EXIT)) {
        emulatorExitScript = emulatorExitScript + "\n" + CURL_COMMAND_TABLE_EXIT + "\n";
        this.connector.updateScript(emulatorName, "PostScript", emulatorExitScript);
      }

      String startupScript = this.connector.getStartupScript();
      if (!startupScript.contains(CURL_COMMAND_POPPER_START)) {
        startupScript = startupScript + "\n" + CURL_COMMAND_POPPER_START + "\n";
        this.connector.updateStartupScript(startupScript);
      }
    }
    LOG.info("Finished Popper configuration check.");
  }

  public String validateScreenConfiguration(PopperScreen screen) {
    PinUPControl fn = null;
    switch (screen) {
      case Other2: {
        fn = connector.getFunction(PinUPControl.FUNCTION_SHOW_OTHER);
        break;
      }
      case GameHelp: {
        fn = connector.getFunction(PinUPControl.FUNCTION_SHOW_HELP);
        break;
      }
      case GameInfo: {
        fn = connector.getFunction(PinUPControl.FUNCTION_SHOW_FLYER);
        break;
      }
      default: {

      }
    }

    if (fn != null) {
      if (!fn.isActive()) {
        return "The screen has not been activated.";
      }

      if (fn.getCtrlKey() == 0) {
        return "The screen is not bound to any key.";
      }
    }

    return null;
  }

  public void notifyPopperLaunch() {
    for (PopperLaunchListener launchListener : launchListeners) {
      launchListener.popperLaunched();
    }
  }
}
