package de.mephisto.vpin.highscores;

import de.mephisto.vpin.commons.GameInfo;
import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.highscores.util.SystemCommandExecutor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class HighsoreResolver {
  private final static Logger LOG = LoggerFactory.getLogger(HighsoreResolver.class);

  private static final String PINEMHI_FOLDER = "pinemhi";
  private static final String PINEMHI_COMMAND = "PINemHi.exe";

  private final HighscoreParser parser;

  public HighsoreResolver() {
    this.parser = new HighscoreParser();
  }

  /**
   * Return a highscore object for the given table or null if no highscore has been achieved or created yet.
   */
  public Highscore getHighscore(GameInfo gameInfo) throws Exception {
    try {
      Highscore highscore = parseNvHighscore(gameInfo);
      if (highscore == null) {
        highscore = parseVRegHighscore(gameInfo);
      }

      if (highscore == null) {
        String msg = "Read highscore for '" + gameInfo.getGameDisplayName() + "' [No nvram highscore and no VPReg.stg entry found with rom " + gameInfo.getRom() + "]";
        if(gameInfo.getGameStatus() == null) {
          gameInfo.setGameStatus(msg);
        }

        LOG.warn(msg);
      }
      return highscore;
    } catch (Exception e) {
      LOG.error("Failed to find highscore for table {}: {}", gameInfo.getGameFileName(), e.getMessage(), e);
      throw e;
    }
  }

  /**
   * Refreshes the extraction of the VPReg.stg file.
   */
  public void refresh() throws Exception {
    File targetFolder = new File("../", "VPReg");
    if (!targetFolder.exists()) {
      targetFolder.mkdirs();
    }

    //check if we have to unzip the score file using the modified date of the target folder
    updateUserScores(targetFolder);
  }

  /**
   * We use the manual setted rom name to find the highscore in the "/User/VPReg.stg" file.
   *
   * @param gameInfo
   * @return
   * @throws IOException
   */
  private Highscore parseVRegHighscore(GameInfo gameInfo) throws IOException {
    File targetFolder = new File("../", "VPReg");
    File tableHighscoreFolder = new File(targetFolder, gameInfo.getRom());

    if (tableHighscoreFolder.exists()) {
      gameInfo.setLastModified(tableHighscoreFolder.lastModified());

      File tableHighscoreFile = new File(tableHighscoreFolder, "HighScore1");
      File tableHighscoreNameFile = new File(tableHighscoreFolder, "HighScore1Name");
      if (tableHighscoreFile.exists() && tableHighscoreNameFile.exists()) {
        String highScoreValue = readFileString(tableHighscoreFile);
        highScoreValue = HighscoreParser.formatScore(highScoreValue);
        String initials = readFileString(tableHighscoreNameFile);

        Highscore highscore = new Highscore(highScoreValue);
        highscore.setPosition(0);
        highscore.setUserInitials(initials);
        highscore.setScore(highScoreValue);

        for (int i = 1; i <= 4; i++) {
          tableHighscoreFile = new File(tableHighscoreFolder, "HighScore" + i);
          tableHighscoreNameFile = new File(tableHighscoreFolder, "HighScore" + i + "Name");
          if (tableHighscoreFile.exists() && tableHighscoreNameFile.exists()) {
            highScoreValue = readFileString(tableHighscoreFile);
            highScoreValue = HighscoreParser.formatScore(highScoreValue);
            initials = readFileString(tableHighscoreNameFile);

            Score score = new Score(initials, highScoreValue, i - 1);
            highscore.getScores().add(score);
          }
        }

        return highscore;
      }
    }

    return null;
  }

  /**
   * Uses 7zip to unzip the stg file into the configured target folder
   *
   * @param targetFolder
   */
  private void updateUserScores(File targetFolder) throws Exception {
    String unzipCommand = SystemInfo.getInstance().get7ZipCommand();
    List<String> commands = Arrays.asList("\"" + unzipCommand + "\"", "-aoa", "x", "\"" + SystemInfo.getInstance().getVPRegFile().getAbsolutePath() + "\"", "-o\"" + targetFolder.getAbsolutePath() + "\"");
    SystemCommandExecutor executor = new SystemCommandExecutor(commands, false);
    executor.setDir(targetFolder);
    executor.executeCommand();

    StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
    StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
    if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
      LOG.error("7zip command failed: {}", standardErrorFromCommand.toString());
      throw new Exception("7zip command failed: " + standardErrorFromCommand.toString());
    }
  }

  private Highscore parseNvHighscore(GameInfo gameInfo) throws Exception {
    File nvRam = gameInfo.getNvRamFile();
    File commandFile = new File(PINEMHI_FOLDER, PINEMHI_COMMAND);
    if(!commandFile.exists()) {
      commandFile = new File("../" + PINEMHI_FOLDER, PINEMHI_COMMAND);
    }

    if (!nvRam.exists()) {
      return null;
    }

    gameInfo.setLastModified(nvRam.lastModified());

    SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList(commandFile.getName(), nvRam.getName()));
    executor.setDir(commandFile.getParentFile());
    executor.executeCommand();
    StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
    StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
    if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
      String error = "Pinemhi command (" + commandFile.getAbsolutePath() + ") failed: " + standardErrorFromCommand;
      LOG.error(error);
      throw new Exception(error);
    }

    String s = standardOutputFromCommand.toString();
    Highscore highscore = null;
    try {
      highscore = parser.parseHighscore(s);
      if (highscore.getScores().isEmpty()) {
        gameInfo.setGameStatus("Unable to parse highscore info from string '" + s + "'");
        return null;
      }
    } catch (Exception e) {
      gameInfo.setGameStatus(e.getMessage());
    }
    return highscore;
  }

  /**
   * Reads the first line of the given file
   */
  private String readFileString(File file) throws IOException {
    BufferedReader brTest = new BufferedReader(new FileReader(file));
    String text = brTest.readLine();
    brTest.close();
    return text.replace("\0", "").trim();
  }
}
