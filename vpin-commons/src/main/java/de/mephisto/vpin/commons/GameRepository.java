package de.mephisto.vpin.commons;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameRepository {
  private final static Logger LOG = LoggerFactory.getLogger(GameRepository.class);

  private SqliteConnector sqliteConnector;

  private Map<String, GameInfo> gameInfoByRom = new HashMap<>();
  private List<GameInfo> errornousTables = new ArrayList<>();

  public static GameRepository create() {
    return new GameRepository(new SqliteConnector());
  }

  private GameRepository(SqliteConnector sqliteConnector) {
    this.sqliteConnector = sqliteConnector;
    this.loadTableInfos();
  }

  public List<GameInfo> getGameInfos() {
    if (gameInfoByRom.isEmpty()) {
      this.reload();
    }

    return new ArrayList<>(gameInfoByRom.values());
  }

  public List<GameInfo> getErrorneousTables() {
    return this.errornousTables;
  }

  public void reload() {
    this.errornousTables.clear();
    this.gameInfoByRom.clear();
    this.loadTableInfos();
  }

  public void reset() {
    this.sqliteConnector.resetAll();
  }

  private void loadTableInfos() {
    LOG.info("*********************** Executing ROM Checks **********************************************************");
    List<GameInfo> games = sqliteConnector.getGames();
    int count = games.size();
    for (GameInfo game : games) {
      String romName = game.getRom();
      if (StringUtils.isEmpty(romName) && !game.isScanned()) {
        sqliteConnector.markAsScanned(game);
        if (updateRomName(game)) {
          LOG.info("Loaded ROM for game '" + game.getGameDisplayName() + "' [" + game.getRom() + "]");
        }
        else {
          continue;
        }
      }

      if(!StringUtils.isEmpty(game.getRom())) {
        gameInfoByRom.put(game.getRom(), game);
      }
    }
  }

  private boolean updateRomName(GameInfo game) {
    String romName = game.getRom();
    if (StringUtils.isAllEmpty(romName)) {
      LOG.info("Searching ROM for " + game.getGameFileName() + "...");
      romName = extractRomName(game.getVpxFile());
      if (romName != null && romName.length() > 0) {
        game.setRom(romName);

        File romFile = new File(SystemInfo.getInstance().getMameRomFolder(), romName + ".zip");
        if (romFile.exists()) {
          game.setRomFile(romFile);
          sqliteConnector.updateRomName(game.getGameFileName(), romName);
        }
        else {
          return false;
        }
      }
      else {
        LOG.error("Failed to determine ROM name of " + game.getGameDisplayName() + ", ignoring table.");
        game.setGameStatus("Failed to determine ROM name");
        this.errornousTables.add(game);
        return false;
      }
    }
    else if (!game.getRomFile().exists()) {
      LOG.info("No rom file '" + game.getRomFile().getName() + "' found for " + game.getVpxFile().getAbsolutePath() + ", ignoring table.");
      game.setGameStatus("No rom file '" + game.getRomFile().getName() + "' found");
      this.errornousTables.add(game);
      return false;
    }

    return true;
  }

  private String extractRomName(File vpxTable) {
    String romName = null;
    try {
      BufferedReader br = new BufferedReader(new FileReader(vpxTable));
      String line;
      while ((line = br.readLine()) != null) {
        if (line.contains("cGameName") && line.contains("cGameName = \"") && line.length() < 160) {
          romName = line.substring(line.indexOf("\"") + 1);
          romName = romName.substring(0, romName.indexOf("\""));
          break;
        }
      }
      br.close();
    } catch (Exception e) {
      LOG.error("Failed to read rom line '" + romName + "' for  " + vpxTable.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return romName;
  }
}
