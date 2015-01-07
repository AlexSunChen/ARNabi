package jp.campus_ar.campusar.component.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

public class MarginLeftAnimation extends Animation {

	private int toLeft;
	private int fromLeft;
	private View view;

	public MarginLeftAnimation(View view, int fromLeft, int toLeft) {
		this.view = view;
		this.toLeft = toLeft;
		this.fromLeft = fromLeft;
	}

	protected void applyTransformation(float interpolatedTime, Transformation t) {
		int newLeft = (int) (fromLeft + (toLeft - fromLeft) * interpolatedTime);
		((RelativeLayout.LayoutParams) view.getLayoutParams()).setMargins(newLeft, 0, 0, 0);
		view.requestLayout();
	}

	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
	}

	public boolean willChangeBounds() {
		return true;
	}
}