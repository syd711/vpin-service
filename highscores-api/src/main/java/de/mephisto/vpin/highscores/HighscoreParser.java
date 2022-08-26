package de.mephisto.vpin.highscores;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

/**
 * e.g.:
 * <p>
 * CANNON BALL CHAMPION
 * TEX - 50
 * <p>
 * GRAND CHAMPION
 * RRR      60.000.000
 * <p>
 * HIGHEST SCORES
 * 1) POP      45.000.000
 * 2) LTD      40.000.000
 * 3) ROB      35.000.000
 * 4) ZAB      30.000.000
 * <p>
 * PARTY CHAMPION
 * PAB      20.000.000
 */
public class HighscoreParser {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreParser.class);

  public Highscore parseHighscore(String cmdOutput) throws Exception {
    Highscore highscore = new Highscore(cmdOutput);
    String[] lines = cmdOutput.split("\\n");
    if (lines.length == 1) {
      throw new Exception("Error parsing highscore command output: " + lines[0]);
    }

    if(lines.length == 2) {
      throw new Exception("No score set. (" + cmdOutput + ")");
    }

    for (String line : lines) {
      if (line.startsWith("1)") || line.startsWith("#1")) {
        String initials = line.substring(3, 6);
        String score = line.substring(7).trim();
        highscore.setUserInitials(initials);
        highscore.setScore(score);
        highscore.getScores().add(new Score(initials, score, 1));
      } else if (line.indexOf(")") == 1) {
        int pos = Integer.parseInt(line.substring(0, 1));
        String shortName = line.substring(3, 6);
        String score = line.substring(7).trim();
        highscore.getScores().add(new Score(shortName, score, pos));
      }
    }

    if(highscore.getScores().isEmpty()) {
      throw new Exception("Failed to read scores from output: " + cmdOutput);
    }

    return highscore;
  }

  public static String formatScore(String score) {
    DecimalFormat decimalFormat = new DecimalFormat("#.##");
    decimalFormat.setGroupingUsed(true);
    decimalFormat.setGroupingSize(3);
    return decimalFormat.format(Long.parseLong(score));
  }
}
