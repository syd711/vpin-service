package de.mephisto.vpin.dof;

import de.mephisto.vpin.util.SystemCommandExecutor;
import de.mephisto.vpin.util.SystemInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DOFCommandExecutor {
  private final static Logger LOG = LoggerFactory.getLogger(DOFCommandExecutor.class);

  private final static String NO_LED_WIZ = "LedWiz units detected: none";
  private final static String NO_PINSCAPE = "Pinscape units detected: none";

  public DOFCommandExecutor() {
  }

  private static File getTesterExe() {
    return new File(SystemInfo.RESOURCES + "DOFTest/", "DirectOutputTest.exe");
  }

  public static void execute(DOFCommand command) {
    new Thread(() -> {
      try {
        Thread.currentThread().setName("DOF Command Thread " + command.getId());

        switch (command.getTrigger()) {
          case TableStart:
          case TableExit: {
            executeCmd(command, false);
            if (command.getDurationMs() > 0) {
              Thread.sleep(command.getDurationMs());
              executeCmd(command, true);
            }
            break;
          }
          case SystemStart: {
            //already executed
            break;
          }
          case KeyEvent: {
            executeCmd(command, command.isToggled());
            if(command.isToggle()) {
              command.setToggled(!command.isToggled());
            }
            break;
          }
        }
      } catch (InterruptedException e) {
        LOG.error("Failed to execute DOF command thread: " + e.getMessage(), e);
      }
    }).start();
  }

  private static DOFCommandResult executeCmd(DOFCommand command, boolean invertValue) {
    int value = command.getValue();
    if (invertValue && value == 0) {
      value = 255;
    }
    else if (invertValue && value == 255) {
      value = 0;
    }

    List<String> commands = Arrays.asList(getTesterExe().getAbsolutePath(), String.valueOf(command.getUnit()), String.valueOf(command.getPortNumber()), String.valueOf(value));
    LOG.error("Executing DOF command '" + String.join(" ", commands) + "'.");
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(commands, false);
      executor.setDir(getTesterExe().getParentFile());
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

  public static List<Unit> scanUnits() {
    List<String> commands = Arrays.asList(getTesterExe().getAbsolutePath());
    List<Unit> units = new ArrayList<>();
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(commands, false);
      executor.setDir(getTesterExe().getParentFile());
      executor.executeCommand();

      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("DOF command '" + String.join(" ", commands) + "' failed: {}", standardErrorFromCommand);
      }
      String output = standardOutputFromCommand.toString();
      if (!output.contains(NO_LED_WIZ) || !output.contains(NO_PINSCAPE)) {
        LOG.info("DOFManager initialized, found...."); //TODO
      }
    } catch (Exception e) {
      LOG.info("Failed execute DOF command: " + e.getMessage(), e);
    }
    return units;
  }

}
