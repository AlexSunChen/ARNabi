package jp.campus_ar.campusar.component;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import jp.campus_ar.campusar.R;
import jp.campus_ar.campusar.model.Entry;
import jp.campus_ar.campusar.model.Position;
import jp.campus_ar.campusar.model.Projection;
import jp.campus_ar.campusar.model.Ray;
import jp.campus_ar.campusar.model.Route;
import jp.campus_ar.campusar.model.Segment;
import jp.campus_ar.campusar.util.LocationUtil;
import jp.campus_ar.campusar.util.SensorUtil;

public class ARView extends View implements SensorUtil.OnSensorChangedListener {

	final public static double kOuterRouteWidth = 2;
	final public static double kInnerRouteWidth = 1.3;
	final public static double kArrowInterval = 10;

	private double yaw;
	private double pitch;
	private double lat;
	private double lng;
	private double delayYaw;
	private double delayPitch;

	private Projection projection;
	private ArrayList<Position> meteredCoordinates;
	private ArrayList<Position> outlineCoordinatesOuter;
	private ArrayList<Position> outlineCoordinatesInner;
	private ArrayList<ArrayList<Position>> outlineArrows;

	private Paint roadArrowPaint;
	private Paint outerRoadPaint;
	private Paint innerRoadPaint;

	public ARView(Context context, AttributeSet attrs) {
        super(context, attrs);
		yaw = pitch = delayYaw = delayPitch = 0;
		projection = new Projection();

		roadArrowPaint = new Paint();
		roadArrowPaint.setStyle(Paint.Style.FILL);
		roadArrowPaint.setColor(context.getResources().getColor(R.color.road_arrow));
		roadArrowPaint.setAntiAlias(true);

		outerRoadPaint = new Paint();
		outerRoadPaint.setStyle(Paint.Style.FILL);
		outerRoadPaint.setColor(context.getResources().getColor(R.color.outer_road));
		outerRoadPaint.setAntiAlias(true);

		innerRoadPaint = new Paint();
		innerRoadPaint.setStyle(Paint.Style.FILL);
		innerRoadPaint.setColor(context.getResources().getColor(R.color.inner_road));
		innerRoadPaint.setAntiAlias(true);

		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	public void setGoal(Entry entry) {
	}

	public void setRoute(Route route) {
		meteredCoordinates = new ArrayList<>();
		Route.Coordinate[] coord = route.coordinates;
        for (Route.Coordinate aCoord : coord) {
            double x = LocationUtil.calcLongitudePositionInMeter(aCoord.lng);
            double y = LocationUtil.calcLatitudePositionInMeter(aCoord.lat);
            meteredCoordinates.add(new Position(x, y));
        }
		refreshAroundCoordinate(LocationUtil.restoreLat(getContext()), LocationUtil.restoreLng(getContext()));
	}

    public void setRoute(Route facilityRoute, Route publicRoute) {
        meteredCoordinates = new ArrayList<>();
        Route.Coordinate[] facilityCoord = facilityRoute.coordinates;
        Route.Coordinate[] publicCoord = publicRoute.coordinates;
        for (Route.Coordinate pCoord : publicCoord) {
            double x = LocationUtil.calcLongitudePositionInMeter(pCoord.lng);
            double y = LocationUtil.calcLatitudePositionInMeter(pCoord.lat);
            meteredCoordinates.add(new Position(x, y));
        }
        for (int i = 1; i < facilityCoord.length; i++) {
            double x = LocationUtil.calcLongitudePositionInMeter(facilityCoord[i].lng);
            double y = LocationUtil.calcLatitudePositionInMeter(facilityCoord[i].lat);
            meteredCoordinates.add(new Position(x, y));
        }
        refreshAroundCoordinate(LocationUtil.restoreLat(getContext()), LocationUtil.restoreLng(getContext()));
    }

	public void unsetGoal() {
		meteredCoordinates = null;
	}

	public void onSensorOrientationChanged(double yaw, double pitch) {
	}

	public void onSensorAverageOrientationChanged(double yaw, double pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
		refreshProjection();
		invalidate();
	}

	public void onSensorLocationChanged(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
		refreshAroundCoordinate(lat, lng);
	}

	private void refreshProjection() {
		projection.setYawPitch(yaw, pitch);
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (outlineCoordinatesOuter == null || outlineCoordinatesOuter.size() == 0) return;

		Path p = new Path();
		ArrayList<Ray> frontsOuter = raysFronted(outlineCoordinatesOuter);
		ArrayList<Ray> frontsInner = raysFronted(outlineCoordinatesInner);
		if (frontsOuter == null || frontsInner == null) return;

		int numFrontsOuter = frontsOuter.size();
		int numFrontsInner = frontsInner.size();
		if (numFrontsOuter == 0 || numFrontsInner == 0) return;

		Ray ray = frontsOuter.get(0);
		p.moveTo((int) ray.x, (int) ray.y);
		for (int i = 1; i < numFrontsOuter; i++) {
			ray = frontsOuter.get(i);
			p.lineTo((int) ray.x, (int) ray.y);
		}
		canvas.drawPath(p, outerRoadPaint);

		p = new Path();
		ray = frontsInner.get(0);
		p.moveTo((int) ray.x, (int) ray.y);
		for (int i = 1; i < numFrontsInner; i++) {
			ray = frontsInner.get(i);
			p.lineTo((int) ray.x, (int) ray.y);
		}
		canvas.drawPath(p, innerRoadPaint);

		p = new Path();
        for (ArrayList<Position> arrow : outlineArrows) {
            ArrayList<Ray> frontsArrow = raysFronted(arrow);
            int numFrontsArrow = frontsArrow.size();
            if (numFrontsArrow == 0) continue;
            ray = frontsArrow.get(0);
            p.moveTo((int) ray.x, (int) ray.y);
            for (int i = 1; i < numFrontsArrow; i++) {
                ray = frontsArrow.get(i);
                p.lineTo((int) ray.x, (int) ray.y);
            }
        }
		canvas.drawPath(p, roadArrowPaint);
	}

	private ArrayList<Ray> raysFronted(ArrayList<Position> positions) {
		ArrayList<Ray> rays = new ArrayList<>();
		int ii = positions.size();
		if (ii == 0) return rays;

		int i = 0;
		int jj = ii - 1;
		Ray ray;
		for (; i < jj; i++) {
			ray = projection.calcRay(positions.get(i), positions.get(i + 1), getHeight());
			if (ray != null) {
				rays.add(ray);
				break;
			}
		}
		for (i++; i < ii; i++) {
			ray = projection.calcRay(positions.get(i), positions.get(i - 1), getHeight());
			if (ray != null) {
				rays.add(ray);
				if (ray.moved) {
					for (; i < jj; i++) {
						ray = projection.calcRay(positions.get(i), positions.get(i + 1), getHeight());
						if (ray != null) {
							rays.add(ray);
							break;
						}
					}
				}
			}
		}
		return rays;
	}

	public void refreshAroundCoordinate(double lat, double lng) {
		double myX = LocationUtil.calcLongitudePositionInMeter(lng);
		double myY = LocationUtil.calcLatitudePositionInMeter(lat);
		int nearestIndex = 0;
		double nearestDistance = Double.MAX_VALUE;
		int i = 0;
		if (meteredCoordinates == null) return;
        for (Position pos : meteredCoordinates) {
            double deltaX = pos.x - myX;
            double deltaY = pos.y - myY;
            double deltaM = deltaX * deltaX + deltaY * deltaY;
            if (deltaM < nearestDistance) {
                nearestDistance = deltaM;
                nearestIndex = i;
            }
            i++;
        }

		int startIndex = nearestIndex - 20;
		if (startIndex < 0) startIndex = 0;

		int finishIndex = nearestIndex + 20;
		if (finishIndex > meteredCoordinates.size()) finishIndex = meteredCoordinates.size();

		ArrayList<Segment> aroundSegment0 = new ArrayList<>();
		ArrayList<Segment> aroundSegment1 = new ArrayList<>();
		outlineArrows = new ArrayList<>();
		Position lastPos = null;
		for (i = startIndex; i < finishIndex; i++) {
			Position pos = meteredCoordinates.get(i);
			Position apos = new Position(pos.x - myX, pos.y - myY);
			if (lastPos != null) {
				Segment outerSegment = new Segment(lastPos, apos, kOuterRouteWidth);
				Segment innerSegment = new Segment(lastPos, apos, kInnerRouteWidth);
				aroundSegment0.add(outerSegment);
				aroundSegment1.add(innerSegment);

				for (double j = 7, r = outerSegment.r; j < r; j += kArrowInterval) {
					ArrayList<Position> arrow = outerSegment.generateArrow(j);
					outlineArrows.add(arrow);
				}
			}
			lastPos = apos;
		}

		if (finishIndex - startIndex <= 0) return;

		outlineCoordinatesOuter = convertOutline(aroundSegment0);
		outlineCoordinatesInner = convertOutline(aroundSegment1);
	}

	private ArrayList<Position> convertOutline(ArrayList<Segment> segments) {
		int numSegments = segments.size();
		Segment seg = segments.get(0);
		ArrayList<Position> result = new ArrayList<>();
		result.add(seg.south.intersect(seg.west));
		result.add(seg.south.intersect(seg.east));
		for (int i = 1; i < numSegments; i++) {
			Segment theSeg = segments.get(i);
			result.add(theSeg.east.intersect(seg.east));
			seg = theSeg;
		}
		seg = segments.get(numSegments - 1);
		result.add(seg.north.intersect(seg.east));
		result.add(seg.north.intersect(seg.west));
		for (int i = numSegments - 2; i >= 0; i--) {
			Segment theSeg = segments.get(i);
			result.add(theSeg.west.intersect(seg.west));
			seg = theSeg;
		}
		result.add(seg.south.intersect(seg.west));
		return result;
	}

    public void hideAR() {
//        Canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }
}
