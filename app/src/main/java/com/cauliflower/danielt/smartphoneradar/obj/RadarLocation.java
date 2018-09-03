package com.cauliflower.danielt.smartphoneradar.obj;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.cauliflower.danielt.smartphoneradar.data.RadarContract;

import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.LocationEntry.COLUMN_LOCATION_EMAIL;
import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.LocationEntry.COLUMN_LOCATION_LATITUDE;
import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.LocationEntry.COLUMN_LOCATION_LONGITUDE;
import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.LocationEntry.COLUMN_LOCATION_TIME;

/**
 * Created by danielt on 2018/3/25.
 */

@Entity(tableName = RadarContract.LocationEntry.TABLE_LOCATION,primaryKeys = {COLUMN_LOCATION_EMAIL,COLUMN_LOCATION_TIME})
public class RadarLocation {

    @ColumnInfo(name = COLUMN_LOCATION_EMAIL)
    private String email;

    @TypeConverters(RoomConverter.class)
    @ColumnInfo(name = COLUMN_LOCATION_TIME)
    private String time;

    @ColumnInfo(name = COLUMN_LOCATION_LATITUDE)
    private double latitude;

    @ColumnInfo(name = COLUMN_LOCATION_LONGITUDE)
    private double longitude;

    public RadarLocation() {
    }

    public RadarLocation(String email, String time, double latitude, double longitude) {
        this.email = email;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public RadarLocation(@NonNull Cursor cursor) {
        int index_email = cursor.getColumnIndex(COLUMN_LOCATION_EMAIL);
        int index_time = cursor.getColumnIndex(COLUMN_LOCATION_TIME);
        int index_lat = cursor.getColumnIndex(COLUMN_LOCATION_LATITUDE);
        int index_lng = cursor.getColumnIndex(COLUMN_LOCATION_LONGITUDE);

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
        values.put(COLUMN_LOCATION_EMAIL, email);
        values.put(COLUMN_LOCATION_TIME, time);
        values.put(COLUMN_LOCATION_LATITUDE, latitude);
        values.put(COLUMN_LOCATION_LONGITUDE, longitude);
        return values;
    }

    @Override
    public String toString() {
        return "RadarLocation" + "\t" +
                email + "\t" +
                time + "\t" +
                latitude + "\t" +
                longitude;
    }
}
