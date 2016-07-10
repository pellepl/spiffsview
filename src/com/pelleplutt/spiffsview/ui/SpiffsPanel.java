package com.pelleplutt.spiffsview.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.pelleplutt.spiffsview.Spiffs;
import com.pelleplutt.spiffsview.SpiffsPage;

public class SpiffsPanel extends JPanel {
  private static final long serialVersionUID = 9114238879663074094L;
  static final int pageWidth = 8;
  static final int pageHeight = 12;
  static final int minPanelWidth = 256;
  
  int grid = 1;
  static final double mags[] = {
      0.5,  0.6,  0.7,  0.8,  0.9, 
      1.0,  1.2,  1.4,  1.6,  1.8, 
      2.0,  2.5,  3.0,  3.5,  4.0,
      5.0,  6.0,  7.0,  8.0,  9.0,
      10.0, 12.0, 14.0, 16.0, 18.0,
      20.0, 25.0, 30.0, 35.0, 40.0,
      50.0, 60.0, 70.0, 80.0, 90.0,
      100.0
  };
  volatile int magIx = 5;
  volatile double mag = mags[magIx];
  
  GradientPaint paintFree = makePaint(8, 8, 8, 24, 2.0);
  GradientPaint paintDeleted = makePaint(32, 32, 32, 48, 2.0);
  GradientPaint paintLUT = makePaint(192, 192, 192, 64, 1.0);

  final static int cd = 96;
  final static int cdb = 96;
  final static int co = 128;
  final static int cob = 192;
  final static int coh = 255;
  final static int cohb = 255;
  
  static final GradientPaint[] paintData = {
    makePaint(0, 0, cd, cdb, 2.0),
    makePaint(0, cd, 0, cdb, 2.0),
    makePaint(0, cd, cd, cdb, 2.0),
    makePaint(cd, cd, 0, cdb, 2.0),
    makePaint(cd, 0, cd, cdb, 2.0),
  };
  static final GradientPaint[] paintObjIx = {
    makePaint(0, 0, co, cob, 1.0),
    makePaint(0, co, 0, cob, 1.0),
    makePaint(0, co, co, cob, 1.0),
    makePaint(co, co, 0, cob, 1.0),
    makePaint(co, 0, co, cob, 1.0),
  };
  static final GradientPaint[] paintObjIxHdr = {
    makePaint(0, 0, coh, cohb, 1.0),
    makePaint(0, coh, 0, cohb, 1.0),
    makePaint(0, coh, coh, cohb, 1.0),
    makePaint(coh, coh, 0, cohb, 1.0),
    makePaint(coh, 0, coh, cohb, 1.0),
  };
  
  static GradientPaint makePaint(int r, int g, int b, int brighter, double w) {
    return new GradientPaint(
        0,0,
        new Color(Math.min(255,r+brighter), Math.min(255,g+brighter), Math.min(255,b+brighter)),
        (int)(pageWidth/w), (int)(pageHeight/w),
        new Color(r,g,b));
  }
  static final Color colorIndexHeader = new Color(255,255,255);
  static final Color colorHover = new Color(255,255,0,150);
  static final Color colorProblem = new Color(255,0,0,255);
  
  static final Font font = MainFrame.DEFAULT_FONT;
  static int strWidth = -1; 
  
  private Rectangle clip = new Rectangle();
  private JScrollPane parent;
  private Set<Long> problemPixes = new HashSet<Long>();
  volatile int offsX;
  volatile int hoverPix = -1;
  
  public SpiffsPanel(JScrollPane parent) {
    setDoubleBuffered(true);
    this.parent = parent;
    parent.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        recalcSize();
        repaint();
      }
    });
    
    this.addMouseWheelListener(new MouseWheelListener() {
      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
        int rot = e.getWheelRotation();
        if (rot > 0) {
          magIx++;
        } else {
          magIx--;
        }
        magIx = Math.min(mags.length-1, Math.max(0, magIx));
        setMagnification(mags[magIx]);
      }
    });
    
    MouseAdapter mouseAdapter = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
      }

      @Override
      public void mouseMoved(MouseEvent e) {
        int newHoverPix = getPixFromMouseEvent(e);
        if (newHoverPix != hoverPix) {
          hoverPix = newHoverPix;
          repaint();
        }
      }
    };
    
    this.addMouseListener(mouseAdapter);
    this.addMouseMotionListener(mouseAdapter);
  }
  
  private int getPixFromMouseEvent(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();
    int magPageWidth = (int)Math.round(mag * pageWidth);
    int magPageHeight = (int)Math.round(mag * pageHeight);
    int w = SpiffsPanel.this.parent.getViewport().getViewRect().width;
    int ppb = Math.min(minPanelWidth / pageWidth, (int)Spiffs.pagesPerBlock());
    if (ppb == 0) ppb = 1;
    int pagesPerRow = Math.max(ppb, (w / (magPageWidth * ppb)) * ppb);
    if (x < offsX) return -1;
    x -= offsX;
    x /= magPageWidth;
    y /= magPageHeight;
    if (x > pagesPerRow) return -1;
    int pix = x + y * pagesPerRow;
    return pix;
  }
  
  public void recalcSize() {
    int magPageWidth = (int)Math.round(mag * pageWidth);
    int magPageHeight = (int)Math.round(mag * pageHeight);
    int w = SpiffsPanel.this.parent.getViewport().getViewRect().width;
    int ppb = Math.min(minPanelWidth / pageWidth, (int)Spiffs.pagesPerBlock());
    if (ppb == 0) ppb = 1;
    int pagesPerRow = Math.max(ppb, (w / (magPageWidth * ppb)) * ppb);
    
    int nw = Math.max(minPanelWidth, Math.max(w, pagesPerRow*magPageWidth));
    int nh = ((int)Spiffs.nbrOfPages() / pagesPerRow) * magPageHeight;
    Dimension d = new Dimension(nw, nh);
    setMinimumSize(d);
    setPreferredSize(d);
    setSize(d);
  }

  private void doPaint(Graphics2D g, double mag) {
    if (strWidth < 0) {
      strWidth = (int)font.getStringBounds("WWWWWWWW", g.getFontRenderContext()).getWidth();
    }
    int w = getWidth();
    int h = getHeight();
    g.setFont(font);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(Color.black);
    g.fillRect(0, 0, w, h);
    
    int magPageWidth = (int)Math.round(mag * pageWidth);
    int magPageHeight = (int)Math.round(mag * pageHeight);
    int ppb = Math.min(minPanelWidth/ pageWidth, (int)Spiffs.pagesPerBlock());
    if (ppb == 0) ppb = 1;
    int pagesPerRow = Math.max(ppb, (w / (magPageWidth * ppb)) * ppb);
    int y = 0;
    int pix = 0;
    offsX = Math.max(0, (w - pagesPerRow * magPageWidth)/2);
    g.getClipBounds(clip);
    int magHeight = (int)Math.round(h / mag);
    int magMinY = (int)Math.round((clip.y) / mag);
    int magMaxY = (int)Math.round((clip.y + clip.height) / mag);

    g.translate(offsX, 0);
    g.scale(mag, mag);
    double invmag = 1.0 / mag;
    int magGrid = (int)Math.round(mag * grid);

    while (y < magHeight) {
      if (y + pageHeight < magMinY) {
        y += pageHeight;
        pix += pagesPerRow;
        continue;
      }
      for (int i = 0; i < pagesPerRow; i++) {
        SpiffsPage p = SpiffsPage.getPage(pix);
        if (p == null) return;

        g.translate(i * pageWidth, y);
        
        g.scale(invmag, invmag);
        if (!problemPixes.isEmpty()) {
          if (problemPixes.contains((long)pix)) {
            g.setColor(colorProblem);
            g.fillRect(-magGrid, -magGrid, magPageWidth+magGrid, magPageHeight+magGrid);
          }
        }
        if (pix == hoverPix) {
          g.setColor(colorHover);
          g.fillRect(-magGrid, -magGrid, magPageWidth+magGrid, magPageHeight+magGrid);
        }
        g.scale(mag, mag);

        paintPage(g, p, mag, magGrid, magPageWidth, magPageHeight);
        
        g.translate(-i * pageWidth, -y);
        
        pix++;
      }
      y += pageHeight;
      if (y > magMaxY) {
        break;
      }
    }
    
    g.scale(invmag, invmag);
    g.translate(-offsX, 0);
  }
  
  private void drawStringRight(Graphics2D g, String s, int y, int width) {
    g.drawString(s, width - ((int)g.getFontMetrics().stringWidth(s)), y);
  }
  
  public void paintPage(Graphics2D g, SpiffsPage p, double mag,
      int spacing, int width, int height) {
    double invmag = 1.0 / mag;
    if (p.isLookUp()) {
      g.setPaint(paintLUT);
    } else if (p.isDeleted()) {
      g.setPaint(paintDeleted);
    } else if (p.isFree()) {
      g.setPaint(paintFree);
    } else {
      int cix = (int)Spiffs.cleanObjectId(p.getObjectId()) % paintData.length;
      if (p.isObjectIndex()) {
        g.setPaint(p.isObjectIndexHeader() ? paintObjIxHdr[cix] : paintObjIx[cix]);
      } else {
        g.setPaint(paintData[cix]);
      }
    }
    g.fillRect(grid/2, grid/2, pageWidth-grid, pageHeight-grid);

    g.scale(invmag, invmag);
    if (!p.isDeleted() && p.isObjectIndexHeader()) {
      g.setColor(colorIndexHeader);
      g.drawRect(0, 0, width-spacing-1, height-spacing-1);

    }
    if (strWidth > 0 && width - spacing >= strWidth) {
      g.translate(spacing/4.0, spacing/4.0);
      paintDescriptive(g, p, width-spacing*3/2, height-spacing*3/2);
      g.translate(-spacing/4.0, -spacing/4.0);
    }
    g.scale(mag, mag);
  }
  
  private final Rectangle oldClip = new Rectangle();
  private void paintDescriptive(Graphics2D g, SpiffsPage p, int width, int height) {
    int h = font.getSize();
    int y = h;
    g.getClipBounds(oldClip);
    g.setClip(oldClip.intersection(new Rectangle(0, 0, width, height)));
    g.setColor(!p.isLookUp() && (p.isDeleted() || p.isFree()) ? Color.white : Color.black);
    drawStringRight(g, p.getPageIndexString(), y, width);
    if (width < 2 * strWidth) y+=h;
    
    if (p.isLookUp()) {
      g.drawString("LOOKUP", 0, y);
      y+=h;
      g.drawString("BLK " + p.getBlockIndexString(), 0, y);
      y+=h;
      
      if (height > h * 16) {
        int lutIx = 0;
        lutLoop:
        while (y < height) {
          int x = 0;
          while (x + strWidth + 4 < width) {
            g.drawString(Spiffs.formatObjId(p.readLUTEntry(lutIx++)), x, y);
            if (lutIx >= Spiffs.cfg.logPageSize / Spiffs.cfg.sizeObjId) {
              break lutLoop;
            }
            x += strWidth + 4;
          }
          y += h;
        }
      }
    } else if (p.isFree()) {
      g.drawString("FREE", 0, y);
      y+=h;
    } else {
      if (p.isDeleted()) {
        g.drawString("DELETED", 0, y);
        g.setColor(Color.lightGray);
        y+=h;
      }
      if (p.isObjectIndexHeader()) {
        g.drawString("INDEXHDR", 0, y);
      } else if (p.isObjectIndex()) {
        g.drawString("INDEX", 0, y);
      } else {
        g.drawString("DATA", 0, y);
      }
      y+=h;
      g.drawString(Spiffs.formatObjId(Spiffs.cleanObjectId(p.getObjectId())), 0, y);
      y+=h;
      g.drawString(p.getSpanIndexString(), 0, y);
      y+=h;
      g.drawString(p.getFlagsString(), 0, y);
      y+=h;
      
      if (p.isObjectIndexHeader()) {
        y+=h/2;
        g.drawString(p.getName(), 0, y);
        y+=h;
        g.drawString("LEN:" + p.getSizeString(), 0, y);
        y+=h;
      }
    }
    g.setClip(oldClip);
  }
  
  public void paint(Graphics g) {
    doPaint((Graphics2D)g, mag);
  }
  
  public void setMagnification(double mag) {
    this.mag = mag;
    recalcSize();
    repaint();
  }
  
  public double getMagnification() {
    return mag;
  }
  
  public void addProblem(SpiffsPage p) {
    if (p != null) problemPixes.add(p.getPageIndex());
  }
  
  public void clearProblems() {
    problemPixes.clear();
  }

}
