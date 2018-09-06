package com.cauliflower.danielt.smartphoneradar;

import android.app.Application;

import com.cauliflower.danielt.smartphoneradar.data.RadarDatabase;

public class RadarApp extends Application {


    public RadarDatabase getDatabase() {
        return RadarDatabase.getInstance(this);
    }
}
