package de.mephisto.vpin.highscores;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.VPinService;
import de.mephisto.vpin.VPinServiceException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HighscoreResolverTest {

  @Test
  public void testHighscoreResolver() throws Exception {
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

    assertFalse(valid.isEmpty());
  }


  @Test
  public void testHighscore() throws VPinServiceException {
    HighscoreResolver highscoreResolver = new HighscoreResolver();
    highscoreResolver.refresh();

    VPinService service = VPinService.create(true);
    GameInfo game = service.getGameByRom("gnr_300");
    assertNotNull(game.resolveHighscore());
    assertNotNull(game.resolveHighscore().getUserInitials());
    assertFalse(game.resolveHighscore().getScores().isEmpty());

    System.out.println(game.resolveHighscore().getRaw());
  }

}
