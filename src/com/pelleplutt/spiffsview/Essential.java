package com.pelleplutt.spiffsview;


/**
 * Parameters used during compilation 
 * @author petera
 */
public interface Essential {
	/** App name */
	String name = "SpiffsView";
	/** Version major */
	int vMaj = 0;
	/** Version minor */
	int vMin = 0;
	/** Version micro */
	int vMic = 1;
	/** Path for app data */
	String userSettingPath = ".spiffsview";
  /** Name of settings file */
  String settingsFile = "spiffsview.settings";

  /** Logging */
	static final boolean LOG_C = true;
	static final boolean LOG_CLASS = true;
	static final boolean LOG_METHOD = true;
	static final boolean LOG_LINE = true;
	static final String LOG_SETTING_FILE_NAME = ".spiffsview.log";
	
}
