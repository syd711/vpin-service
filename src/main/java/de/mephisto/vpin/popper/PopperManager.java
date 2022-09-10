package de.mephisto.vpin.popper;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.highscores.HighscoreChangeListener;
import de.mephisto.vpin.highscores.HighscoreChangedEvent;
import de.mephisto.vpin.util.SqliteConnector;

import java.util.ArrayList;
import java.util.List;

public class PopperManager {
  private final SqliteConnector connector;
  private final List<TableStatusChangeListener> listeners = new ArrayList<>();

  public PopperManager(SqliteConnector connector) {
    this.connector = connector;
  }

  @SuppressWarnings("unused")
  public void addTableStatusChangeListener(TableStatusChangeListener listener) {
    this.listeners.add(listener);
  }

  @SuppressWarnings("unused")
  public void removeTableStatusChangeListener(TableStatusChangeListener listener) {
    this.listeners.remove(listener);
  }

  public void notifyTableStatusChange(GameInfo game, boolean started) {
    TableStatusChangedEvent event = () -> game;
    for (TableStatusChangeListener listener : this.listeners) {
      if(started) {
        listener.tableLaunched(event);
      }
      else {
        listener.tableExited(event);
      }
    }
  }

  public String validateScreenConfiguration(PopperScreen screen) {
    PinUPFunction fn = null;
    switch (screen) {
      case Other2: {
        fn = connector.getFunction(PinUPFunction.FUNCTION_SHOW_OTHER);
        break;
      }
      case GameHelp: {
        fn = connector.getFunction(PinUPFunction.FUNCTION_SHOW_HELP);
        break;
      }
      case GameInfo: {
        fn = connector.getFunction(PinUPFunction.FUNCTION_SHOW_FLYER);
        break;
      }
      default: {

      }
    }

    if(fn != null) {
      if(!fn.isActive()) {
        return "The screen has not been activated.";
      }

      if(fn.getCtrlKey() == 0) {
        return "The screen is not bound to any key.";
      }
    }

    return null;
  }
}
