package de.mephisto.pinup;

import de.mephistor.pinup.SqliteConnector;
import de.mephistor.pinup.SystemInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class SqliteConnectorTest {

  @Test
  public void testSqliteConnector() {
    SystemInfo systemInfo = new SystemInfo();
    SqliteConnector sqliteConnector = new SqliteConnector(systemInfo);
    assertFalse(sqliteConnector.getGames().isEmpty());
  }

}
