package jp.campus_ar.campusar.util;

import android.content.Context;
import android.preference.PreferenceManager;

public class PrefUtil {

	public static void putString(Context context, String key, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).commit();
	}

	public static void putBoolean(Context context, String key, boolean value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).commit();
	}

	public static void putFloat(Context context, String key, float value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat(key, value).commit();
	}

	public static void putInt(Context context, String key, int value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).commit();
	}

	public static void putLong(Context context, String key, long value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, value).commit();
	}

	public static String getString(Context context, String key, String defValue) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defValue);
	}

	public static boolean getBoolean(Context context, String key, boolean defValue) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defValue);
	}

	public static float getFloat(Context context, String key, float defValue) {
		return PreferenceManager.getDefaultSharedPreferences(context).getFloat(key, defValue);
	}

	public static int getInt(Context context, String key, int defValue) {
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defValue);
	}

	public static long getLong(Context context, String key, long defValue) {
		return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defValue);
	}

	public static boolean ifExist(Context context, String key) {
		return PreferenceManager.getDefaultSharedPreferences(context).contains(key);
	}

}
