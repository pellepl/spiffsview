package com.pelleplutt.spiffsview;

public class SpiffsFile {
  SpiffsPage indexHeaderPage;
  public SpiffsPage[] indexPages;
  public SpiffsPage[] dataPages;
  
  public SpiffsFile(SpiffsPage p) {
    indexHeaderPage = p;
  }

  public static int getNbrOfDataPages(SpiffsPage p) {
    int dataPages = 0;
    if (p.getSize() >= 0) {
      dataPages = (int)Math.ceil((double)p.getSize() / (double)Spiffs.dataPageSize());
    }
    return dataPages;
  }

  public static int getNbrOfIndexPages(SpiffsPage p) {
    int ixPages = 1; // always at least one index page
    long sz = p.getSize(); 
    if (sz >= 0) {
      if (sz > Spiffs.dataPageSize() * Spiffs.objectHeaderIndexLength()) {
        // file size longer than what the object index header page can reference,
        // add the leftover index pages
        sz -= Spiffs.dataPageSize() * Spiffs.objectHeaderIndexLength();
        ixPages += Math.ceil((double)sz / (double)(Spiffs.dataPageSize() * Spiffs.objectIndexLength()));
      }
    }
    return ixPages;
  }

  public int getNbrOfDataPages() {
    return getNbrOfDataPages(indexHeaderPage);
  }

  public int getNbrOfIndexPages() {
    return getNbrOfIndexPages(indexHeaderPage);
  }


}
