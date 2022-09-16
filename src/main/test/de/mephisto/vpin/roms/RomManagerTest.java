package de.mephisto.vpin.roms;

import de.mephisto.vpin.util.SqliteConnector;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RomManagerTest {
  private final static Logger LOG = LoggerFactory.getLogger(RomManagerTest.class);

  @Test
  public void testRoms() {
    SqliteConnector connector = new SqliteConnector(new RomManager());
    connector.resetRomNames();
  }

}
