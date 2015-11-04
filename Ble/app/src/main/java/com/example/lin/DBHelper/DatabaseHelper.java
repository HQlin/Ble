package com.example.lin.DBHelper;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{
	
	private static final int VERSION = 1;
	private static final String TABLE_NAME ="user";

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	
	public DatabaseHelper(Context context, String name) {
		this(context,name,null, VERSION);
	}
	
	public DatabaseHelper(Context context, String name, int version) {
		this(context, name, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		//表格式(对应名称)：
		// id(id)：默认为0未登录，1为登陆
		// 秘钥(key)：默认null
		// 开锁模式(type)：默认1为自动，2为手动
		// 锁序列号(lock_seq)：默认为null
		// 用户号码(phonenum)：默认null
		// 用户密码(pwd)：默认null
		// 获取秘钥时间(getkeytime)：默认null
		// 秘钥期限(keyduration)：默认null
		db.execSQL("create table "+ TABLE_NAME + "(id int, key varchar(20),type int,lock_seq varchar(20),phonenum varchar(20),pwd varchar(20),getkeytime varchar(20),keyduration varchar(20))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
//		String tempTable = "temp_user";
//		db.execSQL("alter table " + TABLE_NAME + " rename to " + tempTable);
//		db.execSQL("create table "+ TABLE_NAME + " (id int, name varchar(20), sex varchar(10))");
//		db.execSQL("insert into " + TABLE_NAME +" (id, name, sex) select id,name,'falm' from " + tempTable);
	}

}
