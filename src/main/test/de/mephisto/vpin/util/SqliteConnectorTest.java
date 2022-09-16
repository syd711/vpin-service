package de.mephisto.vpin.util;

import de.mephisto.vpin.VPinService;
import de.mephisto.vpin.roms.RomManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class SqliteConnectorTest {

  @Test
  public void testSqliteConnector() {
    SqliteConnector sqliteConnector = new SqliteConnector(new RomManager());
    VPinService service = VPinService.create();
    assertFalse(sqliteConnector.getGames(service).isEmpty());
  }

}
