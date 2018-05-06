package com.cauliflower.danielt.smartphoneradar.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.data.PositionPreferences;
import com.cauliflower.danielt.smartphoneradar.obj.User;
import com.cauliflower.danielt.smartphoneradar.tool.ConnectDb;
import com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper;
import com.cauliflower.danielt.smartphoneradar.ui.SettingsActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RadarService extends Service {

    private String TAG = RadarService.class.getSimpleName();
    public static boolean inService = false;
    private String account_sendLocation, password_sendLocation;

    private FusedLocationProviderClient client;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private ConnectDb connectDb;

    Runnable task = new Runnable() {
        @Override
        public void run() {
            worker.postDelayed(this, 15000);

//            Toast.makeText(RadarService.this, "inService: " + inService, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "inService: " + inService);
        }
    };
    Handler worker = new Handler();

    public RadarService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
//        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();

    }

    private void createLocationRequest() {
        String frequency = PositionPreferences.getUpdateFrequency(RadarService.this);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(Integer.valueOf(frequency));
        locationRequest.setFastestInterval(Integer.valueOf(frequency));
        locationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        Log.i(TAG, "updateFrequency:" + frequency);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                final Location location = locationResult.getLastLocation();
                Log.i(TAG, "onLocationResult is working");
//                Toast.makeText(RadarService.this, "call onLocationResult", Toast.LENGTH_SHORT).show();

                SimpleDateFormat s = new SimpleDateFormat("yy-MM-dd-HH:mm:ss");
                String time = s.format(new Date());
                try {
                    connectDb.sendLocationToServer(account_sendLocation, password_sendLocation,
                            time, location.getLatitude(), location.getLongitude());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "onStartCommand");
        MyDbHelper dbHelper = new MyDbHelper(RadarService.this);
        List<User> userList = dbHelper.searchUser(MyDbHelper.VALUE_USER_USEDFOR_SENDLOCATION);
        dbHelper.close();
        for (User user : userList) {
            account_sendLocation = null;
            password_sendLocation = null;
            account_sendLocation = user.getAccount();
            password_sendLocation = user.getPassword();
        }
        if (account_sendLocation != null) {
            inService = true;
            showServiceStatus();

            connectDb = new ConnectDb(RadarService.this);
            createLocationRequest();
            createLocationCallback();
            fuseLocationRequest();
            foregroundService();
        } else {
            Log.i(TAG, "There is no result for search account_sendLocation so RadarService stopped");
            this.stopSelf();
        }

        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    private void fuseLocationRequest() {
        client = LocationServices.getFusedLocationProviderClient(RadarService.this);
        client.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onDestroy() {
        client.removeLocationUpdates(locationCallback);
//        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onDestroy");
        worker.removeCallbacks(task);
        RadarService.this.stopForeground(true);
        super.onDestroy();

    }

    private void showServiceStatus() {

        //每 15 秒 Log 一次
        worker.postDelayed(task, 15000);

    }

    //為了讓 Service 活久一點，只好將 Service 推到前景
    private void foregroundService() {
        Intent intent = new Intent(RadarService.this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
                RadarService.this,
                0,
                intent,
                0);

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.pref_positioning))
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .build();
        RadarService.this.startForeground(333, notification);
    }

}
