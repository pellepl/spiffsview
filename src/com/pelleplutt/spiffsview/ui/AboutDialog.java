package com.pelleplutt.spiffsview.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.pelleplutt.spiffsview.Essential;
import com.pelleplutt.util.UIUtil;

public class AboutDialog extends JDialog {
  public AboutDialog() {
    super(MainFrame.inst(), "About...", true);
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    JLabel header = new JLabel("SPI Flash File System viewer");
    JLabel icon = new JLabel(UIUtil.createImageIcon("res/spiffsico.png"));
    JLabel app = new JLabel(Essential.name + " v" + Essential.vMaj + "." + Essential.vMin
        + "." + Essential.vMic);
    JLabel author = new JLabel("(C) 2016 Peter Andersson");
    JLabel url1 = new JLabel("https://github.com/pellepl/spiffsview");
    JLabel url2 = new JLabel("https://github.com/pellepl/spiffs");
    MouseAdapter urlListener = new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (Desktop.isDesktopSupported()) {
          try {
            Desktop.getDesktop().browse(URI.create(((JLabel)e.getSource()).getText()));
          } catch (IOException ioe) {}
        } else {}
      }
    }; 
    url1.setForeground(Color.blue);
    url1.addMouseListener(urlListener);
    url2.setForeground(Color.blue);
    url2.addMouseListener(urlListener);
    JButton exit = new JButton(new AbstractAction("Great help that was!") {
      @Override
      public void actionPerformed(ActionEvent e) {
        AboutDialog.this.dispose();
      }
    });
    header.setAlignmentX(Component.CENTER_ALIGNMENT);
    icon.setAlignmentX(Component.CENTER_ALIGNMENT);
    app.setAlignmentX(Component.CENTER_ALIGNMENT);
    author.setAlignmentX(Component.CENTER_ALIGNMENT);
    url1.setAlignmentX(Component.CENTER_ALIGNMENT);
    url2.setAlignmentX(Component.CENTER_ALIGNMENT);
    exit.setAlignmentX(Component.CENTER_ALIGNMENT);
    p.add(header);
    p.add(new JLabel(" "));
    p.add(icon);
    p.add(new JLabel(" "));
    p.add(app);
    p.add(author);
    p.add(new JLabel(" "));
    p.add(url1);
    p.add(url2);
    p.add(new JLabel(" "));
    p.add(exit);
    p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    getContentPane().add(p);
    pack();
    setLocationRelativeTo(MainFrame.inst());
    setVisible(true);
  }
}
