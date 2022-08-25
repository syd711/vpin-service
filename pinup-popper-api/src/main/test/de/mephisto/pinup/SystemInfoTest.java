package de.mephisto.pinup;

import de.mephistor.pinup.SystemInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemInfoTest {

  @Test
  public void testSystemInfo() {
    SystemInfo info = new SystemInfo();
    assertNotNull(info.getMameRomFolder());
    assertTrue(info.getVPXTables().length > 0);
  }

}
