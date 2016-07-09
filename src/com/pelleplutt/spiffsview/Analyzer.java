package com.pelleplutt.spiffsview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pelleplutt.util.Log;

public class Analyzer implements Progressable {
  
  List<Problem> problems = new ArrayList<Problem>();
  
  Map<Long, SpiffsFile> files = new HashMap<Long, SpiffsFile>();

  Map<Long, SpiffsPage> pagesIdSpan = new HashMap<Long, SpiffsPage>();
  
  ProgressHandler progress = new ProgressHandler(this);

  public List<Problem> getProblems() {
    return problems;
  }
  
  public void analyze() {
    problems = new ArrayList<Problem>();
    files = new HashMap<Long, SpiffsFile>();
    pagesIdSpan = new HashMap<Long, SpiffsPage>();

    progress.fireStarted();
    
    // analyze all ids
    for (int pix = 0; pix < Spiffs.nbrOfPages(); pix++) {
      progress.fireWork(((double)pix / (double)Spiffs.nbrOfPages()) * 0.25);
      idScan(SpiffsPage.getPage(pix));
    }
    // collect file map
    for (int pix = 0; pix < Spiffs.nbrOfPages(); pix++) {
      progress.fireWork(0.25 + ((double)pix / (double)Spiffs.nbrOfPages()) * 0.25);
      fileScan(SpiffsPage.getPage(pix));
    }
    // analyze pages
    for (int pix = 0; pix < Spiffs.nbrOfPages(); pix++) {
      progress.fireWork(0.50 + ((double)pix / (double)Spiffs.nbrOfPages()) * 0.25);
      analyzePage(SpiffsPage.getPage(pix));
    }
    // analyze files
    int fileIx = 0;
    int filesCount = files.values().size();
    for (SpiffsFile f : files.values()) {
      progress.fireWork(0.75 + ((double)fileIx++ / (double)filesCount) * 0.25);
      analyzeFile(f);
    }
    
    progress.fireStopped(null);
  }
  
  SpiffsPage getPageById(long objId, long spanIx) {
    long idspan = (objId << 32) | spanIx;
    return pagesIdSpan.get(idspan);
  }
  
  void idScan(SpiffsPage p) {
    if (!p.isDeleted() && !p.isFree()) {
      long idspan = (p.getObjectId() << 32) | p.getSpanIndex();
      if (pagesIdSpan.containsKey(idspan)) {
        add(new Problem(Problem.PAGE_ID_CONFLICT, p, 0, 0, pagesIdSpan.get(idspan)));
      } else {
        pagesIdSpan.put(idspan, p);
      }
    }
  }
  
  void fileScan(SpiffsPage p) {
    if (!p.isDeleted() && !p.isFree() && p.isObjectIndexHeader()) {
      SpiffsFile f = new SpiffsFile(p);
      files.put(Spiffs.cleanObjectId(p.getObjectId()), f);
      
      f.dataPages = new SpiffsPage[f.getNbrOfDataPages()];
      for (int spix = 0; spix < f.dataPages.length; spix++) {
        f.dataPages[spix] = getPageById(Spiffs.cleanObjectId(p.getObjectId()), spix);
      }
      
      f.indexPages = new SpiffsPage[f.getNbrOfIndexPages()];
      for (int spix = 0; spix < f.indexPages.length; spix++) {
        f.indexPages[spix] = getPageById(p.getObjectId(), spix);
      }
      
    }
  }
  
  void analyzePage(SpiffsPage p) {
    if (p.isLookUp()) {
      analyzeLUT(p);
    } else {
      analyzeCommon(p);
      if (!p.isDeleted() && !p.isFree() && p.isObjectIndex()) {
        analyzeIndex(p);
      }
    }
  }
  
  void analyzeCommon(SpiffsPage page) {
    if (page.isFree()) {
      if (!page.checkErased()) {
        add(new Problem(Problem.PAGE_FREE_DIRTY, page));
      } 
    } else if (!page.isDeleted()) {
      if (page.isFlagUsed() && !page.isFlagFinalized()) {
        add(new Problem(Problem.PAGE_UNFINALIZED, page));
      }
      if (!page.isObjectIndexHeader() && !files.containsKey(Spiffs.cleanObjectId(page.getObjectId()))) {
        add(new Problem(Problem.PAGE_ID_ORPHAN, page));
      }
    }
  }

  void analyzeLUT(SpiffsPage lutPage) {
    if (Spiffs.lookupPageIndexInBlock(lutPage.getPageIndex()) == Spiffs.nbrOfLookupPages() - 1) {
      // check magic TODO
      
    }

    // cross check all lut entries against referred pages
    for (int eix = 0; eix < Spiffs.lookupEntriesInLUTPage(lutPage.getPageIndex()); eix++) {
      long lutObjId = lutPage.readLUTEntry(eix);
      long refPIx = lutPage.getBlockIndex() * Spiffs.pagesPerBlock();
      refPIx += Spiffs.lookupPageIndexInBlock(lutPage.getPageIndex()) * (Spiffs.cfg.logPageSize / Spiffs.cfg.sizeObjId);
      refPIx += Spiffs.nbrOfLookupPages();
      refPIx += eix;
      SpiffsPage refPage = SpiffsPage.getPage(refPIx);
      int entryIx = (int)(lutPage.span_ix * Spiffs.lookupEntriesInLUTPage(0)) + eix;

      // check deleted
      if (lutObjId == Spiffs.getDeletedObjectId()) {
        if (!refPage.isDeleted()) {
          add(new Problem(Problem.LUT_REF_PAGE_NOT_DELETED, lutPage, entryIx, lutObjId, refPage));
        }
      }
      
      // check free
      else if (lutObjId == Spiffs.getFreeObjectId()) {
        if (!refPage.isFree()) {
          add(new Problem(Problem.LUT_REF_PAGE_NOT_FREE, lutPage, entryIx, lutObjId, refPage));
        }
      }
      
      // check taken
      else {
        if (refPage.isDeleted()) {
          add(new Problem(Problem.LUT_REF_PAGE_DELETED, lutPage, entryIx, lutObjId, refPage));
        } else 
        if (refPage.isFree()) {
          add(new Problem(Problem.LUT_REF_PAGE_FREE, lutPage, entryIx, lutObjId, refPage));
        } else {
          if (Spiffs.cleanObjectId(lutObjId) != Spiffs.cleanObjectId(refPage.getObjectId())) {
            add(new Problem(Problem.LUT_REF_ID_MISMATCH, lutPage, entryIx, lutObjId, refPage));
          } 

          if (Spiffs.isObjectIndex(lutObjId) && !Spiffs.isObjectIndex(refPage.getObjectId())) {
            add(new Problem(Problem.LUT_REF_IX_MISMATCH, lutPage, entryIx, lutObjId, refPage));
          } else  
          if (!Spiffs.isObjectIndex(lutObjId) && Spiffs.isObjectIndex(refPage.getObjectId())) {
            add(new Problem(Problem.LUT_REF_NONIX_MISMATCH, lutPage, entryIx, lutObjId, refPage));
          }
          
          if (!files.containsKey(Spiffs.cleanObjectId(lutObjId))) {
            add(new Problem(Problem.LUT_REF_NONEXIST, lutPage, entryIx, lutObjId, null));
          }
        }
      }
    } // per page reference in lut
  }

  void analyzeIndex(SpiffsPage ixPage) {
    int indices = (int)(ixPage.getSpanIndex() == 0 ? Spiffs.objectHeaderIndexLength() : Spiffs.objectIndexLength());
    SpiffsFile file = files.get(Spiffs.cleanObjectId(ixPage.getObjectId())); // TODO check null
    long maxEntryIx = file.getNbrOfDataPages();
    for (int ix = 0; ix < indices; ix++) {
      long ixEntryVal = ixPage.readIxEntry(ix);
      long entryIx =
          ix +
          (ixPage.getSpanIndex() == 0 ? 
              0 : 
              (Spiffs.objectHeaderIndexLength() + Spiffs.objectIndexLength() * (ixPage.getSpanIndex() - 1)));
      if (entryIx > maxEntryIx) {
        if (ixEntryVal != Spiffs.getFreePageId()) {
          add(new Problem(Problem.IX_REF_DIRTY, ixPage, ix, null));
        }
      } else if (ixEntryVal == Spiffs.getFreePageId()) { 
        add(new Problem(Problem.IX_REF_NOT_WRITTEN, ixPage, ix, null));
      } else if (Spiffs.isLookupPage(ixEntryVal)) {
        add(new Problem(Problem.IX_REF_LUT, ixPage, ix, SpiffsPage.getPage(ixEntryVal)));
      } else if (ixEntryVal > Spiffs.nbrOfPages()) {
        add(new Problem(Problem.IX_REF_BAD_PAGE, ixPage, ix, null));
      } else {
        SpiffsPage refPage = SpiffsPage.getPage(ixEntryVal);
        if (refPage.isFree()) {
          add(new Problem(Problem.IX_REF_FREE, ixPage, ix, refPage));
        } else  if (refPage.isDeleted()) {
          add(new Problem(Problem.IX_REF_DELETED, ixPage, ix, refPage));
        } else  if (refPage.isObjectIndex()) {
          add(new Problem(Problem.IX_REF_HEADER_MISMATCH, ixPage, ix, refPage));
        } else if (refPage.getObjectId() != Spiffs.cleanObjectId(ixPage.getObjectId())) {
          add(new Problem(Problem.IX_REF_ID_MISMATCH, ixPage, ix, refPage));
        } else if (refPage.getSpanIndex() != entryIx) {
          add(new Problem(Problem.IX_REF_SPAN_IX_MISMATCH, ixPage, ix, refPage));
        }
      }
    }
      
  }

  void analyzeFile(SpiffsFile f) {
    int dataPages = f.getNbrOfDataPages();
    // check that all data pages exist for file
    for (int spix = 0; spix < dataPages; spix++) {
      if (f.dataPages[spix] == null) {
        add(new Problem(Problem.FILE_MISSING_DATA_SPAN_IX, f.indexHeaderPage, spix));
      }
    }
    
    // check superfluous data pages
    for (long pix = 0; pix < Spiffs.nbrOfPages(); pix++) {
      SpiffsPage p = SpiffsPage.getPage(pix);
      if (!p.isDeleted() && !p.isFree() && !p.isObjectIndex() &&
          p.getObjectId() == Spiffs.cleanObjectId(f.indexHeaderPage.getObjectId())) {
        if (p.getSpanIndex() > dataPages) {
          add(new Problem(Problem.FILE_SUPERFLUOUS_DATA_SPAN_IX, f.indexHeaderPage, p, p.getSpanIndex()));
        }
      }
    }

    int ixPages = f.getNbrOfIndexPages();
    // check that all index pages exist for file
    for (int spix = 0; spix < ixPages; spix++) {
      if (f.indexPages[spix] == null) {
        add(new Problem(Problem.FILE_MISSING_IX_SPAN_IX, f.indexHeaderPage, spix));
      }
    }
    
    // check superfluous index pages
    for (long pix = 0; pix < Spiffs.nbrOfPages(); pix++) {
      SpiffsPage p = SpiffsPage.getPage(pix);
      if (!p.isDeleted() && !p.isFree() && p.isObjectIndex() &&
          p.getObjectId() == f.indexHeaderPage.getObjectId()) {
        if (p.getSpanIndex() > ixPages) {
          add(new Problem(Problem.FILE_SUPERFLUOUS_IX_SPAN_IX, f.indexHeaderPage, p, p.getSpanIndex()));
        }
      }
    }

  }

  private void add(Problem p) {
    Log.println(p.toString());
    problems.add(p);
  }

  @Override
  public void addListener(ProgressListener p) {
    progress.addListener(p);
  }

  @Override
  public void removeListener(ProgressListener p) {
    progress.removeListener(p);
  }
}
