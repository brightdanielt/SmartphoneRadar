package com.cauliflower.danielt.smartphoneradar.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cauliflower.danielt.smartphoneradar.network.HandlerXML;

/**
 * This class serves as the ContentProvider for all of the SmartphoneRadar's data. This class allows us to
 * insert data, query data and update data.
 */
public class RadarProvider extends ContentProvider {

    /**
     * These constant will be used to match URIs with the data they are looking for. We will take
     * advantage of the UriMatcher class to make the matching MUCH easier than doing something
     * ourselves, such as using regular expressions.
     */
    public static final int CODE_USER = 100;
    public static final int CODE_LOCATION = 200;

    /**
     * The URI Matcher used by this content provider. The leading "s" in this variable name
     * signifies that this UriMatcher is a static member variable of RadarProvider and is a
     * common convention in Android programming.
     */
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RadarDbHelper mOpenHelper;

    /**
     * Create the UriMatcher that will match each URI to the CODE_USER and CODE_LOCATION constants
     * defined above.
     *
     * @return A UriMatcher that correctly matches the constants for CODE_USER and CODE_LOCATION
     */
    public static UriMatcher buildUriMatcher() {

        /**
         * All paths added to the UriMatcher have a corresponding code to return when a match is
         * found. THe code passed in the constructor of UriMatcher here represents the code to
         * return for the root URI's. It's the common to use NO_MATCH as the code for this case.
         * */
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RadarContract.CONTENT_AUTHORITY;


        //This URI is content://com.cauliflower.danielt.smartphoneradar/user/
        matcher.addURI(authority, RadarContract.PATH_USER, CODE_USER);
        //This URI is content://com.cauliflower.danielt.smartphoneradar/location/
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
            case CODE_USER: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        RadarContract.UserEntry.TABLE_USER,
                        projection,
                        selection,
                        selectionArgs,
                        null, null, sortOrder);
                break;
            }
            case CODE_LOCATION: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        RadarContract.LocationEntry.TABLE_LOCATION,
                        null,
                        selection,
                        selectionArgs,
                        null, null, sortOrder);
                break;
            }
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("We are not implementing getType in SmartphoneRadar");
    }

    /**
     * This method has not been used.
     * I consider to ues it when get locations from server ,the code should update
     * is {@link HandlerXML}
     * and {@link com.cauliflower.danielt.smartphoneradar.ui.MapsActivity}
     */
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

    /**
     * In SmartphoneRadar, we may insert location and user
     *
     * @param uri    The URI of insertion request, This must not be null.
     * @param values A set of column_name/value pares to add the newly inserted item.
     * @return The Uri of inserted row.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri insertedUri;
        switch (sUriMatcher.match(uri)) {
            //可能要考慮已存在該使用者的情況
            case CODE_USER: {
                long _id = mOpenHelper.getWritableDatabase().insert(RadarContract.UserEntry.TABLE_USER,
                        null,
                        values);
                if (_id != -1) {
                    //return the uri of row inserted
                    insertedUri = ContentUris.withAppendedId(uri, _id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case CODE_LOCATION: {
                long _id = mOpenHelper.getWritableDatabase().insert(RadarContract.LocationEntry.TABLE_LOCATION,
                        null,
                        values);
                if (_id != -1) {
                    //return the uri of row inserted
                    insertedUri = ContentUris.withAppendedId(uri, _id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return insertedUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new RuntimeException("We are not implementing delete in SmartphoneRadar");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rowUpdated;
        switch (sUriMatcher.match(uri)) {
            case CODE_USER: {
                rowUpdated = mOpenHelper.getWritableDatabase().update(
                        RadarContract.UserEntry.TABLE_USER,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            case CODE_LOCATION: {
                rowUpdated = mOpenHelper.getWritableDatabase().update(
                        RadarContract.UserEntry.TABLE_USER,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        if (rowUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowUpdated;
    }

}
