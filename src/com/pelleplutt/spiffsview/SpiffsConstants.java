package com.pelleplutt.spiffsview;

public interface SpiffsConstants {
  //if 0, this page is written to, else clean
  final static public int SPIFFS_PH_FLAG_USED = (1<<0);
  //if 0, writing is finalized, else under modification
  final static public int SPIFFS_PH_FLAG_FINAL = (1<<1);
  //if 0, this is an index page, else a data page
  final static public int SPIFFS_PH_FLAG_INDEX = (1<<2);
  //if 0, page is deleted, else valid
  final static public int SPIFFS_PH_FLAG_DELET = (1<<7);
  //if 0, this index header is being deleted
  final static public int SPIFFS_PH_FLAG_IXDELE = (1<<6);
  
  final static public long SPIFFS_UNDEFINED_LEN = -1;
  final static public long SPIFFS_MAGIC = 0x20140529;
  
  final static public int SPIFFS_TYPE_FILE = 1;
  final static public int SPIFFS_TYPE_DIR = 2;
  final static public int SPIFFS_TYPE_HARD_LINK = 3;
  final static public int SPIFFS_TYPE_SOFT_LINK = 4;

}
