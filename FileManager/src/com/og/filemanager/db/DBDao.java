package com.og.filemanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.og.util.IPBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blitzfeng on 2017/7/19.
 */

public class DBDao {
    private DBHelper helper;
    private SQLiteDatabase db;


    public DBDao(Context context){
        helper = new DBHelper(context);
    }

    public void insertIPInfo(List<IPBean> list) {
       for(IPBean bean:list)
           insertIPInfo(bean);


    }
    public void insertIPInfo(IPBean bean) {
        db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("ip", bean.getIp());
        values.put("port", bean.getPort());
        long l = db.insert(DBHelper.DEVICE_TABLE_NAME, null, values);
        Log.d("debug", "insertIPInfo l:" + l);

    }
    public IPBean queryIP(int location){
        db = helper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.DEVICE_TABLE_NAME,null,"id=?",new String[]{location+""},null,null,null);
        if(cursor.moveToNext()){
            IPBean bean = new IPBean();
            bean.setIp(cursor.getString(cursor.getColumnIndex("ip")));
            bean.setPort(cursor.getInt(cursor.getColumnIndex("port")));
            return bean;
        }
        return null;
    }
    public List<IPBean> query(){
        db = helper.getReadableDatabase();

        Cursor cursor = db.query(DBHelper.DEVICE_TABLE_NAME,null,null,null,null,null,null,null);
        return queryIP(cursor);
    }

    public static List<IPBean> queryIP(Cursor cursor){
        if(cursor==null||!cursor.moveToNext()){
            Log.e("error","cursor is null");
            return null;
        }
        List<IPBean> list = new ArrayList<>();
        while (cursor.moveToNext()){
            IPBean bean = new IPBean();
            bean.setIp(cursor.getString(cursor.getColumnIndex("ip")));
            bean.setPort(cursor.getInt(cursor.getColumnIndex("port")));

            list.add(bean);
        }
        return list;

    }
    public void deleteIP(){
        db = helper.getWritableDatabase();

        int i = db.delete(DBHelper.DEVICE_TABLE_NAME,null,null);
        Log.d("debug", "deleteIP i:" +i);
    }



    public void insertLocation(int location){
        db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("location", location);
        long l = db.insert(DBHelper.LOCATION_TABLE, null, values);
        Log.d("debug", "insertLocation l:" + l);
    }

    public void updateLocation (int location){
        db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("location", location);
        int i = db.update(DBHelper.LOCATION_TABLE,values,"id=?",new String[]{""+location});
        Log.d("debug", "updateLocation i:" + i);
    }

    public int queryLocation(){
        db = helper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.LOCATION_TABLE,null,null,null,null,null,null);
        if(cursor.moveToNext()){
            int lo = cursor.getInt(cursor.getColumnIndex("location"));
            Log.d("debug", "queryLocation i:" + lo);
            return lo;
        }
        return 0;
    }




    public void close(){
        db.close();
    }

}
