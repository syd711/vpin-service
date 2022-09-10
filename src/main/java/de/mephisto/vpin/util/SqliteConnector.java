package de.mephisto.vpin.util;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.VPinService;
import de.mephisto.vpin.popper.PinUPFunction;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SqliteConnector {
  private final static Logger LOG = LoggerFactory.getLogger(SqliteConnector.class);

  public static final String POST_SCRIPT = "PostScript";
  public static final String LAUNCH_SCRIPT = "LaunchScript";
  public static final String ROM = "ROM";
  private final String dbFilePath;

  private Connection conn;
  private final SystemInfo systemInfo;

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
    if (this.conn != null) {
      try {
        this.conn.close();
      } catch (SQLException e) {
        LOG.error("Error disconnecting from sqlite: " + e.getMessage());
      }
    }
  }

  public GameInfo getGame(VPinService service, int id) {
    this.connect();
    GameInfo info = null;
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Games where GameID = " + id + ";");
      while (rs.next()) {
        info = createGameInfo(service, rs);
      }

      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read game info: " + e.getMessage(), e);
    } finally {
      this.disconnect();
    }
    return info;
  }

  public GameInfo getGameByName(VPinService service, String table) {
    this.connect();
    GameInfo info = null;
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Games where GameDisplay = " + table + ";");
      while (rs.next()) {
        info = createGameInfo(service, rs);
      }

      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read game info: " + e.getMessage(), e);
    } finally {
      this.disconnect();
    }
    return info;
  }

  public PinUPFunction getFunction(String description) {
    PinUPFunction f = null;
    this.connect();
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM PinUPFunctions WHERE Descript = '" + description + "';");
      while (rs.next()) {
        f = new PinUPFunction();
        f.setActive(rs.getInt("Active") == 1);
        f.setDescription(rs.getString("Descript"));
        f.setCtrlKey(rs.getInt("CntrlCodes"));
        f.setId(rs.getInt("uniqueID"));
        break;
      }

      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to get function: " + e.getMessage(), e);
    } finally {
      this.disconnect();
    }
    return f;
  }

  public List<PinUPFunction> getFunctions() {
    this.connect();
    List<PinUPFunction> results = new ArrayList<>();
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM PinUPFunctions;");
      while (rs.next()) {
        PinUPFunction f = new PinUPFunction();
        f.setActive(rs.getInt("Active") == 1);
        f.setDescription(rs.getString("Descript"));
        f.setCtrlKey(rs.getInt("CntrlCodes"));
        f.setId(rs.getInt("uniqueID"));
        results.add(f);
      }

      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to functions: " + e.getMessage(), e);
    } finally {
      this.disconnect();
    }
    return results;
  }


  public List<GameInfo> getGames(VPinService service) {
    this.connect();
    List<GameInfo> results = new ArrayList<>();
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Games WHERE EMUID = 1;");
      while (rs.next()) {
        GameInfo info = createGameInfo(service, rs);
        if (info != null) {
          results.add(info);
        }
      }

      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read game info: " + e.getMessage(), e);
    } finally {
      this.disconnect();
    }

    results.sort(Comparator.comparing(GameInfo::getGameDisplayName));
    return results;
  }

  public List<Integer> getGameIdsFromPlaylists() {
    List<Integer> result = new ArrayList<>();
    connect();
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM PlayListDetails;");

      while (rs.next()) {
        int gameId = rs.getInt("GameID");
        result.add(gameId);
      }
    } catch (SQLException e) {
      LOG.error("Failed to read playlists: " + e.getMessage(), e);
    } finally {
      disconnect();
    }
    return result;
  }

  private void loadStats(GameInfo game) {
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM GamesStats where GameID = " + game.getId() + ";");
      while (rs.next()) {
        int numberPlays = rs.getInt("NumberPlays");
        Date lastPlayed = rs.getDate("LastPlayed");

        game.setLastPlayed(lastPlayed);
        game.setNumberPlays(numberPlays);
      }
    } catch (SQLException e) {
      LOG.error("Failed to read game info: " + e.getMessage(), e);
    }
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
    } finally {
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
    } finally {
      this.disconnect();
    }
  }

  private GameInfo createGameInfo(VPinService service, ResultSet rs) throws SQLException {
    GameInfo info = new GameInfo(service);
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
      return null;
    }

    info.setId(id);
    info.setRom(rom);
    info.setTags(tags);
    info.setGameFileName(gameFileName);
    info.setGameDisplayName(gameDisplayName);
    info.setWheelIconFile(wheelIconFile);
    info.setGameFile(vpxFile);
    info.setNvRamFile(nvRamFile);
    info.setRomFile(new File(systemInfo.getMameRomFolder(), rom + ".zip"));

    loadStats(info);
    return info;
  }
}
