package de.mephisto.vpin.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RomScannerTest {
  private final static Logger LOG = LoggerFactory.getLogger(RomScannerTest.class);

  @Test
  public void testRomScanner() {
    RomScanner scanner = new RomScanner();
    File vpxFile = new File("C:\\vPinball\\VisualPinball\\Tables\\SpiderMan (2007).vpx");
    String rom = scanner.scanRomName(vpxFile);
    assertNotNull(rom);
    LOG.info("Resolved from " + rom);
  }

  @Test
  public void testRegEx() {
    Pattern p = java.util.regex.Pattern.compile(".*cGameName.*=.*\".*\".*");
    String testling= "Const cGameName = \"sman_261\"";
    assertTrue(p.matcher(testling).matches());

    p = java.util.regex.Pattern.compile(".*RomSet1.*=.*\".*\".*");
    testling= "Const RomSet1 = \"mb_10\"  ";
    assertTrue(p.matcher(testling).matches());
  }

}
