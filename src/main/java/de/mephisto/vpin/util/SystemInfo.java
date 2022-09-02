package de.mephisto.vpin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class SystemInfo {
  private final static Logger LOG = LoggerFactory.getLogger(SystemInfo.class);

  private final static String REG_KEY = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\Applications\\VPinballX.exe\\shell\\open\\command";
  private final static String POPPER_REG_KEY = "HKEY_LOCAL_MACHINE\\SYSTEM\\ControlSet001\\Control\\Session Manager\\Environment";

  private File popperInstallationFolder;

  private static SystemInfo instance;

  private SystemInfo() {
    this.popperInstallationFolder = this.getPopperInstallationFolder();
  }

  public static SystemInfo getInstance() {
    if(instance == null) {
      instance = new SystemInfo();
    }
    return instance;
  }

  public File getVPRegFile() {
    return new File(this.getVPXInstallationFolder() + "/User/", "VPReg.stg");
  }

  public File getMameRomFolder() {
    return new File(getVPXInstallationFolder(), "VPinMAME/roms/");
  }

  public File getMameFolder() {
    return new File(getVPXInstallationFolder(), "VPinMAME/");
  }

  public File[] getVPXTables() {
    File vpxInstallationFolder = this.getVPXInstallationFolder();
    File folder = new File(vpxInstallationFolder, "Tables/");
    return folder.listFiles((dir, name) -> name.endsWith(".vpx"));
  }

  public String get7ZipCommand() {
    return new File("./resources", "7z.exe").getAbsolutePath();
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
      String output = readRegistry(POPPER_REG_KEY, "PopperInstDir");
      if (output != null && output.trim().length() > 0) {
        String path = extractRegistryValue(output);
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

  static String extractStandardKeyValue(String output) throws Exception {
    String result = output;
    result = result.replace("\n", "").replace("\r", "").trim();

    String[] value = result.split("    ");
    String[] values = value[3].split(" ");
    String path = values[0];
    return path.replaceAll("\"", "");
  }

  static String extractRegistryValue(String output) throws Exception {
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
      String output = reader.getResult();
      return output;
    } catch (Exception e) {
      LOG.error("Failed to read registry key " + location);
      return null;
    }
  }

  static class StreamReader extends Thread {
    private InputStream is;
    private StringWriter sw = new StringWriter();

    public StreamReader(InputStream is) {
      this.is = is;
    }

    public void run() {
      try {
        int c;
        while ((c = is.read()) != -1)
          sw.write(c);
      } catch (IOException e) {
      }
    }

    public String getResult() {
      return sw.toString();
    }
  }
}
