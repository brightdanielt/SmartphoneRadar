package com.cauliflower.danielt.smartphoneradar.tool;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by danielt on 2018/3/27.
 */

public class MyDbHelper extends SQLiteOpenHelper {
    public static final String DB = "SmartphoneRadar.db";

    public static final String TABLE_USER = "user";
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USER_ACCOUNT = "account";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_USEDFOR = "usedFor";
    public static final String VALUE_USER_USEDFOR_SENDLOCATION = "sendLocation";
    public static final String VALUE_USER_USEDFOR_GETLOCATION = "getLocation";

    public static final String TABLE_LOCATION = "location";
    public static final String COLUMN_LOCATION_ID = "_id";
    public static final String COLUMN_LOCATION_ACCOUNT = "account";
    public static final String COLUMN_LOCATION_LATITUDE = "latitude";
    public static final String COLUMN_LOCATION_LONGITUDE = "longitude";
    public static final String COLUMN_LOCATION_TIME = "time";

    private static final int DB_VERSION = 1;

    public MyDbHelper(Context context) {
        super(context, DB, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USER + " (" +
                COLUMN_USER_ID + " INTEGER, " +
                COLUMN_USER_ACCOUNT + " VARCHAR NOT NULL, " +
                COLUMN_USER_PASSWORD + " VARCHAR NOT NULL, " +
                COLUMN_USER_USEDFOR + " VARCHAR NOT NULL, " +
                "PRIMARY KEY( " + COLUMN_USER_ACCOUNT + "," + COLUMN_USER_PASSWORD + ") ) "
        );

        db.execSQL("CREATE TABLE " + TABLE_LOCATION + " (" +
                COLUMN_LOCATION_ID + " INTEGER, " +
                COLUMN_LOCATION_ACCOUNT + " VARCHAR NOT NULL, " +
                COLUMN_LOCATION_LATITUDE + " REAL NOT NULL, " +
                COLUMN_LOCATION_LONGITUDE + " REAL NOT NULL, " +
                COLUMN_LOCATION_TIME + " DATETIME NOT NULL, " +
                "PRIMARY KEY( " + COLUMN_LOCATION_ACCOUNT + "," + COLUMN_LOCATION_TIME + ") ) "
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        onCreate(db);
    }

    public void addUser(String account, String password, String usedFor) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ACCOUNT, account);
        values.put(COLUMN_USER_PASSWORD, password);
        values.put(COLUMN_USER_USEDFOR, usedFor);

        long id = getWritableDatabase().insert(TABLE_USER, null, values);
        Log.i("Add user, id", id + "");
    }

    public void addLocation(String account, double latitude, double longitude, String time) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOCATION_ACCOUNT, account);
        values.put(COLUMN_LOCATION_LATITUDE, latitude);
        values.put(COLUMN_LOCATION_LONGITUDE, longitude);
        values.put(COLUMN_LOCATION_TIME, time);

        long id = getWritableDatabase().insert(TABLE_LOCATION, null, values);
        Log.i("Add location,id", id + "");
    }

    //查詢資料表 Location 的最新 time 值
    public String searchNewTime(String account) {
        //先查詢 Location 是否有紀錄
        Cursor cursor = getReadableDatabase().query(
                TABLE_LOCATION, new String[]{COLUMN_LOCATION_TIME}, COLUMN_LOCATION_TIME + ">?" + " AND " +
                        COLUMN_LOCATION_ACCOUNT + "=?", new String[]{"1911-01-01-00:00:00", account},
                null, null, COLUMN_LOCATION_TIME + " DESC");

        //取得最新 time 值
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int index_time_to_compare = cursor.getColumnIndex(COLUMN_LOCATION_TIME);
            String time_to_compare = cursor.getString(index_time_to_compare);
            return time_to_compare;
        }
        return "1911-01-01-00:00:00";
    }
}
