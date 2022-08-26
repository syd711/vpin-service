package de.mephisto.vpin.commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class SqliteConnectorTest {

  @Test
  public void testSqliteConnector() {
    SqliteConnector sqliteConnector = new SqliteConnector();
    assertFalse(sqliteConnector.getGames().isEmpty());
  }

}
