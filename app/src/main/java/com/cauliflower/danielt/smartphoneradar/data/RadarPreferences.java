package com.cauliflower.danielt.smartphoneradar.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.service.RadarService;

/**
 * Created by danielt on 2018/4/9.
 */

public final class RadarPreferences {

    //Get value of PositionEnable preference
    public static Boolean getPositionEnable(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String key_position = context.getString(R.string.pref_key_position);
        Boolean default_position = context.getResources().getBoolean(R.bool.pref_defaultValue_position);
        return sp.getBoolean(key_position, default_position);
    }
    //Get value of UpdateFrequency preference
    public static String getUpdateFrequency(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String key_updateFrequency = context.getString(R.string.pref_key_updateFrequency);
        String default_updateFrequency = context.getResources().getString(R.string.pref_defaultValue_updateFrequency);
        return sp.getString(key_updateFrequency, default_updateFrequency);
    }

    public static void startRadarService(Context context) {
        Intent i = new Intent();
        i.setClass(context, RadarService.class);
        context.startService(i);
    }

    public static void stopRadarService(Context context) {
        Intent i = new Intent();
        i.setClass(context, RadarService.class);
        context.stopService(i);
    }
}
