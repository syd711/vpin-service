package de.mephisto.vpin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class PropertiesStore {
  private final static Logger LOG = LoggerFactory.getLogger(PropertiesStore.class);

  protected final Properties properties = new Properties();

  private File propertiesFile;

  public static PropertiesStore create(String name) {
    PropertiesStore store = new PropertiesStore();
    try {
      File folder = new File(SystemInfo.RESOURCES);
      store.propertiesFile = new File(SystemInfo.RESOURCES,name);
      if(!folder.exists()) {
        folder.mkdirs();
      }

      if(!store.propertiesFile.exists()) {
        FileOutputStream fileOutputStream = new FileOutputStream(store.propertiesFile);
        store.properties.store(fileOutputStream, null);
        LOG.info("Created " + store.propertiesFile.getAbsolutePath());
        fileOutputStream.close();
      }

      FileInputStream fileInputStream = new FileInputStream(store.propertiesFile);
      store.properties.load(fileInputStream);
      fileInputStream.close();
    } catch (Exception e) {
     LOG.error("Failed to load data store: " + e.getMessage(), e);
    }
    return store;
  }

  public boolean containsKey(String key) {
    return this.properties.containsKey(key);
  }

  public int getInt(String key, int defaultValue) {
    if(properties.containsKey(key)) {
      String value = properties.getProperty(key).trim();
      if(value.length() > 0) {
        return Integer.parseInt(value);
      }
    }
    else {
      set(key, defaultValue);
    }
    return defaultValue;
  }

  public boolean getBoolean(String key) {
    if(properties.containsKey(key)) {
      String value = properties.getProperty(key).trim();
      if(value.length() > 0) {
        return Boolean.parseBoolean(value);
      }
    }
    return false;
  }


  public int getInt(String key) {
    if(properties.containsKey(key)) {
      String value = properties.getProperty(key).trim();
      if(value.length() > 0) {
        return Integer.parseInt(value);
      }
    }
    return -1;
  }

  public float getFloat(String key) {
    if(properties.containsKey(key)) {
      String value = properties.getProperty(key).trim();
      if(value.length() > 0){
        return Float.parseFloat(value);
      }
    }
    return -1f;
  }

  public String get(String key) {
    return properties.getProperty(key);
  }

  public String getString(String key) {
    return properties.getProperty(key);
  }

  public String getString(String key, String defaultValue) {
    if(properties.containsKey(key)) {
      return properties.getProperty(key);
    }
    else {
      set(key, defaultValue);
    }
    return defaultValue;
  }

  public void set(String key, int value) {
    this.set(key, String.valueOf(value));
  }

  public void set(String key, String value) {
    properties.setProperty(key ,value);
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(propertiesFile);
      properties.store(fileOutputStream, null);
      fileOutputStream.close();
    } catch (Exception e) {
      LOG.error("Failed to store data store: " + e.getMessage(), e);
    }
  }
}
