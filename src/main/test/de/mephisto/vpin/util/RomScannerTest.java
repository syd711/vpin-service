package de.mephisto.vpin.util;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RomScannerTest {

  @Test
  public void testRomScanner() {
    RomScanner scanner = new RomScanner();
    File vpxFile = new File("C:\\vPinball\\VisualPinball\\Tables\\Iron Maiden Virtual Time (Original 2020).vpx");
    String rom = scanner.scanRomName(vpxFile);
    assertNotNull(rom);
  }

}
