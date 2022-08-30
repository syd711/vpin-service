package de.mephisto.vpin.highsocres;

import de.mephisto.vpin.commons.GameInfo;
import de.mephisto.vpin.commons.GameRepository;
import de.mephisto.vpin.highscores.Highscore;
import de.mephisto.vpin.highscores.HighsoreResolver;
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
    HighsoreResolver highsoreResolver = new HighsoreResolver(new File("../"));
    highsoreResolver.refresh();

    GameRepository repository = GameRepository.create();
    List<GameInfo> games = repository.getGameInfos();
    List<GameInfo> ignoredOnes = new ArrayList<>();
    for (GameInfo game : games) {
      Highscore highscore = highsoreResolver.getHighscore(game);
      if(highscore != null) {
        assertFalse(highscore.getScores().isEmpty());
      }
      else {
        ignoredOnes.add(game);
      }
    }

    LOG.info("************************************************************************************");
    LOG.info("Finished highscore reading, " + ignoredOnes.stream());
    LOG.info("************************************************************************************");
    for (GameInfo ignoredOne : ignoredOnes) {
      LOG.info("Ignored " + ignoredOne + ": " + ignoredOne.getGameStatus());
    }

  }

}
