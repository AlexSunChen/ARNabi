package jp.campus_ar.campusar.component.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class HeightAnimation extends Animation {

	private int toHeight;
	private int fromHeight;
	private View view;

	public HeightAnimation(View view, int startHeight, int targetHeight) {
		this.view = view;
		this.toHeight = targetHeight;
		this.fromHeight = startHeight;
	}

	protected void applyTransformation(float interpolatedTime, Transformation t) {
		int newHeight = (int) (fromHeight + (toHeight - fromHeight) * interpolatedTime);
		view.getLayoutParams().height = newHeight;
		view.requestLayout();
	}

	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, ((View) view.getParent()).getWidth(), parentHeight);
	}

	public boolean willChangeBounds() {
		return true;
	}
}