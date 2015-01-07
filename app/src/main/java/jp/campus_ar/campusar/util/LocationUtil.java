package jp.campus_ar.campusar.util;

import android.content.Context;

public class LocationUtil {

	public static double defaultLatitude() {
		return 35.681382;
	}

	public static double defaultLongitude() {
		return 139.766084;
	}

	public static void saveLocation(Context context, double lat, double lng) {
		PrefUtil.putFloat(context, "last_lat", (float) lat);
		PrefUtil.putFloat(context, "last_lng", (float) lng);
	}

	public static double calcDistance(Context context, double lat, double lng) {
		double lat2 = restoreLat(context);
		double lng2 = restoreLng(context);
		return calcDistance(lat, lng, lat2, lng2);
	}

	public static double calcDistance(double lat0, double lng0, double lat1, double lng1) {
		double R = 6371000;
		double dLat = deg2rad(lat0 - lat1);
		double dLon = deg2rad(lng0 - lng1);
		double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(
				lat0)) * Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c;
	}

	public static double calcLatitudePositionInMeter(double lat) {
		return calcDistance(0, 0, lat, 0);
	}

	public static double calcLongitudePositionInMeter(double lng) {
		return calcDistance(0, 0, 0, lng);
	}

	public static double deg2rad(double deg) {
		return deg * Math.PI / 180.0;
	}


	public static String getReadableDistance(Context context, double lat, double lng) {
		double n = calcDistance(context, lat, lng);
		if (n < 1000) {
			return ((int) n) + " m";
		} else {
			return ((int) Math.floor(n / 1000)) + " km";
		}
	}

	public static double restoreLat(Context context) {
		return PrefUtil.getFloat(context, "last_lat", (float) defaultLatitude());
	}

	public static double restoreLng(Context context) {
		return PrefUtil.getFloat(context, "last_lng", (float) defaultLongitude());
	}


}
