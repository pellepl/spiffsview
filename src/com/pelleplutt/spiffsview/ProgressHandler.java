package com.pelleplutt.spiffsview;

import java.util.ArrayList;
import java.util.List;

public class ProgressHandler implements Progressable {
  List<ProgressListener> listeners = new ArrayList<ProgressListener>();
  Progressable wrapper;
  
  public ProgressHandler(Progressable wrapper) {
    this.wrapper = wrapper;
  }
  
  public void addListener(ProgressListener p) {
    if (!listeners.contains(p)) listeners.add(p);
  }

  public void removeListener(ProgressListener p) {
    if (!listeners.contains(p)) listeners.add(p);
  }
  
  public void fireStarted() {
    for (ProgressListener p : listeners) {
      p.started(wrapper);
    }
  }

  public void fireStopped(String message) {
    for (ProgressListener p : listeners) {
      p.stopped(wrapper, message);
    }
  }

  public void fireWork(double percentage) {
    for (ProgressListener p : listeners) {
      p.work(wrapper, percentage);
    }
  }

}
