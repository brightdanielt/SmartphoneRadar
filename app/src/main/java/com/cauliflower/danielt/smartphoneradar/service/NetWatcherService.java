package com.cauliflower.danielt.smartphoneradar.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;

import com.cauliflower.danielt.smartphoneradar.data.PositionPreferences;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NetWatcherService extends JobService {
    private Context mContext;

    public NetWatcherService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        //若定位功能開啟
        if (PositionPreferences.getPositionEnable(mContext)) {
            if (info != null) {
                //開啟網路時同時開啟 RadarService
                if (info.isConnected()) {
                    Intent i = new Intent(mContext, RadarService.class);
                    mContext.startService(i);
                } else {
                    //關閉網路時同時停止 RadarService
                    Intent i = new Intent(mContext, RadarService.class);
                    mContext.stopService(i);
                }
            } else {
                //關閉網路時同時停止 RadarService
                Intent i = new Intent(mContext, RadarService.class);
                mContext.stopService(i);
            }
        } else {
            //若定位功能未開啟，則不做任何動作
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


}
