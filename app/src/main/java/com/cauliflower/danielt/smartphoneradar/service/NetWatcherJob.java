package com.cauliflower.danielt.smartphoneradar.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;

import com.cauliflower.danielt.smartphoneradar.receiver.NetWatcher;

/**
 * NetWatcherJob works for apps targeting N and higher
 * The content of job is that registering the receiver {@link NetWatcher}
 */
@TargetApi(Build.VERSION_CODES.N)
public class NetWatcherJob extends JobService {
    private static final String TAG = NetWatcherJob.class.getSimpleName();
    private NetWatcher mNetWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "onStartJob");
        mNetWatcher = new NetWatcher();
        //Register a receiver triggered by network connectivity event
        registerReceiver(mNetWatcher, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        //Continue running job
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob");
        unregisterReceiver(mNetWatcher);
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }
}
