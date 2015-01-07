package jp.campus_ar.campusar.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OpenURI extends AsyncTask<String, String, String> {

	final private static String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	public interface OpenURICallback {
		public void onOpenURIComplete(boolean success, String data);
	}

	private Context context;

	private String url;
	private String method;
	private HashMap<String, String> params;
	private OpenURICallback listener;

	private HttpGet get;
	private HttpPost post;

	public OpenURI(Context context, String url, String method, HashMap<String, String> params, OpenURICallback listener) {
		this.context = context;
		this.url = url;
		this.method = method.toUpperCase();
		this.params = params;
		this.listener = listener;
	}

	public void abort() {
		super.cancel(true);
		if (get != null) get.abort();
		if (post != null) post.abort();
	}

	protected String doInBackground(String... strings) {
		HttpClient client = new DefaultHttpClient();
		HttpResponse response;

		try {
			appendDefaultParams(params);
			if (method.equals("GET")) {
				get = new HttpGet("http://devel.campus-ar.jp/" + url + "?" + buildQuery(params));
				Log.d("art", get.getURI().toString());
				response = client.execute(get);
			} else if (method.equals("POST")) {
				post = new HttpPost("http://devel.campus-ar.jp/" + url);
				try {
					post.setEntity(new UrlEncodedFormEntity(buildNameValuePair(params), HTTP.UTF_8));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				response = client.execute(post);
			} else {
				throw new Exception();
			}

			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpURLConnection.HTTP_OK) {
				byte[] result = EntityUtils.toByteArray(response.getEntity());
				final String data = new String(result, "UTF-8");

				if (isCancelled()) return null;
				publishProgress(data);
				return data;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (isCancelled()) return null;
		publishProgress(null);
		return null;
	}

	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);

		if (listener == null) return;
		String value = values == null ? null : values[0];
		Log.d("art", ""+value);
		listener.onOpenURIComplete(value != null, value);
	}

	private HashMap<String, String> appendDefaultParams(HashMap<String, String> param) {
		param.put("version", "2");
		param.put("pid", fetchPID());
		param.put("os", "Android");
		return param;
	}

	private static String buildQuery(HashMap<String, String> param) {
		StringBuilder str = new StringBuilder();
		for (Map.Entry<String, String> entry : param.entrySet()) {
			try {
				str.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
				str.append("=");
				str.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
				str.append("&");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return str.toString();
	}

	private List<NameValuePair> buildNameValuePair(HashMap<String, String> param) {
		List<NameValuePair> nameValuePairs = new ArrayList<>();
        for (String key : param.keySet()) {
            nameValuePairs.add(new BasicNameValuePair(key, param.get(key)));
        }
		return nameValuePairs;
	}

	private String fetchPID() {
		String pid = PrefUtil.getString(context, "PID", null);
		if (pid == null) {
			pid = generatePID();
			PrefUtil.putString(context, "PID", pid);
		}
		return pid;
	}

	private static String generatePID() {
		StringBuilder sb = new StringBuilder();
		int length = CHARS.length();
		Random rnd = new Random();
		for (int i = 0; i < 32; i++) {
			sb.append(CHARS.charAt(rnd.nextInt(length)));
		}
		return sb.toString();
	}

	public static OpenURI open(Context context, String url, String method, HashMap<String, String> params, OpenURICallback listener) {
		OpenURI open = new OpenURI(context, url, method, params, listener);
		open.execute();
		return open;
	}
}
