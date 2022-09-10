package de.mephisto.vpin;

import de.mephisto.vpin.highscores.Highscore;
import de.mephisto.vpin.highscores.HighscoreChangeListener;
import de.mephisto.vpin.highscores.HighscoreManager;
import de.mephisto.vpin.http.GrizzlyHttpServer;
import de.mephisto.vpin.popper.PinUPFunction;
import de.mephisto.vpin.popper.PopperScreen;
import de.mephisto.vpin.roms.RomScanner;
import de.mephisto.vpin.util.SqliteConnector;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class VPinService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinService.class);

  private final SqliteConnector sqliteConnector;

  private final RomScanner romScanner;

  private final HighscoreManager highscoreManager;

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private static VPinService instance;

  private final GrizzlyHttpServer httpServer;

  public static VPinService create() {
    if(instance == null) {
      instance = new VPinService();
    }
    return instance;
  }

  private VPinService() {
    this.sqliteConnector = new SqliteConnector();
    this.romScanner = new RomScanner(this, sqliteConnector);
    this.highscoreManager = new HighscoreManager(this);
    this.httpServer = new GrizzlyHttpServer();
    LOG.info("VPinService created.");
  }

  @SuppressWarnings("unused")
  public void shutdown() {
    this.executor.shutdown();
    this.highscoreManager.destroy();
    this.httpServer.stop();
  }

  public String validateScreenConfiguration(PopperScreen screen) {
    PinUPFunction fn = null;
    switch (screen) {
      case Other2: {
        fn = sqliteConnector.getFunction(PinUPFunction.FUNCTION_SHOW_OTHER);
        break;
      }
      case GameHelp: {
        fn = sqliteConnector.getFunction(PinUPFunction.FUNCTION_SHOW_HELP);
        break;
      }
      case GameInfo: {
        fn = sqliteConnector.getFunction(PinUPFunction.FUNCTION_SHOW_FLYER);
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

  @SuppressWarnings("unused")
  public void addHighscoreChangeListener(HighscoreChangeListener listener) {
    this.highscoreManager.addHighscoreChangeListener(listener);
  }

  @SuppressWarnings("unused")
  public void removeHighscoreChangeListener(HighscoreChangeListener listener) {
    this.highscoreManager.removeHighscoreChangeListener(listener);
  }

  @SuppressWarnings("unused")
  public GameInfo getGameInfo(int id) {
    return sqliteConnector.getGame(this, id);
  }

  @SuppressWarnings("unused")
  public List<GameInfo> getActiveGameInfos() {
    List<Integer> gameIdsFromPlaylists = this.sqliteConnector.getGameIdsFromPlaylists();
    List<GameInfo> games = sqliteConnector.getGames(this);
    return games.stream().filter(g -> gameIdsFromPlaylists.contains(g.getId())).collect(Collectors.toList());
  }

  public List<GameInfo> getGameInfos() {
    return sqliteConnector.getGames(this);
  }

  public Future<List<GameInfo>> rescanAllTables() {
    return this.executor.submit(() -> this.romScanner.loadTableInfos(true));
  }

  public void rescanRom(GameInfo gameInfo) {
    this.romScanner.scanRom(gameInfo);
  }

  public GameInfo getGameByVpxFilename(String filename) {
    List<GameInfo> games = sqliteConnector.getGames(this);
    for (GameInfo gameInfo : games) {
      if (gameInfo.getGameFile().getName().equals(filename)) {
        return gameInfo;
      }
    }
    return null;
  }

  public List<GameInfo> getGamesWithEmptyRoms() {
    List<GameInfo> games = sqliteConnector.getGames(this);
    List<GameInfo> result = new ArrayList<>();
    for (GameInfo gameInfo : games) {
      if (StringUtils.isEmpty(gameInfo.getRom())) {
        result.add(gameInfo);
      }
    }
    return result;
  }

  public GameInfo getGameByRom(String romName) {
    List<GameInfo> games = sqliteConnector.getGames(this);
    for (GameInfo gameInfo : games) {
      if (gameInfo.getRom() != null && gameInfo.getRom().equals(romName)) {
        return gameInfo;
      }
    }
    return null;
  }

  public Highscore getHighscore(GameInfo gameInfo) {
    return highscoreManager.getHighscore(gameInfo);
  }
}
