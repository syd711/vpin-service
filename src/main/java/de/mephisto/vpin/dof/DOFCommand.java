package de.mephisto.vpin.dof;

import de.mephisto.vpin.util.SystemCommandExecutor;
import de.mephisto.vpin.util.SystemInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class DOFCommand {
  private final static Logger LOG = LoggerFactory.getLogger(DOFCommand.class);

  private int id;
  private int unit;
  private int portNumber;
  private int value;
  private int durationMs;
  private Trigger trigger;
  private String keyBinding;
  private boolean toggle;
  private String description;

  public DOFCommand(int id, int unit, int portNumber, int value, int durationMs, Trigger trigger, String keyBinding, boolean toggle, String description) {
    this.id = id;
    this.unit = unit;
    this.portNumber = portNumber;
    this.value = value;
    this.durationMs = durationMs;
    this.trigger = trigger;
    this.keyBinding = keyBinding;
    this.toggle = toggle;
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setUnit(int unit) {
    this.unit = unit;
  }

  public void setPortNumber(int portNumber) {
    this.portNumber = portNumber;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public void setDurationMs(int durationMs) {
    this.durationMs = durationMs;
  }

  public void setTrigger(Trigger trigger) {
    this.trigger = trigger;
  }

  public void setKeyBinding(String keyBinding) {
    this.keyBinding = keyBinding;
  }

  public void setToggle(boolean toggle) {
    this.toggle = toggle;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getId() {
    return id;
  }

  public int getUnit() {
    return unit;
  }

  public int getPortNumber() {
    return portNumber;
  }

  public int getValue() {
    return value;
  }

  public int getDurationMs() {
    return durationMs;
  }

  public Trigger getTrigger() {
    return trigger;
  }

  public String getKeyBinding() {
    return keyBinding;
  }

  public boolean isToggle() {
    return toggle;
  }

  public DOFCommandResult execute() {
    File testerFile = new File(SystemInfo.RESOURCES + "DOFTest/", "DirectOutputTest.exe");
    List<String> commands = Arrays.asList(testerFile.getAbsolutePath(), String.valueOf(unit), String.valueOf(portNumber), String.valueOf(value));
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(commands, false);
      executor.setDir(testerFile.getParentFile());
      executor.executeCommand();

      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("DOF command '" + String.join(" ", commands) + "' failed: {}", standardErrorFromCommand);
      }
      return new DOFCommandResult(standardOutputFromCommand.toString());
    } catch (Exception e) {
      LOG.info("Failed execute DOF command: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof DOFCommand) {
      return this.id == ((DOFCommand)obj).getId();
    }
    return false;
  }
}
