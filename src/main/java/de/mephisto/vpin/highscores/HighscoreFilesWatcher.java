package de.mephisto.vpin.highscores;

import de.mephisto.vpin.games.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
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
          LOG.info("Event kind : " + event.kind() + " - File : " + event.context());
          //filter multiple events this way
          Thread.sleep(5000);
          gameRepository.notifyHighscoreChange();
        }
        poll = key.reset();
      }
      LOG.info("Highscore Watcher shutdown");
    } catch (Exception e) {
//      LOG.error("Failed to watch files: " + e.getMessage(), e);
    }
  }
}
