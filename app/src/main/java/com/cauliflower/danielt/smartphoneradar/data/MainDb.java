package com.cauliflower.danielt.smartphoneradar.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.LocationEntry.COLUMN_LOCATION_EMAIL;
import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.LocationEntry.COLUMN_LOCATION_TIME;

/**
 * Use MainDb please if you want to do anything related with database.
 * MainDb is created to prevent from repeating same code in different class
 */
public final class MainDb {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private static boolean debug = false;

    private MainDb() {
    }

    public static void addUser(Context context, RadarUser radarUser) {
        //先檢查使否存在該使用者
        Cursor cursor = context.getContentResolver().query(
                RadarContract.UserEntry.CONTENT_URI,
                new String[]{RadarContract.UserEntry.COLUMN_USER_EMAIL},
                RadarContract.UserEntry.COLUMN_USER_EMAIL + " = ? and " + RadarContract.UserEntry.COLUMN_USER_USED_FOR + " = ? ",
                new String[]{radarUser.getEmail(), radarUser.getUsedFor()}, null);
        if (cursor != null) {
            cursor.moveToFirst();
            //不存在該 radarUser 則能夠新增
            if (cursor.getCount() == 0) {
                cursor.close();
                Uri uri = context.getContentResolver().insert(RadarContract.UserEntry.CONTENT_URI, radarUser.getContentValues());
                Log.i(context.getClass().toString(), "Add radarUser success,uri: " + uri);
            } else {
                Log.i(context.getClass().toString(), "The same email already exists ,stop add the radarUser.");
            }
        } else {
            Log.i(context.getClass().toString(), "Query return null cursor");
        }

    }

    public static void addLocation(Context context, RadarLocation radarLocation) {
        Uri uri = context.getContentResolver().insert(
                RadarContract.LocationEntry.CONTENT_URI, radarLocation.getContentValues());
        Log.i(context.getClass().getSimpleName(), "Add location success,uri: " + uri + "\n" +
                "detail:" + radarLocation.toString());
    }

    //查詢資料表 Location 的最新 time 值
    public static String searchNewTime(Context context, String email) {
        //先查詢 Location 是否有紀錄
        Cursor cursor = context.getContentResolver().query(
                RadarContract.LocationEntry.CONTENT_URI,
                new String[]{RadarContract.LocationEntry.COLUMN_LOCATION_TIME},
                RadarContract.LocationEntry.COLUMN_LOCATION_EMAIL + "=?",
                new String[]{email},
                null
        );

        //取得最新 time 值
        if (cursor != null) {
            //最後一筆，最新的資料
            cursor.moveToLast();
            if (cursor.getCount() > 0) {
                int index_time_to_compare = cursor.getColumnIndex(RadarContract.LocationEntry.COLUMN_LOCATION_TIME);
                String newestTime = cursor.getString(index_time_to_compare);
                int index_id = cursor.getColumnIndex(RadarContract.LocationEntry._ID);
                if (debug) {
                    Log.i(context.getClass().getSimpleName(), "Query location id:" + cursor.getInt(index_id));
                    Log.i(context.getClass().getSimpleName(), "Query location time:" + newestTime);
                }
                cursor.close();
                return newestTime;
            }
        }
        //還沒有 Location 資料
        return null;
    }

    public static List<RadarLocation> searchAllLocation(Context context, String email) {
        List<RadarLocation> locationList = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(
                RadarContract.LocationEntry.CONTENT_URI,
                null,
                RadarContract.LocationEntry.COLUMN_LOCATION_EMAIL + "=?",
                new String[]{email},
                null);

        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                while (!cursor.isAfterLast()) {
                    RadarLocation radarLocation = new RadarLocation(cursor);
                    Log.i(context.getClass().toString(), radarLocation.toString());
                    locationList.add(radarLocation);
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        return locationList;
    }

    public static List<RadarUser> searchUser(Context context, String usedFor) {
//        String[] user = new String[2];
        List<RadarUser> radarUserList = new ArrayList<>();
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
                    radarUserList.add(new RadarUser(cursor));
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        return radarUserList;
    }

    //更新使用者為已登入

    public static void updateUser_in_use(Context context, String email) {
        //先更新所有 getLocation 帳號為未登入
        ContentValues values_in_use_no = new ContentValues();
        values_in_use_no.put(RadarContract.UserEntry.COLUMN_USER_IN_USE, RadarContract.UserEntry.IN_USE_NO);
        int i = context.getContentResolver().update(
                RadarContract.UserEntry.CONTENT_URI,
                values_in_use_no, RadarContract.UserEntry.COLUMN_USER_USED_FOR + "=?",
                new String[]{RadarContract.UserEntry.USED_FOR_GETLOCATION});
        Log.i(context.getClass().getSimpleName(), "update user column in_use,count:" + i);
        if (email == null) {
            return;
        }
        //再更新指定的使用者為已登入
        ContentValues values_in_use_yes = new ContentValues();
        values_in_use_yes.put(RadarContract.UserEntry.COLUMN_USER_IN_USE, RadarContract.UserEntry.IN_USE_YES);
        int j = context.getContentResolver().update(RadarContract.UserEntry.CONTENT_URI,
                values_in_use_yes,
                RadarContract.UserEntry.COLUMN_USER_EMAIL + "=?",
                new String[]{email});
        Log.i(context.getClass().getSimpleName(), "update user column in_use,count:" + j);
    }

    public static void updatePassword(Context context, String email, String password) {
        Cursor c = context.getContentResolver().query(
                RadarContract.UserEntry.CONTENT_URI,
                new String[]{RadarContract.UserEntry.COLUMN_USER_EMAIL},
                RadarContract.UserEntry.COLUMN_USER_EMAIL + " = ? and " + RadarContract.UserEntry.COLUMN_USER_PASSWORD + " = ? ",
                new String[]{email, password},
                null);
        if (c != null) {
            c.close();
            ContentValues values = new ContentValues();
            values.put(RadarContract.UserEntry.COLUMN_USER_PASSWORD, password);

            long i = context.getContentResolver().update(
                    RadarContract.UserEntry.CONTENT_URI,
                    values,
                    RadarContract.UserEntry.COLUMN_USER_EMAIL + " = ? ",
                    new String[]{email});
            Log.i(context.getClass().getSimpleName(), "update user column password,count:" + i);
        } else {
            Log.i(context.getClass().getSimpleName(), "The same email and password already exists ,do not update the password");
        }
    }

    /**
     * Delete one row in location table
     *
     * @param radarLocation The location you want to delete.
     */
    public static int deleteLocation(Context context, RadarLocation radarLocation) {
        int rowDeleted = context.getContentResolver().delete(
                RadarContract.LocationEntry.CONTENT_URI,
                COLUMN_LOCATION_EMAIL + "=? AND " + COLUMN_LOCATION_TIME + "=? ",
                new String[]{radarLocation.getEmail(), radarLocation.getTime()});
        Log.i(context.getClass().getSimpleName(), "Delete: " + radarLocation.toString());
        return rowDeleted;
    }
}
