package com.cauliflower.danielt.smartphoneradar.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import android.widget.Toast;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.data.MainDb;
import com.cauliflower.danielt.smartphoneradar.data.RadarPreferences;
import com.cauliflower.danielt.smartphoneradar.data.RadarContract;
import com.cauliflower.danielt.smartphoneradar.firebase.RadarFirestore;
import com.cauliflower.danielt.smartphoneradar.obj.User;
import com.cauliflower.danielt.smartphoneradar.network.ConnectServer;
import com.cauliflower.danielt.smartphoneradar.ui.AccountActivity;
import com.cauliflower.danielt.smartphoneradar.ui.SettingsActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class RadarService extends Service {

    private String TAG = RadarService.class.getSimpleName();
    public static boolean mInService = false;

    private FusedLocationProviderClient mClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private String mEmail, mPassword;
    private String mIMEI;
    private int mDocumentId = 1;
    private FirebaseAuth mAuth;

    private Runnable mTask_serviceStatus = new Runnable() {
        @Override
        public void run() {
            mWorker.postDelayed(this, 15000);
            Log.d(TAG, "InService: " + mInService);
        }
    };
    private Handler mWorker = new Handler();

    //http no response should not bigger than 3 times
    private static final int NO_RESPONSE_MAXIMUM = 3;
    private int mUpdateLocationFailureCount = 0;

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
        mAuth = FirebaseAuth.getInstance();
//        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
    }

    private void createLocationRequest() {
        String frequency = RadarPreferences.getUpdateFrequency(RadarService.this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Integer.valueOf(frequency));
        mLocationRequest.setFastestInterval(Integer.valueOf(frequency));
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        Log.i(TAG, "updateFrequency:" + frequency);
    }

    private void createLocationCallback() {

        //取得裝置位置後的動作
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //指定要更新的 document id
//                updateDocumentId();
                final Location location = locationResult.getLastLocation();
                Log.i(TAG, "onLocationResult is working");
//                Toast.makeText(RadarService.this, "call onLocationResult", Toast.LENGTH_SHORT).show();

                //更新位置資訊到 fireStore
                RadarFirestore.updateLocation(
                        String.valueOf(mDocumentId), mEmail, mPassword, mAuth.getUid(), mIMEI, location.getLatitude(),
                        location.getLongitude(), new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //更新成功
                                Log.d(TAG, "UpdateLocation success:");
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //更新失敗
                                mUpdateLocationFailureCount++;
                                if (mUpdateLocationFailureCount >= NO_RESPONSE_MAXIMUM) {
                                    Toast.makeText(RadarService.this,
                                            getString(R.string.serverNoResponse_close_service), Toast.LENGTH_SHORT).show();
                                    RadarService.this.stopSelf();
                                }
                                Log.d(TAG, "updateLocation failure times:" + mUpdateLocationFailureCount, e);
                            }
                        });
            }
        };
    }

    /**
     * 緩衝座標資訊，有時更新頻率太快，監聽反應又太慢，舊的座標已經被新座標覆蓋，而舊座標卻沒有被查詢到
     * 所以在 firestore 建立 5 個座標文件，作為緩衝
     */
    private void updateDocumentId() {
        mDocumentId++;
        if (mDocumentId > 5) {
            mDocumentId = 1;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "onStartCommand");
        //檢查使用者身份
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.i(TAG, "No permission to get IMEI");
            RadarService.this.stopSelf();
        }
        mIMEI = telephonyManager.getDeviceId();
        mAuth = FirebaseAuth.getInstance();
        List<User> userList = MainDb.searchUser(RadarService.this, RadarContract.UserEntry.USED_FOR_SENDLOCATION);
        for (User user : userList) {
            mPassword = user.getPassword();
            break;
        }
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mEmail = user.getEmail();
        }
        if (mEmail != null && !mPassword.trim().equals("") && mIMEI != null) {
            mInService = true;
            showServiceStatus();

            createLocationRequest();
            createLocationCallback();
            fuseLocationRequest();
            foregroundService();
        } else {
            Log.i(TAG, "No firebaseUser so RadarService stopped");
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
        if (mClient != null) {
            mClient.removeLocationUpdates(mLocationCallback);
        }
//        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onDestroy");
        mWorker.removeCallbacks(mTask_serviceStatus);

        mInService = false;
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

        //android O need a channel for notification
        NotificationChannel radarChannel = null;
        String channelId = "AndroidChannel";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            radarChannel = new NotificationChannel(
                    channelId, "AndroidRadar", NotificationManager.IMPORTANCE_HIGH);
            radarChannel.setBypassDnd(true);
            radarChannel.enableLights(false);
            radarChannel.enableVibration(true);
        }

        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.pref_positioning))
//                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.))
                .setSmallIcon(R.drawable.ic_hat_notify)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(channelId);
        }
        RadarService.this.startForeground(333, builder.build());
    }

}
