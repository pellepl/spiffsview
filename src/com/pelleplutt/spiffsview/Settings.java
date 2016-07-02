package com.pelleplutt.spiffsview;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.pelleplutt.util.Log;

public class Settings {
  private static Settings _inst;
  
  public static final int MAX_LIST_ENTRIES = 9;
  
  Properties props = new Properties();
  File settingsFile = new File(System.getProperty("user.home") + File.separator + 
      Essential.userSettingPath + File.separator + Essential.settingsFile);
  File settingsPath = settingsFile.getParentFile();
  
  public static Settings inst() {
    if (_inst == null) {
      _inst = new Settings();
    }
    return _inst;
  }
  
  void defaultSettings() {
//    props.setProperty(ELF_FILE+".0", "");
//    props.setProperty(ADDR2LINE_PATH, "addr2line");
  }
  
  public void saveSettings() {
    try {
      if (!settingsFile.exists()) settingsFile.createNewFile();
      FileWriter fw = new FileWriter(settingsFile); 
      props.store(fw, Essential.name + " v" + Essential.vMaj + "." + Essential.vMin + "." + Essential.vMic);
      fw.close();
    } catch (IOException ioe) {
      Log.printStackTrace(ioe);
    }
  }
  
  void loadSettings() {
    if (!settingsFile.exists()) {
      settingsPath.mkdirs();
      defaultSettings();
      saveSettings();
    } else {
      try {
        props.load(new FileReader(settingsFile));
      } catch (IOException e) {
        Log.printStackTrace(e);
      }
    }
  }
  
  private Settings() {
    loadSettings();
  }
  
  public String string(String s) {
    if (s.endsWith(".list")) s += ".0";
    String ss = props.getProperty(s);
    return ss == null ? "" : props.getProperty(s);
  }
  
  public void setString(String key, String s) {
    Log.println(key+"="+s);
    props.setProperty(key, s);
  }
  
  public int integer(String s) {
    return Integer.parseInt(props.getProperty(s));
  }
  
  public void setInt(String key, int i) {
    Log.println(key+"="+i);
    props.setProperty(key, new Integer(i).toString());
  }
  
  public void listAdd(String key, String s) {
    if (s.equals(props.getProperty(key + ".0"))) return;
    
    for (int i = 0; i <= MAX_LIST_ENTRIES; i++) {
      if (s.equals(props.getProperty(key + "." + Integer.toString(i)))) {
        for (int j = i; j > 0; j--) {
          String preVal = props.getProperty(key + "." + Integer.toString(j-1));
          props.setProperty(key + "." + Integer.toString(j), preVal);
        }
        props.setProperty(key + "." + 0, s);
        return;
      }
    }
    
    int ix = MAX_LIST_ENTRIES;
    String ixVal;
    while (ix > 0) {
      ixVal = props.getProperty(key + "." + Integer.toString(ix-1));
      if (ixVal != null) {
        props.setProperty(key + "." + Integer.toString(ix), ixVal);
      }
      ix--;
    }
    props.setProperty(key + "." + 0, s);
  }
  
  public String[] list(String key) {
    int ix = 0;
    List<String> vals = new ArrayList<String>();
    String ixVal;
    while (ix <= MAX_LIST_ENTRIES && (ixVal = props.getProperty(key + "." + Integer.toString(ix))) != null) {
      vals.add(ixVal);
      ix++;
    }
    
    return vals.size() == 0 ? new String[0] : vals.toArray(new String[vals.size()]);
  }
}
