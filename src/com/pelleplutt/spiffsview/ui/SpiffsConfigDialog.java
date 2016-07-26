package com.pelleplutt.spiffsview.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.pelleplutt.spiffsview.SpiffsConfig;
import com.pelleplutt.util.Log;

public class SpiffsConfigDialog extends JDialog implements FocusListener {
  File file;
  SpiffsConfig config;
  long fileSize;
  
  IntegerTextField uiFsOffset;
  IntegerTextField uiFsSize;
  JRadioButton uiEndianessBig;
  JRadioButton uiEndianessLittle;
  IntegerTextField uiLogBlockSz;
  IntegerTextField uiLogPageSz;
  IntegerTextField uiFilenameSz;
  JComboBox<Integer> uiObjIdSz;
  JComboBox<Integer> uiPageIxSz;
  JComboBox<Integer> uiSpanIxSz;
  JCheckBox uiCfgAlignedObjIxTbl;
  JCheckBox uiCfgMagic;
  JCheckBox uiCfgMagicLength;

  public SpiffsConfigDialog(File f) {
    super(MainFrame.inst(), false);
    file = f;
    fileSize = f.length();
    setTitle(f.getName() + " [" + fileSize + " bytes]");
  }
  
  public void openConfig(SpiffsConfig cfg) {
    if (cfg != null) {
      config = cfg.clone();
    } else {
      config = new SpiffsConfig();
      config.physSize = fileSize;
      config.physOffset = 0;
      config.bigEndian = true;
      config.fileNameSize = 32;
      config.logBlockSize = 64 * 1024;
      config.logPageSize = 256;
      config.sizeObjId = 2;
      config.sizePageIx = 2;
      config.sizeSpanIx = 2;
      config.alignObjectIndexTables = false;
      config.magic = true;
      config.magicLength = true;
    }
    build();
    pack();
    setLocationRelativeTo(MainFrame.inst());
    setVisible(true);
  }
  
  private void build() {
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(buildPanel(), BorderLayout.CENTER);
    JPanel butPanel = new JPanel();
    butPanel.add(new JButton("OK"));
    butPanel.add(new JButton("Cancel"));
    butPanel.add(new JButton("Apply"));
    getContentPane().add(butPanel, BorderLayout.SOUTH);
  }
  
  private JPanel buildPanel() {
    JPanel p = new JPanel(new GridBagLayout());
    uiFsSize = new IntegerTextField();
    uiFsOffset = new IntegerTextField();;
    uiEndianessBig = new JRadioButton("Big");
    uiEndianessLittle = new JRadioButton("Little");
    uiLogBlockSz = new IntegerTextField();
    uiLogPageSz = new IntegerTextField();
    uiFilenameSz = new IntegerTextField();
    Integer[] typeSizes = {8,16,32};
    uiObjIdSz = new JComboBox<Integer>(typeSizes);
    uiPageIxSz = new JComboBox<Integer>(typeSizes);
    uiSpanIxSz = new JComboBox<Integer>(typeSizes);
    uiCfgAlignedObjIxTbl = new JCheckBox();
    uiCfgMagic = new JCheckBox();
    uiCfgMagicLength = new JCheckBox();
    
    uiFsSize.setValue((int)config.physSize);
    uiFsOffset.setValue((int)config.physOffset);
    if (config.bigEndian) 
      uiEndianessBig.setSelected(true);
    else
      uiEndianessLittle.setSelected(true);
    uiLogPageSz.setValue((int)config.logPageSize);
    uiLogBlockSz.setValue((int)config.logBlockSize);
    uiFilenameSz.setValue((int)config.fileNameSize);
    uiObjIdSz.setSelectedItem(config.sizeObjId * 8);
    uiPageIxSz.setSelectedItem(config.sizePageIx * 8);
    uiSpanIxSz.setSelectedItem(config.sizeSpanIx * 8);
    uiCfgAlignedObjIxTbl.setSelected(config.alignObjectIndexTables);
    uiCfgMagic.setSelected(config.magic);
    uiCfgMagicLength.setSelected(config.magicLength);
    
    uiFsOffset.addFocusListener(this);
    uiFsSize.addFocusListener(this);
    uiEndianessBig.addFocusListener(this);
    uiEndianessLittle.addFocusListener(this);
    uiLogBlockSz.addFocusListener(this);
    uiLogPageSz.addFocusListener(this);
    uiFilenameSz.addFocusListener(this);
    uiObjIdSz.addFocusListener(this);
    uiPageIxSz.addFocusListener(this);
    uiSpanIxSz.addFocusListener(this);
    uiCfgAlignedObjIxTbl.addFocusListener(this);
    uiCfgMagic.addFocusListener(this);
    uiCfgMagicLength.addFocusListener(this);
    
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(2, 4, 2, 4);
    
    c.gridy = 0;
    c.gridx = 0;
    c.gridwidth = 1;
    p.add(new JLabel("FS File Offset"), c);
    c.gridx = 1;
    c.gridwidth = 2;
    p.add(uiFsOffset, c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    p.add(new JLabel("FS Size"), c);
    c.gridx = 1;
    c.gridwidth = 2;
    p.add(uiFsSize, c);

    ButtonGroup uiEndianess = new ButtonGroup(); 
    uiEndianess.add(uiEndianessBig);
    uiEndianess.add(uiEndianessLittle);
    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    p.add(new JLabel("Endianess"), c);
    c.gridx = 1;
    p.add(uiEndianessBig, c);
    c.gridx = 2;
    p.add(uiEndianessLittle, c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    p.add(new JLabel("Logical Block Size"), c);
    c.gridx = 1;
    c.gridwidth = 2;
    p.add(uiLogBlockSz, c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    p.add(new JLabel("Logical Page Size"), c);
    c.gridx = 1;
    c.gridwidth = 2;
    p.add(uiLogPageSz, c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    p.add(new JLabel("File name size"), c);
    c.gridx = 1;
    p.add(uiFilenameSz, c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    p.add(new JLabel("Object Id Size"), c);
    c.gridx = 1;
    p.add(uiObjIdSz, c);
    c.gridx = 2;
    p.add(new JLabel("bits"), c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    p.add(new JLabel("Page Index Size"), c);
    c.gridx = 1;
    p.add(uiPageIxSz, c);
    c.gridx = 2;
    p.add(new JLabel("bits"), c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    p.add(new JLabel("Span Index Size"), c);
    c.gridx = 1;
    p.add(uiSpanIxSz, c);
    c.gridx = 2;
    p.add(new JLabel("bits"), c);

    c.gridy++;
    c.gridx = 0;
    p.add(new JLabel("Aligned Object Index Tables"), c);
    c.gridx = 1;
    p.add(uiCfgAlignedObjIxTbl, c);

    c.gridy++;
    c.gridx = 0;
    p.add(new JLabel("Magic Enabled"), c);
    c.gridx = 1;
    p.add(uiCfgMagic, c);

    c.gridy++;
    c.gridx = 0;
    p.add(new JLabel("Magic Length Enabled"), c);
    c.gridx = 1;
    p.add(uiCfgMagicLength, c);
    
    return p;
  }

  @Override
  public void focusGained(FocusEvent e) {
    validateInput(e.getSource());
  }

  @Override
  public void focusLost(FocusEvent e) {
    validateInput(e.getSource());
  }
  
  private void validateInput(Object source) {
    int fsSize = uiFsSize.getValue();
    int fsOffset = uiFsOffset.getValue();
    int logBlockSz = uiLogBlockSz.getValue();
    int logPageSz = uiLogPageSz.getValue();
    int filenameSz = uiFilenameSz.getValue();
    
    if (source == uiFsOffset && fsOffset > fileSize) {
      fsOffset = (int)(fileSize - fsSize);
      uiFsOffset.setValue(fsOffset);
    }
    if (source == uiFsSize && fsSize > fileSize) {
      fsSize = (int)(fileSize - fsOffset);
      uiFsSize.setValue(fsSize);
    }
    if (source == uiFsOffset && fsOffset + fsSize > fileSize) {
      fsSize = (int)(fileSize - fsOffset);
      uiFsSize.setValue(fsSize);
    }
    if (source == uiFsSize && fsOffset + fsSize > fileSize) {
      fsOffset = (int)(fileSize - fsSize);
      uiFsOffset.setValue(fsOffset);
    }
    if (fsSize > 0) {
      if (fsSize / (logBlockSz == 0 ? 1 : logBlockSz) < 4) {
        logBlockSz = fsSize / 4;
        uiLogBlockSz.setValue(logBlockSz);
      }
    }
    if (logBlockSz > 0) {
      if (logBlockSz / (logPageSz == 0 ? 1 : logPageSz)  < 4) {
        logPageSz = logBlockSz / 4;
        uiLogPageSz.setValue(logPageSz);
      }
    }
    if (logPageSz > 0) {
      if (filenameSz > logPageSz / 2) {
        filenameSz = logPageSz / 2;
        uiFilenameSz.setValue(filenameSz);
      }
    }

    config.physSize = fsSize;
    config.physOffset = fsOffset;
    config.bigEndian = uiEndianessBig.isSelected();
    config.logBlockSize = logBlockSz;
    config.logPageSize = logPageSz;
    config.sizeObjId = ((Integer)uiObjIdSz.getSelectedItem()).intValue()/8;
    config.sizePageIx = ((Integer)uiPageIxSz.getSelectedItem()).intValue()/8;
    config.sizeSpanIx = ((Integer)uiSpanIxSz.getSelectedItem()).intValue()/8;
    config.alignObjectIndexTables = uiCfgAlignedObjIxTbl.isSelected();
    config.magic = uiCfgMagic.isSelected();
    config.magicLength = uiCfgMagicLength.isSelected();
  }
  
  class IntegerTextField extends JTextField {
    String lastValid = "";
    public IntegerTextField() {
      super();
      getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent e) {
          verify();
        }
        public void removeUpdate(DocumentEvent e) {
          verify();
        }
        public void insertUpdate(DocumentEvent e) {
          verify();
        }
      });
    }
    
    private void verify() {
      String text = getText().toLowerCase();
      try {
        // scan invalid chars
        for (int i = 0; i < text.length(); i++) {
          char c = text.charAt(i);
          if (i == 0 && (c == 'k' || c == 'm')) {
            throw new Exception(c + " in beginning, " + text);
          }
          if (!(c >= '0' && c <= '9' || c == 'k' || c == 'm')) {
            throw new Exception("invalid char " + c + ", " + text);
          }
          if (i != text.length()-1 && ((c == 'k' || c == 'm'))) {
            text = text.substring(0, i);
            final String sText = text; 
            SwingUtilities.invokeLater(new Runnable() { public void run() {setText(sText);}});
            break;
          }
        }
        
        lastValid = text;
      } catch (Throwable t) {
        //t.printStackTrace();
        final String sText = lastValid; 
        SwingUtilities.invokeLater(new Runnable() { public void run() {setText(sText);}});
      }
    }
    
    public int getValue() {
      String intString = getText().toLowerCase().trim();
      int mul = 1;
      if (intString.endsWith("k")) {
        intString = intString.substring(0, intString.length()-1);
        mul = 1024;
      } else if (intString.endsWith("m")) {
        intString = intString.substring(0, intString.length()-1);
        mul = 1024*1024;
      }
      if (intString.length() == 0) {
        return 0;
      } else {
        return Integer.parseInt(intString) * mul;
      }
    }
    
    public void setValue(int v) {
      String endMul = "";
      if (v >= 1024*1024 && (v & (1024*1024-1)) == 0) {
        endMul = "M";
        v /= 1024*1024;
      }
      else if (v >= 1024 && (v & (1024-1)) == 0) {
        endMul = "k";
        v /= 1024;
      }
      String s = Integer.toString(v)+endMul;
      Log.println(s);
      
      setText(s);
    }
  }
}
