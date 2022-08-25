package de.mephistor.pinup;

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

  private SystemInfo systemInfo;

  private SqliteConnector sqliteConnector;

  private Map<String, GameInfo> gameInfoByRom;

  public GameRepository(SystemInfo systemInfo, SqliteConnector sqliteConnector) {
    this.systemInfo = systemInfo;
    this.sqliteConnector = sqliteConnector;
    this.loadTableInfos();
  }

  public List<GameInfo> getTables() {
    return new ArrayList<>(gameInfoByRom.values());
  }

  private void loadTableInfos() {
    gameInfoByRom = new HashMap<>();

    LOG.info("*********************** Executing ROM Checks **********************************************************");
    List<GameInfo> games = sqliteConnector.getGames();
    for (GameInfo game : games) {
      String romName = game.getRom();
      if(StringUtils.isAllEmpty(romName)) {
        LOG.info("No rom found in database, checking ROM name for " + game.getGameFileName());
        romName = extractRomName(game.getVpxFile());
        if (romName != null && romName.length() > 0) {
          sqliteConnector.updateRomName(game.getGameFileName(), romName);
        }

        if (romName == null) {
          LOG.error("Failed to determine ROM name of " + game.getGameDisplayName() + ", ignoring table.");
          continue;
        }
      }

      if(!game.getRomFile().exists()) {
        LOG.info("No rom file '" + game.getRomFile().getName() + "' found for " + game.getVpxFile().getAbsolutePath() + ", ignoring table.");
        continue;
      }


      LOG.info("Loaded ROM for game '" + game.getGameDisplayName() + "' [" + romName+ "]");
      gameInfoByRom.put(romName, game);
    }
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
