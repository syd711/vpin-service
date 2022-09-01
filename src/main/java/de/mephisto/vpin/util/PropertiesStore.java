package de.mephisto.vpin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class PropertiesStore {
  private final static Logger LOG = LoggerFactory.getLogger(PropertiesStore.class);

  private final Properties properties = new Properties();

  private File propertiesFile;

  public static PropertiesStore create(File resources) {
    PropertiesStore store = new PropertiesStore();
    try {
      store.propertiesFile = new File(resources,"repository.properties");
      if(!resources.exists()) {
        resources.mkdirs();
        store.properties.store(new FileOutputStream(store.propertiesFile), null);
        LOG.info("Created " + store.propertiesFile.getAbsolutePath());
      }
      store.properties.load(new FileInputStream(store.propertiesFile));
    } catch (IOException e) {
     LOG.error("Failed to load data store: " + e.getMessage(), e);
    }
    return store;
  }

  public boolean containsKey(String key) {
    return this.properties.containsKey(key);
  }

  public String get(String key) {
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
