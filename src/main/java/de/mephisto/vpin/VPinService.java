package de.mephisto.vpin;

import de.mephisto.vpin.dof.DOFCommand;
import de.mephisto.vpin.dof.DOFCommandData;
import de.mephisto.vpin.dof.DOFManager;
import de.mephisto.vpin.dof.Unit;
import de.mephisto.vpin.highscores.Highscore;
import de.mephisto.vpin.highscores.HighscoreManager;
import de.mephisto.vpin.http.HttpServer;
import de.mephisto.vpin.popper.PopperManager;
import de.mephisto.vpin.popper.PopperScreen;
import de.mephisto.vpin.popper.TableStatusChangeListener;
import de.mephisto.vpin.roms.RomManager;
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
import java.util.stream.Collectors;

public class VPinService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinService.class);

  private final SqliteConnector sqliteConnector;

  private final RomManager romManager;

  private final HighscoreManager highscoreManager;

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private static VPinService instance;

  private HttpServer httpServer;

  private final DOFManager dofManager;

  private final PopperManager popperManager;

  private final DOFCommandData dofCommandData;

  private final List<GameInfo> gameInfos = new ArrayList<>();

  public static VPinService create(boolean headless) {
    if (instance == null) {
      instance = new VPinService(headless);
    }
    return instance;
  }

  private VPinService(boolean headless) {
    this.romManager = new RomManager();
    this.sqliteConnector = new SqliteConnector(romManager);
    this.highscoreManager = new HighscoreManager(this);
    this.popperManager = new PopperManager(sqliteConnector, highscoreManager);

    dofCommandData = DOFCommandData.create();
    this.dofManager = new DOFManager(dofCommandData);

    if(headless) {
      if(!SystemInfo.isAvailable(HttpServer.PORT)) {
        LOG.warn("VPinService already running, exiting.");
        System.exit(0);
      }

      this.httpServer = new HttpServer(popperManager);
      this.dofManager.startRuleEngine();
    }

    if(headless) {
      LOG.info("VPinService created [headless-mode]");
    }
    else {
      LOG.info("VPinService created [config-mode]");
    }
  }

  @SuppressWarnings("unused")
  public void shutdown() {
    this.executor.shutdown();
    this.httpServer.stop();
  }

  @SuppressWarnings("unused")
  public String validateScreenConfiguration(PopperScreen screen) {
    return popperManager.validateScreenConfiguration(screen);
  }

  @SuppressWarnings("unused")
  public void addTableStatusChangeListener(TableStatusChangeListener listener) {
    this.popperManager.addTableStatusChangeListener(listener);
  }

  @SuppressWarnings("unused")
  public void removeTableStatusChangeListener(TableStatusChangeListener listener) {
    this.popperManager.removeTableStatusChangeListener(listener);
  }

  @SuppressWarnings("unused")
  public GameInfo getGameInfo(int id) {
    return sqliteConnector.getGame(this, id);
  }

  @SuppressWarnings("unused")
  public void updateDOFCommand(DOFCommand command) {
    this.dofCommandData.updateDOFCommand(command);
  }

  @SuppressWarnings("unused")
  public void addDOFCommand(DOFCommand command) {
    this.dofCommandData.addDOFCommand(command);
  }

  @SuppressWarnings("unused")
  public void removeDOFCommand(DOFCommand command) {
    this.dofCommandData.removeDOFCommand(command);
  }

  @SuppressWarnings("unused")
  public List<DOFCommand> getDOFCommands() {
    return dofCommandData.getCommands();
  }

  @SuppressWarnings("unused")
  public List<Unit> getUnits() {
    return dofManager.getUnits();
  }

  @SuppressWarnings("unused")
  public Unit getUnit(int id) {
    return dofManager.getUnit(id);
  }

  @SuppressWarnings("unused")
  public List<GameInfo> getActiveGameInfos() {
    List<Integer> gameIdsFromPlaylists = this.sqliteConnector.getGameIdsFromPlaylists();
    List<GameInfo> games = sqliteConnector.getGames(this);
    return games.stream().filter(g -> gameIdsFromPlaylists.contains(g.getId())).collect(Collectors.toList());
  }

  public List<GameInfo> getGameInfos() {
    if(this.gameInfos.isEmpty()) {
      this.gameInfos.addAll(sqliteConnector.getGames(this));
      LOG.info("Loading of all GameInfo finished, loaded " + this.gameInfos.size() + " games.");
    }
    return this.gameInfos;
  }

  @SuppressWarnings("unused")
  public void refreshGameInfos() {
    this.gameInfos.clear();
    LOG.info("Resetted game info list.");
  }

  @SuppressWarnings("unused")
  public String rescanRom(GameInfo gameInfo) {
    return this.romManager.scanRom(gameInfo);
  }

  @SuppressWarnings("unused")
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

  @SuppressWarnings("unused")
  public GameInfo getGameByName(String table) {
    return this.sqliteConnector.getGameByName(this, table);
  }

  public GameInfo getGameByFile(File file) {
    return this.sqliteConnector.getGameByFilename(this, file.getName());
  }
}
