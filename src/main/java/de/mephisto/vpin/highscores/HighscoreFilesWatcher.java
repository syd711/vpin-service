package de.mephisto.vpin.highscores;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.VPinService;
import de.mephisto.vpin.util.SystemInfo;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class HighscoreFilesWatcher extends Thread {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreFilesWatcher.class);
  private final static int INPUT_OFFSET = 5000;

  private final VPinService service;
  private final HighscoreManager highscoreManager;
  private final List<File> files;

  private boolean running = true;
  private WatchService watchService;

  private long lastPoll = new Date().getTime();

  public HighscoreFilesWatcher(VPinService service, HighscoreManager highscoreManager) {
    this.service = service;
    this.highscoreManager = highscoreManager;

    SystemInfo info = SystemInfo.getInstance();
    files = Arrays.asList(info.getNvramFolder());
  }

  public void setRunning(boolean running) {
    this.running = running;
    if (!this.running) {
      try {
        watchService.close();
      } catch (IOException e) {
//        LOG.error("Error stopping Watch Service: " + e.getMessage(), e);
      }
    }
  }

  public void run() {
    try {
      Thread.currentThread().setName("Highscore Watcher");
      LOG.info("Initializing Highscore Watcher");
      watchService = FileSystems.getDefault().newWatchService();
      for (File file : files) {
        Path path = Paths.get(file.getAbsolutePath());
        path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        LOG.info("Monitoring " + file.getAbsolutePath());
      }

      boolean poll = true;
      while (running && poll) {
        WatchKey key = watchService.take();
        for (WatchEvent<?> event : key.pollEvents()) {
          Path info = (Path) event.context();
          File file = info.toFile();

          //filter multiple events this way
          if (System.currentTimeMillis() - lastPoll < INPUT_OFFSET) {
            continue;
          }

          LOG.info("Event kind : " + event.kind() + " - File : " + file.getAbsolutePath());
          lastPoll = System.currentTimeMillis();
          HighscoreChangedEvent highscoreChangedEvent = generateEvent(file);
          if (highscoreChangedEvent != null) {
            highscoreManager.notifyHighscoreChange(highscoreChangedEvent);
          }
          else {
            LOG.warn("Failed to generate highscore change event for file " + file.getAbsolutePath());
          }
        }
        poll = key.reset();
      }
      LOG.info("Highscore Watcher shutdown");
    } catch (Exception e) {
      if (this.running) {
        LOG.error("Failed to watch files: " + e.getMessage(), e);
      }
    }
  }

  private HighscoreChangedEvent generateEvent(File file) {
    String rom = null;
    if (file.getName().endsWith(".nv")) {
      rom = FilenameUtils.getBaseName(file.getName());
    }

    if (rom != null) {
      GameInfo gameByRom = service.getGameByRom(rom);
      if (gameByRom != null) {
        return new HighscoreChangedEventImpl(gameByRom);
      }
      else {
        LOG.warn("Highscore watcher could not resolve game for ROM name '" + rom + "'");
      }
    }
    return null;
  }
}
