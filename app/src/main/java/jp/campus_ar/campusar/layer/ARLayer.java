package jp.campus_ar.campusar.layer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.campus_ar.campusar.R;
import jp.campus_ar.campusar.component.ARView;
import jp.campus_ar.campusar.component.support.CameraView;
import jp.campus_ar.campusar.model.Entry;
import jp.campus_ar.campusar.model.Route;
import jp.campus_ar.campusar.util.SensorUtil;

public class ARLayer extends Fragment implements SensorUtil.OnSensorChangedListener {

	private CameraView cameraView;
	private ARView arView;

	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.layer_ar, container, false);

		cameraView = (CameraView) v.findViewById(R.id.cameraView);
		arView = (ARView) v.findViewById(R.id.arView);
		return v;
	}

	public void startCamera() {
		cameraView.start();
	}

	public void stopCamera() {
		cameraView.stop();
	}

	public void onSensorOrientationChanged(double yaw, double pitch) {
		arView.onSensorOrientationChanged(yaw, pitch);
	}

	public void onSensorAverageOrientationChanged(double yaw, double pitch) {
		arView.onSensorAverageOrientationChanged(yaw, pitch);
	}

	public void onSensorLocationChanged(double lat, double lng) {
		arView.onSensorLocationChanged(lat, lng);
	}

	public void setGoal(Entry entry) {
		arView.setGoal(entry);
	}

	public void setRoute(Route route) {
		arView.setRoute(route);
	}

    public void setRoute(Route facilityRoute, Route publicRoute) {
        arView.setRoute(facilityRoute, publicRoute);
    }

	public void unsetGoal() {
		arView.unsetGoal();
	}

	public void onResume() {
		super.onResume();
		if (!isHidden()) {
			cameraView.start();
		}
	}

	public void onPause() {
		super.onPause();
		cameraView.stop();
	}

    public void onDestroyView() {
        Log.d("tks", "on destroy panorama ar");
        super.onDestroyView();
    }

}
