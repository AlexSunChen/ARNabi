package jp.campus_ar.campusar.layer;

import android.graphics.Matrix;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import jp.campus_ar.campusar.R;
import jp.campus_ar.campusar.component.support.TouchableMapView;
import jp.campus_ar.campusar.model.Entry;
import jp.campus_ar.campusar.model.Route;
import jp.campus_ar.campusar.util.DimenUtil;
import jp.campus_ar.campusar.util.LocationUtil;
import jp.campus_ar.campusar.util.SensorUtil;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MapLayer extends Fragment implements SensorUtil.OnSensorChangedListener, TouchableMapView.OnMapTouchedListener, View.OnClickListener {

    private TouchableMapView mapView;
    private Marker goalMarker;
    private Polyline routeLine;

    private int status;
    private boolean hasCoordinate;
    private double yaw;

    private ImageButton statusButton;
    private ImageView compassImage;

    private long lastAnimationTime;

    private LatLng cameraPosition;
    private float cameraBearing;
    private float cameraTilt;
    private float cameraZoom;

    private TouchableMapView.OnMapTouchedListener listener;

    private Entry goalEntry;
    private Bundle mBundle;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBundle = savedInstanceState;
        View v = inflater.inflate(R.layout.layer_map, container, false);

        mapView = (TouchableMapView) v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMap().setMyLocationEnabled(true);
        mapView.getMap().getUiSettings().setCompassEnabled(false);
        mapView.getMap().getUiSettings().setMyLocationButtonEnabled(false);
        mapView.getMap().getUiSettings().setZoomControlsEnabled(false);
        mapView.getMap().setOnMapLongClickListener((latLng) -> {
            if (goalEntry == null) {
                goalEntry = new Entry();
                goalEntry.lat = latLng.latitude;
                goalEntry.lng = latLng.longitude;
                goalEntry.name = "N/A";
                goalEntry.detail = getAddress(goalEntry.lat, goalEntry.lng);
                Random random = new Random();
                goalEntry.identity = random.nextInt(2000000000) % 2000000000 + 100000000;
            }

            if (listener != null) {
                goalEntry.lat = latLng.latitude;
                goalEntry.lng = latLng.longitude;
                listener.onMapLongPressed(null, goalEntry);
            }
        });

        MapsInitializer.initialize(getActivity());
        statusButton = (ImageButton) v.findViewById(R.id.statusButton);
        compassImage = (ImageView) v.findViewById(R.id.compassImage);

        cameraPosition = new LatLng(LocationUtil.restoreLat(getActivity()), LocationUtil.restoreLng(getActivity()));
        cameraBearing = 0;
        cameraTilt = 0;
        cameraZoom = 15;

        status = 0;
        yaw = 0;
        lastAnimationTime = 0;

        statusButton.setEnabled(false);
        statusButton.setAlpha(0.5f);

        LatLng cameraLatLng = new LatLng(LocationUtil.restoreLat(getActivity()), LocationUtil.restoreLng(getActivity()));
        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(cameraLatLng, 16);
        mapView.getMap().moveCamera(camera);
        mapView.getMap().setOnCameraChangeListener(cameraPosition1 -> refreshCompass());
        mapView.setOnMapTouchedListener(this);
        statusButton.setOnClickListener(this);

        return v;
    }

    public void onClick(View view) {
        if (!hasCoordinate) return;

        if (status == 1) {
            bringMapCenterWithTilt();
            changeStatus(2);
        } else {
            bringMapCenter();
            changeStatus(1);
        }
        refreshCompass();
    }

    public void onDestroyView() {
        Log.d("tks", "on destroy panorama");
        super.onDestroyView();
    }

    public void onSensorOrientationChanged(double yaw, double pitch) {
    }

    public void onSensorAverageOrientationChanged(double yaw, double pitch) {
        if (status == 2 && !isAnimatingCamera()) {
            cameraBearing = (float) (yaw / Math.PI * 180.0);
            moveCamera();
        }
    }

    public void onSensorLocationChanged(double lat, double lng) {
        // 初回のGPS取得
        if (!hasCoordinate) {
            hasCoordinate = true;
            statusButton.setEnabled(true);
            statusButton.setAlpha(1f);
            changeStatus(1);
            bringMapCenter();
        }

        cameraPosition = new LatLng(lat, lng);

        if (status >= 2) {
            animateCamera();
        }
    }

    public void setOnMapTouchedListener(TouchableMapView.OnMapTouchedListener listener) {
        this.listener = listener;
    }

    public void onMapTouched(MotionEvent ev) {
        refreshCompass();
        changeStatus(0);
        if (listener != null) {
            listener.onMapTouched(ev);
        }
    }

    public void onMapLongPressed(MotionEvent ev, Entry goalEntry) {
        Projection mapProjection = mapView.getMap().getProjection();
        LatLng goalLatLng = mapProjection.fromScreenLocation(new Point((int) ev.getX(), (int) ev.getY()));
        goalEntry.lat = goalLatLng.latitude;
        goalEntry.lng = goalLatLng.longitude;

        if (listener != null) {
            listener.onMapLongPressed(ev, goalEntry);
        }
    }

    public void setPopup() {
        mapView.setPopup();
    }

    public void unSetPopup() {
        mapView.unSetPopup();
    }

    public void setGoal(Entry entry) {
        if (goalMarker != null) {
            goalMarker.remove();
        }

        LatLng ll = new LatLng(entry.lat, entry.lng);
        goalMarker = mapView.getMap().addMarker(new MarkerOptions().position(ll));

        LatLngBounds bounds = new LatLngBounds.Builder().include(ll).include(cameraPosition).build();
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, DimenUtil.dp2px(getActivity(), 100));
        changeStatus(0);
//		animateCamera(update);

        goalEntry = entry;
    }

    public void setRoute(Route route) {
        if (routeLine != null) {
            routeLine.remove();
        }

        PolylineOptions options = new PolylineOptions();
        for (int i = 0, ii = route.coordinates.length; i < ii; i++) {
            Route.Coordinate coord = route.coordinates[i];
            LatLng ll = new LatLng(coord.lat, coord.lng);
            options.add(ll);
        }
        options.color(getResources().getColor(R.color.blue));
        options.width(DimenUtil.dp2px(getActivity(), 3));
        if (routeLine != null) {
            routeLine.remove();
        }
        routeLine = mapView.getMap().addPolyline(options);
    }

    public void setRoute(Route facilityRoute, Route publicRoute) {
        if (routeLine != null) {
            routeLine.remove();
        }

        PolylineOptions options = new PolylineOptions();
        for (int i = 0, ii = publicRoute.coordinates.length; i < ii; i++) {
            Route.Coordinate coord = publicRoute.coordinates[i];
            LatLng ll = new LatLng(coord.lat, coord.lng);
            options.add(ll);
        }
        for (int i = 1, ii = facilityRoute.coordinates.length; i < ii; i++) {
            Route.Coordinate coord = facilityRoute.coordinates[i];
            LatLng ll = new LatLng(coord.lat, coord.lng);
            options.add(ll);
        }
        options.color(getResources().getColor(R.color.blue));
        options.width(DimenUtil.dp2px(getActivity(), 3));
        if (routeLine != null) {
            routeLine.remove();
        }
        routeLine = mapView.getMap().addPolyline(options);
    }

    public String getAddress(double lat, double lng) {
        StringBuffer strAddr = new StringBuffer();
        Geocoder gcoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
        String returnAddressString = "";

        try {
            List<Address> lstAddrs = gcoder.getFromLocation(lat, lng, 1);
            for (Address addr : lstAddrs) {
                int idx = addr.getMaxAddressLineIndex();
                for (int i = 1; i <= idx; i++) {
                    strAddr.append(addr.getAddressLine(i));
                    returnAddressString += addr.getAddressLine(i);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnAddressString;
    }

    public void unsetGoal() {
        if (goalMarker != null) {
            goalMarker.remove();
        }
        if (routeLine != null) {
            routeLine.remove();
        }
        if (goalEntry != null) {
            goalEntry = null;
        }
    }

    public void refreshCompass() {
        float angle = -mapView.getMap().getCameraPosition().bearing;
        int iw = compassImage.getDrawable().getBounds().width();
        int ih = compassImage.getDrawable().getBounds().height();
        int vw = compassImage.getWidth();
        int vh = compassImage.getHeight();
        Matrix matrix = new Matrix();
        compassImage.setScaleType(ImageView.ScaleType.MATRIX);
        matrix.postRotate(angle, iw / 2, ih / 2);
        matrix.postScale(vw / (float) iw, vh / (float) ih);
        compassImage.setImageMatrix(matrix);
    }

    public void changeStatus(int s) {
        status = s;
        switch (s) {
            case 0:
                statusButton.setImageResource(R.drawable.map_status_1);
                break;
            case 1:
                statusButton.setImageResource(R.drawable.map_status_2);
                break;
            case 2:
                statusButton.setImageResource(R.drawable.map_status_3);
                break;
        }
    }

    public void bringMapCenter() {
        cameraBearing = 0;
        cameraTilt = 0;
        cameraZoom = mapView.getMap().getCameraPosition().zoom;
        animateCamera();
    }

    public void bringMapCenterWithTilt() {
        cameraZoom = mapView.getMap().getCameraPosition().zoom;
        cameraBearing = (float) (-yaw / Math.PI * 180.0 - 90.0);
        cameraTilt = 50;
        animateCamera();
    }

    private void animateCamera() {
        CameraPosition camera = CameraPosition
                .builder()
                .target(cameraPosition)
                .zoom(cameraZoom)
                .bearing(cameraBearing)
                .tilt(cameraTilt)
                .build();
        animateCamera(CameraUpdateFactory.newCameraPosition(camera));
    }

    private void animateCamera(CameraUpdate update) {
        lastAnimationTime = System.currentTimeMillis();
        mapView.getMap().animateCamera(update, 1000, null);
    }

    private boolean isAnimatingCamera() {
        return System.currentTimeMillis() - lastAnimationTime < 1050;
    }

    private void moveCamera() {
        CameraPosition camera = CameraPosition
                .builder()
                .target(cameraPosition)
                .zoom(cameraZoom)
                .bearing(cameraBearing)
                .tilt(cameraTilt)
                .build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(camera);
        mapView.getMap().moveCamera(update);
    }

    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
