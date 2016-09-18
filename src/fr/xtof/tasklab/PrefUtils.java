package fr.xtof.tasklab;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {
	/**
	 * Called to save supplied value in shared preferences against given key.
	 * @param context Context of caller activity
	 * @param key Key of value to save against
	 * @param value Value to save
	 */
	public static void saveToPrefs(Context context, String key, String value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key,value);
		editor.commit();
	}
	public static void saveToPrefs(Context context, String key, int value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(key,value);
		editor.commit();
	}

	/**
	 * Called to retrieve required value from shared preferences, identified by given key.
	 * Default value will be returned of no value found or error occurred.
	 * @param context Context of caller activity
	 * @param key Key to find value against
	 * @param defaultValue Value to return if no data found against given key
	 * @return Return the value found against given key, default if not found or any error occurs
	 */
	public static String getFromPrefs(Context context, String key, String defaultValue) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			return sharedPrefs.getString(key, defaultValue);
		} catch (Exception e) {
			e.printStackTrace();
			return defaultValue;
		}
	}
	public static int getFromPrefs(Context context, String key, int defaultValue) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			return sharedPrefs.getInt(key, defaultValue);
		} catch (Exception e) {
			e.printStackTrace();
			return defaultValue;
		}
	}
}
