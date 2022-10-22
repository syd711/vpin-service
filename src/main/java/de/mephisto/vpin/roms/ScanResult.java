package de.mephisto.vpin.roms;

public class ScanResult {
  private int nvOffset = 0;
  private String rom;
  private String hsFileName;

  public String getHsFileName() {
    return hsFileName;
  }

  public void setHsFileName(String hsFileName) {
    this.hsFileName = hsFileName;
  }

  public int getNvOffset() {
    return nvOffset;
  }

  public void setNvOffset(int nvOffset) {
    this.nvOffset = nvOffset;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public boolean isScanComplete() {
    return this.nvOffset > 0 && this.rom != null && hsFileName != null;
  }
}
