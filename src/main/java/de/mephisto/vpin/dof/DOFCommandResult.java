package de.mephisto.vpin.dof;

public class DOFCommandResult {
  private String out;

  public DOFCommandResult(String out) {
    this.out = out;
  }

  public String getOutput() {
    return out;
  }

  public boolean isSuccessful() {
    return out != null && !out.contains("No such unit");
  }
}
