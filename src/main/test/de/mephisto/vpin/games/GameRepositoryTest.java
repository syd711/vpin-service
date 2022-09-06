package de.mephisto.vpin.games;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameRepositoryTest {
  private final static Logger LOG = LoggerFactory.getLogger(GameRepositoryTest.class);

  @Test
  public void testTableRepository() {
    GameRepository repository = GameRepository.create();
    repository.invalidateAll();
    List<GameInfo> tables = repository.getGameInfos();
    for (GameInfo table : tables) {
      assertTrue(table.getGameFile().exists());
    }
  }

  @Test
  public void testTableRepositoryWithoutReset() {
    GameRepository repository = GameRepository.create();
    assertFalse(repository.getGameInfos().isEmpty());

    List<GameInfo> tables = repository.getGameInfos();
    for (GameInfo table : tables) {
      assertTrue(table.getGameFile().exists());
    }
    LOG.info("Loaded " + tables.size() + " tables.");
  }

  @Test
  public void testTableRepositoryGetGamesWithoutRoms() {
    GameRepository repository = GameRepository.create();
    List<GameInfo> tables = repository.getGamesWithEmptyRoms();
    for (GameInfo table : tables) {
      LOG.info(table.getId() + ": " + table.getGameFile().getAbsolutePath());
    }
  }

  @Test
  public void testTableInvalidate() {
    GameRepository repository = GameRepository.create();
    List<GameInfo> tables = repository.getGamesWithEmptyRoms();
    for (GameInfo table : tables) {
      if(table.getId() == 372) {
        LOG.info(table.getId() + ": " + table.getGameFile().getAbsolutePath());
        repository.invalidate(table);
      }
    }
  }
}
