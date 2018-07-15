package com.cauliflower.danielt.smartphoneradar.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cauliflower.danielt.smartphoneradar.data.RadarContract.UserEntry;
import com.cauliflower.danielt.smartphoneradar.data.RadarContract.LocationEntry;

/**
 * Created by danielt on 2018/3/27.
 */

public class RadarDbHelper extends SQLiteOpenHelper {
    private static final String TAG = RadarDbHelper.class.getSimpleName();
    public static final String DB = "SmartphoneRadar.db";

    private static final int DB_VERSION = 1;

    public RadarDbHelper(Context context) {
        super(context, DB, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + UserEntry.TABLE_USER + " (" +
                UserEntry._ID + " INTEGER , " +
                UserEntry.COLUMN_USER_EMAIL + " TEXT NOT NULL, " +
                UserEntry.COLUMN_USER_PASSWORD + " TEXT NOT NULL, " +
                UserEntry.COLUMN_USER_USED_FOR + " TEXT NOT NULL, " +
                UserEntry.COLUMN_USER_IN_USE + " TEXT NOT NULL, " +
                "PRIMARY KEY( " + UserEntry.COLUMN_USER_EMAIL + "," + UserEntry.COLUMN_USER_USED_FOR + ") ) "
        );

        db.execSQL("CREATE TABLE " + LocationEntry.TABLE_LOCATION + " (" +
                LocationEntry._ID + " INTEGER , " +
                LocationEntry.COLUMN_LOCATION_EMAIL + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_LATITUDE + " REAL NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_LONGITUDE + " REAL NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_TIME + " DATETIME NOT NULL, " +
                "PRIMARY KEY( " + LocationEntry.COLUMN_LOCATION_EMAIL + "," + LocationEntry.COLUMN_LOCATION_TIME + ") ) "
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_LOCATION);
        onCreate(db);
    }
/*
    public void addUser(String account, String password, String usedFor, String in_use) {
        Cursor c = getReadableDatabase().query(UserEntry.TABLE_USER, new String[]{"account"},
                UserEntry.COLUMN_USER_EMAIL + " = ? and " + UserEntry.COLUMN_USER_USED_FOR + " = ? ",
                new String[]{account, usedFor}, null, null, null);
        if (!c.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(UserEntry.COLUMN_USER_EMAIL, account);
            values.put(UserEntry.COLUMN_USER_PASSWORD, password);
            values.put(UserEntry.COLUMN_USER_USED_FOR, usedFor);
            values.put(UserEntry.COLUMN_USER_IN_USE, in_use);
            long id = getWritableDatabase().insert(UserEntry.TABLE_USER, null, values);
            Log.i(TAG, "Add user,id: " + id);
        } else {
            Log.i(TAG, "The same account already exists ,do not add the user.");
        }

    }

    public void addLocation(String account, double latitude, double longitude, String time) {
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_LOCATION_EMAIL, account);
        values.put(LocationEntry.COLUMN_LOCATION_LATITUDE, latitude);
        values.put(LocationEntry.COLUMN_LOCATION_LONGITUDE, longitude);
        values.put(LocationEntry.COLUMN_LOCATION_TIME, time);

        long id = getWritableDatabase().insert(LocationEntry.TABLE_LOCATION, null, values);
        Log.i(TAG, "Add location,id: " + id);
    }

    //查詢資料表 Location 的最新 time 值

    public String searchNewTime(String account) {
        //先查詢 Location 是否有紀錄
        Cursor cursor = getReadableDatabase().query(
                LocationEntry.TABLE_LOCATION, new String[]{LocationEntry.COLUMN_LOCATION_TIME}, LocationEntry.COLUMN_LOCATION_TIME + ">?" + " AND " +
                        LocationEntry.COLUMN_LOCATION_EMAIL + "=?", new String[]{"1911-01-01-00:00:00", account},
                null, null, LocationEntry.COLUMN_LOCATION_TIME + " DESC");

        //取得最新 time 值
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int index_time_to_compare = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_TIME);
            String time_to_compare = cursor.getString(index_time_to_compare);
            return time_to_compare;
        }
        return "1911-01-01-00:00:00";
    }
    public List<SimpleLocation> searchAllLocation(String account) {
        List<SimpleLocation> locationList = new ArrayList<>();

        Cursor cursor = getReadableDatabase().query(
                LocationEntry.TABLE_LOCATION, null, LocationEntry.COLUMN_LOCATION_EMAIL + "=?", new String[]{account},
                null, null, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int index_id = cursor.getColumnIndex(LocationEntry._ID);
                int index_ac = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_EMAIL);
                int index_time = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_TIME);
                int index_lat = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_LATITUDE);
                int index_lng = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_LONGITUDE);

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
                UserEntry.TABLE_USER, null, UserEntry.COLUMN_USER_USED_FOR + "=?", new String[]{usedFor},
                null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                User user = new User();
                int index_account = cursor.getColumnIndex(UserEntry.COLUMN_USER_EMAIL);
                int index_password = cursor.getColumnIndex(UserEntry.COLUMN_USER_PASSWORD);
                int index_in_use = cursor.getColumnIndex(UserEntry.COLUMN_USER_IN_USE);
                user.setEmail(cursor.getString(index_account));
                user.setPassword(cursor.getString(index_password));
                user.setUsedFor(usedFor);
                user.setIn_use(cursor.getString(index_in_use));
                //存在帳密，已註冊
                if (user.getEmail() != null && user.getPassword() != null) {
                    userList.add(user);
                }
                cursor.moveToNext();
            }
        }
        return userList;
    }

    //更新使用者為已登入

    public void updateUser_in_use(String account) {
        //先更新所有 getLocation 帳號為未登入
        ContentValues values_in_use_no = new ContentValues();
        values_in_use_no.put(UserEntry.COLUMN_USER_IN_USE, UserEntry.IN_USE_NO);
        int i = getWritableDatabase().update(UserEntry.TABLE_USER, values_in_use_no, UserEntry.COLUMN_USER_USED_FOR + "=?", new String[]{UserEntry.USED_FOR_GETLOCATION});
        Log.i(TAG, "update user column in_use,count:" + i);
        //再更新指定的使用者為已登入
        ContentValues values_in_use_yes = new ContentValues();
        values_in_use_yes.put(UserEntry.COLUMN_USER_IN_USE, UserEntry.IN_USE_YES);
        int j = getWritableDatabase().update(UserEntry.TABLE_USER, values_in_use_yes, UserEntry.COLUMN_USER_EMAIL + "=?", new String[]{account});
        Log.i(TAG, "update user column in_use,count:" + j);
    }

    public void updatePassword(String account, String password) {
        Cursor c = getReadableDatabase().query(UserEntry.TABLE_USER, new String[]{"account"},
                UserEntry.COLUMN_USER_EMAIL + " = ? and " + UserEntry.COLUMN_USER_PASSWORD + " = ? ",
                new String[]{account, password}, null, null, null);
        if (!c.moveToFirst()) {
            c.close();
            ContentValues values = new ContentValues();
            values.put(UserEntry.COLUMN_USER_PASSWORD, password);

            long i = getWritableDatabase().update(
                    UserEntry.TABLE_USER, values, UserEntry.COLUMN_USER_EMAIL + " = ? ", new String[]{account});
            Log.i(TAG, "update user column password,count:" + i);
        } else {
            Log.i(TAG, "The same account and password already exists ,do not update the password");
        }
    }*/
}
