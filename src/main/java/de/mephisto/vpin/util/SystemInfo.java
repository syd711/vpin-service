package de.mephisto.vpin.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class SystemInfo {
  private final static Logger LOG = LoggerFactory.getLogger(SystemInfo.class);

  private final static String POPPER_REG_KEY = "HKEY_LOCAL_MACHINE\\SYSTEM\\ControlSet001\\Control\\Session Manager\\Environment";
  private final static String VPREG_STG = "VPReg.stg";

  private final static String PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR = "pinupSystem.installationDir";
  private final static String VISUAL_PINBALL_INST_DIR = "visualPinball.installationDir";

  public static final String RESOURCES = "./resources/";

  private File pinUPSystenInstallationFolder;
  private File visualPinballInstallationFolder;

  private static SystemInfo instance;

  private SystemInfo() {
    PropertiesStore store = PropertiesStore.create("env");

    this.pinUPSystenInstallationFolder = this.getPinUPSystemInstallationFolder();
    if(!store.containsKey(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR)) {
      store.set(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR, pinUPSystenInstallationFolder.getAbsolutePath().replaceAll("\\\\", "/"));
    }
    else {
      this.pinUPSystenInstallationFolder = new File(store.get(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR));
    }

    this.visualPinballInstallationFolder = this.resolveVisualPinballInstallationFolder();
    if(!store.containsKey(VISUAL_PINBALL_INST_DIR)) {
      store.set(VISUAL_PINBALL_INST_DIR, visualPinballInstallationFolder.getAbsolutePath().replaceAll("\\\\", "/"));
    }
    else {
      this.visualPinballInstallationFolder = new File(store.get(VISUAL_PINBALL_INST_DIR));
    }
  }

  public static SystemInfo getInstance() {
    if(instance == null) {
      instance = new SystemInfo();
    }
    return instance;
  }

  @SuppressWarnings("unused")
  public Dimension getScreenSize() {
    return Toolkit.getDefaultToolkit().getScreenSize();
  }

  public File getVPRegFile() {
    return new File(this.getVisualPinballInstallationFolder() + "/User/", VPREG_STG);
  }

  public File getMameRomFolder() {
    return new File(getVisualPinballInstallationFolder(), "VPinMAME/roms/");
  }

  @SuppressWarnings("unused")
  public File getNvramFolder() {
    return new File(getMameFolder(), "nvram/");
  }

  public File getMameFolder() {
    return new File(getVisualPinballInstallationFolder(), "VPinMAME/");
  }

  public File getExtractedVPRegFolder() {
    return new File("./", "VPReg");
  }

  public File[] getVPXTables() {
    File vpxInstallationFolder = this.getVisualPinballInstallationFolder();
    File folder = new File(vpxInstallationFolder, "Tables/");
    return folder.listFiles((dir, name) -> name.endsWith(".vpx"));
  }

  public File getVisualPinballInstallationFolder() {
    return visualPinballInstallationFolder;
  }

  public String get7ZipCommand() {
    return new File(SystemInfo.RESOURCES, "7z.exe").getAbsolutePath();
  }

  public File getVPXTablesFolder() {
    return new File(getVisualPinballInstallationFolder(), "Tables/");
  }

  File resolveVisualPinballInstallationFolder() {
    return new File(pinUPSystenInstallationFolder.getParent(), "VisualPinball");
  }

  public File getPinUPSystemFolder() {
    return pinUPSystenInstallationFolder;
  }

  public File getPinUPMediaFolder() {
    return new File(getPinUPSystemFolder(), "POPMedia");
  }

  /**
   * Checks to see if a specific port is available.
   *
   * @param port the port to check for availability
   */
  public static boolean isAvailable(int port) {
    ServerSocket ss = null;
    DatagramSocket ds = null;
    try {
      ss = new ServerSocket(port);
      ss.setReuseAddress(true);
      ds = new DatagramSocket(port);
      ds.setReuseAddress(true);
      return true;
    } catch (IOException e) {
    } finally {
      if (ds != null) {
        ds.close();
      }

      if (ss != null) {
        try {
          ss.close();
        } catch (IOException e) {
          /* should not be thrown */
        }
      }
    }

    return false;
  }

  File getPinUPSystemInstallationFolder() {
    try {
      String popperInstDir = System.getenv("PopperInstDir");
      if(!StringUtils.isEmpty(popperInstDir)) {
        return new File(popperInstDir, "PinUPSystem");
      }

      String output = readRegistry(POPPER_REG_KEY, "PopperInstDir");
      if (output != null && output.trim().length() > 0) {
        String path = extractRegistryValue(output);
        File folder = new File(path, "PinUPSystem");
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

  static String extractRegistryValue(String output) {
    String result = output;
    result = result.replace("\n", "").replace("\r", "").trim();

    String[] s = result.split("    ");
    return s[3];
  }

  static final String readRegistry(String location, String key) {
    try {
      // Run reg query, then read output with StreamReader (internal class)
      String cmd = "reg query " + '"' + location;
      if (key != null) {
        cmd = "reg query " + '"' + location + "\" /v " + key;
      }
      Process process = Runtime.getRuntime().exec(cmd);
      StreamReader reader = new StreamReader(process.getInputStream());
      reader.start();
      process.waitFor();
      reader.join();
      return reader.getResult();
    } catch (Exception e) {
      LOG.error("Failed to read registry key " + location);
      return null;
    }
  }

  static class StreamReader extends Thread {
    private InputStream is = null;
    private final StringWriter sw = new StringWriter();

    public StreamReader(InputStream is) {
      this.is = is;
    }

    public void run() {
      try {
        int c;
        while ((c = is.read()) != -1)
          sw.write(c);
      } catch (IOException e) {
        LOG.error("Failed to execute stream reader: " + e.getMessage(), e);
      }
    }

    public String getResult() {
      return sw.toString();
    }
  }
}
