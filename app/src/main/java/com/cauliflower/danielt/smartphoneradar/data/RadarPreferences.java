package com.cauliflower.danielt.smartphoneradar.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.service.RadarService;

/**
 * Created by danielt on 2018/4/9.
 */

public final class RadarPreferences {

    public static void setTrackingTargetEmail(Context context, String email) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        String key_trackingTarget = context.getString(R.string.pref_key_trackingTarget);
        editor.putString(key_trackingTarget, email);
        editor.apply();
    }

    public static String getTrackingTargetEmail(Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String key_trackingTarget = context.getString(R.string.pref_key_trackingTarget);
        return preferences.getString(key_trackingTarget, "");
    }

    //Get value of PositionEnable preference
    public static Boolean getPositionEnable(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String key_position = context.getString(R.string.pref_key_position);
        Boolean default_position = context.getResources().getBoolean(R.bool.pref_defaultValue_position);
        return sp.getBoolean(key_position, default_position);
    }

    //Set value of PositionEnable preference
    public static void setPositionCheck(Context context, boolean check) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String key_position = context.getString(R.string.pref_key_position);
        editor.putBoolean(key_position, check);
        editor.apply();
    }

    //Set value of PositionEnable preference
    public static boolean getPositionCheck(Context context) {
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

    //Set value of showNewMarkOnly in MapsActivity
    public static void setShowNewMarkOnly(Context context, boolean show) {
        SharedPreferences preferences = context.getSharedPreferences(
                context.getString(R.string.mapsActivity_pref), Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean(context.getString(R.string.pref_key_showNewMarkOnly), show);
        edit.apply();
    }

    //Get value of showNewMarkOnly in MapsActivity
    public static boolean getShowNewMarkOnly(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                context.getString(R.string.mapsActivity_pref), Context.MODE_PRIVATE);
        return preferences.getBoolean(context.getString(R.string.pref_key_showNewMarkOnly),
                context.getResources().getBoolean(R.bool.pref_defaultValue_showNewMarkOnly));
    }

    //Set value of showLocationList in MapsActivity
    public static void setShowLocationList(Context context, boolean show) {
        SharedPreferences preferences = context.getSharedPreferences(
                context.getString(R.string.mapsActivity_pref), Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean(context.getString(R.string.pref_key_showList), show);
        edit.apply();
    }

    //Get value of showLocationList in MapsActivity
    public static boolean getShowLocationList(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                context.getString(R.string.mapsActivity_pref), Context.MODE_PRIVATE);
        return preferences.getBoolean(context.getString(R.string.pref_key_showList),
                context.getResources().getBoolean(R.bool.pref_defaultValue_showList));
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
