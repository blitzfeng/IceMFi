package com.og.filemanager.util;

import android.app.Activity;
import android.preference.PreferenceManager;

public abstract class UIUtils {

	public static void setThemeFor(Activity act) {
		if (PreferenceManager.getDefaultSharedPreferences(act).getBoolean("usedarktheme", true)) {
			act.setTheme(com.og.filemanager.R.style.Theme_Dark);
		} else {
			act.setTheme(com.og.filemanager.R.style.Theme_Light_DarkTitle);
		}
	}
	
	public static boolean shouldDialogInverseBackground(Activity act){
		return !PreferenceManager.getDefaultSharedPreferences(act).getBoolean("usedarktheme", true);
	}
}
