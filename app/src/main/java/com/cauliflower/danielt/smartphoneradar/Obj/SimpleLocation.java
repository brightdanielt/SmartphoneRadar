package com.cauliflower.danielt.smartphoneradar.Obj;

/**
 * Created by danielt on 2018/3/25.
 */

public class SimpleLocation {
    public static final String TIME = "time";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    private String time;
    private double latitude;
    private double longitude;

    public SimpleLocation(String time, double latitude, double longitude) {
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
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
}
