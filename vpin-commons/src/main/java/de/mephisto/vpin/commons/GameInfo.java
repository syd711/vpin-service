package de.mephisto.vpin.commons;

import java.io.File;
import java.util.Date;

public class GameInfo {

  private String rom;
  private String gameDisplayName;
  private String gameFileName;
  private int id;

  private File vpxFile;
  private File romFile;
  private File nvRamFile;
  private File wheelIconFile;

  private long lastModified;

  private String gameStatus;

  private boolean scanned;

  public GameInfo() {
  }

  public boolean isScanned() {
    return scanned;
  }

  public void setScanned(boolean scanned) {
    this.scanned = scanned;
  }

  public long getLastModified() {
    return lastModified;
  }

  public void setLastModified(long lastModified) {
    this.lastModified = lastModified;
  }

  public File getWheelIconFile() {
    return wheelIconFile;
  }

  public void setWheelIconFile(File wheelIconFile) {
    this.wheelIconFile = wheelIconFile;
  }

  public File getNvRamFile() {
    return nvRamFile;
  }

  public void setNvRamFile(File nvRamFile) {
    this.nvRamFile = nvRamFile;
  }

  public File getVpxFile() {
    return vpxFile;
  }

  public void setVpxFile(File vpxFile) {
    this.vpxFile = vpxFile;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public String getGameDisplayName() {
    return gameDisplayName;
  }

  public void setGameDisplayName(String gameDisplayName) {
    this.gameDisplayName = gameDisplayName;
  }

  public String getGameFileName() {
    return gameFileName;
  }

  public void setGameFileName(String gameFileName) {
    this.gameFileName = gameFileName;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public File getRomFile() {
    return romFile;
  }

  public void setRomFile(File romFile) {
    this.romFile = romFile;
  }

  public String getGameStatus() {
    return gameStatus;
  }

  public void setGameStatus(String gameStatus) {
    this.gameStatus = gameStatus;
  }

  @Override
  public String toString() {
    return this.getGameDisplayName();
  }
}
