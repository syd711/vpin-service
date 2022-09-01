package de.mephisto.vpin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class RomScanner {
  private final static Logger LOG = LoggerFactory.getLogger(RomScanner.class);

  public String scanRomName(File vpxFile) {
    String romName = null;
    try {
      BufferedReader br = new BufferedReader(new FileReader(vpxFile));
      String line;
      while ((line = br.readLine()) != null) {
        if (line.contains("cGameName")) {
          if((line.contains("cGameName = \"") || line.contains("cGameName=\"") || line.contains("cGameName= \"")) && line.length() < 160) {
            romName = line.substring(line.indexOf("\"") + 1);
            romName = romName.substring(0, romName.indexOf("\"")).trim();
            break;
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
