package de.mephisto.vpin.highscores;

import de.mephisto.vpin.games.GameInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

  public Highscore parseHighscore(GameInfo game, File file, String cmdOutput) throws Exception {
    Highscore highscore = null;
    try {
      highscore = new Highscore(cmdOutput);
      String[] lines = cmdOutput.split("\\n");
      if (lines.length < 3) {
        LOG.debug("Skipped highscore parsing for " + game.getGameDisplayName() + ", output too short:\n" + cmdOutput);
        return null;
      }

      LOG.debug("Parsing Highscore text for " + game.getGameDisplayName() + "\n" + cmdOutput);

      boolean listStarted = false;
      for (String line : lines) {
        if (line.startsWith("1)") || line.startsWith("#1")) {
          listStarted = true;
          String initials = line.substring(3, 6);
          String score = line.substring(7).trim();
          if(initials.startsWith(" ")) {
            initials = "";
          }

          highscore.setScore(score);
          highscore.getScores().add(new Score(initials, score, 1));
        }
        else if (line.indexOf(")") == 1) {
          listStarted = true;
          int pos = Integer.parseInt(line.substring(0, 1));
          String initials = line.substring(3, 6);
          if(initials.startsWith(" ")) {
            initials = "";
          }
          String score = line.substring(7).trim();
          highscore.getScores().add(new Score(initials, score, pos));
        }
        else {
          //list has been read, ignore following lines.
          if(listStarted) {
            break;
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to parse highscore file '" + file.getAbsolutePath() + "': " + e.getMessage() + "\nPinemhi Command Output:\n==================================\n" + cmdOutput, e);
      throw e;
    }

    if (highscore.getScores().isEmpty()) {
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
