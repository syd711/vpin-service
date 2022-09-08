package de.mephisto.vpin.games;

import de.mephisto.vpin.highscores.Highscore;
import de.mephisto.vpin.highscores.HighscoreFilesWatcher;
import de.mephisto.vpin.highscores.HighscoreResolver;
import de.mephisto.vpin.roms.RomScanner;
import de.mephisto.vpin.util.PropertiesStore;
import de.mephisto.vpin.util.SqliteConnector;
import de.mephisto.vpin.util.SystemInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class GameRepository {
  private final static Logger LOG = LoggerFactory.getLogger(GameRepository.class);

  private final SqliteConnector sqliteConnector;

  private final RomScanner romScanner;
  private final HighscoreResolver highscoreResolver;

  private final PropertiesStore store;

  private final HighscoreFilesWatcher highscoreWatcher;

  private final List<RepositoryListener> listeners = new ArrayList<>();

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  public static GameRepository create() {
    return new GameRepository();
  }

  private GameRepository() {
    this.sqliteConnector = new SqliteConnector();
    this.romScanner = new RomScanner();
    this.highscoreResolver = new HighscoreResolver();
    this.highscoreResolver.refresh();
    this.store = PropertiesStore.create("repository.properties");

    SystemInfo info = SystemInfo.getInstance();
    List<File> watching = Arrays.asList(info.getNvramFolder(), info.getVPRegFile().getParentFile());
    this.highscoreWatcher = new HighscoreFilesWatcher(this, watching);
    this.highscoreWatcher.start();
  }

  public void shutdown() {
    this.executor.shutdown();
    this.highscoreWatcher.setRunning(false);
  }

  public void addListener(RepositoryListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(RepositoryListener listener) {
    this.listeners.remove(listener);
  }

  public void notifyGameScanned(GameInfo game) {
    for (RepositoryListener listener : this.listeners) {
      listener.gameScanned(game);
    }
  }

  public void notifyHighscoreChange(HighscoreChangedEvent event) {
    for (RepositoryListener listener : this.listeners) {
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

  public Future<List<GameInfo>> invalidateAll() {
    return this.executor.submit(() -> loadTableInfos(true));
  }

  void invalidate(GameInfo gameInfo) {
    String romName = romScanner.scanRomName(gameInfo.getGameFile());
    gameInfo.setRom(romName);
    if (!StringUtils.isEmpty(romName)) {
      writeGameInfo(gameInfo);
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

  public void refreshHighscores() {
    this.highscoreResolver.refresh();
  }

  Highscore loadHighscore(GameInfo gameInfo) {
    return this.highscoreResolver.loadHighscore(gameInfo);
  }

  boolean isHighscoreSupported(String rom) {
    return this.highscoreResolver.isRomSupported(rom);
  }

  private boolean wasScanned(GameInfo game) {
    return store.containsKey(formatGameKey(game) + ".rom");
  }

  private String formatGameKey(GameInfo game) {
    return "gameId." + game.getId();
  }
}
