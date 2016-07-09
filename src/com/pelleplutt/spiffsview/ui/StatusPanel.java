package com.pelleplutt.spiffsview.ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatusPanel extends JPanel {
  JLabel status;
  volatile double progress;
  
  public StatusPanel() {
    setLayout(new FlowLayout(FlowLayout.LEFT));
    status = new JLabel(" ");
    add(status);
  }
  
  public void paint(Graphics g1) {
    Graphics2D g = (Graphics2D)g1;
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    super.paint(g);
    if (progress > 0) {
      progress = Math.min(1, progress);
      g.setColor(getBackground());
      g.setXORMode(Color.blue);
      g.fillRoundRect(0,0,(int)((double)getWidth()*progress), getHeight(),4,4);
    }
  }
  
  public void setText(String text) {
    status.setText(text);
    repaint();
  }
  
  public void setProgress(double p) {
    progress = p;
    repaint();
  }

}
