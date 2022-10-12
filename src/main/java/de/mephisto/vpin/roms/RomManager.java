package de.mephisto.vpin.roms;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.util.PropertiesStore;
import de.mephisto.vpin.util.SystemInfo;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RomManager {
  private final static Logger LOG = LoggerFactory.getLogger(RomManager.class);

  private final PropertiesStore store;

  private Map<String, String> aliasMapping = new HashMap<>();

  public RomManager() {
    this.store = PropertiesStore.create("repository.properties");
    loadAliasMapping();
  }

  private void loadAliasMapping() {
    File vpmAliasFile = SystemInfo.getInstance().getVPMAliasFile();
    try {
      if (vpmAliasFile.exists()) {
        FileInputStream fileInputStream = new FileInputStream(vpmAliasFile);
        List<String> mappings = IOUtils.readLines(fileInputStream, "utf8");
        fileInputStream.close();

        for (String mapping : mappings) {
          if (mapping.contains(",")) {
            String[] split = mapping.split(",");
            String[] aliases = Arrays.copyOfRange(split, 0, split.length - 1);
            String rom = split[split.length - 1];

            for (String alias : aliases) {
              aliasMapping.put(alias, rom);
            }
          }
        }
      }
    } catch (IOException e) {
      LOG.error("Error loading " + vpmAliasFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
  }

  @Nullable
  public String scanRom(GameInfo gameInfo) {
    scanRomName(gameInfo);
    String romName = gameInfo.getRom();
    writeGameInfo(gameInfo);
    if (!StringUtils.isEmpty(romName)) {
      LOG.info("Finished scan of table " + gameInfo + ", found ROM '" + romName + "'.");
      return romName;
    }
    LOG.info("Finished scan of table " + gameInfo + ", no ROM found.");
    return null;
  }

  private void writeGameInfo(GameInfo game) {
    String romName = game.getRom();
    if (romName != null && romName.length() > 0) {
      LOG.info("Update of " + game.getGameFile().getName() + " successful, written ROM name '" + romName + "'");
      File romFile = new File(SystemInfo.getInstance().getMameRomFolder(), romName + ".zip");
      if (romFile.exists()) {
        game.setRomFile(romFile);
      }
    }
    else {
      LOG.info("Skipped Update of " + game.getGameFile().getName() + ", no rom name found.");
    }
    this.store.set(formatGameKey(game.getId()) + ".rom", romName != null ? romName : "");
    this.store.set(formatGameKey(game.getId()) + ".nvOffset", game.getNvOffset());
    this.store.set(formatGameKey(game.getId()) + ".displayName", game.getGameDisplayName());
  }

  public String getRomName(int id) {
    return this.store.getString(formatGameKey(id) + ".rom");
  }

  public String getOriginalRom(int id) {
    String rom = this.store.getString(formatGameKey(id) + ".rom");
    if (rom != null && aliasMapping.containsValue(rom)) {
      String alias = aliasMapping
          .entrySet()
          .stream()
          .filter(entry -> rom.equals(entry.getValue()))
          .map(Map.Entry::getKey).findFirst().get();
      return alias;
    }
    return null;
  }

  private String formatGameKey(int id) {
    return "gameId." + id;
  }

  public boolean wasScanned(int id) {
    return store.containsKey(formatGameKey(id) + ".rom");
  }

  /**
   * Checks the different lines that are in the vpx file.
   * Usually the variable not does differ that much.
   * We read the file from the end to save time.
   *
   * @param game the game to search the rom for
   */
  void scanRomName(GameInfo game) {
    ScanResult result = VPXFileScanner.scan(game.getGameFile());
    game.setRom(result.getRom());
    game.setNvOffset(result.getNvOffset());

    game.setRom(result.getRom());
    game.setNvOffset(result.getNvOffset());
  }
}
