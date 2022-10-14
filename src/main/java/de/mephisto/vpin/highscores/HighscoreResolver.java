package de.mephisto.vpin.highscores;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.util.SystemCommandExecutor;
import de.mephisto.vpin.util.SystemInfo;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

class HighscoreResolver {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreResolver.class);

  private final HighscoreParser parser;
  private List<String> supportedRoms;

  public HighscoreResolver() {
    this.parser = new HighscoreParser();
    this.loadSupportedScores();
    this.refresh();
  }

  private void loadSupportedScores() {
    try {
      String roms = executePINemHi("-lr");
      this.supportedRoms = Arrays.asList(roms.split("\n"));
    } catch (Exception e) {
      LOG.error("Failed to load supported rom names from PINemHi: " + e.getMessage(), e);
    }
  }

  public boolean isRomSupported(String rom) {
    return this.supportedRoms.contains(rom);
  }

  /**
   * Return a highscore object for the given table or null if no highscore has been achieved or created yet.
   */
  public Highscore loadHighscore(GameInfo gameInfo) {
    try {
      String romName = gameInfo.getRom();
      if (StringUtils.isEmpty(romName)) {
        String msg = "Skipped highscore reading for '" + gameInfo.getGameDisplayName() + "' failed, no rom name found.";
        LOG.info(msg);
        return null;
      }


      Highscore highscore = parseNvHighscore(gameInfo);
      if (highscore == null) {
        highscore = parseVRegHighscore(gameInfo);
      }

      if (highscore == null) {
        String msg = "Reading highscore for '" + gameInfo.getGameDisplayName() + "' failed, no nvram file and no VPReg.stg entry found for rom name '" + romName + "'";
        LOG.info(msg);
      }
      else {
        LOG.debug("Successfully read highscore for " + gameInfo.getGameDisplayName());
        return highscore;
      }

    } catch (Exception e) {
      LOG.error("Failed to find highscore for table {}: {}", gameInfo.getGameFileName(), e.getMessage(), e);
    }
    return null;
  }

  /**
   * Refreshes the extraction of the VPReg.stg file.
   */
  public void refresh() {
    File targetFolder = SystemInfo.getInstance().getExtractedVPRegFolder();
    if (!targetFolder.exists()) {
      boolean mkdirs = targetFolder.mkdirs();
      if (!mkdirs) {
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
    File tableHighscoreFolder = gameInfo.getVPRegFolder();

    if (tableHighscoreFolder != null && gameInfo.getVPRegFolder().exists()) {
      File tableHighscoreFile = new File(tableHighscoreFolder, "HighScore1");
      File tableHighscoreNameFile = new File(tableHighscoreFolder, "HighScore1Name");
      StringBuilder rawBuilder = new StringBuilder();
      if (tableHighscoreFile.exists() && tableHighscoreNameFile.exists()) {
        String highScoreValue = readFileString(tableHighscoreFile);
        highScoreValue = HighscoreParser.formatScore(highScoreValue);
        String initials = readFileString(tableHighscoreNameFile);

        Highscore highscore = new Highscore("");
        highscore.setUserInitials(initials);
        highscore.setScore(highScoreValue);

        int index = 1;
        tableHighscoreFile = new File(tableHighscoreFolder, "HighScore" + index);
        tableHighscoreNameFile = new File(tableHighscoreFolder, "HighScore" + index + "Name");
        while (tableHighscoreFile.exists() && tableHighscoreNameFile.exists()) {
          highScoreValue = readFileString(tableHighscoreFile);
          if (highScoreValue != null) {
            highScoreValue = HighscoreParser.formatScore(highScoreValue);
            initials = readFileString(tableHighscoreNameFile);

            Score score = new Score(initials, highScoreValue, index);
            highscore.getScores().add(score);

            rawBuilder.append(score);
            rawBuilder.append("\n");
          }
          index++;
          tableHighscoreFile = new File(tableHighscoreFolder, "HighScore" + index);
          tableHighscoreNameFile = new File(tableHighscoreFolder, "HighScore" + index + "Name");
        }

        highscore.setRaw(rawBuilder.toString());
        return highscore;
      }
      else {
        LOG.debug("No VPReg highscore file found for '" + gameInfo.getRom() + "'");
      }
    }
    else {
      LOG.debug("VPReg highscore folder does not exist: " + tableHighscoreFolder.getAbsolutePath());
    }
    return null;
  }

  /**
   * Uses 7zip to unzip the stg file into the configured target folder.
   *
   * @param vpRegFolderFile the VPReg file to expand
   */
  private void updateUserScores(File vpRegFolderFile) {
    if (!SystemInfo.getInstance().getVPRegFile().exists()) {
      LOG.info("Skipped VPReg extraction, file does not exists yet.");
      return;
    }

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
      LOG.info("Finished VPReg folder refresh of " + vpRegFolderFile.getAbsolutePath());
    } catch (Exception e) {
      LOG.info("Failed to init VPReg: " + e.getMessage(), e);
    }
  }

  /**
   * Executes a single PINemHi command for the given game.
   *
   * @param gameInfo the game to parse the highscore for
   * @return the Highscore object or null if no highscore could be parsed.
   */
  @Nullable
  private Highscore parseNvHighscore(GameInfo gameInfo) {
    Highscore highscore = null;
    try {
      File nvRam = gameInfo.getNvRamFile();
      if (nvRam == null || !nvRam.exists()) {
        return null;
      }

      String romName = gameInfo.getRom();
      String originalRom = gameInfo.getOriginalRom();

      String pinemHiRom = originalRom != null ? originalRom : romName;
      if (!this.supportedRoms.contains(pinemHiRom)) {
        LOG.warn("The resolved rom name '" + originalRom + "' of game '" + gameInfo.getGameDisplayName() + "' is not supported by PINemHi.");
        return null;
      }

      LOG.info("Parsing nvram file " + nvRam.getAbsolutePath());
      String output = executePINemHi(nvRam.getName());
      if (output != null) {
        highscore = parser.parseHighscore(gameInfo, nvRam, output);
        if (highscore == null || highscore.getScores().isEmpty()) {
          return null;
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to parse highscore: " + e.getMessage(), e);
    }
    return highscore;
  }

  private String executePINemHi(String param) throws Exception {
    File commandFile = SystemInfo.getInstance().getPinemhiCommandFile();
    try {
      List<String> commands = Arrays.asList(commandFile.getName(), param);
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(commandFile.getParentFile());
      executor.executeCommand();
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        String error = "Pinemhi command (" + commandFile.getAbsolutePath() + ") failed: " + standardErrorFromCommand;
        LOG.error(error);
        throw new Exception(error);
      }
      return standardOutputFromCommand.toString();
    } catch (Exception e) {
      LOG.error(commandFile.getAbsolutePath() + " command failed for directory " + commandFile.getAbsolutePath() + ": " + e.getMessage());
      throw e;
    }
  }

  /**
   * Reads the first line of the given file
   */
  private String readFileString(File file) throws IOException {
    BufferedReader brTest = new BufferedReader(new FileReader(file));
    try {
      String text = brTest.readLine();
      if (text != null) {
        return text.replace("\0", "").trim();
      }
      else {
        LOG.debug("Error reading highscore file " + file.getAbsolutePath() + ", reader returned null.");
      }
      return null;
    } catch (IOException e) {
      throw e;
    } finally {
      brTest.close();
    }
  }
}
