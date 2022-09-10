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

  private final int unit;
  private final int output;
  private final int value;

  public DOFCommand(int unit, int output, int value) {
    this.unit = unit;
    this.output = output;
    this.value = value;
  }

  public DOFCommandResult execute() {
    File testerFile = new File(SystemInfo.RESOURCES + "DOFTest/", "DirectOutputTest.exe");
    List<String> commands = Arrays.asList(testerFile.getAbsolutePath(), String.valueOf(unit), String.valueOf(output), String.valueOf(value));
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
}
