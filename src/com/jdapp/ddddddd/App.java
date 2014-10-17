package com.jdapp.ddddddd;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * my application to hold static variable
 * @author Owner
 *
 */
public class App extends Application {


	public enum ZoomMode {
		FIT_WIDTH_OR_HEIGHT(0), FIT_WIDTH_AUTO_SPLIT(1), FIT_WIDTH(2), FIT_HEIGHT(3), FIT_SCREEN(4);

		public static ZoomMode fromValue(int value) {
			switch (value) {
			case 0:
				return FIT_WIDTH_OR_HEIGHT;
			case 1:
				return FIT_WIDTH_AUTO_SPLIT;
			case 2:
				return FIT_WIDTH;
			case 3:
				return FIT_HEIGHT;
			case 4:
				return FIT_SCREEN;
			default:
				return null;
			}
		}

		private final int value;

		ZoomMode(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}
	}

	private static final String TAG = "APP";

	public static int DEBUG = -1;

	public static Context CONTEXT;
	public static String NAME;
	public static String PACKAGE;
	public static String VERSION_NAME;
	public static int VERSION_CODE;

	public static SharedPreferences APP_PREFERENCES = null;

	public static File APP_FILES_DIR;
	public static File APP_CACHE_DIR;
	public static File APP_THUMB_DIR;
	public static File APP_EXTERNAL_FILES_DIR;
	public static File APP_EXTERNAL_CACHE_DIR;

	// Settings
	public static float WIDTH_AUTO_SPLIT_THRESHOLD = 1.0f;
	public static float WIDTH_AUTO_SPLIT_MARGIN = .2f;
	public static int MAX_RETRY_DOWNLOAD_IMG = 3;
	public static int MAX_CACHE_IMGS = 400;
	
	public static final String salt = "oaodfoieiiw74243988FDGHJ@#$%^&JJJFEYUUINGG";
	public static final String PROGRESS_RECORD_NAME = "progress_record";
	public static String key;
	public static final String bundleKeyFileinfo = "zipfileinfo";
	public static String sessionApi;
	public static String sessionBaiduPan;

	public static SharedPreferences getSharedPreferences() {
		try {
			if (APP_PREFERENCES == null) {
				APP_PREFERENCES = PreferenceManager.getDefaultSharedPreferences(CONTEXT);
			}
			return APP_PREFERENCES;
		} catch (NullPointerException e) {
			e.printStackTrace();
			Log.e(TAG, "Null SharedPreferences.");
		}
		return null;
	}

	public static int getPageOrientation() {
		return Integer.parseInt(getSharedPreferences().getString("iPageOrientation", "2"));
	}

	public static ZoomMode getPageZoomMode() {
		return ZoomMode.fromValue(Integer.parseInt(getSharedPreferences().getString("iPageZoomMode", "0")));
	}

	public static void setPageZoomMode(ZoomMode mode) {
		APP_PREFERENCES.edit().putString("iPageZoomMode", "" + mode.value()).commit();
	}

	public static int getPreloadPages() {
		return Integer.parseInt(getSharedPreferences().getString("iPreloadPages", "2"));
	}

	@Override
	public void onCreate() {
		CONTEXT = this;
		NAME = getString(R.string.app_name);
		PACKAGE = getClass().getPackage().getName();
		try {
			VERSION_NAME = getPackageManager().getPackageInfo(PACKAGE, 0).versionName;
			VERSION_CODE = getPackageManager().getPackageInfo(PACKAGE, 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			Log.e(TAG,"Fail to get version code.");
		}
		
		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
		
		APP_FILES_DIR = getFilesDir();
		APP_CACHE_DIR = getCacheDir();
		APP_THUMB_DIR = new File(APP_CACHE_DIR, "thumbs");
		if (!APP_THUMB_DIR.exists()) {
		    APP_THUMB_DIR.mkdir();
		}
		APP_EXTERNAL_FILES_DIR = CONTEXT.getExternalFilesDir(null);
		APP_EXTERNAL_CACHE_DIR = CONTEXT.getExternalCacheDir();
		Log.d(TAG, "APP_FILES_DIR:"+APP_FILES_DIR);
		Log.d(TAG, "APP_CACHE_DIR:"+APP_CACHE_DIR);
		Log.d(TAG, "APP_EXTERNAL_FILES_DIR:"+APP_EXTERNAL_FILES_DIR);
		Log.d(TAG, "APP_EXTERNAL_CACHE_DIR:"+APP_EXTERNAL_CACHE_DIR);
	}
}
