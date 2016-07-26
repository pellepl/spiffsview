package com.pelleplutt.spiffsview;

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
}
