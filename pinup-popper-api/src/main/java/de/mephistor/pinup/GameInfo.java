package de.mephistor.pinup;

import java.io.File;

public class GameInfo {

  private String rom;
  private String gameDisplayName;
  private String gameFileName;
  private int id;

  private File vpxFile;
  private File romFile;

  public GameInfo() {
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
}
