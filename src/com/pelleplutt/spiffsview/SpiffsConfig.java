package com.pelleplutt.spiffsview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

public class SpiffsConfig implements Cloneable {
  public long physOffset;
  public long physSize;
  public int logPageSize;
  public int logBlockSize;
  public int fileNameSize;
  public boolean bigEndian;
  public int sizeObjId;
  public int sizePageIx;
  public int sizeSpanIx;
  public boolean magic;
  public boolean magicLength;
  public boolean alignObjectIndexTables;
  
  @Override
  public SpiffsConfig clone() {
    try {
      return (SpiffsConfig)super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public static SpiffsConfig load(File f) throws FileNotFoundException, IOException, IllegalArgumentException, IllegalAccessException {
    Properties p = new Properties();
    p.load(new FileReader(f));
    SpiffsConfig c = new SpiffsConfig();
    Field[] fields = SpiffsConfig.class.getFields();
    for (Field field : fields) {
      if (field.getType() == long.class) {
        field.set(c, Long.parseLong(p.getProperty(field.getName())));
      } else if (field.getType() == int.class) {
        field.set(c, Integer.parseInt(p.getProperty(field.getName())));
      } else if (field.getType() == boolean.class) {
        field.set(c, Boolean.parseBoolean(p.getProperty(field.getName())));
      }
    }
    
    return c;
  }
  
  public static void store(SpiffsConfig c, File f) throws IOException, IllegalArgumentException, IllegalAccessException {
    Properties p = new Properties();
    Field[] fields = SpiffsConfig.class.getFields();
    for (Field field : fields) {
      if (field.getType() == long.class) {
        p.setProperty(field.getName(), Long.toString(field.getLong(c)));
      } else if (field.getType() == int.class) {
        p.setProperty(field.getName(), Integer.toString(field.getInt(c)));
      } else if (field.getType() == boolean.class) {
        p.setProperty(field.getName(), Boolean.toString(field.getBoolean(c)));
      }
    }
    p.store(new FileWriter(f), "spiffsviewer config for " + f.getAbsolutePath());
  }
}
