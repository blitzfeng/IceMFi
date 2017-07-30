package com.og.filemanager.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by blitzfeng on 2017/7/19.
 */

public class DBHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "device_info.db";
    public final static String DEVICE_TABLE_NAME = "ip";
    public final static String LOCATION_TABLE = "location";
    private final static int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {


        String deviceTable = "CREATE TABLE IF NOT EXISTS " + DEVICE_TABLE_NAME + "(id Integer PRIMARY KEY AUTOINCREMENT,port Integer,ip);";
        sqLiteDatabase.execSQL(deviceTable);

        String locationTabl = "CREATE TABLE IF NOT EXISTS " + LOCATION_TABLE + "(id Integer PRIMARY KEY AUTOINCREMENT,location Integer);";
        sqLiteDatabase.execSQL(locationTabl);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
