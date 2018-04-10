package com.cauliflower.danielt.smartphoneradar.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.cauliflower.danielt.smartphoneradar.data.PositionPreferences;

/**
 * Created by danielt on 2018/4/10.
 */

public class NetWatcher extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        //定位功能開啟時，開啟網路時同時開啟 RadarService
        //定位功能開啟時，關閉網路時同時停止 RadarService
        if(PositionPreferences.getPositionEnable(context)){
            if (info != null) {
                if (info.isConnected()) {
                    //start service
                    Intent i = new Intent(context, RadarService.class);
                    context.startService(i);
                } else {
                    //stop service
                    Intent i = new Intent(context, RadarService.class);
                    context.stopService(i);
                }
            }else {
                //stop service
                Intent i = new Intent(context, RadarService.class);
                context.stopService(i);
            }
        }
    }
}
