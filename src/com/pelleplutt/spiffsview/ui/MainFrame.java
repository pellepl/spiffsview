package com.pelleplutt.spiffsview.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.pelleplutt.spiffsview.Analyzer;
import com.pelleplutt.spiffsview.Essential;
import com.pelleplutt.spiffsview.Problem;
import com.pelleplutt.spiffsview.Settings;
import com.pelleplutt.spiffsview.Spiffs;
import com.pelleplutt.spiffsview.SpiffsPage;
import com.pelleplutt.util.AppSystem;
import com.pelleplutt.util.UIUtil;

public class MainFrame extends JFrame {
  public static final Font DEFAULT_FONT = Font.decode("courier-plain-12");
  private static final long serialVersionUID = 4632950413113732498L;
  private static MainFrame _inst;
  SpiffsPanel pagePanel;
  JTree problemTree;
  TreeCellRenderer problemTreeRenderer = new ProblemTreeCellRenderer();
  TreeModel problemTreeModel = new ProblemTreeModel();
  
  static public synchronized MainFrame inst() {
    if (_inst == null) {
      _inst = new MainFrame();
    }
    return _inst;
  }
  
  private MainFrame() {
    setup();
  }

  private void setup() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Throwable t) {
    }
    setIconImage(UIUtil.createImageIcon("res/spiffsview.png").getImage());
    setTitle(Essential.name + " v" + Essential.vMaj + "." + Essential.vMin
        + "." + Essential.vMic);
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowListener() {
      @Override
      public void windowOpened(WindowEvent e) {
      }

      @Override
      public void windowIconified(WindowEvent e) {
      }

      @Override
      public void windowDeiconified(WindowEvent e) {
      }

      @Override
      public void windowDeactivated(WindowEvent e) {
      }

      @Override
      public void windowClosing(WindowEvent e) {
        doExit();
      }

      @Override
      public void windowClosed(WindowEvent e) {
      }

      @Override
      public void windowActivated(WindowEvent e) {
      }
    });
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        build();
        pack();
        setLocationByPlatform(true);
        setVisible(true);
      }
    });
  }
  
  private void build() {
    Container c = getContentPane();
    c.setLayout(new BorderLayout());
    
    JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    
    JScrollPane pageScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    pagePanel = new SpiffsPanel(pageScroll);
    pageScroll.setViewportView(pagePanel);
    
    pageScroll.setPreferredSize(new Dimension(700,500));
    pageScroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE); // fix artefacts on xubuntu while horizontal scrolling
    pagePanel.recalcSize();
    
    problemTree = new JTree(problemTreeModel);
    problemTree.setRootVisible(true);
    problemTree.setCellRenderer(problemTreeRenderer);
    problemTree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        TreePath[] selectedPaths = problemTree.getSelectionPaths();
        pagePanel.clearProblems();
        if (selectedPaths != null) {
          for (TreePath p : selectedPaths) {
            if (p.getPathCount() == 2) {
              Problem prob = (Problem)p.getPathComponent(1);
              pagePanel.addProblem(prob.page);
              pagePanel.addProblem(prob.refPage);
            } else if (p.getPathCount() == 3) {
              SpiffsPage page = (SpiffsPage)p.getPathComponent(2);
              pagePanel.addProblem(page);
            }
          }
        }
        pagePanel.repaint();
      }
    });
    JScrollPane treeScroll = new JScrollPane(problemTree, 
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    treeScroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE); // fix artefacts on xubuntu while horizontal scrolling

    splitter.setTopComponent(pageScroll);
    splitter.setBottomComponent(treeScroll);
    splitter.setDividerSize(4);
    
    c.add(splitter, BorderLayout.CENTER);
  }

  
  private void doExit() {
    Settings.inst().saveSettings();
    AppSystem.disposeAll();
    this.dispose();
  }
  
  
  
  class ProblemTreeCellRenderer implements TreeCellRenderer {
    JPanel commonRenderer = new JPanel(new BorderLayout());
    JLabel commonRendererContent = new JLabel();
    TreePagePainter treePagePainter = new TreePagePainter();
    DataPanel hexPainter = new DataPanel(); 

    public ProblemTreeCellRenderer() {
      commonRenderer.add(commonRendererContent, BorderLayout.CENTER);
      commonRendererContent.setFont(DEFAULT_FONT);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean selected, boolean expanded, boolean leaf, int row,
        boolean hasFocus) {
      Component renderer;
      if (value instanceof SpiffsPage) {
        treePagePainter.setPage((SpiffsPage)value);
        renderer = treePagePainter;
      } else if (value instanceof Long) {
        long pix = ((Long)value).longValue();
        hexPainter.setData((int)Spiffs.physOffsetOfPage(pix),
            SpiffsPage.getPage(pix).readContents());
        renderer = hexPainter;
      } else {
        commonRendererContent.setText(value.toString());
        renderer = commonRenderer;
      }
      
      if (renderer != null && !(value instanceof Long)) {
        renderer.setBackground(selected ? Color.lightGray : Color.white);
        renderer.setForeground(selected ? Color.blue : Color.black);
      }
      return renderer;
    }
  }
  

  
  class TreePagePainter extends JPanel {
    SpiffsPage page;
    JLabel label;
    public TreePagePainter() {
      JPanel pageGfx = new JPanel() {
        @Override
        public void paint(Graphics g1)  {
          Graphics2D g = (Graphics2D)g1;
          g.translate(1, 1);
          pagePanel.paintPage(g, page, 1.0, 0, 8, 12);
          g.translate(-1, -1);
          g.setColor(Color.lightGray);
          g.drawRect(0, 0, 8, 12);
        }
      };
      pageGfx.setPreferredSize(new Dimension(8+2,12+3));
      label = new JLabel();
      label.setFont(DEFAULT_FONT);
      setLayout(new BorderLayout());
      add(pageGfx, BorderLayout.WEST);
      add(label, BorderLayout.CENTER);
    }
    
    public void setPage(SpiffsPage p) {
      if (p != null) {
        page = p;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%8x: ", page.getPageIndex()));
        if (page.isLookUp()) {
          sb.append("lookup, block " + page.getBlockIndexString());
        } else if (page.isFree()) {
          sb.append("free");
        }
        else {
          sb.append(
              (Spiffs.isObjectIndex(page.getObjectId()) ? '*' : "") + 
              Spiffs.formatObjId(Spiffs.cleanObjectId(page.getObjectId())) + "/" + 
                  page.getSpanIndexString() + "  ");
          sb.append(page.isFlagUsed() ? 'U' : 'u');
          sb.append(page.isFlagFinalized() ? 'F' : 'f');
          sb.append(page.isFlagIndex() ? 'I' : 'i');
          sb.append(page.isFlagDeleted() ? 'D' : 'd');
          sb.append(page.isFlagIndexDeleted() ? 'X' : 'x');
          if (page.isDeleted()) {
            sb.append("(deleted)  ");
          } else {
            sb.append("  ");
          }
          if (page.isObjectIndexHeader()) {
            sb.append("OBJ.IX.HDR ");
          } else if (page.isObjectIndex()) {
            sb.append("OBJ.IX ");
          } else {
            sb.append("DATA ");
          }
        }
          
        label.setText(sb.toString());
      }
    }
    
    @Override
    public void setBackground(Color c) {
      super.setBackground(c);
      if (label != null) label.setBackground(c);
    }
    @Override
    public void setForeground(Color c) {
      super.setForeground(c);
      if (label != null) label.setForeground(c);
    }
  }
  
  
  
  class ProblemTreeModel implements TreeModel {
    Object root = new String("Problems");
    @Override
    public Object getRoot() {
      return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
      if (parent == root) {
        return Analyzer.getProblems().get(index);
      } else if (parent instanceof Problem) {
        Problem p = (Problem)parent;
        if (p.page != null && p.refPage != null) {
          if (index == 0) return p.page;
          if (index == 1) return p.refPage;
        }
        else if (p.page != null) {
          if (index == 0) return p.page;
        }
        else if (p.refPage != null) {
          if (index == 0) return p.refPage;
        }
      } else if (parent instanceof SpiffsPage) {
        return ((SpiffsPage)parent).getPageIndex();
      }
      return null;
    }

    @Override
    public int getChildCount(Object parent) {
      if (parent == root) {
        return Analyzer.getProblems().size();
      } else if (parent instanceof Problem) {
        Problem p = (Problem)parent;
        return (p.page == null ? 0 : 1) + (p.refPage == null ? 0 : 1);
      } else if (parent instanceof SpiffsPage) {
        return 1;
      }
      return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
      if (node instanceof Long) {
        return true;
      }
      return false;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
      if (parent == root) {
        return Analyzer.getProblems().indexOf(child);
      } else if (parent instanceof Problem) {
        Problem p = (Problem)parent;
        if (p.page != null && p.refPage != null) {
          if (child == p.page) return 0;
          if (child == p.refPage) return 1;
        }
        else if (p.page != null || p.refPage != null) {
          if (child == p.page || child == p.refPage) return 0;
        }
      }
      return 0;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
    }
    
  };


}
