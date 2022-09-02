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

  private List<GameInfo> games = new ArrayList<>();

  private PropertiesStore store;

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

  public void reload() {
    this.games.clear();
    this.loadTableInfos();
  }

  public void reset() {
    this.sqliteConnector.resetAll();
  }

  private void loadTableInfos() {
    List<GameInfo> games = sqliteConnector.getGames();
    for (GameInfo game : games) {
      if (!wasScanned(game)) {
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

  private String updateGameInfo(GameInfo game) {
    String romName = game.getRom();
    if (romName != null && romName.length() > 0) {
      game.setRom(romName);

      File romFile = new File(SystemInfo.getInstance().getMameRomFolder(), romName + ".zip");
      if (romFile.exists()) {
        game.setRomFile(romFile);
        sqliteConnector.updateRomName(game.getGameFileName(), romName);
        LOG.info("Update of " + game.getVpxFile().getName() + " successful, written ROM name '" + romName + "'");
      }
      else {
        LOG.info("Skipped Update of " + game.getVpxFile().getName() + ", rom file (" + romFile.getAbsolutePath() + ") found.");
      }
    }
    else {
      LOG.info("Skipped Update of " + game.getVpxFile().getName() + ", no rom name found.");
    }
    this.store.set(formatGameKey(game), romName != null ? romName : "");
    return romName;
  }





  private boolean wasScanned(GameInfo game) {
    return store.containsKey(formatGameKey(game));
  }

  private String formatGameKey(GameInfo game) {
    return "gameId." + game.getId();
  }

}
