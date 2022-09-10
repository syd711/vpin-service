package de.mephisto.vpin;

import de.mephisto.vpin.highscores.HighscoreChangedEvent;
import de.mephisto.vpin.highscores.Highscore;
import de.mephisto.vpin.highscores.HighscoreManager;
import de.mephisto.vpin.http.GrizzlyHttpServer;
import de.mephisto.vpin.popper.PinUPFunction;
import de.mephisto.vpin.popper.PopperScreen;
import de.mephisto.vpin.roms.RomScanner;
import de.mephisto.vpin.util.PropertiesStore;
import de.mephisto.vpin.util.SqliteConnector;
import de.mephisto.vpin.util.SystemInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class VPinService implements ServiceListener {
  private final static Logger LOG = LoggerFactory.getLogger(VPinService.class);

  private final SqliteConnector sqliteConnector;

  private final RomScanner romScanner;

  private final PropertiesStore store;

  private final HighscoreManager highscoreManager;

  private final List<ServiceListener> listeners = new ArrayList<>();

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
    this.romScanner = new RomScanner();
    this.highscoreManager = new HighscoreManager(this);
    this.store = PropertiesStore.create("repository.properties");
    this.httpServer = new GrizzlyHttpServer();
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

  @Override
  public void gameScanned(GameInfo info) {
    //no required
  }

  @Override
  public void highscoreChanged(HighscoreChangedEvent event) {
    GameInfo gameInfo = event.getGameInfo();
    this.highscoreManager.invalidateHighscore(gameInfo);
  }

  public void shutdown() {
    this.executor.shutdown();
    this.highscoreManager.destroy();
  }

  public void addListener(ServiceListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(ServiceListener listener) {
    this.listeners.remove(listener);
  }

  public void notifyGameScanned(GameInfo game) {
    for (ServiceListener listener : this.listeners) {
      listener.gameScanned(game);
    }
  }

  public void notifyHighscoreChange(HighscoreChangedEvent event) {
    for (ServiceListener listener : this.listeners) {
      listener.highscoreChanged(event);
    }
  }

  public GameInfo getGameInfo(int id) {
    return sqliteConnector.getGame(this, id);
  }

  public List<GameInfo> getActiveGameInfos() {
    List<Integer> gameIdsFromPlaylists = this.sqliteConnector.getGameIdsFromPlaylists();
    List<GameInfo> games = sqliteConnector.getGames(this);
    return games.stream().filter(g -> gameIdsFromPlaylists.contains(g.getId())).collect(Collectors.toList());
  }

  public List<GameInfo> getGameInfos() {
    return sqliteConnector.getGames(this);
  }

  public Future<List<GameInfo>> rescanAllTables() {
    return this.executor.submit(() -> loadTableInfos(true));
  }

  public void rescanRom(GameInfo gameInfo) {
    String romName = romScanner.scanRomName(gameInfo.getGameFile());
    gameInfo.setRom(romName);
    if (!StringUtils.isEmpty(romName)) {
      writeGameInfo(gameInfo);
      LOG.info("Finished re-scan of table " + gameInfo + ", found ROM '" + romName + "'.");
    }
    else {
      LOG.info("Finished re-scan of table " + gameInfo + ", no ROM found.");
    }
    notifyGameScanned(gameInfo);
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

  private List<GameInfo> loadTableInfos(boolean forceRomScan) {
    List<GameInfo> games = sqliteConnector.getGames(this);
    for (GameInfo game : games) {
      if (!wasScanned(game) || forceRomScan) {
        String romName = romScanner.scanRomName(game.getGameFile());
        game.setRom(romName);
        writeGameInfo(game);
        notifyGameScanned(game);
      }
    }
    return games;
  }

  private void writeGameInfo(GameInfo game) {
    String romName = game.getRom();
    if (romName != null && romName.length() > 0) {
      game.setRom(romName);
      LOG.info("Update of " + game.getGameFile().getName() + " successful, written ROM name '" + romName + "'");

      File romFile = new File(SystemInfo.getInstance().getMameRomFolder(), romName + ".zip");
      if (romFile.exists()) {
        game.setRomFile(romFile);
      }
    }
    else {
      LOG.info("Skipped Update of " + game.getGameFile().getName() + ", no rom name found.");
    }
    this.store.set(formatGameKey(game) + ".rom", romName != null ? romName : "");
    this.store.set(formatGameKey(game) + ".displayName", game.getGameDisplayName());
  }

  private boolean wasScanned(GameInfo game) {
    return store.containsKey(formatGameKey(game) + ".rom");
  }

  private String formatGameKey(GameInfo game) {
    return "gameId." + game.getId();
  }
}
