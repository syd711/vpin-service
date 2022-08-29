package de.mephisto.vpin.commons;

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
      assertTrue(table.getRomFile().exists());
      assertTrue(table.getVpxFile().exists());
    }
  }

  @Test
  public void testTableRepositorWithoutReset() {
    GameRepository repository = GameRepository.create();
    assertFalse(repository.getGameInfos().isEmpty());

    List<GameInfo> tables = repository.getGameInfos();
    for (GameInfo table : tables) {
      assertTrue(table.getRomFile().exists());
      assertTrue(table.getVpxFile().exists());
    }
  }
}
