package de.mephisto.vpin.roms;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RomManagerTest {

  @Test
  public void testRomScan() {
    RomManager romManager = new RomManager();
    String s = romManager.scanRomName(new File("C:\\vPinball\\VisualPinball\\Tables\\Harry Potter.vpx"));
    System.out.println(s);
    assertNotNull(s);
  }

  @Test
  public void testPattern() {
    String p = "RomSet1";
    Pattern compile = Pattern.compile(".*" + p + ".*=.*\".*\".*");
    Matcher matcher = compile.matcher("Const RomSet1 = \"mb_10\"  ");
    System.out.println(matcher.matches());
  }

//  @Test
//  public void testRomScans() {
//    RomManager romManager = new RomManager();
//    File folder = new File("C:\\vPinball\\VisualPinball\\Tables\\");
//    File[] vpxes = folder.listFiles(new FilenameFilter() {
//      @Override
//      public boolean accept(File dir, String name) {
//        return name.endsWith("vpx");
//      }
//    });
//
//    for (File vpx : vpxes) {
//      String s = romManager.scanRomName(vpx);
//      System.out.println(vpx.getName() + ": " + s);
//    }
//  }

}
