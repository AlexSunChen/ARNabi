package jp.campus_ar.campusar.util;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;

public class CameraUtil {

	private static Boolean available;

	static public int getBackId() throws Exception {
		int numberOfCameras = Camera.getNumberOfCameras();
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				return i;
			}
		}
		throw new Exception();
	}

	static public boolean isAvailable() {
		if (available == null) {
			try {
				getBackId();
				available = true;
			} catch (Exception e) {
				available = false;
			}
		}
		return available;
	}
}
