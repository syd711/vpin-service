package de.mephisto.vpin.games;

import de.mephisto.vpin.games.GameInfo;
import de.mephisto.vpin.games.GameRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameRepositoryTest {

  @Test
  public void testTableRepository() {
    GameRepository repository = GameRepository.create();
    repository.reset();
    assertFalse(repository.getGameInfos().isEmpty());

    List<GameInfo> tables = repository.getGameInfos();
    for (GameInfo table : tables) {
      assertTrue(table.getVpxFile().exists());
    }
  }

  @Test
  public void testTableRepositoryWithoutReset() {
    GameRepository repository = GameRepository.create();
    assertFalse(repository.getGameInfos().isEmpty());

    List<GameInfo> tables = repository.getGameInfos();
    for (GameInfo table : tables) {
      assertTrue(table.getVpxFile().exists());
    }
  }
}
