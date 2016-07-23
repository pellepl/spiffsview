package com.pelleplutt.spiffsview.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class SpiffsConfigPanel extends JPanel {

  public SpiffsConfigPanel(File f) {
    setLayout(new GridBagLayout());
    build();
  }
  
  private void build() {
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(2, 4, 2, 4);
    
    c.gridy = 0;
    c.gridx = 0;
    c.gridwidth = 1;
    add(new JLabel("FS File Offset"), c);
    c.gridx = 1;
    c.gridwidth = 2;
    add(new JTextField(), c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    add(new JLabel("FS Size"), c);
    c.gridx = 1;
    c.gridwidth = 2;
    add(new JTextField(), c);

    ButtonGroup bg = new ButtonGroup();
    JRadioButton bEndianessBig = new JRadioButton("Big");
    JRadioButton bEndianessLittle = new JRadioButton("Little");
    bg.add(bEndianessBig);
    bg.add(bEndianessLittle);
    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    add(new JLabel("Endianess"), c);
    c.gridx = 1;
    add(bEndianessBig, c);
    c.gridx = 2;
    add(bEndianessLittle, c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    add(new JLabel("Logical Block Size"), c);
    c.gridx = 1;
    c.gridwidth = 2;
    add(new JTextField(), c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    add(new JLabel("Logical Page Size"), c);
    c.gridx = 1;
    c.gridwidth = 2;
    add(new JTextField(), c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    add(new JLabel("File name size"), c);
    c.gridx = 1;
    add(new JTextField(), c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    add(new JLabel("Object Id Size"), c);
    c.gridx = 1;
    add(new JComboBox<Integer>(), c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    add(new JLabel("Page Index Size"), c);
    c.gridx = 1;
    add(new JComboBox<Integer>(), c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    add(new JLabel("Span Index Size"), c);
    c.gridx = 1;
    add(new JComboBox<Integer>(), c);

    c.gridy++;
    c.gridx = 0;
    add(new JLabel("Aligned Object Index Tables"), c);
    c.gridx = 1;
    add(new JCheckBox(), c);

    c.gridy++;
    c.gridx = 0;
    add(new JLabel("Magic Enabled"), c);
    c.gridx = 1;
    add(new JCheckBox(), c);

    c.gridy++;
    c.gridx = 0;
    add(new JLabel("Magic Length Enabled"), c);
    c.gridx = 1;
    add(new JCheckBox(), c);
  }

}
