package jp.campus_ar.campusar.util;

import android.content.Context;

import java.util.ArrayList;

import jp.campus_ar.campusar.model.Entry;
import jp.campus_ar.campusar.model.io.AbstSQLiteHelper;

public class BookmarkUtil extends AbstSQLiteHelper<Entry> {

	public BookmarkUtil(Context context) {
		super(context);
	}

	public boolean isFavorited(int identity) {
		return super.get("type=? AND identity=?", new String[]{Entry.TYPE_FAVORITE + "", identity + ""}).size() != 0;
	}

	public void removeFavorite(int identity) {
		super.delete("type=? AND identity=?", new String[]{Entry.TYPE_FAVORITE + "", identity + ""});
	}

	public void pushFavorite(Entry entry) {
		entry.type = Entry.TYPE_FAVORITE;
		super.add(entry);
	}

	public void pushHistory(Entry entry) {
		entry.type = Entry.TYPE_HISTORY;
		super.add(entry);
	}

	public ArrayList<Entry> getFavorite() {
		return super.get("type=?", new String[]{Entry.TYPE_FAVORITE + ""}, "id DESC", "20");
	}

	public ArrayList<Entry> getHistory() {
		return super.get("type=?", new String[]{Entry.TYPE_HISTORY + ""}, "id DESC", "20");
	}

	public String getTableName() {
		return "bookmark";
	}

	public ArrayList<SQLiteColumn> getModel() {
		ArrayList<SQLiteColumn> column = new ArrayList<>();
		column.add(new SQLiteColumn("id", TYPE_INTEGER, true, true));
		column.add(new SQLiteColumn("identity", TYPE_INTEGER));
		column.add(new SQLiteColumn("name", TYPE_TEXT));
		column.add(new SQLiteColumn("detail", TYPE_TEXT));
		column.add(new SQLiteColumn("lat", TYPE_FLOAT));
		column.add(new SQLiteColumn("lng", TYPE_FLOAT));
		column.add(new SQLiteColumn("type", TYPE_INTEGER));
		return column;
	}
}
