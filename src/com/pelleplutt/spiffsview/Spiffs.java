package com.pelleplutt.spiffsview;

import java.nio.ByteBuffer;

public class Spiffs {
  static SpiffsConfig cfg;
  static ByteBuffer data;
  
  public static long nbrOfPages() {
    return (int)((cfg.physSize - cfg.physOffset) / cfg.logPageSize);
  }
  
  public static long pagesPerBlock() {
    return cfg.logBlockSize / cfg.logPageSize;
  }
  
  public static long nbrOfLookupPages() {
    return Math.max(1, pagesPerBlock() * cfg.sizeObjId / cfg.logPageSize);
  }
  
  public static boolean isLookupPage(long pix) {
    return pix % pagesPerBlock() < nbrOfLookupPages(); 
  }
  
  public static long lookupMaxEntries() {
    return pagesPerBlock() - nbrOfLookupPages();
  }

  public static long lookupPageIndexInBlock(long pix) {
    return pix % pagesPerBlock();
  }
  
  public static long lookupEntriesInLUTPage(long pix) {
    if (lookupPageIndexInBlock(pix) < nbrOfLookupPages() - 1) {
      return cfg.logPageSize / cfg.sizeObjId;
    } else {
      return lookupMaxEntries() % (cfg.logPageSize / cfg.sizeObjId);
    }
  }
  
  public static long pageToBlock(long pix) {
    return pix / pagesPerBlock();
  }
  
  public static long sizeOfPageHeader() {
    return cfg.sizeObjId + cfg.sizeSpanIx + 1 /*flag byte*/;
  }
  
  public static long sizeOfObjectHeader() {
    long phdrSz = sizeOfPageHeader();
    return (long)(Math.ceil((double)phdrSz / 4.0) * 4.0);
  }
  
  public static long sizeOfObjectIndexHeader() {
    long sz = sizeOfObjectHeader();
    sz += 4; // size
    sz += 1; // type
    sz += cfg.fileNameSize;
    // TODO if SPIFFS_ALIGNED_OBJECT_INDEX_TABLES, align on sizeof(spiffs_page_ix)
    return (long)(Math.ceil((double)sz / (double)cfg.sizePageIx) * (double)cfg.sizePageIx);
  }
  
  public static long dataPageSize() {
    return cfg.logPageSize - sizeOfPageHeader();
  }
  
  public static long objectHeaderIndexLength() {
    return (cfg.logPageSize - sizeOfObjectIndexHeader())/cfg.sizePageIx;
  }
  
  public static long objectIndexLength() {
    return (cfg.logPageSize - sizeOfObjectHeader())/cfg.sizePageIx;
  }
  
  public static long getFreeObjectId() {
    return (long)((1L << (8 * Spiffs.cfg.sizeObjId)) - 1L);
  }
  
  public static long getFreePageId() {
    return (long)((1L << (8 * Spiffs.cfg.sizePageIx)) - 1L);
  }
  
  public static long getDeletedObjectId() {
    return 0L;
  }
  
  public static boolean isObjectIndex(long objId) {
    return (objId & (long)(1L << (8 * Spiffs.cfg.sizeObjId - 1L))) != 0; 
  }

  public static long cleanObjectId(long objId) {
    return (objId & (long)((1L << (8 * Spiffs.cfg.sizeObjId - 1L)) - 1)); 
  }
  
  // formatting
  
  public static String formatObjId(long objId) {
    if (isObjectIndex(objId)) {
      return "*" + String.format(typeFormat(Spiffs.cfg.sizeObjId), cleanObjectId(objId));
    }
    return String.format(typeFormat(Spiffs.cfg.sizeObjId), objId);
  }
  
  public static String formatPageIndex(long pix) {
    return String.format(typeFormat(Spiffs.cfg.sizePageIx), pix);
  }
  
  public static String formatSpanIndex(long spix) {
    return String.format(typeFormat(Spiffs.cfg.sizeSpanIx), spix);
  }
  
  public static String formatBlockIndex(long bix) {
    return String.format(typeFormat(2), bix);
  }
  
  public static String typeFormat(int size) {
    if (size == 1) {
      return "%x";
    }
    else if (size == 2) {
      return "%x";
    }
    else if (size == 4) {
      return "%x";
    }
    else if (size == 8) {
      return "%x";
    }
    else {
      throw new RuntimeException("size invalid (" + size + ")");
    }
  }
  
  // I/O
  
  public static byte[] readBuffer(int pos, int size) {
    byte[] b = new byte[size];
    data.position(pos);
    data.get(b);
    return b;
  }

  public static long readType(int pos, int size) {
    if (size == 1) {
      return (long)(data.get(pos)) & 0xff;
    }
    else if (size == 2) {
      return (long)(data.getShort(pos)) & 0xffff;
    }
    else if (size == 4) {
      return (long)(data.getInt(pos)) & 0xffffffffL;
    }
    else if (size == 8) {
      return (long)(data.getLong(pos));
    }
    else {
      throw new RuntimeException("size invalid (" + size + ")");
    }
  }
  
  public static long readType(int size) {
    if (size == 1) {
      return (long)(data.get()) & 0xff;
    }
    else if (size == 2) {
      return (long)(data.getShort()) & 0xffff;
    }
    else if (size == 4) {
      return (long)(data.getInt()) & 0xffffffffL;
    }
    else if (size == 8) {
      return (long)(data.getLong());
    }
    else {
      throw new RuntimeException("size invalid (" + size + ")");
    }
  }
  
  public static long readObjectId(int pos) {
    return readType( pos, cfg.sizeObjId);
  }
  
  public static long readObjectId() {
    return readType(cfg.sizeObjId);
  }
  
  public static long readPageIx(int pos) {
    return readType(pos, cfg.sizePageIx);
  }
  
  public static long readPageIx() {
    return readType(cfg.sizePageIx);
  }
  
  public static long readSpanIx(int pos) {
    return readType(pos, cfg.sizeSpanIx);
  }

  public static long readSpanIx() {
    return readType(cfg.sizeSpanIx);
  }
  
  public static void alignPosition(int alignment) {
    int p = data.position();
    int leftOver = p % alignment;
    if (leftOver > 0) {
      data.position(p + alignment - leftOver);
    }
  }

  public static long physOffsetOfPage(long pix) {
    return pix * cfg.logPageSize + cfg.physOffset;
  }
}
