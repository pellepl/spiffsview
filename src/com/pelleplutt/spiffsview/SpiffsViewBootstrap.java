package com.pelleplutt.spiffsview;

import java.io.IOException;

import com.pelleplutt.spiffsview.ui.MainFrame;
import com.pelleplutt.util.Log;

public class SpiffsViewBootstrap {
  public static void main(String[] args) throws IOException {
    Log.log = true;
    System.setProperty("awt.useSystemAAFontSettings","on");
    System.setProperty("swing.aatext", "true");

    startGui();
    return;
  }
  
  static void startGui() {
    MainFrame.inst();
  }
}
