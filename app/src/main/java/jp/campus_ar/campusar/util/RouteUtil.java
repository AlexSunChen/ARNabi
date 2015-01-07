package jp.campus_ar.campusar.util;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import jp.campus_ar.campusar.model.GoogleMapRoute;
import jp.campus_ar.campusar.model.Route;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class RouteUtil extends AsyncTask<String, Void, Route> {
    final private static String ENDPOINT_URL = "https://maps.googleapis.com/maps/api/directions/json";
    final private static int TIMEOUT_IN_MILLIS = 20000;
    private RouteCallback listener;
    private LatLng start;
    private LatLng goal;

    public interface RouteCallback {
        public void searchRouteComplete(Route route);
    }

    public RouteUtil(LatLng start, LatLng goal, RouteCallback listener) {
        this.start = start;
        this.goal = goal;
        this.listener = listener;
    }

    public static RouteUtil calcRoute(LatLng start, LatLng goal, RouteCallback listener) {
        RouteUtil routeUtil = new RouteUtil(start, goal, listener);
        routeUtil.execute();
        return routeUtil;
    }

    @Override
    protected Route doInBackground(String... arg0) {
        Route route = new Route();

        try {
            Uri.Builder uriBuilder = new Uri.Builder();
            String queryUrl = buildRouteUrl(start, goal);
            uriBuilder.path(queryUrl);
            String postUrl = Uri.decode(uriBuilder.build().toString());

            HttpPost httpPost = new HttpPost(postUrl);
            httpPost.setHeader("Content-type", "application/json");

            DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
            HttpParams httpParams = defaultHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_IN_MILLIS);
            HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_IN_MILLIS);

            HttpResponse httpResponse = defaultHttpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_OK) {
                String entity = EntityUtils.toString(httpResponse.getEntity());
                GoogleMapRoute googleMapRoute = new Gson().fromJson(entity, GoogleMapRoute.class);
                route = googleMapRoute.decodePolyline(googleMapRoute.getOverwiewPolyline(0));
                route.length = String.valueOf(googleMapRoute.getDistance());
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return route;
    }

    @Override
    protected void onPostExecute(Route route) {
        if (route.coordinates != null) {
            if (route.coordinates.length > 0) {
                this.listener.searchRouteComplete(route);
            }
        } else {
            Log.d("tks" , "no route");
        }
    }

    private static String buildRouteUrl(LatLng start, LatLng goal) {
        String origin = "origin=" + start.latitude + "," + start.longitude;
        String destination = "destination=" + goal.latitude + "," + goal.longitude;
        String sensor = "sensor=false";
        String language = "language=ja";
        String mode = "mode=walking";
        String parameters = origin + "&" + destination + "&" + sensor + "&" + language + "&" + mode;
        String url = ENDPOINT_URL + "?" + parameters;
        return url;
    }
}
