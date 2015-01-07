package jp.campus_ar.campusar.component.support;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

import android.view.View;
import jp.campus_ar.campusar.util.CameraUtil;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

	final private static double ASPECT_TOLERANCE = 0.05;

	private SurfaceHolder holder;
	private Camera camera;
	private Context context;
	private boolean waitForCamera = false;

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		holder = getHolder();
		holder.addCallback(this);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera = Camera.open(CameraUtil.getBackId());
			camera.setPreviewDisplay(holder);
        } catch (Exception e) {
			destroy();
		}
		if (waitForCamera) {
			start();
			waitForCamera = false;
		}
	}

	public void stop() {
        if (this.getVisibility() == View.VISIBLE) {
            this.setVisibility(View.GONE);
        }
		if (camera != null) {
			camera.stopPreview();
		}
	}

	public void start() {
        if (this.getVisibility() == View.GONE) {
            this.setVisibility(View.VISIBLE);
        }
		if (camera != null) {
			camera.startPreview();
		} else {
			waitForCamera = true;
		}
	}

	public void destroy() {
		if (camera != null) {
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			camera = null;
		}
	}


	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		this.setPreviewSize(width, height);
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		this.destroy();
	}

	private void setPreviewSize(int w, int h) {
		if (camera == null) {
			return;
		}
		camera.stopPreview();

		Camera.Parameters parameters = camera.getParameters();

		List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
		Camera.Size optimalSize = getOptimalPreviewSize(sizes, w, h);
		parameters.setPreviewSize(optimalSize.width, optimalSize.height);
		setCameraDisplayOrientation(0, camera, w, h);

		camera.setParameters(parameters);
		camera.startPreview();
	}

	private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
		if (w < h) {
			int temp = w;
			w = h;
			h = temp;
		}
		float targetRatio = (float) w / h;
		if (sizes == null) return null;

		Camera.Size optimalSize = null;
		float minDiff = Float.MAX_VALUE;

		int targetHeight = h;

		for (Camera.Size size : sizes) {
			float ratio = (float) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			minDiff = Float.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void setCameraDisplayOrientation(int cameraId, Camera camera, int width, int height) {
		camera.setDisplayOrientation(getCameraDisplayOrientation(width, height));
	}

	public int getCameraDisplayOrientation(int w, int h) {
		int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
		}
		int base;
		if (degrees == 90 || degrees == 270) {
			if (h < w) {
				base = 90;
			} else {
				base = 0;
			}
		} else {
			if (h > w) {
				base = 90;
			} else {
				base = 0;
			}
		}
		return (base + 360 - degrees) % 360;
	}
}