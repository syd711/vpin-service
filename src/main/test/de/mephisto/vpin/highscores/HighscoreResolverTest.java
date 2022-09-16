package de.mephisto.vpin.highscores;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.VPinService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HighscoreResolverTest {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreResolverTest.class);

  @Test
  public void testHighscoreResolver() throws Exception {
    HighscoreResolver highscoreResolver = new HighscoreResolver();
    highscoreResolver.refresh();

    VPinService service = VPinService.create(true);
    List<GameInfo> games = service.getGameInfos();
    List<GameInfo> valid = new ArrayList<>();
    for (GameInfo game : games) {
      if (game.resolveHighscore() != null) {
        assertFalse(game.resolveHighscore().getScores().isEmpty());
        assertFalse(game.resolveHighscore().getUserInitials().isEmpty());
        valid.add(game);
      }
    }

    valid.sort((o1, o2) -> (int) (o1.getLastPlayed().getTime() - o2.getLastPlayed().getTime()));


    LOG.info("************************************************************************************");
    LOG.info("Finished highscore reading, " + valid.stream());
    LOG.info("************************************************************************************");
    for (GameInfo gameInfo : valid) {
      LOG.info(gameInfo.getLastPlayed() + ": " + gameInfo.getGameDisplayName());
    }
  }


  @Test
  public void testHighscore() {
    HighscoreResolver highscoreResolver = new HighscoreResolver();
    highscoreResolver.refresh();

    VPinService service = VPinService.create(true);
    GameInfo game = service.getGameByRom("STLE");
    assertNotNull(game.resolveHighscore());
    assertNotNull(game.resolveHighscore().getUserInitials());
    assertFalse(game.resolveHighscore().getScores().isEmpty());
    LOG.info("---------------------------------");
    LOG.info("Scores: " + game.resolveHighscore().getScores().size());
    List<Score> scores = game.resolveHighscore().getScores();
    for (Score score : scores) {
      LOG.info(score.toString());
    }

    LOG.info("---------------------------------");
    LOG.info(game.resolveHighscore().getRaw());
    LOG.info(game.resolveHighscore().getRaw());
  }

}
