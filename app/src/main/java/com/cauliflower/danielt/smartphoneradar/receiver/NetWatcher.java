package com.cauliflower.danielt.smartphoneradar.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.cauliflower.danielt.smartphoneradar.data.RadarPreferences;
import com.cauliflower.danielt.smartphoneradar.service.NetWatcherJob;
import com.cauliflower.danielt.smartphoneradar.service.RadarService;

/**
 * Created by danielt on 2018/4/10.
 * <p>
 * 監聽裝置的網路連線狀況，當定位設定已開啟且網路連接上，啟動 RadarService
 * <p>
 * {@link NetWatcher} will be registered in two ways:
 * 1.<action android:name="android.net.conn.CONNECTIVITY_CHANGE" /> in Manifest and it works for
 * </>apps targeting M and below
 * <p>
 * 2.Registered in {@link NetWatcherJob} programmatically and it works for apps targeting N and higher
 */

public class NetWatcher extends BroadcastReceiver {
    private static final String TAG = NetWatcher.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive action: " + intent.getAction());
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Log.d(TAG, "Can not get system service: CONNECTIVITY_SERVICE in onReceive");
            return;
        }
        NetworkInfo info = cm.getActiveNetworkInfo();

        //若定位功能開啟
        if (RadarPreferences.getPositionCheck(context)) {
            if (info != null) {
                //開啟網路時同時開啟 RadarService
                if (info.isConnected() && !RadarService.mInService) {
                    Intent i = new Intent(context, RadarService.class);
                    // Can not use startService when app is in background for device api O
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(i);
                    } else {
                        context.startService(i);
                    }
                } else {
                    //關閉網路時同時停止 RadarService
                    Intent i = new Intent(context, RadarService.class);
                    context.stopService(i);
                }
            } else {
                //關閉網路時同時停止 RadarService
                Intent i = new Intent(context, RadarService.class);
                context.stopService(i);
            }
        } else {
            //若定位功能未開啟，則不做任何動作
        }
    }
}
