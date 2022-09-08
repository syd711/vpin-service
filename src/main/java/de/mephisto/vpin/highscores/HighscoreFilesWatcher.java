package de.mephisto.vpin.highscores;

import de.mephisto.vpin.games.GameInfo;
import de.mephisto.vpin.games.GameRepository;
import de.mephisto.vpin.games.HighscoreChangedEvent;
import de.mephisto.vpin.games.HighscoreChangedEventImpl;
import de.mephisto.vpin.util.SystemInfo;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class HighscoreFilesWatcher extends Thread {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreFilesWatcher.class);

  private final GameRepository gameRepository;
  private final List<File> files;

  private boolean running = true;
  private WatchService watchService;

  public HighscoreFilesWatcher(GameRepository gameRepository, List<File> files) {
    this.gameRepository = gameRepository;
    this.files = files;
  }

  public void setRunning(boolean running) {
    this.running = running;
    if(!this.running) {
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
          LOG.info("Event kind : " + event.kind() + " - File : " + file.getAbsolutePath());
          //filter multiple events this way
          Thread.sleep(5000);
          HighscoreChangedEvent highscoreChangedEvent = generateEvent(file);
          if(highscoreChangedEvent != null) {
            gameRepository.notifyHighscoreChange(highscoreChangedEvent);
          }
        }
        poll = key.reset();
      }
      LOG.info("Highscore Watcher shutdown");
    } catch (Exception e) {
      if(this.running) {
        LOG.error("Failed to watch files: " + e.getMessage(), e);
      }
    }
  }

  private HighscoreChangedEvent generateEvent(File file) {
    String rom = null;
    if(file.getName().endsWith(".nv")) {
      rom = FilenameUtils.getBaseName(file.getName());
    }
    else if(file.getName().equals(SystemInfo.VPREG_STG)) {
      gameRepository.refreshHighscores();
      File target = new File(SystemInfo.RESOURCES, SystemInfo.VPREG);
      File[] subFolders = target.listFiles((dir, name) -> new File(dir, name).isDirectory());
      if(subFolders != null) {
        List<File> folders = Arrays.asList(subFolders);
        folders.sort((o1, o2) -> (int) (o1.lastModified() - o2.lastModified()));
        rom = folders.get(0).getName();
      }
    }

    if(rom != null) {
      GameInfo gameByRom = gameRepository.getGameByRom(rom);
      if(gameByRom != null) {
        return new HighscoreChangedEventImpl(gameByRom);
      }
    }
    return null;
  }


}
