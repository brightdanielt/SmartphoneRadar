package com.cauliflower.danielt.smartphoneradar.Tool;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cauliflower.danielt.smartphoneradar.MainActivity;
import com.cauliflower.danielt.smartphoneradar.UI.MapsActivity;

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
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ACCOUNT + " VARCHAR PRIMARY KEY NOT NULL, " +
                COLUMN_USER_PASSWORD + " VARCHAR PRIMARY KEY NOT NULL, " +
                COLUMN_USER_USEDFOR + " VARCHAR NOT NULL) "
        );

        db.execSQL("CREATE TABLE " + TABLE_LOCATION + " (" +
                COLUMN_LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LOCATION_ACCOUNT + " VARCHAR PRIMARY KEY NOT NULL, " +
                COLUMN_LOCATION_LATITUDE + " REAL NOT NULL, " +
                COLUMN_LOCATION_LONGITUDE + " REAL NOT NULL, " +
                COLUMN_LOCATION_TIME + " DATETIME PRIMARY KEY NOT NULL) "
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        onCreate(db);
    }

    public void addUser(String account, String password,String usedFor) {
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
}
