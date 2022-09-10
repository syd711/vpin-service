package de.mephisto.vpin.roms;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.VPinService;
import de.mephisto.vpin.util.PropertiesStore;
import de.mephisto.vpin.util.ReverseLineInputStream;
import de.mephisto.vpin.util.SqliteConnector;
import de.mephisto.vpin.util.SystemInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RomScanner {
  private final static Logger LOG = LoggerFactory.getLogger(RomScanner.class);
  public static final int MAX_ROM_FILENAME_LENGTH = 16;

  private final VPinService service;
  private final SqliteConnector connector;
  private final PropertiesStore store;
  private final List<RomScanListener> listeners = new ArrayList<>();

  public RomScanner(VPinService service, SqliteConnector connector) {
    this.service = service;
    this.connector = connector;
    this.store = PropertiesStore.create("repository.properties");
  }

  public List<GameInfo> loadTableInfos(boolean forceRomScan) {
    List<GameInfo> games = connector.getGames(service);
    for (GameInfo game : games) {
      if (!wasScanned(game) || forceRomScan) {
        String romName = scanRomName(game.getGameFile());
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


  private String formatGameKey(GameInfo game) {
    return "gameId." + game.getId();
  }

  private boolean wasScanned(GameInfo game) {
    return store.containsKey(formatGameKey(game) + ".rom");
  }

  /**
   * Checks the different lines that are in the vpx file.
   * Usually the variable not does not differ that much.
   * We read the file from the end to save time.
   *
   * @param gameFile the table file which contains the rom that isued.
   * @return the ROM name or null
   */
  public String scanRomName(File gameFile) {
    String rom = scanRomName(gameFile, "cGameName");
    if (rom == null) {
      rom = scanRomName(gameFile, "cgamename");
    }
    if (rom == null) {
      rom = scanRomName(gameFile, "RomSet1");
    }
    if (rom == null) {
      rom = scanRomName(gameFile, "GameName");
    }
    return rom;
  }

  public void scanRom(GameInfo gameInfo) {
    String romName = scanRomName(gameInfo.getGameFile());
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

  public void notifyGameScanned(GameInfo game) {
    RomScannedEvent event = new RomScannedEventImpl(game);
    for (RomScanListener listener : this.listeners) {
      listener.romScanned(event);
    }
  }

  private String scanRomName(File vpxFile, String pattern) {
    String romName = null;
    BufferedReader bufferedReader = null;
    ReverseLineInputStream reverseLineInputStream = null;
    try {
      reverseLineInputStream = new ReverseLineInputStream(vpxFile);
      bufferedReader = new BufferedReader(new InputStreamReader(reverseLineInputStream));

      String line;
      Pattern p = Pattern.compile(".*" + pattern + ".*=.*\".*\".*");
      bufferedReader.readLine();//skip last line if empty
      int count = 0;
      while ((line = bufferedReader.readLine()) != null || count < 1000) {
        count++;
        if (line != null && line.contains(pattern)) {
          if (p.matcher(line).matches()) {
            if (line.indexOf("'") != 0) {
              int start = line.indexOf("\"") + 1;
              romName = line.substring(start);
              int end = romName.indexOf("\"");

              if (end - start < MAX_ROM_FILENAME_LENGTH) {
                romName = romName.substring(0, end).trim();
                break;
              }
            }
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to read rom line '" + romName + "' for  " + vpxFile.getAbsolutePath() + ": " + e.getMessage(), e);
    } finally {
      try {
        reverseLineInputStream.close();
        bufferedReader.close();
      } catch (Exception e) {
        LOG.error("Failed to close vpx file stream: " + e.getMessage(), e);
      }
    }
    return romName;
  }
}
