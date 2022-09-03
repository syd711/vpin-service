package de.mephisto.vpin.highscores;

import de.mephisto.vpin.games.GameInfo;
import de.mephisto.vpin.games.GameRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HighscoreResolverTest {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreResolverTest.class);

  @Test
  public void testHighscoreResolver() throws Exception {
    highscoreResolver highscoreResolver = new highscoreResolver();
    highscoreResolver.refresh();

    GameRepository repository = GameRepository.create();
    List<GameInfo> games = repository.getGameInfos();
    List<GameInfo> valid = new ArrayList<>();
    for (GameInfo game : games) {
      highscoreResolver.loadHighscore(game);
      if (game.getHighscore() != null) {
        assertFalse(game.getHighscore().getScores().isEmpty());
        valid.add(game);
      }
    }

    valid.sort((o1, o2) -> (int) (o1.getLastModified() - o2.getLastModified()));


    LOG.info("************************************************************************************");
    LOG.info("Finished highscore reading, " + valid.stream());
    LOG.info("************************************************************************************");
    for (GameInfo gameInfo : valid) {
      LOG.info(new Date(gameInfo.getLastModified()) + ": " + gameInfo.getGameDisplayName());
    }
  }


  @Test
  public void testHighscore() {
    GameRepository gameRepository = GameRepository.create();
    GameInfo game = gameRepository.getGameByRom("deadweap");
    assertNotNull(game.getHighscore());
    LOG.info(game.getHighscore().getRaw());
  }

}
