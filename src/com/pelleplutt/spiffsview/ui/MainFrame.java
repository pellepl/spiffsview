package com.pelleplutt.spiffsview.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
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

import com.pelleplutt.spiffsview.AnalyzerConsistency;
import com.pelleplutt.spiffsview.Essential;
import com.pelleplutt.spiffsview.Problem;
import com.pelleplutt.spiffsview.ProgressListener;
import com.pelleplutt.spiffsview.Progressable;
import com.pelleplutt.spiffsview.Settings;
import com.pelleplutt.spiffsview.Spiffs;
import com.pelleplutt.spiffsview.SpiffsConfig;
import com.pelleplutt.spiffsview.SpiffsPage;
import com.pelleplutt.util.AppSystem;
import com.pelleplutt.util.Log;
import com.pelleplutt.util.UIUtil;

public class MainFrame extends JFrame implements ProgressListener {
  public static final Font DEFAULT_FONT = Font.decode("courier-plain-12");
  private static final long serialVersionUID = 4632950413113732498L;
  private static MainFrame _inst;
  SpiffsPanel pagePanel;
  JTree problemTree;
  TreeCellRenderer problemTreeRenderer = new ProblemTreeCellRenderer();
  TreeModel problemTreeModel = new ProblemTreeModel();
  StatusPanel statusPanel;
  AnalyzerConsistency consAnalyzer;
  boolean validSpiffsData;
  File curDumpFile;
  FileInputStream curDumpFileStream;
  FileChannel curDumpFileChannel;
  DataPanel pageDataPanel;
  
  static public synchronized MainFrame inst() {
    if (_inst == null) {
      _inst = new MainFrame();
    }
    return _inst;
  }
  
  private MainFrame() {
    setup();
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // Construction
  /////////////////////////////////////////////////////////////////////////////

  private void setup() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Throwable t) {
    }
    setIconImage(UIUtil.createImageIcon("res/spiffsico.png").getImage());
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
        buildUI();
        buildMenu();
        pack();
        setLocationByPlatform(true);
        setVisible(true);
      }
    });
  }
  
  JMenu recentFiles = new JMenu("Recent dumps");
  private void menuUpdateRecent() {
    String[] recent = Settings.inst().list(Settings.RECENT_FILES);
    recentFiles.removeAll();
    if (recent != null && recent.length > 0) {
      recentFiles.setEnabled(true);
      for (String path : recent) {
        recentFiles.add(new JMenuItem(new ActionOpenFileDumpRecent(new File(path))));
      }
    } else {
      recentFiles.setEnabled(false);
    }
  }
  
  private void buildMenu() {
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu("File");
    menuBar.add(menu);
    menu.add(new JMenuItem(new ActionOpenFileDump()));
    menu.add(recentFiles);
    menu.add(new JSeparator());
    menu.add(new JMenuItem(new ActionSpiffsConfig()));
    menu.add(new JSeparator());
    menu.add(new JMenuItem(new ActionExit()));
    menuUpdateRecent();

    menu = new JMenu("Help");
    menuBar.add(menu);
    menu.add(new JMenuItem(new ActionAbout()));

    
    setJMenuBar(menuBar);
  }
  
  private void buildUI() {
    Container c = getContentPane();
    c.setLayout(new BorderLayout());
    
    JSplitPane mainsplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    
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

    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.add(treeScroll, BorderLayout.CENTER);
    pageDataPanel = new DataPanel();
    bottomPanel.add(pageDataPanel, BorderLayout.EAST);
    
    mainsplitter.setTopComponent(pageScroll);
    mainsplitter.setBottomComponent(bottomPanel);
    mainsplitter.setDividerSize(4);
    
    statusPanel = new StatusPanel();
    
    c.add(mainsplitter, BorderLayout.CENTER);
    c.add(statusPanel, BorderLayout.SOUTH);
  }

  
  private void doExit() {
    Settings.inst().saveSettings();
    AppSystem.disposeAll();
    this.dispose();
    if (curDumpFile != null) {
      AppSystem.closeSilently(curDumpFileStream);
      try {
        curDumpFileChannel.close();
      } catch (IOException e) {
      }
    }
  }
  

  /////////////////////////////////////////////////////////////////////////////
  // Tree stuff
  /////////////////////////////////////////////////////////////////////////////
  
  
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
        return consAnalyzer.getProblems().get(index);
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
      if (consAnalyzer == null) return 0;
      if (parent == root) {
        return consAnalyzer.getProblems().size();
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
        return consAnalyzer.getProblems().indexOf(child);
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
  
  
  /////////////////////////////////////////////////////////////////////////////
  // MainFrame general ops 
  /////////////////////////////////////////////////////////////////////////////

  public void displayPageContents(int pix) {
    pageDataPanel.setData((int)Spiffs.physOffsetOfPage(pix),
        SpiffsPage.getPage(pix).readContents());
    repaint();
  }
  
  public void setStatus(String status) {
    statusPanel.setText(status);
  }
  
  public void refreshAll() {
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        setStatus("Loading...");
        SpiffsPage.update();
        
        // TODO AnalyzerConfig cfgAnalyzer = new AnalyzerConfig();
        // TODO cfgAnalyzer.analyze(Spiffs.cfg.physOffset, Spiffs.cfg.physSize);
        
        if (consAnalyzer != null) {
          consAnalyzer.removeListener(MainFrame.this);
        }
        consAnalyzer = new AnalyzerConsistency();
        consAnalyzer.addListener(MainFrame.this);
        setStatus("Analyzing...");
        consAnalyzer.analyze();
        setStatus("Analyzed, " + (consAnalyzer.getProblems().isEmpty() ? "OK" : ((consAnalyzer.getProblems().size() + " problems"))));
        problemTree.setModel(problemTreeModel);
        problemTree.repaint();
        problemTree.validate();
        problemTree.updateUI();
        validSpiffsData = true;
        repaint();
      }
    }, "refresher");
    t.setDaemon(true);
    t.start();
  }
  
  public boolean hasValidSpiffsData(){
    return validSpiffsData;
  }
  
  public File makeConfigPathFromDumpFile(File file) {
    return new File(file.getParentFile(), "." + file.getName() + ".spiffsview");
  }
  
  public SpiffsConfig loadConfig(File file) {
    SpiffsConfig cfg = null;
    File configFile = makeConfigPathFromDumpFile(file);
    if (configFile.exists() && configFile.isFile()) {
      // start trying with local file
      try {
        cfg = SpiffsConfig.load(configFile);
      } catch (IllegalArgumentException | IllegalAccessException | IOException e) {
        Log.printStackTrace(e);
      }
    } else {
      // next try with folder file
      configFile = new File(file.getParentFile(), "." + file.getParentFile().getName() + ".spiffsview");
      if (configFile.exists() && configFile.isFile()) {
        try {
          cfg = SpiffsConfig.load(configFile);
        } catch (IllegalArgumentException | IllegalAccessException | IOException e) {
          Log.printStackTrace(e);
        }
      }
    
    }
    return cfg;
  }
    
  public void loadSpiffsDump(File file) {
    FileInputStream fart;
    if (curDumpFile != null) {
      AppSystem.closeSilently(curDumpFileStream);
      try {
        curDumpFileChannel.close();
      } catch (IOException e) {
      }
      curDumpFile = null;
      curDumpFileStream = null;
      curDumpFileChannel = null;
      validSpiffsData = false;
    }
    try {
      fart = new FileInputStream(file);
      FileChannel fc = fart.getChannel();
      curDumpFile = file;
      curDumpFileStream = fart;
      curDumpFileChannel = fc;
      Spiffs.data = fc.map(FileChannel.MapMode.READ_ONLY, Spiffs.cfg.physOffset, Spiffs.cfg.physSize);
      Spiffs.data.order(Spiffs.cfg.bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
      setTitle(Essential.name + " v" + Essential.vMaj + "." + Essential.vMin
          + "." + Essential.vMic + " [" + file.getName() + "]");

      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          refreshAll();
        }
      });

    } catch (IOException e) {
    }
  }
  
  private void uiOpenDump(File file) {
    SpiffsConfig config = loadConfig(file);
    if (config == null) {
      SpiffsConfigDialog sfc = new SpiffsConfigDialog(file);
      sfc.openConfig(null);
    } else {
      Spiffs.cfg = config;
      loadSpiffsDump(file);
    }
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // Actions
  /////////////////////////////////////////////////////////////////////////////

  class ActionOpenFileDump extends AbstractAction {
    public ActionOpenFileDump() {
      super("Open file dump...");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      File file = UIUtil.selectFile(MainFrame.this, getValue(AbstractAction.NAME).toString(), "Open", true, false);
      if (file != null) {
        Settings.inst().listAdd(Settings.RECENT_FILES, file.getAbsolutePath());
        menuUpdateRecent();
        uiOpenDump(file);
      }
    }
  }

  class ActionOpenFileDumpRecent extends AbstractAction {
    File file;
    public ActionOpenFileDumpRecent(File file) {
      super(file.getName());
      this.file = file;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      if (file != null) {
        Settings.inst().listAdd(Settings.RECENT_FILES, file.getAbsolutePath());
        menuUpdateRecent();
        uiOpenDump(file);
      }
    }
  }

  class ActionSpiffsConfig extends AbstractAction {
    public ActionSpiffsConfig() {
      super("Spiffs config");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      if (curDumpFile != null) {
        SpiffsConfigDialog sfc = new SpiffsConfigDialog(curDumpFile);
        sfc.openConfig(Spiffs.cfg);
      }
    }
  }

  class ActionExit extends AbstractAction {
    public ActionExit() {
      super("Exit");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      doExit();
    }
  }

  class ActionAbout extends AbstractAction {
    public ActionAbout() {
      super("About...");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      new AboutDialog();
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // Progress listener stuff
  /////////////////////////////////////////////////////////////////////////////

  @Override
  public void started(Progressable p) {
    statusPanel.setProgress(0);
  }

  @Override
  public void work(Progressable p, double percentage) {
    statusPanel.setProgress(percentage);
  }

  @Override
  public void stopped(Progressable p, String message) {
    statusPanel.setProgress(0);
  }
}
