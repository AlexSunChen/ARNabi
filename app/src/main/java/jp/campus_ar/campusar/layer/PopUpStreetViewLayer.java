package jp.campus_ar.campusar.layer;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.StreetViewPanoramaOptions;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import jp.campus_ar.campusar.R;


public class PopUpStreetViewLayer extends Fragment {
    private StreetViewPanoramaFragment streetViewPanoramaFragment;
    private StreetViewPanorama streetViewPanorama;
    private StreetViewPanoramaView streetViewPanoramaView;
    private LatLng position;
    public Boolean setPositionFlag = false;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layer_popup, container, false);

        StreetViewPanoramaOptions options = new StreetViewPanoramaOptions();
        if (savedInstanceState == null) {
            options.position(new LatLng(36.111155, 140.099987));
            options.userNavigationEnabled(false);
            options.panningGesturesEnabled(false);
            options.zoomGesturesEnabled(false);
        }

        streetViewPanoramaFragment = StreetViewPanoramaFragment.newInstance(options);

        getChildFragmentManager()
                .beginTransaction()
                .show(streetViewPanoramaFragment)
                .commit();

        streetViewPanoramaView = new StreetViewPanoramaView(getActivity().getApplicationContext(), options);
        streetViewPanoramaView.onCreate(savedInstanceState);
        streetViewPanorama = streetViewPanoramaFragment.getStreetViewPanorama();

        return v;
    }

    public void onResume() {
        super.onResume();
        streetViewPanoramaView.onResume();
    }

    public void onPause() {
        super.onPause();
        streetViewPanoramaView.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        streetViewPanoramaView.onDestroy();
    }

    public void onSensorOrientationChanged(double yaw, double pitch) {
        if (!setPositionFlag) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.popUpStreetViewLayout, streetViewPanoramaFragment, "streetView")
                    .commit();
        }

        streetViewPanorama = streetViewPanoramaFragment.getStreetViewPanorama();

        StreetViewPanoramaCamera streetViewPanoramaCamera = new StreetViewPanoramaCamera.Builder()
                .bearing((float) yaw * 57)
                .tilt((float) (pitch - 0.5) * 30)
                .build();
        if (streetViewPanorama != null) {
            streetViewPanorama.animateTo(streetViewPanoramaCamera, 65);
        }

        if (!setPositionFlag && streetViewPanorama != null) {
            setPositionFlag = true;
            streetViewPanorama.setPosition(this.position);
        }
    }

    public void onSensorAverageOrientationChanged(double yaw, double pitch) {
    }

    public void onSensorLocationChanged(double lat, double lng) {
    }

    public void setLatLng(LatLng position) {
        this.position = position;
        StreetViewPanoramaOptions options = new StreetViewPanoramaOptions();

        options.position(this.position);
        options.userNavigationEnabled(false);
        options.panningGesturesEnabled(false);
        options.zoomGesturesEnabled(false);

        StreetViewPanoramaFragment streetViewPanoramaFragment = StreetViewPanoramaFragment.newInstance(options);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.popUpStreetViewLayout, streetViewPanoramaFragment, "streetView")
                .commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (streetViewPanoramaView != null) {
            streetViewPanoramaView.onSaveInstanceState(outState);
        }
    }

    public void showAR() {
        getChildFragmentManager()
                .beginTransaction()
                .remove(streetViewPanoramaFragment)
                .commit();
    }

    public void hideAR() {
        StreetViewPanoramaOptions options = new StreetViewPanoramaOptions();
        options.position(this.position);
        options.userNavigationEnabled(false);
        options.panningGesturesEnabled(false);
        options.zoomGesturesEnabled(false);
        streetViewPanoramaFragment = StreetViewPanoramaFragment.newInstance(options);
        streetViewPanorama = streetViewPanoramaFragment.getStreetViewPanorama();

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.popUpStreetViewLayout, streetViewPanoramaFragment, "streetView")
                .commit();
    }
}
