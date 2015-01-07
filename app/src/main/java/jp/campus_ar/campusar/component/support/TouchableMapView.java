package jp.campus_ar.campusar.component.support;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import jp.campus_ar.campusar.model.Entry;

public class TouchableMapView extends MapView {

	public interface OnMapTouchedListener {
		public void onMapTouched(MotionEvent ev);

        public void onMapLongPressed(MotionEvent ev, Entry goalEntry);
	}

	private OnMapTouchedListener listener;

    private boolean mWasLongClick = false;
    private boolean mIsPopup = false;

	public TouchableMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setOnMapTouchedListener(OnMapTouchedListener listener) {
		this.listener = listener;
	}

	public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            mIsPopup = false;
            listener.onMapTouched(ev);
            return super.dispatchTouchEvent(ev);
        } else {
            if (mIsPopup) {
                return false;
            } else {
                listener.onMapTouched(ev);
                if(mWasLongClick) {
                    mWasLongClick = false;
                    return true;
                } else {
                    return super.dispatchTouchEvent(ev);
                }
            }
        }
	}

    public void setPopup() {
        mIsPopup = true;
    }

    public void unSetPopup() {
        mIsPopup = false;
    }
}
