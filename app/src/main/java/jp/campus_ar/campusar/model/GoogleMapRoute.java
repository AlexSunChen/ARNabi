package jp.campus_ar.campusar.model;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class GoogleMapRoute {

    private String status;
    private List<GRoute> routes;

    public final String getStatus() {
        return status;
    }

    public int getRouteCount() {
        return routes.size();
    }

    public final GRoute getRoute(int index) {
        return routes.get(index);
    }

    public final int getDistance() {
        return routes.get(0).getLegs().get(0).getDistance().value;
    }

//    public final List<GLocation> getOverwiewPolyline(int routeIndex) {
    public final String getOverwiewPolyline(int routeIndex) {
        GRoute route = routes.get(routeIndex);
        GPolyline polyline = route.getOverviewPolyline();
        return polyline.getPoints();
//        return polyline.getPoints();
    }

    public final List<GRoute> getRoutes() {
        return routes;
    }

    public final Route decodePolyline(String encodedPoly) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0;
        int length = encodedPoly.length();
        int lat = 0;
        int lng = 0;

        while(index < length){
            int b;
            int shift = 0;
            int result = 0;

            do {
                //ASCII文字の「?」= 63?
                b = encodedPoly.charAt(index++) - 63;
                //5ビット単位の結果を取得
                result |= (b & 0x1f) << shift;
                shift += 5;
            }while(b >= 0x20);

            int deltaLat =  ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += deltaLat;

            shift = 0;
            result = 0;
            do {
                //ASCII文字の「?」= 63?
                b = encodedPoly.charAt(index++) - 63;
                //5ビット単位の結果を取得
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int deltaLng =  ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += deltaLng;

            LatLng position = new LatLng(((double) lat / 1E5),((double) lng / 1E5));
            poly.add(position);
        }

        Route route = new Gson().fromJson(createJson(poly), Route.class);

        return route;
    }

    private String createJson(List<LatLng> data) {
        String buildQuery = "{coordinates:[";
        for (LatLng latlng : data) {
            buildQuery += "{lat:"+latlng.latitude+","+"lng:"+latlng.longitude+"},";
        }
        buildQuery = buildQuery.substring(0, buildQuery.length()-1);
        buildQuery +="]}";

        return buildQuery;
    }

    private class GRoute {
        private String summary;
        private List<GLeg> legs;
        private List<String> waypoint_order;
        private GPolyline overview_polyline;
        private GBounds bounds;
        private String copyrights;
        private List<String> warnings;

        public final String getSummary() {
            return summary;
        }
        public final List<GLeg> getLegs() {
            return legs;
        }
        public final List<String> getWaypointOrder() {
            return waypoint_order;
        }
        public final GPolyline getOverviewPolyline() {
            return overview_polyline;
        }
        public final GBounds getBounds() {
            return bounds;
        }
        public final String getCopyrights() {
            return copyrights;
        }
        public final List<String> getWarnings() {
            return warnings;
        }
    }
    private class GLeg {
        private List<GStep> steps;
        private GDistance distance;
        private GDuration duration;
        private GLocation start_location;
        private GLocation end_location;
        private String start_address;
        private String end_address;

        public final List<GStep> getSteps() {
            return steps;
        }
        public final GDistance getDistance() {
            return distance;
        }
        public final GDuration getDuration() {
            return duration;
        }
        public final GLocation getStartLocation() {
            return start_location;
        }
        public final GLocation getEndLocation() {
            return end_location;
        }
        public final String getStartAddress() {
            return start_address;
        }
        public final String getEndAddress() {
            return end_address;
        }
    }
    private class GBounds {
        private GLocation southwest;
        private GLocation northeast;

        public final GLocation getSouthWest() {
            return southwest;
        }
        public final GLocation getNorthEast() {
            return northeast;
        }
    }
    private class GPolyline {
        private String points;
        private String levels;

        public final String getPoints() {
            return points;
        }

        public final String getLevels() {
            return levels;
        }
    }
    private class GStep {
        private String html_instructions;
        private GDistance distance;
        private GDuration duration;
        private GLocation start_location;
        private GLocation end_location;
        private GPolyline polyline;
        private String travel_mode;
    }
    private class GDistance {
        private int value;
        private String text;

        public final int getValue() {
            return value;
        }
        public final String getText() {
            return text;
        }
    }
    private class GDuration {
        private int value;
        private String text;

        public final int getValue() {
            return value;
        }
        public final String getText() {
            return text;
        }
    }
    private class GStatus {
        public static final String OK = "OK";
        public static final String NOT_FOUND = "NOT_FOUND";
        public static final String ZERO_RESULTS = "ZERO_RESULTS";
        public static final String MAX_WAYPOINTS_EXCEEDED = "MAX_WAYPOINTS_EXCEEDED";
        public static final String INVALID_REQUEST = "INVALID_REQUEST";
        public static final String OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
        public static final String REQUEST_DENIED = "REQUEST_DENIED";
        public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
    }

    private class GLocation {
        private double lat;
        private double lng;

        public GLocation() {
            lat = 0.0;
            lng = 0.0;
        }

        public GLocation(double latitude, double longitude) {
            this.lat = latitude;
            this.lng = longitude;
        }

        public void setLocation(double latitude, double longitude) {
            this.lat = latitude;
            this.lng = longitude;
        }

        public void setLatitude(double latitude) {
            this.lat = latitude;
        }

        public void setLongitude(double longitude) {
            this.lng = longitude;
        }

        public double getLatitude() {
            return lat;
        }

        public double getLongitude() {
            return lng;
        }

        public int getLatitudeE6() {
            return (int)(lat * 1E6);
        }

        public int getLongitudeE6() {
            return (int)(lng *1E6);
        }
    }
}
