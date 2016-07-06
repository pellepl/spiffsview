package com.pelleplutt.spiffsview;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import com.pelleplutt.spiffsview.ui.MainFrame;
import com.pelleplutt.util.Log;

public class SpiffsViewBootstrap {
  public static void main(String[] args) throws IOException {
    Log.log = true;
    System.setProperty("awt.useSystemAAFontSettings","on");
    System.setProperty("swing.aatext", "true");

    
    {
      Spiffs.cfg = new SpiffsConfig();

      Spiffs.cfg.bigEndian = false;
      Spiffs.cfg.physOffset = 0;//4*1024*1024;
      Spiffs.cfg.physBlockSize = 4096;
      Spiffs.cfg.logBlockSize = 4096;
      Spiffs.cfg.logPageSize = 256;
      Spiffs.cfg.fileNameSize = 32;
      Spiffs.cfg.sizeObjId = 4;
      Spiffs.cfg.sizePageIx = 4;
      Spiffs.cfg.sizeSpanIx = 4;
      FileInputStream fart = null;
      try {
        //fart = new FileInputStream("/home/petera/proj/generic/spiffs/imgs/90.hidden_file.spiffs");
        fart = new FileInputStream("/home/petera/proj/generic/spiffs/imgs/93.dump.bin");
        //fart = new FileInputStream("/home/petera/poo/spiffs/fsdump.bin");
        //fart = new FileInputStream("/home/petera/poo/spiffs/93.clean.img");
        FileChannel fc = fart.getChannel();
        Spiffs.cfg.physSize = fc.size();
        System.out.println("file size = " + Spiffs.cfg.physSize/1024 + "k");
        Spiffs.data = fc.map(FileChannel.MapMode.READ_ONLY, Spiffs.cfg.physOffset, Spiffs.cfg.physSize - Spiffs.cfg.physOffset);
        Spiffs.data.order(Spiffs.cfg.bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
//        SpiffsPage p;
//        for (int i = 0; i < 256; i++) {
//          p = SpiffsPage.loadPage(i);
//          System.out.println(p);
//        }
        SpiffsPage.populate();
        Analyzer.analyze();
      } finally {
//        AppSystem.closeSilently(fart);
      }
    }
    
    
    
    startGui();
    return;
  }
  
  static void startGui() {
    MainFrame.inst();
  }
}
