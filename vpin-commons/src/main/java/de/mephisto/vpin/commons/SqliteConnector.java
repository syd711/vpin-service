package de.mephisto.vpin.commons;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteConnector {
  private final static Logger LOG = LoggerFactory.getLogger(SqliteConnector.class);

  public static final String POST_SCRIPT = "PostScript";
  public static final String LAUNCH_SCRIPT = "LaunchScript";
  public static final String ROM = "ROM";

  private Connection conn;
  private SystemInfo systemInfo;

  public SqliteConnector() {
    this.systemInfo = SystemInfo.getInstance();
    File dbFile = new File(systemInfo.getPinUPSystemFolder(), "PUPDatabase.db");
    if (!dbFile.exists()) {
      throw new IllegalArgumentException("Wrong PUPDatabase.db folder: " + dbFile.getAbsolutePath());
    }
    String dbFilePath = dbFile.getAbsolutePath().replaceAll("\\\\", "/");
    this.connect(dbFilePath);
  }

  /**
   * Connect to a database
   */
  private void connect(String dbFilePath) {
    try {
      // db parameters
      String url = "jdbc:sqlite:" + dbFilePath;
      // create a connection to the database
      conn = DriverManager.getConnection(url);
      LOG.info("Connection to SQLite (" + dbFilePath + ") has been established.");
    } catch (SQLException e) {
      LOG.error("Failed to connect to sqllite: " + e.getMessage(), e);
    }
  }

  public List<GameInfo> getGames() {
    List<GameInfo> results = new ArrayList<>();
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Games;");
      while (rs.next()) {
        GameInfo info = new GameInfo();
        int id = rs.getInt("GameID");
        String rom = rs.getString("ROM");
        String gameFileName = rs.getString("GameFileName");
        String gameDisplayName = rs.getString("GameDisplay");
        String tags = rs.getString("TAGS");
        boolean scanned = tags != null && tags.contains("scanned");

        File wheelIconFile = new File(systemInfo.getPinUPSystemFolder() + "/POPMedia/Visual Pinball X/Wheel/", FilenameUtils.getBaseName(gameFileName) + ".png");
        File nvRamFolder = new File(systemInfo.getMameFolder(), "nvram");
        File nvRamFile = new File(nvRamFolder, rom + ".nv");
        File vpxFile = new File(systemInfo.getVPXTablesFolder(), gameFileName);
        if (!vpxFile.exists()) {
          LOG.warn("No vpx file " + vpxFile.getAbsolutePath() + " found, ignoring game.");
          continue;
        }

        info.setId(id);
        info.setRom(rom);
        info.setScanned(scanned);
        info.setGameFileName(gameFileName);
        info.setGameDisplayName(gameDisplayName);
        info.setWheelIconFile(wheelIconFile);
        info.setVpxFile(vpxFile);
        info.setNvRamFile(nvRamFile);
        info.setRomFile(new File(systemInfo.getMameRomFolder(), rom + ".zip"));

        results.add(info);
      }

      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read game infos: " + e.getMessage(), e);
    }
    return results;
  }

  public String getEmulatorStartupScript(String emuName) {
    String script = null;
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Emulators where EmuName = '" + emuName + "';");
      rs.next();
      script = rs.getString(LAUNCH_SCRIPT);
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read startup script or " + emuName + ": " + e.getMessage(), e);
    }
    return script;
  }

  public void resetAll() {
    List<GameInfo> games = this.getGames();
    for (GameInfo game : games) {
      resetGame(game);
    }
  }

  public void resetGame(GameInfo gameInfo) {
    String gameFileName = gameInfo.getGameFileName();
    try {
      Statement stmt = conn.createStatement();
      String sql = "UPDATE Games SET 'TAGS'='' WHERE GameID = " + gameInfo.getId() + ";";
      stmt.executeUpdate(sql);
      stmt.close();

      stmt = conn.createStatement();
      sql = "UPDATE Games SET 'ROM'='' WHERE GameID = " + gameInfo.getId() + ";";
      stmt.executeUpdate(sql);
      stmt.close();
    } catch (Exception e) {
      LOG.error("Failed to reset table info for " + gameFileName + ": " + e.getMessage(), e);
    }
  }

  public String getEmulatorExitScript(String emuName) {
    String script = null;
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Emulators where EmuName = '" + emuName + "';");
      rs.next();
      script = rs.getString(POST_SCRIPT);
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read exit script or " + emuName + ": " + e.getMessage(), e);
    }
    return script;
  }

  public String getRomName(String tableFileName) {
    String rom = null;
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Games where GameFileName = '" + tableFileName + "';");
      if (rs.next()) {
        rom = rs.getString(ROM);
      }
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read rom info for " + tableFileName + ": " + e.getMessage(), e);
    }
    return rom;
  }

  public void updateScript(String emuName, String scriptName, String content) {
    try {
      Statement stmt = conn.createStatement();
      String sql = "UPDATE Emulators SET '" + scriptName + "'='" + content + "' WHERE EmuName = '" + emuName + "';";
      stmt.executeUpdate(sql);
      stmt.close();
      LOG.info("Update of " + scriptName + " successful.");
    } catch (Exception e) {
      LOG.error("Failed to update script script " + scriptName + ": " + e.getMessage(), e);
    }
  }

  public void updateRomName(String gameFileName, String romName) {
    try {
      Statement stmt = conn.createStatement();
      String sql = "UPDATE Games SET 'ROM'='" + romName.trim() + "' WHERE GameFileName = '" + gameFileName.replaceAll("'", "''") + "';";
      int result = stmt.executeUpdate(sql);
      stmt.close();
      if (result > 0) {
        LOG.info("Update of " + gameFileName + " successful, written ROM name '" + romName + "'");
      }
      else {
        LOG.info("Skipped writing rom name '" + romName + "' to database, the game '" + gameFileName + "' was not found there.");
      }
    } catch (Exception e) {
      LOG.error("Failed to update script script " + gameFileName + ": " + e.getMessage(), e);
    }
  }

  public void markAsScanned(GameInfo gameInfo) {
    String gameFileName = gameInfo.getGameFileName();
    try {
      Statement stmt = conn.createStatement();
      String sql = "UPDATE Games SET 'TAGS'='scanned' WHERE GameID = " + gameInfo.getId() + ";";
      int result = stmt.executeUpdate(sql);
      stmt.close();
      if (result > 0) {
        LOG.info("Update of " + gameFileName + " successful, table marked as scanned.");
      }
      else {
        LOG.error("Failed to mark table " + gameFileName + " as marked.");
      }
    } catch (Exception e) {
      LOG.error("Failed to update table marker info for " + gameFileName + ": " + e.getMessage(), e);
    }
  }
}
