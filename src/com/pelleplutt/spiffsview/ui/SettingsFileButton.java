package com.pelleplutt.spiffsview.ui;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import com.pelleplutt.spiffsview.Settings;
import com.pelleplutt.util.UIUtil;

public class SettingsFileButton extends JButton {
  private static final long serialVersionUID = 35976819343218965L;

  public SettingsFileButton(String text, final JComponent c, final String setting, final String fileChooserTitle,
      final boolean files, final boolean dirs) {
    super(text);
    this.setAction(new AbstractAction() {
      private static final long serialVersionUID = -6259598044237734581L;

      @Override
      public void actionPerformed(ActionEvent e) {
        System.setProperty(UIUtil.PROP_DEFUALT_PATH, Settings.inst().string(setting));
        File f = UIUtil.selectFile(MainFrame.inst(), fileChooserTitle, "Select", files, dirs);
        if (f != null) {
          if (setting.endsWith(".list")) {
            @SuppressWarnings("unchecked")
            JComboBox<String> cb = (JComboBox<String>)c;
            Settings.inst().listAdd(setting, f.getAbsolutePath());
            cb.removeAllItems();
            String[] items = Settings.inst().list(setting);
            for (String item : items) {
              cb.addItem(item);
            }
          } else {
            Settings.inst().setString(setting, f.getAbsolutePath());
          }
          if (c instanceof JComboBox) {
            ((JComboBox<?>)c).getEditor().setItem(f.getAbsolutePath());
          }
          else if (c instanceof JTextField) {
            ((JTextField)c).setText(f.getAbsolutePath());
          }
        }
      }
    });
    setText(text);
  }
}
