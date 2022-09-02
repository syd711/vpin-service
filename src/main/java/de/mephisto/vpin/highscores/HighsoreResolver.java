package de.mephisto.vpin.highscores;

import de.mephisto.vpin.util.SystemInfo;
import de.mephisto.vpin.games.GameInfo;
import de.mephisto.vpin.util.SystemCommandExecutor;
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
  private final File rootFolder;

  public HighsoreResolver() {
    this.rootFolder = new File("./");
    this.parser = new HighscoreParser();
    this.refresh();
  }

  /**
   * Return a highscore object for the given table or null if no highscore has been achieved or created yet.
   */
  public void loadHighscore(GameInfo gameInfo) {
    try {
      if(StringUtils.isEmpty(gameInfo.getRom())) {
        String msg = "Skipped highscore reading for '" + gameInfo.getGameDisplayName() + "' failed, no rom name found.";
        LOG.info(msg);
        return;
      }

      Highscore highscore = parseNvHighscore(gameInfo);
      if (highscore == null) {
        highscore = parseVRegHighscore(gameInfo);
      }

      if (highscore == null) {
        String msg = "Reading highscore for '" + gameInfo.getGameDisplayName() + "' failed, no nvram file and no VPReg.stg entry found for rom name '" + gameInfo.getRom() + "'";
        LOG.info(msg);
      }
      else {
        gameInfo.setHighscore(highscore);
        LOG.debug("Successfully read highscore for " + gameInfo.getGameDisplayName());
      }
    } catch (Exception e) {
      LOG.error("Failed to find highscore for table {}: {}", gameInfo.getGameFileName(), e.getMessage(), e);
    }
  }

  /**
   * Refreshes the extraction of the VPReg.stg file.
   */
  public void refresh() {
    File targetFolder = new File(rootFolder, "VPReg");
    if (!targetFolder.exists()) {
      boolean mkdirs = targetFolder.mkdirs();
      if(!mkdirs) {
        LOG.error("Failed to create VPReg target directory");
      }
    }

    //check if we have to unzip the score file using the modified date of the target folder
    updateUserScores(targetFolder);
  }

  /**
   * We use the manual set rom name to find the highscore in the "/User/VPReg.stg" file.
   */
  private Highscore parseVRegHighscore(GameInfo gameInfo) throws IOException {
    File targetFolder = new File(rootFolder, "VPReg");
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
            if(highScoreValue != null) {
              highScoreValue = HighscoreParser.formatScore(highScoreValue);
              initials = readFileString(tableHighscoreNameFile);

              Score score = new Score(initials, highScoreValue, i - 1);
              highscore.getScores().add(score);
            }
          }
        }

        return highscore;
      }
      else {
        LOG.debug("No VPReg highscore file found: " + tableHighscoreFile.getAbsolutePath());
      }
    }
    else {
      LOG.debug("VPReg highscore folder does not exist: " + tableHighscoreFolder.getAbsolutePath());
    }
    return null;
  }

  /**
   * Uses 7zip to unzip the stg file into the configured target folder
   *
   * @param vpRegFolderFile
   */
  private void updateUserScores(File vpRegFolderFile) {
    String unzipCommand = SystemInfo.getInstance().get7ZipCommand();
    List<String> commands = Arrays.asList("\"" + unzipCommand + "\"", "-aoa", "x", "\"" + SystemInfo.getInstance().getVPRegFile().getAbsolutePath() + "\"", "-o\"" + vpRegFolderFile.getAbsolutePath() + "\"");
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(commands, false);
      executor.setDir(vpRegFolderFile);
      executor.executeCommand();

      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("7zip command '" + String.join(" ", commands) + "' failed: {}", standardErrorFromCommand);
      }
    } catch (Exception e) {
      LOG.info("Failed to init VPReg: " + e.getMessage(), e);
    }
  }

  private Highscore parseNvHighscore(GameInfo gameInfo) throws Exception {
    File nvRam = gameInfo.getNvRamFile();
    File commandFile = new File(PINEMHI_FOLDER, PINEMHI_COMMAND);
    if (!commandFile.exists()) {
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
      highscore = parser.parseHighscore(gameInfo, nvRam, s);
      if (highscore == null || highscore.getScores().isEmpty()) {
        return null;
      }
    } catch (Exception e) {
      LOG.error("Failed to parse highscore: " + e.getMessage(), e);
    }
    return highscore;
  }

  /**
   * Reads the first line of the given file
   */
  private String readFileString(File file) throws IOException {
    BufferedReader brTest = new BufferedReader(new FileReader(file));
    try {
      String text = brTest.readLine();
      if(text != null) {
        return text.replace("\0", "").trim();
      }
      else {
        LOG.error("Error reading highscore file " + file.getAbsolutePath() + ", reader returned null.");
      }
      return null;
    } catch (IOException e) {
      throw e;
    }
    finally {
      brTest.close();;
    }
  }
}
