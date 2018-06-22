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

import android.widget.Toast;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.data.PositionPreferences;
import com.cauliflower.danielt.smartphoneradar.data.RadarContract;
import com.cauliflower.danielt.smartphoneradar.obj.User;
import com.cauliflower.danielt.smartphoneradar.tool.ConnectServer;
import com.cauliflower.danielt.smartphoneradar.data.RadarDbHelper;
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
    public static boolean mInService = false;
    private String mAccount_sendLocation, mPassword_sendLocation;

    private FusedLocationProviderClient mClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private ConnectServer mConnectServer;

    private Runnable mTask_serviceStatus = new Runnable() {
        @Override
        public void run() {
            mWorker.postDelayed(this, 15000);
            Log.d(TAG, "InService: " + mInService);
        }
    };
    private Handler mWorker = new Handler();

    private static final int NO_RESPONSE_MAXIMUM = 3;
    private int mNoResponseCount = 0;

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
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Integer.valueOf(frequency));
        mLocationRequest.setFastestInterval(Integer.valueOf(frequency));
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        Log.i(TAG, "updateFrequency:" + frequency);
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                final Location location = locationResult.getLastLocation();
                Log.i(TAG, "onLocationResult is working");
//                Toast.makeText(RadarService.this, "call onLocationResult", Toast.LENGTH_SHORT).show();

                SimpleDateFormat s = new SimpleDateFormat("yy-MM-dd-HH:mm:ss");
                String time = s.format(new Date());
                try {
                    String response = mConnectServer.sendLocationToServer(mAccount_sendLocation, mPassword_sendLocation,
                            time, location.getLatitude(), location.getLongitude());
                    if (response.contains(ConnectServer.NO_RESPONSE)) {
                        mNoResponseCount++;
                        Log.i(TAG, "Server no response times: " + mNoResponseCount);
                        if (mNoResponseCount >= NO_RESPONSE_MAXIMUM) {
                            Toast.makeText(RadarService.this,
                                    getString(R.string.serverNoResponse_close_service), Toast.LENGTH_SHORT).show();
                            RadarService.this.stopSelf();
                        }
                    }
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
        RadarDbHelper dbHelper = new RadarDbHelper(RadarService.this);
        List<User> userList = dbHelper.searchUser(RadarContract.UserEntry.USED_FOR_SENDLOCATION);
        dbHelper.close();
        for (User user : userList) {
            mAccount_sendLocation = null;
            mPassword_sendLocation = null;
            mAccount_sendLocation = user.getAccount();
            mPassword_sendLocation = user.getPassword();
        }
        if (mAccount_sendLocation != null) {
            mInService = true;
            showServiceStatus();

            mConnectServer = new ConnectServer(RadarService.this);
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
        mClient = LocationServices.getFusedLocationProviderClient(RadarService.this);
        mClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    @Override
    public void onDestroy() {
        if(mClient!=null){
            mClient.removeLocationUpdates(mLocationCallback);
        }
//        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onDestroy");
        mWorker.removeCallbacks(mTask_serviceStatus);
        RadarService.this.stopForeground(true);
        super.onDestroy();

    }

    private void showServiceStatus() {
        //每 15 秒 Log 一次
        mWorker.postDelayed(mTask_serviceStatus, 15000);
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
//                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.))
                .setSmallIcon(R.drawable.ic_hat_notify)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .build();
        RadarService.this.startForeground(333, notification);
    }

}
