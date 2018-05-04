package com.cauliflower.danielt.smartphoneradar.tool;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cauliflower.danielt.smartphoneradar.obj.SimpleLocation;
import com.cauliflower.danielt.smartphoneradar.obj.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielt on 2018/3/27.
 */

public class MyDbHelper extends SQLiteOpenHelper {
    private static final String TAG = MyDbHelper.class.getSimpleName();
    public static final String DB = "SmartphoneRadar.db";

    public static final String TABLE_USER = "user";
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USER_ACCOUNT = "account";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_USEDFOR = "usedFor";
    public static final String COLUMN_USER_IN_USE = "in_use";

    public static final String VALUE_USER_USEDFOR_SENDLOCATION = "sendLocation";
    public static final String VALUE_USER_USEDFOR_GETLOCATION = "getLocation";
    public static final String VALUE_USER_IN_USE_YES = "yes";
    public static final String VALUE_USER_IN_USE_NO = "no";

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
                COLUMN_USER_IN_USE + " VARCHAR NOT NULL, " +
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

    public void addUser(String account, String password, String usedFor, String in_use) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ACCOUNT, account);
        values.put(COLUMN_USER_PASSWORD, password);
        values.put(COLUMN_USER_USEDFOR, usedFor);
        values.put(COLUMN_USER_IN_USE, in_use);

        long id = getWritableDatabase().insert(TABLE_USER, null, values);
        Log.i(TAG, "Add user,id: " + id);
    }

    public void addLocation(String account, double latitude, double longitude, String time) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOCATION_ACCOUNT, account);
        values.put(COLUMN_LOCATION_LATITUDE, latitude);
        values.put(COLUMN_LOCATION_LONGITUDE, longitude);
        values.put(COLUMN_LOCATION_TIME, time);

        long id = getWritableDatabase().insert(TABLE_LOCATION, null, values);
        Log.i(TAG, "Add location,id: " + id);
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

    public List<SimpleLocation> searchAllLocation(String account) {
        List<SimpleLocation> locationList = new ArrayList<>();

        Cursor cursor = getReadableDatabase().query(
                TABLE_LOCATION, null, COLUMN_LOCATION_ACCOUNT + "=?", new String[]{account},
                null, null, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int index_id = cursor.getColumnIndex(COLUMN_LOCATION_ID);
                int index_ac = cursor.getColumnIndex(COLUMN_LOCATION_ACCOUNT);
                int index_time = cursor.getColumnIndex(COLUMN_LOCATION_TIME);
                int index_lat = cursor.getColumnIndex(COLUMN_LOCATION_LATITUDE);
                int index_lng = cursor.getColumnIndex(COLUMN_LOCATION_LONGITUDE);

                int id = cursor.getInt(index_id);
                String ac = cursor.getString(index_ac);
                String time = cursor.getString(index_time);
                double lat = cursor.getDouble(index_lat);
                double lng = cursor.getDouble(index_lng);
                SimpleLocation simpleLocation = new SimpleLocation(time, lat, lng);
                locationList.add(simpleLocation);
                Log.i(TAG, id + "\n" + ac + "\n" + time + "\n" + lat + "\n" + lng);
                cursor.moveToNext();
            }
        }
        return locationList;
    }

    public List<User> searchUser(String usedFor) {
//        String[] user = new String[2];
        List<User> userList = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(
                TABLE_USER, null, COLUMN_USER_USEDFOR + "=?", new String[]{usedFor},
                null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                User user = new User();
                int index_account = cursor.getColumnIndex(COLUMN_USER_ACCOUNT);
                int index_password = cursor.getColumnIndex(COLUMN_USER_PASSWORD);
                int index_in_use = cursor.getColumnIndex(COLUMN_USER_IN_USE);
                user.setAccount(cursor.getString(index_account));
                user.setPassword(cursor.getString(index_password));
                user.setUsedFor(usedFor);
                user.setIn_use(cursor.getString(index_in_use));
                //存在帳密，已註冊
                if (user.getAccount() != null && user.getPassword() != null) {
                    userList.add(user);
                }
                cursor.moveToNext();
            }
        }
        return userList;
    }

    //更新改使用者為已登入
    public void updateUser_in_use(String account) {
        //先更新所有 getLocation 帳號為未登入
        ContentValues values_in_use_no = new ContentValues();
        values_in_use_no.put(COLUMN_USER_IN_USE, VALUE_USER_IN_USE_NO);
        int i = getWritableDatabase().update(TABLE_USER, values_in_use_no, COLUMN_USER_USEDFOR + "=?", new String[]{VALUE_USER_USEDFOR_GETLOCATION});
        Log.i(TAG, "update user column in_use,count:" + i);
        //再更新指定的使用者為已登入
        ContentValues values_in_use_yes = new ContentValues();
        values_in_use_yes.put(COLUMN_USER_IN_USE, VALUE_USER_IN_USE_YES);
        int j = getWritableDatabase().update(TABLE_USER, values_in_use_yes, COLUMN_USER_ACCOUNT + "=?", new String[]{account});
        Log.i(TAG, "update user column in_use,count:" + j);
    }
}
