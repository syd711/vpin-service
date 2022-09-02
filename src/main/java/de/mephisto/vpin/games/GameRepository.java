package de.mephisto.vpin.games;

import de.mephisto.vpin.highscores.HighsoreResolver;
import de.mephisto.vpin.util.SqliteConnector;
import de.mephisto.vpin.util.SystemInfo;
import de.mephisto.vpin.util.PropertiesStore;
import de.mephisto.vpin.util.RomScanner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GameRepository {
  private final static Logger LOG = LoggerFactory.getLogger(GameRepository.class);

  private final SqliteConnector sqliteConnector;

  private final RomScanner romScanner;
  private final HighsoreResolver highscoreResolver;

  private final List<GameInfo> games = new ArrayList<>();

  private final PropertiesStore store;

  public static GameRepository create() {
    return new GameRepository();
  }

  private GameRepository() {
    this.sqliteConnector = new SqliteConnector();
    this.romScanner = new RomScanner();
    this.highscoreResolver = new HighsoreResolver();
    this.store = PropertiesStore.create(new File("./resources"));
  }

  public List<GameInfo> getGameInfos() {
    if (games.isEmpty()) {
      this.reload();
    }
    return new ArrayList<>(games);
  }

  public void invalidateAll() {
    this.games.clear();
    this.loadTableInfos(true);
  }

  public void invalidate(GameInfo gameInfo) {
    String romName = romScanner.scanRomName(gameInfo.getVpxFile());
    gameInfo.setRom(romName);
    if(!StringUtils.isEmpty(romName)) {
      updateGameInfo(gameInfo);
    }
  }

  public void reload() {
    this.games.clear();
    this.loadTableInfos(false);
  }

  public GameInfo getGameByVpxFilename(String filename) {
    List<GameInfo> games = sqliteConnector.getGames();
    for (GameInfo gameInfo : games) {
      if(gameInfo.getVpxFile().getName().equals(filename)) {
        highscoreResolver.loadHighscore(gameInfo);
        return gameInfo;
      }
    }
    return null;
  }

  public List<GameInfo> getGamesWithEmptyRoms() {
    List<GameInfo> games = sqliteConnector.getGames();
    List<GameInfo> result = new ArrayList<>();
    for (GameInfo gameInfo : games) {
      if(StringUtils.isEmpty(gameInfo.getRom())) {
        result.add(gameInfo);
      }
    }
    return result;
  }

  public GameInfo getGameByRom(String romName) {
    List<GameInfo> games = sqliteConnector.getGames();
    for (GameInfo gameInfo : games) {
      if(gameInfo.getRom() != null && gameInfo.getRom().equals(romName)) {
        highscoreResolver.loadHighscore(gameInfo);
        return gameInfo;
      }
    }
    return null;
  }

  public void reset() {
    this.sqliteConnector.resetAll();
  }

  private void loadTableInfos(boolean forceRomScan) {
    List<GameInfo> games = sqliteConnector.getGames();
    for (GameInfo game : games) {
      if (!wasScanned(game) || forceRomScan) {
        String romName = romScanner.scanRomName(game.getVpxFile());
        game.setRom(romName);
        updateGameInfo(game);
      }

      if(!StringUtils.isEmpty(game.getRom())) {
        highscoreResolver.loadHighscore(game);
      }

      this.games.add(game);
    }
  }

  private void updateGameInfo(GameInfo game) {
    String romName = game.getRom();
    if (romName != null && romName.length() > 0) {
      game.setRom(romName);
      sqliteConnector.updateRomName(game.getGameFileName(), romName);
      LOG.info("Update of " + game.getVpxFile().getName() + " successful, written ROM name '" + romName + "'");

      File romFile = new File(SystemInfo.getInstance().getMameRomFolder(), romName + ".zip");
      if (romFile.exists()) {
        game.setRomFile(romFile);
      }
    }
    else {
      LOG.info("Skipped Update of " + game.getVpxFile().getName() + ", no rom name found.");
    }
    this.store.set(formatGameKey(game) + ".rom", romName != null ? romName : "");
  }





  private boolean wasScanned(GameInfo game) {
    return store.containsKey(formatGameKey(game) + ".rom");
  }

  private String formatGameKey(GameInfo game) {
    return "gameId." + game.getId();
  }

}
