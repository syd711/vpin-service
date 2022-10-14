package de.mephisto.vpin.roms;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.VPinService;
import de.mephisto.vpin.VPinServiceException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RomManagerTest {

  @Test
  public void testRomScan() throws VPinServiceException {
    RomManager romManager = new RomManager();

    GameInfo info = new GameInfo(VPinService.create(false));
    info.setGameFile(new File("C:\\vPinball\\VisualPinball\\Tables\\Guardians of the Galaxy.vpx"));
    romManager.scanVPXFile(info);
    System.out.println(info.getRom());
    System.out.println(info.getNvOffset());
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
