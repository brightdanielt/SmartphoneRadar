package com.cauliflower.danielt.smartphoneradar.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.cauliflower.danielt.smartphoneradar.obj.SimpleLocation;
import com.cauliflower.danielt.smartphoneradar.obj.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Use MainDb please if you want to do anything related with database.
 * MainDb is created to prevent from repeating same code in different class
 */
public final class MainDb {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private MainDb() {
    }

    public static void addUser(Context context, String account, String password, String usedFor, String in_use) {
        Cursor cursor = context.getContentResolver().query(
                RadarContract.UserEntry.CONTENT_URI,
                new String[]{RadarContract.UserEntry.COLUMN_USER_ACCOUNT},
                RadarContract.UserEntry.COLUMN_USER_ACCOUNT + " = ? and " + RadarContract.UserEntry.COLUMN_USER_USED_FOR + " = ? ",
                new String[]{account, usedFor}, null);
        if (cursor != null) {
            cursor.moveToFirst();
            //不存在該user則能夠新增
            if (cursor.getCount() == 0) {
                cursor.close();
                ContentValues values = new ContentValues();
                values.put(RadarContract.UserEntry.COLUMN_USER_ACCOUNT, account);
                values.put(RadarContract.UserEntry.COLUMN_USER_PASSWORD, password);
                values.put(RadarContract.UserEntry.COLUMN_USER_USED_FOR, usedFor);
                values.put(RadarContract.UserEntry.COLUMN_USER_IN_USE, in_use);
                Uri uri = context.getContentResolver().insert(RadarContract.UserEntry.CONTENT_URI, values);
                Log.i(context.getClass().toString(), "Add user success,uri: " + uri);
            }else {
                Log.i(context.getClass().toString(), "The same account already exists ,do not add the user.");
            }
        } else {
            Log.i(context.getClass().toString(), "Query return null cursor");
        }

    }

    public static void addLocation(Context context, String account, double latitude, double longitude, String time) {
        ContentValues values = new ContentValues();
        values.put(RadarContract.LocationEntry.COLUMN_LOCATION_ACCOUNT, account);
        values.put(RadarContract.LocationEntry.COLUMN_LOCATION_LATITUDE, latitude);
        values.put(RadarContract.LocationEntry.COLUMN_LOCATION_LONGITUDE, longitude);
        values.put(RadarContract.LocationEntry.COLUMN_LOCATION_TIME, time);

        Uri uri = context.getContentResolver().insert(RadarContract.LocationEntry.CONTENT_URI, values);
        Log.i(context.getClass().getSimpleName(), "Add location success,uri: " + uri);
    }

    //查詢資料表 Location 的最新 time 值

    public static String searchNewTime(Context context, String account) {
        //先查詢 Location 是否有紀錄
        Cursor cursor = context.getContentResolver().query(
                RadarContract.LocationEntry.CONTENT_URI,
                new String[]{RadarContract.LocationEntry.COLUMN_LOCATION_TIME},
                RadarContract.LocationEntry.COLUMN_LOCATION_TIME + ">?" + " AND " +
                        RadarContract.LocationEntry.COLUMN_LOCATION_ACCOUNT + "=?",
                new String[]{"1911-01-01-00:00:00", account},
                RadarContract.LocationEntry.COLUMN_LOCATION_TIME + " DESC");

        //取得最新 time 值
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                int index_time_to_compare = cursor.getColumnIndex(RadarContract.LocationEntry.COLUMN_LOCATION_TIME);
                String newestTime = cursor.getString(index_time_to_compare);
                cursor.close();
                return newestTime;
            }
        }
        return "1911-01-01-00:00:00";
    }

    public static List<SimpleLocation> searchAllLocation(Context context, String account) {
        List<SimpleLocation> locationList = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(
                RadarContract.LocationEntry.CONTENT_URI,
                null,
                RadarContract.LocationEntry.COLUMN_LOCATION_ACCOUNT + "=?",
                new String[]{account},
                null);

        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                while (!cursor.isAfterLast()) {
                    int index_id = cursor.getColumnIndex(RadarContract.LocationEntry._ID);
                    int index_ac = cursor.getColumnIndex(RadarContract.LocationEntry.COLUMN_LOCATION_ACCOUNT);
                    int index_time = cursor.getColumnIndex(RadarContract.LocationEntry.COLUMN_LOCATION_TIME);
                    int index_lat = cursor.getColumnIndex(RadarContract.LocationEntry.COLUMN_LOCATION_LATITUDE);
                    int index_lng = cursor.getColumnIndex(RadarContract.LocationEntry.COLUMN_LOCATION_LONGITUDE);

                    int id = cursor.getInt(index_id);
                    String ac = cursor.getString(index_ac);
                    String time = cursor.getString(index_time);
                    double lat = cursor.getDouble(index_lat);
                    double lng = cursor.getDouble(index_lng);
                    SimpleLocation simpleLocation = new SimpleLocation(time, lat, lng);
                    locationList.add(simpleLocation);
                    Log.i(context.getClass().toString(), id + "\n" + ac + "\n" + time + "\n" + lat + "\n" + lng);
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        return locationList;
    }

    public static List<User> searchUser(Context context, String usedFor) {
//        String[] user = new String[2];
        List<User> userList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(
                RadarContract.UserEntry.CONTENT_URI,
                null,
                RadarContract.UserEntry.COLUMN_USER_USED_FOR + "=?",
                new String[]{usedFor},
                null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                while (!cursor.isAfterLast()) {
                    User user = new User();
                    int index_account = cursor.getColumnIndex(RadarContract.UserEntry.COLUMN_USER_ACCOUNT);
                    int index_password = cursor.getColumnIndex(RadarContract.UserEntry.COLUMN_USER_PASSWORD);
                    int index_in_use = cursor.getColumnIndex(RadarContract.UserEntry.COLUMN_USER_IN_USE);
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
            cursor.close();
        }
        return userList;
    }

    //更新使用者為已登入

    public static void updateUser_in_use(Context context, String account) {
        //先更新所有 getLocation 帳號為未登入
        ContentValues values_in_use_no = new ContentValues();
        values_in_use_no.put(RadarContract.UserEntry.COLUMN_USER_IN_USE, RadarContract.UserEntry.IN_USE_NO);
        int i = context.getContentResolver().update(
                RadarContract.UserEntry.CONTENT_URI,
                values_in_use_no, RadarContract.UserEntry.COLUMN_USER_USED_FOR + "=?",
                new String[]{RadarContract.UserEntry.USED_FOR_GETLOCATION});
        Log.i(context.getClass().getSimpleName(), "update user column in_use,count:" + i);
        //再更新指定的使用者為已登入
        ContentValues values_in_use_yes = new ContentValues();
        values_in_use_yes.put(RadarContract.UserEntry.COLUMN_USER_IN_USE, RadarContract.UserEntry.IN_USE_YES);
        int j = context.getContentResolver().update(RadarContract.UserEntry.CONTENT_URI,
                values_in_use_yes,
                RadarContract.UserEntry.COLUMN_USER_ACCOUNT + "=?",
                new String[]{account});
        Log.i(context.getClass().getSimpleName(), "update user column in_use,count:" + j);
    }

    public static void updatePassword(Context context, String account, String password) {
        Cursor c = context.getContentResolver().query(
                RadarContract.UserEntry.CONTENT_URI,
                new String[]{RadarContract.UserEntry.COLUMN_USER_ACCOUNT},
                RadarContract.UserEntry.COLUMN_USER_ACCOUNT + " = ? and " + RadarContract.UserEntry.COLUMN_USER_PASSWORD + " = ? ",
                new String[]{account, password},
                null);
        if (c != null) {
            c.close();
            ContentValues values = new ContentValues();
            values.put(RadarContract.UserEntry.COLUMN_USER_PASSWORD, password);

            long i = context.getContentResolver().update(
                    RadarContract.UserEntry.CONTENT_URI,
                    values,
                    RadarContract.UserEntry.COLUMN_USER_ACCOUNT + " = ? ",
                    new String[]{account});
            Log.i(context.getClass().getSimpleName(), "update user column password,count:" + i);
        } else {
            Log.i(context.getClass().getSimpleName(), "The same account and password already exists ,do not update the password");
        }
    }
}
