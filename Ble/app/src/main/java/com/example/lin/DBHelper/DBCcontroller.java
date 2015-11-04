package com.example.lin.DBHelper;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBCcontroller {
	/**
	 * 插入数据
	 * 
	 * @param context
	 */
	public static void insertDB(Context context) {
		ContentValues values = new ContentValues();
		values.put("id", 1);
		values.put("key", "OPEN");
		values.put("type", 1);
		values.put("keyduration", "no time");
		DatabaseHelper dbHelper = new DatabaseHelper(context, "db_name", 1);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.insert("user", null, values);
		db.close();

	}

	/**
	 * 更新数据
	 * 
	 * @param context
	 * @param key
	 * @param type
	 * @param lock_seq
	 * @param phonenum
	 * @param pwd
	 * @param keyduration
	 */
	public static void updateDBtype(Context context,
									String key,
									int type,
									String lock_seq,
									String phonenum,
									String pwd,
									String keyduration
									) {
		DatabaseHelper dbHelper = new DatabaseHelper(context, "db_name", 1);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("id", 1);
		values.put("key", key);
		values.put("type", type);
		values.put("lock_seq", lock_seq);
		values.put("phonenum", phonenum);
		values.put("pwd", pwd);
		values.put("getkeytime", System.currentTimeMillis() + "");
		values.put("keyduration", keyduration);
		db.update("user", values, "id = ?", new String[] { "1" });
		db.close();
	}

	/**
	 * 查询数据
	 * 
	 * @param context
	 * @param ss
	 * @return
	 */
	public static String queryBD(Context context, String ss) {
		DatabaseHelper dbHelper = new DatabaseHelper(context, "db_name", 1);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(false, "user", new String[] { "id", "key",
				"type", "lock_seq", "phonenum", "pwd", "getkeytime",
				"keyduration" }, "id = ?", new String[] { "1" }, null, null,
				null, null, null);
		while (cursor.moveToNext()) {
			ss = cursor.getString(cursor.getColumnIndex(ss));
			//System.out.println("query ->" + ss);
		}
		db.close();
		return ss;
	}
}
