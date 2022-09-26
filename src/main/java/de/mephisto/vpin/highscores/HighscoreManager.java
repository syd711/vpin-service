package de.mephisto.vpin.highscores;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.VPinService;
import de.mephisto.vpin.VPinServiceException;
import de.mephisto.vpin.util.SystemInfo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HighscoreManager {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreManager.class);

  private final Map<Integer, Highscore> cache = new HashMap<>();
  private final HighscoreResolver highscoreResolver;

  private final VPinService VPinService;

  public HighscoreManager(VPinService service) throws VPinServiceException {
    this.VPinService = service;
    this.highscoreResolver = new HighscoreResolver();
    init();
  }

  private void init() throws VPinServiceException {
    try {
      File file = new File(HighscoreResolver.PINEMHI_FOLDER, HighscoreResolver.PINEMHI_INI);
      if (!file.exists()) {
        throw new FileNotFoundException("pinemhi.ini file (" + file.getAbsolutePath() + ") not found.");
      }

      FileInputStream fileInputStream = new FileInputStream(file);
      List<String> lines = IOUtils.readLines(fileInputStream, StandardCharsets.UTF_8);
      fileInputStream.close();

      boolean writeUpdates = false;
      List<String> updatedLines = new ArrayList<>();
      for (String line : lines) {
        if (line.startsWith("VP=")) {
          String vpValue = line.split("=")[1];
          File vpxPath = new File(vpValue);
          if (!vpxPath.exists()) {
            line = "VP=" + SystemInfo.getInstance().getNvramFolder().getAbsolutePath() + "\\";
            writeUpdates = true;
          }
        }
        updatedLines.add(line);
      }

      if (writeUpdates) {
        FileOutputStream out = new FileOutputStream(file);
        IOUtils.writeLines(updatedLines, "\n", out, StandardCharsets.UTF_8);
        out.close();
        LOG.info("Written updates to " + file.getAbsolutePath());
      }

      LOG.info("Finished pinemhi installation check.");
    } catch (Exception e) {
      String msg = "Failed to run installation for pinemhi: " + e.getMessage();
      LOG.error(msg, e);
      throw new VPinServiceException(msg, e);
    }
  }

  public Highscore getHighscore(GameInfo game) {
    if (StringUtils.isEmpty(game.getRom())) {
      return null;
    }

    if (!cache.containsKey(game.getId())) {
      Highscore highscore = highscoreResolver.loadHighscore(game);
      cache.put(game.getId(), highscore);
    }

    return cache.get(game.getId());
  }

  public void invalidateHighscore(GameInfo game) {
    highscoreResolver.refresh();
    cache.remove(game.getId());
    LOG.info("Invalidated cached highscore of " + game);
  }
}
