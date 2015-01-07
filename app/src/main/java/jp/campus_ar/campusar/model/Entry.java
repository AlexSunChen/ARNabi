package jp.campus_ar.campusar.model;

import android.content.ContentValues;

import android.util.Base64;
import android.util.Log;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import jp.campus_ar.campusar.model.io.AbstSQLiteHelper;

public class Entry implements Serializable, AbstSQLiteHelper.SQLiteModel {

	private static final long serialVersionUID = 10L;

	final public static int TYPE_HISTORY = 1;
	final public static int TYPE_FAVORITE = 2;

	public String name;
	public String detail;
	@SerializedName("id")
	public int identity;
	public double lat;
	public double lng;
	public int type;

	public ContentValues generateContentValues() {
		ContentValues cv = new ContentValues();
		cv.put("name", name);
		cv.put("detail", detail);
		cv.put("identity", identity);
		cv.put("lat", lat);
		cv.put("lng", lng);
		cv.put("type", type);
		return cv;
	}

	public void populateContentValues(ContentValues cv) {
		name = cv.getAsString("name");
		detail = cv.getAsString("detail");
		identity = cv.getAsInteger("identity");
		lat = cv.getAsDouble("lat");
		lng = cv.getAsDouble("lng");
		type = cv.getAsInteger("type");
	}

    public static Entry getInstance(String path) {
        Log.d("tksasd", path);
        if (!path.substring(0, 3).equals("/@/")) {
            return null;
        }

        HashMap<String, String> queries = new HashMap<>();
        String query = new String(Base64.decode(path.substring(3), Base64.URL_SAFE | Base64.NO_WRAP));

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx == -1) {
                continue;
            }
            try {
                String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                queries.put(key, value);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        if (queries.containsKey("entry_id")) {
            return getFacilityEntry(queries);
        }

        if (!queries.containsKey("lat") || !queries.containsKey("lng")) {
            return null;
        }
        double lat, lng;
        try {
            lat = Double.parseDouble(queries.get("lat"));
            lng = Double.parseDouble(queries.get("lng"));
        } catch (Exception e) {
            return null;
        }

        Entry p = new Entry();
        p.lat = lat;
        p.lng = lng;

        if (queries.containsKey("building")) {
            p.name = queries.get("building");
        }
        if (queries.containsKey("room")) {
            p.detail = queries.get("room");
        }

        return p;
    }

    private static Entry getFacilityEntry(HashMap<String, String> queries) {
        Entry entry = new Entry();
        entry.identity = Integer.parseInt(queries.get("entry_id"));
        entry.name = "";
        switch (entry.identity) {
            case 17:    entry.detail = "3B棟";             break;
            case 134:   entry.detail = "総合研究棟B";        break;
            case 249:   entry.detail = "第一エリア前";       break;
            case 250:   entry.detail = "第三エリア前";       break;
            case 404:   entry.detail = "3F棟(3F1102)";     break;
            case 8387:  entry.detail = "第三エリア食堂";     break;
            case 8237:  entry.detail = "第二エリア食堂";     break;
        }
        return entry;
    }
}
