package com.pelleplutt.spiffsview.ui;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.pelleplutt.spiffsview.Settings;
import com.pelleplutt.util.Log;

public class SettingTextField extends JTextField implements DocumentListener {
  private static final long serialVersionUID = -123241771410552213L;
  String setting;

  public SettingTextField(String setting) {
    this.setting = setting;
    setText(Settings.inst().string(setting));
    getDocument().addDocumentListener(this);
  }
  
  void update() {
    String text = getText();
    if (setting.endsWith(".string")) {
      Settings.inst().setString(setting, text);
      Log.println(setting + " = " + text);
    } else if (setting.endsWith(".int")) {
      try {
        int i = Integer.parseInt(text);
        Settings.inst().setInt(setting, i);
        Log.println(setting + " = " + text);
      } catch (Throwable t) {}
    }
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    update();
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    update();
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
    update();
  }

}
