package jp.campus_ar.campusar.util;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DimenUtil {

	private static final int LOW_DPI_STATUS_BAR_HEIGHT = 19;
	private static final int MEDIUM_DPI_STATUS_BAR_HEIGHT = 25;
	private static final int HIGH_DPI_STATUS_BAR_HEIGHT = 38;
	private static final int XHIGH_DPI_STATUS_BAR_HEIGHT = 57;
	private static final int XXHIGH_DPI_STATUS_BAR_HEIGHT = HIGH_DPI_STATUS_BAR_HEIGHT * 2;
	private static final int XXXHIGH_DPI_STATUS_BAR_HEIGHT = XHIGH_DPI_STATUS_BAR_HEIGHT * 2;

	public static float getDisplayDensity(Context context) {
		return context.getResources().getDisplayMetrics().density;
	}

	public static int dp2px(Context context, double dp) {
		return (int) Math.floor(dp * getDisplayDensity(context));
	}

	public static int px2dp(Context context, double dp) {
		return (int) Math.floor(dp / getDisplayDensity(context));
	}

	public static int getWidth(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Point size = new Point();
		wm.getDefaultDisplay().getSize(size);
		return size.x;
	}

	public static int getHeight(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Point size = new Point();
		wm.getDefaultDisplay().getSize(size);
		return size.y;
	}

	public static int getInnerHeight(Context context) {
		return getHeight(context) - getStatusBarHeight(context);
	}

	public static int getStatusBarHeight(Context context) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);

		switch (displayMetrics.densityDpi) {
		case DisplayMetrics.DENSITY_XXXHIGH:
			return XXXHIGH_DPI_STATUS_BAR_HEIGHT;
		case DisplayMetrics.DENSITY_XXHIGH:
			return XXHIGH_DPI_STATUS_BAR_HEIGHT;
		case DisplayMetrics.DENSITY_XHIGH:
			return XHIGH_DPI_STATUS_BAR_HEIGHT;
		case DisplayMetrics.DENSITY_HIGH:
			return HIGH_DPI_STATUS_BAR_HEIGHT;
		case DisplayMetrics.DENSITY_MEDIUM:
			return MEDIUM_DPI_STATUS_BAR_HEIGHT;
		case DisplayMetrics.DENSITY_LOW:
			return LOW_DPI_STATUS_BAR_HEIGHT;
		default:
			return MEDIUM_DPI_STATUS_BAR_HEIGHT;
		}
	}

}
