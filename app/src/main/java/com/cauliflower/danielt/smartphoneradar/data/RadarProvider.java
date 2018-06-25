package com.cauliflower.danielt.smartphoneradar.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * This class serves as the ContentProvider for all of the SmartphoneRadar's data. This class allows us to
 * bulkInsert data, query data and delete data.
 */
public class RadarProvider extends ContentProvider {

    /*
     * These constant will be used to match URIs with the data they are looking for. We will take
     * advantage of the UriMatcher class to make the matching MUCH easier than doing something
     * ourselves, such as using regular expressions.
     * */
    public static final int CODE_USER = 100;
    public static final int CODE_UPDATE_USER_IN_USED = 101;
    public static final int CODE_UPDATE_USER_PASSWORD = 102;
    public static final int CODE_SEARCH_USER_USED_FOR_GETLOCATION = 103;
    public static final int CODE_SEARCH_USER_USED_FOR_SENDLOCATION = 104;

    public static final int CODE_LOCATION = 200;
    public static final int CODE_LOCATION_SEARCH_NEWEST_TIME = 201;


    /*
     * The URI Matcher used by this content provider. The leading "s" in this variable name
     * signifies that this UriMatcher is a static member variable of RadarProvider and is a
     * common convention in Android programming.
     * */
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RadarDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RadarContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, RadarContract.PATH_USER, CODE_USER);
        matcher.addURI(authority, RadarContract.PATH_LOCATION, CODE_LOCATION);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        /*
         * onCreate is run on the main thread, so performing nay lengthy operations will
         * cause lag in your app. Since RadarDbHelper's constructor is very lightweight,
         * we are safe to perform that initialization here.
         * */
        mOpenHelper = new RadarDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor = null;
        switch (sUriMatcher.match(uri)) {
            case CODE_SEARCH_USER_USED_FOR_SENDLOCATION: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        RadarContract.UserEntry.TABLE_USER,
                        null,
                        RadarContract.UserEntry.COLUMN_USER_USED_FOR + "=?",
                        new String[]{RadarContract.UserEntry.USED_FOR_SENDLOCATION},
                        null, null, null);
                break;
            }
            case CODE_SEARCH_USER_USED_FOR_GETLOCATION: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        RadarContract.UserEntry.TABLE_USER,
                        null,
                        RadarContract.UserEntry.COLUMN_USER_USED_FOR + "=?",
                        new String[]{RadarContract.UserEntry.USED_FOR_GETLOCATION},
                        null, null, null);
                break;
            }
            case CODE_LOCATION_SEARCH_NEWEST_TIME: {
                String account = "";
                Cursor cursorUser = mOpenHelper.getReadableDatabase().query(
                        RadarContract.UserEntry.TABLE_USER,
                        null,
                        RadarContract.UserEntry.COLUMN_USER_USED_FOR + "=?",
                        new String[]{RadarContract.UserEntry.USED_FOR_GETLOCATION},
                        null, null, null);

                if (cursorUser.getCount() > 0) {
                    cursorUser.moveToFirst();
                    while (!cursorUser.isAfterLast()) {
                        int index_account = cursorUser.getColumnIndex(RadarContract.UserEntry.COLUMN_USER_ACCOUNT);
                        account = cursorUser.getString(index_account);
                    }
                }

                if (!account.trim().equals("")) {
                    cursor = mOpenHelper.getReadableDatabase().query(
                            RadarContract.LocationEntry.TABLE_LOCATION, new String[]{RadarContract.LocationEntry.COLUMN_LOCATION_TIME}, RadarContract.LocationEntry.COLUMN_LOCATION_TIME + ">?" + " AND " +
                                    RadarContract.LocationEntry.COLUMN_LOCATION_ACCOUNT + "=?", new String[]{"1911-01-01-00:00:00", account},
                            null, null, RadarContract.LocationEntry.COLUMN_LOCATION_TIME + " DESC");
                } else {
                    Log.i(getContext().toString(), "There is no user in used for getLocation so failed to query newest time");
                    return cursor;
                }
            }
            case CODE_LOCATION: {
                String account = "";
                Cursor cursorUser = mOpenHelper.getReadableDatabase().query(
                        RadarContract.UserEntry.TABLE_USER,
                        null,
                        RadarContract.UserEntry.COLUMN_USER_USED_FOR + "=?",
                        new String[]{RadarContract.UserEntry.USED_FOR_GETLOCATION},
                        null, null, null);

                if (cursorUser.getCount() > 0) {
                    cursorUser.moveToFirst();
                    while (!cursorUser.isAfterLast()) {
                        int index_account = cursorUser.getColumnIndex(RadarContract.UserEntry.COLUMN_USER_ACCOUNT);
                        account = cursorUser.getString(index_account);
                    }
                }

                if (!account.trim().equals("")) {
                    cursor = mOpenHelper.getReadableDatabase().query(
                            RadarContract.LocationEntry.TABLE_LOCATION,
                            null,
                            RadarContract.LocationEntry.COLUMN_LOCATION_ACCOUNT + "=?",
                            new String[]{account},
                            null, null, null);
                } else {
                    Log.i(getContext().toString(), "There is no user in used for getLocation so failed to query locations");
                }
                break;
            }
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
//            case CODE_LOCATION: {
//                break;
//            }
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        switch (sUriMatcher.match(uri)) {

            //可能要考慮已存在該使用者的情況
            case CODE_USER: {
                long _id = mOpenHelper.getWritableDatabase().insert(RadarContract.UserEntry.TABLE_USER,
                        null,
                        values);
                if (_id != -1) {
                    //return the uri of row inserted
                    return RadarContract.BASE_CONTENT_URI.buildUpon().path("#" + _id).build();
                }
            }
            case CODE_LOCATION: {
                long _id = mOpenHelper.getWritableDatabase().insert(RadarContract.LocationEntry.TABLE_LOCATION,
                        null,
                        values);
                if (_id != -1) {
                    //return the uri of row inserted
                    return RadarContract.BASE_CONTENT_URI.buildUpon().path("#" + _id).build();
                }
            }
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rowUpdated = 0;
        switch (sUriMatcher.match(uri)) {
            case CODE_UPDATE_USER_IN_USED: {
                ContentValues values_in_use_no = new ContentValues();
                values_in_use_no.put(RadarContract.UserEntry.COLUMN_USER_IN_USE, RadarContract.UserEntry.IN_USE_NO);
                int i = mOpenHelper.getWritableDatabase().update(RadarContract.UserEntry.TABLE_USER, values_in_use_no, RadarContract.UserEntry.COLUMN_USER_USED_FOR + "=?", new String[]{RadarContract.UserEntry.USED_FOR_GETLOCATION});
                Log.i(getContext().toString(), "update user column in_use,count:" + i);
                //再更新指定的使用者為已登入
                ContentValues values_in_use_yes = new ContentValues();
                values_in_use_yes.put(RadarContract.UserEntry.COLUMN_USER_IN_USE, RadarContract.UserEntry.IN_USE_YES);
                rowUpdated = mOpenHelper.getWritableDatabase().update(RadarContract.UserEntry.TABLE_USER,
                        values_in_use_yes,
                        RadarContract.UserEntry.COLUMN_USER_ACCOUNT + "=?",
                        selectionArgs);
                Log.i(getContext().toString(), "update user column in_use,count:" + rowUpdated);
            }
            case CODE_UPDATE_USER_PASSWORD: {
                rowUpdated = mOpenHelper.getWritableDatabase().update(
                        RadarContract.UserEntry.TABLE_USER, values,
                        RadarContract.UserEntry.COLUMN_USER_ACCOUNT + " = ? ",
                        selectionArgs);
                Log.i(getContext().toString(), "update user column password,count:" + rowUpdated);
            }
        }
        return rowUpdated;
    }
}
