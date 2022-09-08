package de.mephisto.vpin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class PropertiesStore {
  private final static Logger LOG = LoggerFactory.getLogger(PropertiesStore.class);

  private final Properties properties = new Properties();

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
        store.properties.store(new FileOutputStream(store.propertiesFile), null);
        LOG.info("Created " + store.propertiesFile.getAbsolutePath());
      }

      store.properties.load(new FileInputStream(store.propertiesFile));
    } catch (Exception e) {
     LOG.error("Failed to load data store: " + e.getMessage(), e);
    }
    return store;
  }

  public boolean containsKey(String key) {
    return this.properties.containsKey(key);
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

  public void set(String key, String value) {
    properties.setProperty(key ,value);
    try {
      properties.store(new FileOutputStream(propertiesFile), null);
    } catch (Exception e) {
      LOG.error("Failed to store data store: " + e.getMessage(), e);
    }
  }
}
