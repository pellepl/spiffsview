package com.pelleplutt.spiffsview.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextArea;

public class DataPanel extends JPanel {
  public JTextArea view = new JTextArea();
  static final int WRAP = 16;
  
  public DataPanel() {
    setLayout(new BorderLayout());
    add(view, BorderLayout.CENTER);
    view.setFont(MainFrame.DEFAULT_FONT);
  }
  
  public void setData(int offs, byte[] data) {
    view.setText("");
    StringBuilder sbHex = new StringBuilder(String.format("%08x: ", offs));
    StringBuilder sbAscii = new StringBuilder();
    for (int i = 0; i < data.length; i++) {
      if (i != 0 && (i % WRAP) == 0) {
        for (int j = 0; j < WRAP+5+1 - sbHex.length()/2; j++) {
          sbHex.append("  ");
        }
        view.append(sbHex.toString() + sbAscii.toString() + "\n");
        sbHex = new StringBuilder(String.format("%08x: ", offs + i));
        sbAscii = new StringBuilder();
      }
      int b = (int)data[i] & 0xff;
      sbHex.append(String.format("%02x", b));
      sbAscii.append(b < 0x20 ? '.' : (char)b);
    }
    for (int j = 0; j < WRAP+5+1 - sbHex.length()/2; j++) {
      sbHex.append("  ");
    }
    view.append(sbHex.toString() + sbAscii.toString() + "\n");
  }
}

