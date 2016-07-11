package com.pelleplutt.spiffsview;

import java.util.HashMap;
import java.util.Map;

public class SpiffsPage implements SpiffsConstants {
  long page_ix;
  long obj_id;
  long span_ix;
  int flags;
  
  long size;
  int type;
  char[] name;
  
  static Map<Integer, SpiffsPage> cache = new HashMap<Integer, SpiffsPage>(); 
  
  public static void update() {
    cache = new HashMap<Integer, SpiffsPage>();
    for (int i = 0; i < Spiffs.nbrOfPages(); i++) {
      getPage(i);
    }
  }
  
  public static SpiffsPage getPage(long pix) {
    if (pix >= Spiffs.nbrOfPages()) {
      return null;
    }
    
    if (!cache.containsKey(pix)) {
      SpiffsPage p = new SpiffsPage();
      p.page_ix = pix;
      Spiffs.data.position((int)pix * Spiffs.cfg.logPageSize);
      
      p.obj_id = Spiffs.readObjectId();
      p.span_ix = Spiffs.readSpanIx();
      p.flags = (int)Spiffs.readType(1);
      Spiffs.alignPosition(4);
      p.size = Spiffs.readType(4);
      p.type = (int)Spiffs.readType(1);
      p.name = new char[Spiffs.cfg.fileNameSize];
      for (int i = 0; i < p.name.length; i++) {
        p.name[i] = (char)Spiffs.readType(1);
      }
      // TODO if SPIFFS_ALIGNED_OBJECT_INDEX_TABLES
      Spiffs.alignPosition(4);
      cache.put((int)pix, p);
    }
    
    return cache.get((int)pix);
  }
  
  public byte[] readContents() {
    return Spiffs.readBuffer((int)(page_ix * Spiffs.cfg.logPageSize), (int)Spiffs.cfg.logPageSize);
  }
  
  public int readContent(int ix) {
    return Spiffs.read((int)(page_ix * Spiffs.cfg.logPageSize) + ix);
  }
  
  public long readIxEntry(int ix) {
    long nbrOfEntries = span_ix == 0 ? Spiffs.objectHeaderIndexLength() : Spiffs.objectIndexLength();
    if (ix > nbrOfEntries) {
      throw new RuntimeException("read objix index beyond bounds " + ix + " > " + nbrOfEntries);
    }
    return Spiffs.readPageIx((int)(
        page_ix * Spiffs.cfg.logPageSize +
        (span_ix == 0 ? Spiffs.sizeOfObjectIndexHeader() : Spiffs.sizeOfObjectHeader()) +
        ix * Spiffs.cfg.sizePageIx
        ));
  }
  
  public long readLUTEntry(int ix) {
    if (ix > Spiffs.cfg.logPageSize / Spiffs.cfg.sizeObjId) {
      throw new RuntimeException("read lut index beyond bounds " + ix + " > " + (Spiffs.cfg.logPageSize / Spiffs.cfg.sizeObjId));
    }
    return Spiffs.readObjectId((int)page_ix * Spiffs.cfg.logPageSize + ix * Spiffs.cfg.sizeObjId);
  }
  
  public boolean checkErased() {
    Spiffs.data.position((int)page_ix * Spiffs.cfg.logPageSize);
    for (int i = 0; i < Spiffs.cfg.logPageSize; i++) {
      long d = Spiffs.readType(1);
      if (d != 0xff) return false;
    }
    return true;

  }
  
  public long getBlockIndex() {
    return Spiffs.pageToBlock((int)page_ix);
  }
  
  public long getPageIndex() {
    return this.page_ix;
  }
  
  public long getSpanIndex() {
    return span_ix;
  }
  
  public long getObjectId() {
    return obj_id;
  }
  
  public String getBlockIndexString() {
    return Spiffs.formatBlockIndex(getBlockIndex());
  }
  
  public String getSpanIndexString() {
    return Spiffs.formatSpanIndex(getSpanIndex());
  }
  
  public String getObjectIdString() {
    return Spiffs.formatObjId(getObjectId());
  }
  
  public String getPageIndexString() {
    return Spiffs.formatPageIndex(getPageIndex());
  }
  
  public String getFlagsString() {
    return
        (isFlagUsed() ? "U" : "u") +
        (isFlagFinalized() ? "F" : "f") +
        (isFlagIndex() ? "I" : "i") +
        (isFlagDeleted() ? "D" : "d") +
        (isFlagIndexDeleted() ? "X" : "x");
  }
  
  public boolean isObjectIndex() {
    return Spiffs.isObjectIndex(obj_id); 
  }
  
  public boolean isObjectIndexHeader() {
    return span_ix == 0 && Spiffs.isObjectIndex(obj_id);
  }
  
  public boolean isFree() {
    return obj_id == Spiffs.getFreeObjectId();
  }
  
  public boolean isDeleted() {
    return (flags & SPIFFS_PH_FLAG_DELET) == 0;
  }
  
  public boolean isLookUp() {
    return Spiffs.isLookupPage((int)page_ix);
  }
  
  public boolean isFlagUsed() {
    return (flags & SPIFFS_PH_FLAG_USED) == 0;
  }
  
  public boolean isFlagFinalized() {
    return (flags & SPIFFS_PH_FLAG_FINAL) == 0;
  }
  
  public boolean isFlagIndex() {
    return (flags & SPIFFS_PH_FLAG_INDEX) == 0;
  }
  
  public boolean isFlagDeleted() {
    return (flags & SPIFFS_PH_FLAG_DELET) == 0;
  }
  
  public boolean isFlagIndexDeleted() {
    return (flags & SPIFFS_PH_FLAG_IXDELE) == 0;
  }
  
  
  
  public long getSize() {
    return size == (long)((int)(-1)&0xffffffffl) ? -1L : size;
  }
  
  public String getSizeString() {
    return getSize() == -1 ? "undef" : Long.toString(getSize());
  }
  
  public String getName() {
    return String.valueOf(name);
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("pix:%08x  ", page_ix));
    if (Spiffs.isLookupPage(page_ix)) {
      sb.append("LOOKUP");
    }  else { 
      if (!isFree()) {
        sb.append(String.format("oid:%08x  ", obj_id));
        sb.append(String.format("spx:%08x  ", span_ix));
        sb.append(String.format("flg:%02x ", flags));
        sb.append((flags & SPIFFS_PH_FLAG_USED) == 0 ? "USE " : "use ");
        sb.append((flags & SPIFFS_PH_FLAG_FINAL) == 0 ? "FIN " : "fin ");
        sb.append((flags & SPIFFS_PH_FLAG_INDEX) == 0 ? "IX  " : "ix  ");
        sb.append((flags & SPIFFS_PH_FLAG_DELET) == 0 ? "DEL " : "del ");
        sb.append((flags & SPIFFS_PH_FLAG_IXDELE) == 0 ? "IXD " : "ixd ");
        if (isObjectIndexHeader()) {
          sb.append("  ");
          sb.append(String.format("typ:%02x  ", type));
          sb.append(String.format("sze:%08x  ", size));
          sb.append(String.format("name:%s", String.valueOf(name)));
        }
      } else {
        sb.append("FREE");
      }
    }
    return sb.toString();
  }
 }
