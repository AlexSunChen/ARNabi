package jp.campus_ar.campusar.util;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Surface;

public class SensorUtil implements LocationListener, SensorEventListener {

	public interface OnSensorChangedListener {
		public void onSensorOrientationChanged(double yaw, double pitch);

		public void onSensorAverageOrientationChanged(double yaw, double pitch);

		public void onSensorLocationChanged(double lat, double lng);
	}

	private static SensorUtil sensorUtil;
	final private static double DELAY_COEF = 0.2;
	final private static double PI2 = Math.PI * 2;

	private Context context;
	private OnSensorChangedListener listener = new OnSensorChangedListener() {
		public void onSensorOrientationChanged(double yaw, double pitch) {
		}

		public void onSensorAverageOrientationChanged(double yaw, double pitch) {
		}

		public void onSensorLocationChanged(double lat, double lng) {
		}
	};
	private SensorManager sensorManager;
	private LocationManager locationManager;
	private Sensor accSensor, magSensor;
	private float[] accValue = new float[3];
	private float[] magValue = new float[3];

	private double lat;
	private double lng;
	private double delayYaw;
	private double delayPitch;
	private double yaw;
	private double pitch;
	private Handler handler;

	private int timerId = 0;

	public static SensorUtil getInstance(Context context, Handler handler) {
		if (sensorUtil == null) sensorUtil = new SensorUtil(context, handler);
		return sensorUtil;
	}

	private SensorUtil(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;

		lat = LocationUtil.restoreLat(context);
		lng = LocationUtil.restoreLng(context);

		sensorManager = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	public void setOnSensorChangedListener(OnSensorChangedListener listener) {
		if (listener != null) this.listener = listener;
	}

	public double getLat() {
		return lat;
	}

	public double getLng() {
		return lng;
	}

	public double getYaw() {
		return yaw;
	}

	public double getPitch() {
		return pitch;
	}

	public void start() {
		stop();
		if (sensorManager != null) {
			if (accSensor != null) sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_UI);
			if (magSensor != null) sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_UI);
		}
		if (locationManager != null) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, this);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, this);
		}

		final int thisId = ++timerId;
		delayPitch = pitch;
		delayYaw = yaw;
		handler.post(new Runnable() {
			public void run() {
				if (thisId != timerId) return;

				updateDelayPitch(pitch);
				updateDelayYaw(yaw);
				listener.onSensorAverageOrientationChanged(delayYaw, delayPitch);
				handler.postDelayed(this, 50);
			}

			public void updateDelayPitch(double n) {
				if (Math.abs(n - delayPitch) > Math.PI) {
					if (n < 0) {
						n += PI2;
					} else {
						n -= PI2;
					}
				}
				delayPitch += (n - delayPitch) * DELAY_COEF;
				if (delayPitch > Math.PI) {
					delayPitch -= PI2;
				} else if (delayPitch < -Math.PI) {
					delayPitch += PI2;
				}
			}

			public void updateDelayYaw(double n) {
				if (Math.abs(n - delayYaw) > Math.PI) {
					if (n < 0) {
						n += PI2;
					} else {
						n -= PI2;
					}
				}
				delayYaw += (n - delayYaw) * DELAY_COEF;
				if (delayYaw > Math.PI) {
					delayYaw -= PI2;
				} else if (delayYaw < -Math.PI) {
					delayYaw += PI2;
				}
			}
		});
	}

	public void stop() {
		timerId++;
		if (sensorManager != null) {
			sensorManager.unregisterListener(this);
		}
		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}
	}

	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
			case Sensor.TYPE_MAGNETIC_FIELD:
				magValue = event.values.clone();
				break;
			case Sensor.TYPE_ACCELEROMETER:
				accValue = event.values.clone();
				break;
		}

		float[] oriValue = new float[3];
		float[] inR = new float[9];
		float[] outR = new float[9];
		SensorManager.getRotationMatrix(inR, null, accValue, magValue);

		switch (((Activity) context).getWindowManager().getDefaultDisplay().getRotation()) {
			case Surface.ROTATION_0:
				SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_MINUS_X, outR);
				break;
			case Surface.ROTATION_90:
				SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_Y, outR);
				break;
			case Surface.ROTATION_270:
				SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Y, outR);
				break;
			case Surface.ROTATION_180:
				SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Y, outR);
				break;
		}
		SensorManager.getOrientation(outR, oriValue);
		yaw = (oriValue[0] + Math.PI * 1.5) % PI2 - Math.PI;
		pitch = Math.PI - Math.abs(oriValue[2]);
		listener.onSensorOrientationChanged(yaw, pitch);
	}

	public void onAccuracyChanged(Sensor sensor, int i) {

	}

	public void onLocationChanged(Location location) {
		lat = location.getLatitude();
		lng = location.getLongitude();
		LocationUtil.saveLocation(context, lat, lng);
		listener.onSensorLocationChanged(lat, lng);
	}

	public void onStatusChanged(String s, int i, Bundle bundle) {

	}

	public void onProviderEnabled(String s) {

	}

	public void onProviderDisabled(String s) {

	}
}
