package de.mephisto.vpin.roms;

import de.mephisto.vpin.util.ReverseLineInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class RomScanner {
  private final static Logger LOG = LoggerFactory.getLogger(RomScanner.class);
  public static final int MAX_ROM_FILENAME_LENGTH = 16;

  public String scanRomName(File vpxFile) {
    String rom = scanRomName(vpxFile, "cGameName");
    if (rom == null) {
      rom = scanRomName(vpxFile, "cgamename");
    }
    if (rom == null) {
      rom = scanRomName(vpxFile, "RomSet1");
    }
    if (rom == null) {
      rom = scanRomName(vpxFile, "GameName");
    }
    return rom;
  }

  private String scanRomName(File vpxFile, String pattern) {
    String romName = null;
    BufferedReader bufferedReader = null;
    ReverseLineInputStream reverseLineInputStream = null;
    try {
      reverseLineInputStream = new ReverseLineInputStream(vpxFile);
      bufferedReader = new BufferedReader(new InputStreamReader(reverseLineInputStream));

      String line;
      Pattern p = Pattern.compile(".*" + pattern + ".*=.*\".*\".*");
      bufferedReader.readLine();//skip last line if empty
      int count = 0;
      while ((line = bufferedReader.readLine()) != null || count < 1000) {
        count++;
        if (line != null && line.contains(pattern)) {
          if (p.matcher(line).matches()) {
            if (line.indexOf("'") != 0) {
              int start = line.indexOf("\"") + 1;
              romName = line.substring(start);
              int end = romName.indexOf("\"");

              if (end - start < MAX_ROM_FILENAME_LENGTH) {
                romName = romName.substring(0, end).trim();
                break;
              }
            }
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to read rom line '" + romName + "' for  " + vpxFile.getAbsolutePath() + ": " + e.getMessage(), e);
    } finally {
      try {
        reverseLineInputStream.close();
        bufferedReader.close();
      } catch (Exception e) {
        LOG.error("Failed to close vpx file stream: " + e.getMessage(), e);
      }
    }
    return romName;
  }
}
