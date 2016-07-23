package com.pelleplutt.spiffsview;

import java.util.HashMap;
import java.util.Map;

import com.pelleplutt.util.Log;


public class AnalyzerConfig implements Progressable {
  ProgressHandler progress = new ProgressHandler(this);
  SpiffsConfig cfg = new SpiffsConfig();

  static final long KB = 1024;
  
  static final long LOG_BLOCK_SZ[] = {
    4*KB,
    8*KB,
    16*KB,
    32*KB,
    64*KB,
    128*KB,
    256*KB,
    512*KB,
    1024*KB,
  };
  
  static final long LOG_PAGE_SZ[] = {
    64,
    128,
    256,
    512,
    1024,
    2048,
    4096
  };
  
  public void analyze(long offset, long size) {
    cfg.physOffset = offset;
    cfg.physSize = size;
    searchLogicalBlockPatterns();
  }
  
  /**
   * Search start of logical blocks, presume we find LUTs.
   * LUT entries are always size of obj id.
   * 
   * LUTs are assumed to have following structure:
   * Firstly, a number of bytes containing mostly 0x00, few 0xff, and rest
   * 0x01-0xfe.
   * 
   * Secondly, only a bunch of 0xff until end of LUT, the last 2 till 8 bytes
   * will contain erase count and maybe magic.
   * 
   * Depending on endianess and obj id size, first part might look like this
   * 00001014 00000045 00000123 80000123
   * 
   * MSB set if LUT points to object index. Value must never be bigger than
   * fs_size / log_page_size
   */
  void searchLogicalBlockPatterns() {
    Info info[] = new Info[LOG_BLOCK_SZ.length];
    for (int logBlockSzIx = 0; logBlockSzIx < LOG_BLOCK_SZ.length; logBlockSzIx++) {
      info[logBlockSzIx] = new Info();
      long blockSz = LOG_BLOCK_SZ[logBlockSzIx];
      info[logBlockSzIx].blockSize = blockSz;
      info[logBlockSzIx].blocks = (int)(cfg.physSize / blockSz);
      Map<Integer, Integer> lutSizes = new HashMap<Integer, Integer>();
      
      Log.println("===== CANDIDATE LOGBLKSZ " + blockSz);
      for (int blkIx = 0; blkIx < cfg.physSize / blockSz; blkIx++) {
        int conseqFF = 0;
        int cntFF = 0;
        int cnt00 = 0;
        int cntData = 0;
        int addr = (int)(blkIx * blockSz);
        int blockOffs = 0;
        int b = 0;
        // first read lut entries
        while (blockOffs < blockSz/4) {
          int nb = Spiffs.read(addr + blockOffs);
          if (nb == 0xff) {
            cntFF++;
          } else if (nb == 0x00) {
            cnt00++;
          } else {
            cntData++;
          }
          if (b == 0xff && nb == 0xff) {
            conseqFF++;
            if (conseqFF >= 4) {
              blockOffs -= 4;
              cntFF -= 4;
              break;
            }
          }
          b = nb;
          blockOffs++;
        } // lut entries
        // then read the free entries until possibly magic and erase cnt
        while (blockOffs < blockSz/4) {
          int nb = Spiffs.read(addr + blockOffs);
          if (nb != 0xff) {
            break;
          }
          blockOffs++;
        } // block
        Log.println("  " + blkIx + " broken @ " + blockOffs + "  cntFF " + cntFF+ "  cnt00 " + cnt00 + "  cntDa " + cntData);
        if (lutSizes.containsKey(blockOffs)) {
          lutSizes.put(blockOffs, lutSizes.get(blockOffs).intValue() + 1);
        } else {
          lutSizes.put(blockOffs, 1);
        }
      } // per all blocks
      Log.println("" + lutSizes);
    } // per logical block size candidate
  }
  
  @Override
  public void addListener(ProgressListener p) {
    progress.addListener(p);
  }

  @Override
  public void removeListener(ProgressListener p) {
    progress.removeListener(p);
  }
  
  static class Info {
    public long blockSize;
    public int blocks;
    public int winningLutSize;
    public int averageLutSizeDeviation;
  }
}
