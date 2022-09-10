package de.mephisto.vpin.util;

import de.mephisto.vpin.VPinService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class SqliteConnectorTest {

  @Test
  public void testSqliteConnector() {
    SqliteConnector sqliteConnector = new SqliteConnector();
    VPinService service = VPinService.create();
    assertFalse(sqliteConnector.getGames(service).isEmpty());
  }

}
