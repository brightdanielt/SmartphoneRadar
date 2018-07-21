package com.cauliflower.danielt.smartphoneradar.obj;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.cauliflower.danielt.smartphoneradar.data.RadarContract;

/**
 * Created by danielt on 2018/3/25.
 */

public class SimpleLocation {
    public static final String TIME = "time";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    private String email;
    private String time;
    private double latitude;
    private double longitude;

    public SimpleLocation() {
    }

    public SimpleLocation(String email, String time, double latitude, double longitude) {
        this.email = email;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public SimpleLocation(@NonNull Cursor cursor) {
        int index_email = cursor.getColumnIndex(RadarContract.LocationEntry.COLUMN_LOCATION_EMAIL);
        int index_time = cursor.getColumnIndex(RadarContract.LocationEntry.COLUMN_LOCATION_TIME);
        int index_lat = cursor.getColumnIndex(RadarContract.LocationEntry.COLUMN_LOCATION_LATITUDE);
        int index_lng = cursor.getColumnIndex(RadarContract.LocationEntry.COLUMN_LOCATION_LONGITUDE);

        email = cursor.getString(index_email);
        time = cursor.getString(index_time);
        latitude = cursor.getDouble(index_lat);
        longitude = cursor.getDouble(index_lng);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getEmail() {
        return email;
    }

    public String getTime() {
        return time;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(RadarContract.LocationEntry.COLUMN_LOCATION_EMAIL, email);
        values.put(RadarContract.LocationEntry.COLUMN_LOCATION_TIME, time);
        values.put(RadarContract.LocationEntry.COLUMN_LOCATION_LATITUDE, latitude);
        values.put(RadarContract.LocationEntry.COLUMN_LOCATION_LONGITUDE, longitude);
        return values;
    }

    @Override
    public String toString() {
        return "SimpleLocation" + "\t" +
                email + "\t" +
                time + "\t" +
                latitude + "\t" +
                longitude;
    }
}
