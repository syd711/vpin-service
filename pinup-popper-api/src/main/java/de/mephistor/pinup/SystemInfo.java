package de.mephistor.pinup;

import de.mephistor.pinup.util.CommandResultParser;
import de.mephistor.pinup.util.WindowsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SystemInfo {
  private final static Logger LOG = LoggerFactory.getLogger(SystemInfo.class);

  private final static String REG_KEY = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\Applications\\VPinballX.exe\\shell\\open\\command";
  private final static String POPPER_REG_KEY = "HKEY_LOCAL_MACHINE\\SYSTEM\\ControlSet001\\Control\\Session Manager\\Environment";

  private File popperInstallationFolder;

  public SystemInfo() {
    this.popperInstallationFolder = this.getPopperInstallationFolder();
  }

  public File getMameRomFolder() {
    return new File(getVPXInstallationFolder(), "VPinMAME/roms/");
  }

  public File[] getVPXTables() {
    File vpxInstallationFolder = this.getVPXInstallationFolder();
    File folder = new File(vpxInstallationFolder, "Tables/");
    return folder.listFiles((dir, name) -> name.endsWith(".vpx"));
  }

  public File getVPXTablesFolder() {
    return new File(getVPXInstallationFolder(), "Tables/");
  }

  File getVPXInstallationFolder() {
    return new File(popperInstallationFolder, "VisualPinball");
  }

  public File getPinUPSystemFolder() {
    return new File(popperInstallationFolder, "PinUPSystem");
  }

  File getPopperInstallationFolder() {
    try {
      String output = WindowsRegistry.readRegistry(POPPER_REG_KEY, "PopperInstDir");
      if (output != null && output.trim().length() > 0) {
        String path = CommandResultParser.extractRegistryValue(output);
        File folder = new File(path);
        if (folder.exists()) {
          return folder;
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to read installation folder: " + e.getMessage(), e);
    }
    System.exit(-1);
    return null;
  }
}
