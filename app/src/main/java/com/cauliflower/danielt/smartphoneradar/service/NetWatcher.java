package com.cauliflower.danielt.smartphoneradar.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.cauliflower.danielt.smartphoneradar.data.PositionPreferences;

/**
 * Created by danielt on 2018/4/10.
 * 監聽裝置的網路連線狀況
 */

public class NetWatcher extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        //若定位功能開啟
        if (PositionPreferences.getPositionEnable(context)) {
            if (info != null) {
                //開啟網路時同時開啟 RadarService
                if (info.isConnected()) {
                    Intent i = new Intent(context, RadarService.class);
                    context.startService(i);
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
