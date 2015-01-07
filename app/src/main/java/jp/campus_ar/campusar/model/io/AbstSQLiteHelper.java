package jp.campus_ar.campusar.model.io;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class AbstSQLiteHelper<T extends AbstSQLiteHelper.SQLiteModel> {

	final public static int TYPE_TEXT = 0;
	final public static int TYPE_INTEGER = 1;
	final public static int TYPE_FLOAT = 2;

	public interface SQLiteModel {
		public ContentValues generateContentValues();

		public void populateContentValues(ContentValues cv);
	}

	public static class SQLiteColumn {
		public String name;
		public int type;
		public boolean notNull = false;
		public boolean primaryKey = false;
		public boolean autoIncrement = false;

		public SQLiteColumn(String name, int type) {
			this.name = name;
			this.type = type;
		}

		public SQLiteColumn(String name, int type, boolean notNull) {
			this.name = name;
			this.type = type;
			this.notNull = notNull;
		}

		public SQLiteColumn(String name, int type, boolean notNull, boolean autoIncrementAndPrimaryKey) {
			this.name = name;
			this.type = type;
			this.notNull = notNull;
			this.primaryKey = this.autoIncrement = autoIncrementAndPrimaryKey;
		}
	}

	private SQLiteDatabase db;
	private String[] columnNames;
	private Class<T> genericsClass;

	public AbstSQLiteHelper(Context context, T... t) {
		Class<T> type = (Class<T>) t.getClass().getComponentType();
		this.genericsClass = type;

		db = (new SQLiteOpenHelper(context, getTableName() + ".db", null, 1) {
			public void onCreate(SQLiteDatabase db) {
				ArrayList<SQLiteColumn> column = getModel();
				int limit = column.size() - 1;
				Iterator<SQLiteColumn> it = column.iterator();

				StringBuilder sb = new StringBuilder();
				sb.append("CREATE TABLE " + getTableName() + " (");
				int i = 0;
				while (it.hasNext()) {
					SQLiteColumn c = it.next();
					sb.append(c.name).append(" ");
					switch (c.type) {
						case TYPE_INTEGER:
							sb.append("INTEGER");
							break;
						case TYPE_FLOAT:
							sb.append("FLOAT");
							break;
						default:
							sb.append("TEXT");
					}
					if (c.primaryKey) {
						sb.append(" PRIMARY KEY");
					}
					if (c.autoIncrement) {
						sb.append(" AUTOINCREMENT");
					}
					if (c.notNull) {
						sb.append(" NOT NULL");
					}
					if (i != limit) {
						sb.append(",");
					}
					i++;
				}
				sb.append(")");
//				Log.d("art", sb.toString());
				db.execSQL(sb.toString());
			}

			public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			}
		}).getWritableDatabase();

		ArrayList<SQLiteColumn> columns = getModel();
		columnNames = new String[columns.size()];
		Iterator<SQLiteColumn> it = columns.iterator();
		int i = 0;
		while (it.hasNext()) {
			columnNames[i++] = it.next().name;
		}
	}

	public void add(T data) {
		Log.d("art", data.generateContentValues().toString());
		db.insert(getTableName(), null, data.generateContentValues());
	}

	public ArrayList<T> get() {
		return get(null, null, null, null);
	}

	public ArrayList<T> get(String where, String[] bindValue) {
		return get(where, bindValue, null, null);
	}

	public ArrayList<T> get(String order) {
		return get(null, null, order, null);
	}

	public ArrayList<T> get(String where, String[] bindValue, String order, String limit) {
		Cursor cursor = null;
		try {
			cursor = db.query(getTableName(), columnNames, where, bindValue, null, null, order, limit);
			return fetchAll(cursor);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public void delete(String where, String[] bindValue) {
		db.delete(getTableName(), where, bindValue);
	}

	private ArrayList<T> fetchAll(Cursor cursor) {
		ArrayList<SQLiteColumn> columns = this.getModel();
		int length = columns.size();
		int indexes[] = new int[length];
		for (int i = 0; i < length; i++) {
			indexes[i] = cursor.getColumnIndex(columns.get(i).name);
		}

		ArrayList<T> result = new ArrayList<>();
		while (cursor.moveToNext()) {
			try {
				T m = this.genericsClass.newInstance();
				ContentValues cv = new ContentValues();
				Iterator<SQLiteColumn> it = columns.iterator();
				int i = 0;
				while (it.hasNext()) {
					SQLiteColumn column = it.next();
					switch (column.type) {
						case TYPE_TEXT:
							cv.put(column.name, cursor.getString(indexes[i]));
							break;
						case TYPE_INTEGER:
							cv.put(column.name, cursor.getInt(indexes[i]));
							break;
						case TYPE_FLOAT:
							cv.put(column.name, cursor.getFloat(indexes[i]));
							break;
					}
					i++;
				}
				m.populateContentValues(cv);
//				Log.d("art", m.toString());
				result.add(m);
			} catch (Exception ignored) {
			}
		}
		return result;
	}

	public abstract String getTableName();

	public abstract ArrayList<SQLiteColumn> getModel();

}
