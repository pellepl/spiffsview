package com.pelleplutt.spiffsview;

public interface ProgressListener {
  public void started(Progressable p);
  public void work(Progressable p, double percentage);
  public void stopped(Progressable p, String message);
}
