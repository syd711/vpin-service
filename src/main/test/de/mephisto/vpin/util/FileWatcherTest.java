package de.mephisto.vpin.util;

import de.mephisto.vpin.games.GameRepository;
import de.mephisto.vpin.highscores.HighscoreFilesWatcher;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileWatcherTest {

  @Test
  public void testFileWatcher() {
    SystemInfo info = SystemInfo.getInstance();

    GameRepository gameRepository = GameRepository.create();
    HighscoreFilesWatcher watcher = new HighscoreFilesWatcher(gameRepository,Arrays.asList(info.getVPRegFile().getParentFile(), info.getNvramFolder()));

  }

}
