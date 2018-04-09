package com.cauliflower.danielt.smartphoneradar.tool;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by danielt on 2018/4/9.
 */

public final class RequestPermission {

    public static void accessFineLocation(Activity activity, int request_code) {
        if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, request_code);
        }
    }
}
