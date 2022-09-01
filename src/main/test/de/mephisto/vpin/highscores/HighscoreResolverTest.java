package de.mephisto.vpin.highscores;

import de.mephisto.vpin.games.GameInfo;
import de.mephisto.vpin.games.GameRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class HighscoreResolverTest {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreResolverTest.class);

  @Test
  public void testHighscoreResolver() throws Exception {
    HighsoreResolver highsoreResolver = new HighsoreResolver(new File("./"));
    highsoreResolver.refresh();

    GameRepository repository = GameRepository.create();
    List<GameInfo> games = repository.getGameInfos();
    List<GameInfo> ignoredOnes = new ArrayList<>();
    for (GameInfo game : games) {
      Highscore highscore = highsoreResolver.getHighscore(game);
      if (highscore != null) {
        assertFalse(highscore.getScores().isEmpty());
      }
      else {
        ignoredOnes.add(game);
      }
    }

    LOG.info("************************************************************************************");
    LOG.info("Finished highscore reading, " + ignoredOnes.stream());
    LOG.info("************************************************************************************");

  }

}
