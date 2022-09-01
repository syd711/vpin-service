package de.mephisto.vpin.util;

import de.mephisto.vpin.games.GameInfo;
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
  private final String dbFilePath;

  private Connection conn;
  private SystemInfo systemInfo;

  public SqliteConnector() {
    this.systemInfo = SystemInfo.getInstance();
    File dbFile = new File(systemInfo.getPinUPSystemFolder(), "PUPDatabase.db");
    if (!dbFile.exists()) {
      throw new IllegalArgumentException("Wrong PUPDatabase.db folder: " + dbFile.getAbsolutePath());
    }
    dbFilePath = dbFile.getAbsolutePath().replaceAll("\\\\", "/");
  }

  /**
   * Connect to a database
   */
  private void connect() {
    try {
      String url = "jdbc:sqlite:" + dbFilePath;
      conn = DriverManager.getConnection(url);
    } catch (SQLException e) {
      LOG.error("Failed to connect to sqlite: " + e.getMessage(), e);
    }
  }

  private void disconnect() {
    if(this.conn != null) {
      try {
        this.conn.close();
      } catch (SQLException e) {
        LOG.error("Error disconnecting from sqlite: " + e.getMessage());
      }
    }
  }

  public List<GameInfo> getGames() {
    this.connect();
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
        info.setTags(tags);
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
    finally {
     this.disconnect();
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
    this.connect();
    String gameFileName = gameInfo.getGameFileName();
    try {
      Statement stmt = conn.createStatement();
      String sql = "UPDATE Games SET 'ROM'='' WHERE GameID = " + gameInfo.getId() + ";";
      stmt.executeUpdate(sql);
      stmt.close();
    } catch (Exception e) {
      LOG.error("Failed to reset table info for " + gameFileName + ": " + e.getMessage(), e);
    }
    finally {
      this.disconnect();
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
    this.connect();
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
    finally {
      this.disconnect();
    }
    return rom;
  }

  public void updateScript(String emuName, String scriptName, String content) {
    this.connect();
    try {
      Statement stmt = conn.createStatement();
      String sql = "UPDATE Emulators SET '" + scriptName + "'='" + content + "' WHERE EmuName = '" + emuName + "';";
      stmt.executeUpdate(sql);
      stmt.close();
      LOG.info("Update of " + scriptName + " successful.");
    } catch (Exception e) {
      LOG.error("Failed to update script script " + scriptName + ": " + e.getMessage(), e);
    }
    finally {
      this.disconnect();
    }
  }

  public void updateRomName(String gameFileName, String romName) {
    this.connect();
    try {
      Statement stmt = conn.createStatement();
      String sql = "UPDATE Games SET 'ROM'='" + romName.trim() + "' WHERE GameFileName = '" + gameFileName.replaceAll("'", "''") + "';";
      int result = stmt.executeUpdate(sql);
      stmt.close();
      if (result > 0) {
      }
      else {
        LOG.info("Skipped writing rom name '" + romName + "' to database, the game '" + gameFileName + "' was not found there.");
      }
    } catch (Exception e) {
      LOG.error("Failed to update script script " + gameFileName + ": " + e.getMessage(), e);
    }
    finally {
      this.disconnect();
    }
  }
}
