package de.mephisto.pinup;

import de.mephistor.pinup.SqliteConnector;
import de.mephistor.pinup.SystemInfo;
import de.mephistor.pinup.GameRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameRepositoryTest {

  @Test
  public void testTableRepository() {
    SystemInfo systemInfo = new SystemInfo();
    SqliteConnector sqliteConnector = new SqliteConnector(systemInfo);
    GameRepository repository = new GameRepository(systemInfo, sqliteConnector);
    assertFalse(repository.getTables().isEmpty());
  }

}
