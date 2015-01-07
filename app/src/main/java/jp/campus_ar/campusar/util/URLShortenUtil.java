package jp.campus_ar.campusar.util;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class URLShortenUtil {

    public interface OnShortenCompleteListener {
        public void onShortenComplete(String url);
    }

    final private static String ENDPOINT_URL = "https://www.googleapis.com/urlshortener/v1/url";
    final private static int TIMEOUT_IN_MILLIS = 5000;

    public static void shorten(String url, OnShortenCompleteListener listener) {
        Log.d("TKS shorten", url);

        Worker w = new Worker(url, listener);
        w.execute();
    }

    private static class Worker extends AsyncTask<String, Void, String> {

        private OnShortenCompleteListener listener;
        private String url;

        public Worker(String url, OnShortenCompleteListener listener) {
            this.url = url;
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... arg0) {
            String str = null;
            try {
                Uri.Builder uriBuilder = new Uri.Builder();
                uriBuilder.path(ENDPOINT_URL);
                String postUrl = Uri.decode(uriBuilder.build().toString());
                Log.d("art", postUrl);

                HttpPost httpPost = new HttpPost(postUrl);
                httpPost.setHeader("Content-type", "application/json");
                httpPost.setEntity(new StringEntity("{\"longUrl\":\"" + url + "\"}"));

                DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
                HttpParams httpParams = defaultHttpClient.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_IN_MILLIS);
                HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_IN_MILLIS);

                HttpResponse httpResponse = defaultHttpClient.execute(httpPost);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    String entity = EntityUtils.toString(httpResponse.getEntity());
                    JSONObject jsonEntity = new JSONObject(entity);
                    str = jsonEntity.optString("id");
                }
            } catch (Exception ignored) {
            }
            this.listener.onShortenComplete(str);
            return str;
        }
    }
}
