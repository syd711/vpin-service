package de.mephisto.vpin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Pattern;

public class RomScanner {
  private final static Logger LOG = LoggerFactory.getLogger(RomScanner.class);
  public static final int MAX_ROM_FILENAME_LENGTH = 16;

  public String scanRomName(File vpxFile) {
    String romName = null;
    try {
      BufferedReader br = new BufferedReader(new FileReader(vpxFile));
      String line;
      Pattern p = Pattern.compile(".*cGameName.*=.*\".*\".*");
      while ((line = br.readLine()) != null) {
        if (line.contains("cGameName")) {
          if(p.matcher(line).matches()) {
            int start = line.indexOf("\"") + 1;
            romName = line.substring(start);
            int end = romName.indexOf("\"");

            if(end - start < MAX_ROM_FILENAME_LENGTH) {
              romName = romName.substring(0, end).trim();
              break;
            }
          }
        }
      }
      br.close();
    } catch (Exception e) {
      LOG.error("Failed to read rom line '" + romName + "' for  " + vpxFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return romName;
  }
}
