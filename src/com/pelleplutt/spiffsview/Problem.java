package com.pelleplutt.spiffsview;

public class Problem {
  public static final int LUT_REF_PAGE_NOT_DELETED = 0x10;
  public static final int LUT_REF_PAGE_NOT_FREE = 0x11;
  public static final int LUT_REF_PAGE_FREE = 0x12;
  public static final int LUT_REF_PAGE_DELETED = 0x13;
  public static final int LUT_REF_ID_MISMATCH = 0x14;
  public static final int LUT_REF_IX_MISMATCH = 0x15;
  public static final int LUT_REF_NONIX_MISMATCH = 0x16;
  public static final int LUT_REF_NONEXIST = 0x17;
  public static final int IX_REF_DELETED = 0x30;
  public static final int IX_REF_FREE = 0x31;
  public static final int IX_REF_ID_MISMATCH = 0x32;
  public static final int IX_REF_HEADER_MISMATCH = 0x33;
  public static final int IX_REF_SPAN_IX_MISMATCH = 0x34;
  public static final int IX_REF_BAD_PAGE = 0x35;
  public static final int IX_REF_LUT = 0x36;
  public static final int IX_REF_DIRTY = 0x37;
  public static final int IX_REF_NOT_WRITTEN = 0x38;
  public static final int PAGE_FREE_DIRTY = 0x50;
  public static final int PAGE_UNFINALIZED = 0x51;
  public static final int PAGE_ID_CONFLICT = 0x52;
  public static final int PAGE_ID_ORPHAN = 0x53;
  public static final int FILE_MISSING_DATA_SPAN_IX = 0x72;
  public static final int FILE_SUPERFLUOUS_DATA_SPAN_IX = 0x73;
  public static final int FILE_MISSING_IX_SPAN_IX = 0x70;
  public static final int FILE_SUPERFLUOUS_IX_SPAN_IX = 0x71;
  
  public final int type;
  public final SpiffsPage page;
  public final int entryIx;
  public final long lutObjId;
  public final long spanIx;
  public final SpiffsPage refPage;
  
  public Problem(int type, SpiffsPage page) {
    this.type = type;
    this.page = page;
    this.lutObjId = -1;
    this.entryIx = -1;
    this.spanIx = -1;
    this.refPage = null;
  }
  
  public Problem(int type, SpiffsPage page, int entryIx, long lutObjId, SpiffsPage refPage) {
    this.type = type;
    this.page = page;
    this.entryIx = entryIx;
    this.lutObjId = lutObjId;
    this.refPage = refPage;
    this.spanIx = -1;
  }
  
  public Problem(int type, SpiffsPage page, long spanIndex) {
    this.type = type;
    this.page = page;
    this.spanIx = spanIndex;
    this.lutObjId = -1;
    this.entryIx = -1;
    this.refPage = null;
  }

  public Problem(int type, SpiffsPage page, SpiffsPage refPage, long spanIndex) {
    this.type = type;
    this.page = page;
    this.refPage = refPage;
    this.spanIx = spanIndex;
    this.lutObjId = -1;
    this.entryIx = -1;
  }

  public Problem(int type, SpiffsPage page, int entryIx, SpiffsPage refPage) {
    this.type = type;
    this.page = page;
    this.entryIx = entryIx;
    this.refPage = refPage;
    this.lutObjId = -1;
    this.spanIx = -1;
  }

  private String err() {
    return String.format("[%02x] ", type);
  }
  
  public String toString() {
    switch (type) {
    case LUT_REF_PAGE_NOT_DELETED:
      return err() + "LUT page " + page.getPageIndexString() + " entry ix " + entryIx + 
          " marked as deleted, but refers to nondeleted page " + refPage.getPageIndexString();
    case LUT_REF_PAGE_NOT_FREE:
      return err() + "LUT page " + page.getPageIndexString() + " entry ix " + entryIx + 
          " marked as free, but refers to nonfree page " + refPage.getPageIndexString();
    case LUT_REF_PAGE_FREE:
      return err() + "LUT page " + page.getPageIndexString() + " entry ix " + entryIx + 
          " is referring to obj id " + Spiffs.formatObjId(lutObjId) + 
          " but points to free page " + refPage.getPageIndexString();
    case LUT_REF_PAGE_DELETED:
      return err() + "LUT page " + page.getPageIndexString() + " entry ix " + entryIx + 
          " is referring to obj id " + Spiffs.formatObjId(lutObjId) + 
          " but points to deleted page " + refPage.getPageIndexString();
    case LUT_REF_ID_MISMATCH:
      return err() + "LUT page " + page.getPageIndexString() + " entry ix " + entryIx + 
          " is referring to obj id " + Spiffs.formatObjId(lutObjId) + 
          " but points to obj id " + refPage.getObjectIdString() + ", page " + refPage.getPageIndexString();
    case LUT_REF_IX_MISMATCH:
      return err() + "LUT page " + page.getPageIndexString() + " entry ix " + entryIx + 
          " is referring to obj index id " + Spiffs.formatObjId(lutObjId) + 
          " but points to non index obj id " + refPage.getObjectIdString() + ", page " + refPage.getPageIndexString();
    case LUT_REF_NONIX_MISMATCH:
      return err() + "LUT page " + page.getPageIndexString() + " entry ix " + entryIx + 
          " is referring to non index obj id " + Spiffs.formatObjId(lutObjId) + 
          " but points to index obj id " + refPage.getObjectIdString() + ", page " + refPage.getPageIndexString();
    case LUT_REF_NONEXIST:
      return err() + "LUT page " + page.getPageIndexString() + " entry ix " + entryIx + 
          " is referring to obj id " + Spiffs.formatObjId(lutObjId) + 
          " which does not exist as file";
    case IX_REF_DELETED:
      return err() + "Index page " + page.getPageIndexString() + " span ix " + page.getSpanIndexString() + 
          " entry ix " + entryIx + " points to deleted page " + refPage.getPageIndexString();
    case IX_REF_FREE:
      return err() + "Index page " + page.getPageIndexString() + " span ix " + page.getSpanIndexString() + 
          " entry ix " + entryIx + " points to free page " + refPage.getPageIndexString();
    case IX_REF_BAD_PAGE:
      return err() + "Index page " + page.getPageIndexString() + " span ix " + page.getSpanIndexString() + 
          " entry ix " + entryIx + " points to invalid page";
    case IX_REF_LUT:
      return err() + "Index page " + page.getPageIndexString() + " span ix " + page.getSpanIndexString() + 
          " entry ix " + entryIx + " points to LUT page " + refPage.getPageIndexString();
    case IX_REF_ID_MISMATCH:
      return err() + "Index page " + page.getPageIndexString() + " span ix " + page.getSpanIndexString() + 
          " entry ix " + entryIx + " points to page " + refPage.getPageIndexString() + " with wrong obj id " +
          refPage.getObjectIdString();
    case IX_REF_HEADER_MISMATCH:
      return err() + "Index page " + page.getPageIndexString() + " span ix " + page.getSpanIndexString() + 
          " entry ix " + entryIx + " points to page " + refPage.getPageIndexString() + " being a object index " +
          refPage.getObjectIdString();
    case IX_REF_SPAN_IX_MISMATCH:
      return err() + "Index page " + page.getPageIndexString() + " span ix " + page.getSpanIndexString() + 
          " entry ix " + entryIx + " points to page " + refPage.getPageIndexString() + " with wrong span ix " +
          refPage.getSpanIndexString();
    case IX_REF_DIRTY:
      return err() + "Index page " + page.getPageIndexString() + " span ix " + page.getSpanIndexString() + 
          " entry ix " + entryIx + " should be free, but has zeroed bits";
    case IX_REF_NOT_WRITTEN:
      return err() + "Index page " + page.getPageIndexString() + " span ix " + page.getSpanIndexString() + 
          " entry ix " + entryIx + " have a value, but is unwritten";
    case PAGE_FREE_DIRTY:
      return err() + "Page " + page.getPageIndexString() + " marked as free, but has zeroed bits";
    case PAGE_UNFINALIZED:
      return err() + "Page " + page.getPageIndexString() + " is used but unfinalized";
    case PAGE_ID_CONFLICT:
      return err() + "Page " + page.getPageIndexString() + " with obj id " + page.getObjectIdString() + ", span ix " +
        page.getSpanIndexString() + " has same obj id and span ix as page " + refPage.getPageIndexString();
    case PAGE_ID_ORPHAN:
      return err() + "Page " + page.getPageIndexString() + " with obj id " + page.getObjectIdString() + ", span ix " +
        page.getSpanIndexString() + " has no corresponding object index header";
    case FILE_MISSING_DATA_SPAN_IX:
      return err() + "File with obj id " + page.getObjectIdString() + " has size " + page.getSizeString() + " but" +
        " has not enough data pages, missing span ix " + spanIx;
    case FILE_SUPERFLUOUS_DATA_SPAN_IX:
      return err() + "File with obj id " + page.getObjectIdString() + " has size " + page.getSizeString() + " but" +
      " has superfluous data pages, page " + refPage.getPageIndexString() + " with span ix " + refPage.getSpanIndexString();
    case FILE_MISSING_IX_SPAN_IX:
      return err() + "File with obj id " + page.getObjectIdString() + " has size " + page.getSizeString() + " but" +
        " has not enough object index pages, missing span ix " + spanIx;
    case FILE_SUPERFLUOUS_IX_SPAN_IX:
      return err() + "File with obj id " + page.getObjectIdString() + " has size " + page.getSizeString() + " but" +
      " has superfluous object index pages, page " + refPage.getPageIndexString() + " with span ix " + refPage.getSpanIndexString();
    default:
      return err() + "unknown";
    }
  }
}
