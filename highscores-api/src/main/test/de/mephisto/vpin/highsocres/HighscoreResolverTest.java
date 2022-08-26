package de.mephisto.vpin.highsocres;

import de.mephisto.vpin.commons.GameInfo;
import de.mephisto.vpin.commons.GameRepository;
import de.mephisto.vpin.highscores.Highscore;
import de.mephisto.vpin.highscores.HighsoreResolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class HighscoreResolverTest {

  @Test
  public void testHighscoreResolver() throws Exception {
    HighsoreResolver highsoreResolver = new HighsoreResolver();
    highsoreResolver.refresh();

    GameRepository repository = GameRepository.create();
    List<GameInfo> games = repository.getGameInfos();
    for (GameInfo game : games) {
      Highscore highscore = highsoreResolver.getHighscore(game);
      if(highscore != null) {
        assertFalse(highscore.getScores().isEmpty());
      }
    }
  }

}
