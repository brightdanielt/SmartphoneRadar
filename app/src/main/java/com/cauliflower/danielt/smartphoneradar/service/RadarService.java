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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import android.widget.Toast;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.data.RadarPreferences;
import com.cauliflower.danielt.smartphoneradar.firebase.RadarFirestore;
import com.cauliflower.danielt.smartphoneradar.ui.SettingsActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RadarService extends Service {

    private String TAG = RadarService.class.getSimpleName();
    public static boolean mInService = false;
    private static boolean debug = false;

    private FusedLocationProviderClient mClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private String mEmail;
    private String mIMEI;
    //在 firebase 的座標文件數量
    private int mDocumentId = 1;
    private Location mLastValidLocation;
    private static final float VALID_DISTANCE = 50;

    //更新座標失敗次數不能超過 3 次
    private static final int UPDATE_LOCATION_FAILED_MAXIMUM = 3;
    private int mUpdateLocationFailedCount = 0;

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
        if (debug) {
            Log.i(TAG, "onCreate");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (debug) {
            Log.i(TAG, "onStartCommand");
        }
        getUserIdentity();

        if (mEmail != null && mIMEI != null) {
            mInService = true;
            if (debug) {
                showServiceStatus();
            }
            createLocationRequest();
            createLocationCallback();
            fuseLocationRequest();
            foregroundService();
        } else {
            Log.i(TAG, "Missing identity for verify so RadarService stopped");
            this.stopSelf();
        }

        return START_STICKY;
    }

    private void getUserIdentity() {
        //取得 IMEI
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.w(TAG, "No permission to get IMEI");
            RadarService.this.stopSelf();
        }
        if (telephonyManager == null) {
            Log.w(TAG, "Null IMEI");
            this.stopSelf();
        }

        //取得使用者 IMEI
        mIMEI = telephonyManager.getDeviceId();

        //取得使用者 email
        mEmail = RadarPreferences.getUserEmail(this);
    }

    /**
     * Set up location request
     */
    private void createLocationRequest() {
        int frequency = Integer.parseInt(RadarPreferences.getUpdateFrequency(RadarService.this));
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(frequency);
        mLocationRequest.setFastestInterval(frequency);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (debug) {
            Log.i(TAG, "updateFrequency:" + frequency);
        }
    }

    /**
     * Implement callback after get location
     */
    private void createLocationCallback() {
        //取得裝置位置後的動作
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.i(TAG, "Get location result");
                //更新目標文件 id
                /*updateDocumentId();*/
                final Location location = locationResult.getLastLocation();
                //檢查座標是否有效
                if (!isValidLocation(location)) {
                    return;
                }
                //更新位置資訊到 firestore
                RadarFirestore.updateLocation(String.valueOf(mDocumentId), mEmail,
                        mIMEI, location.getLatitude(), location.getLongitude(),
                        aVoid -> {
                            //更新成功
                            Log.d(TAG, "Successfully update location");
                        }, e -> {
                            //更新失敗
                            mUpdateLocationFailedCount++;
                            if (mUpdateLocationFailedCount > UPDATE_LOCATION_FAILED_MAXIMUM) {
                                Toast.makeText(RadarService.this,
                                        getString(R.string.updateLocationFailed_close_service), Toast.LENGTH_SHORT).show();
                                RadarService.this.stopSelf();
                            }
                            Log.d(TAG, "Update location failed count:" + mUpdateLocationFailedCount, e);
                        });
            }
        };
    }

    /**
     * Start request location
     */
    @SuppressLint("MissingPermission")
    private void fuseLocationRequest() {
        mClient = LocationServices.getFusedLocationProviderClient(RadarService.this);
        mClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    /**
     * 緩衝座標資訊，有時更新頻率太快，監聽反應又太慢，舊的座標已經被新座標覆蓋，而舊座標卻沒有被查詢到
     * 所以在 firestore 建立 5 個座標文件，作為緩衝;
     * <p>
     * ***還在測試中，雖然目前監聽狀況良好，不會漏掉座標(看來文件沒有看仔細Ｒ～)***
     */
    private void updateDocumentId() {
        mDocumentId++;
        if (mDocumentId > 5) {
            mDocumentId = 1;
        }
    }

    /**
     * 判斷上一次更新到 firestore 的座標 與現在的座標距離是否大於 15 公尺; 避免使用者靜止時更新大量無效座標
     *
     * @return false 距離小於等於 15 公尺，不應該向 firestore 更新該座標
     */
    private boolean isValidLocation(Location location) {
        if (mLastValidLocation != null) {
            float distance = location.distanceTo(mLastValidLocation);
            if (distance > VALID_DISTANCE) {
                mLastValidLocation = location;
                Log.d(TAG, "Valid location," + distance + " > " + VALID_DISTANCE);
                return true;
            }
            Log.d(TAG, "Invalid location," + distance + " <= " + VALID_DISTANCE);
            return false;
        }
        mLastValidLocation = location;
        return true;
    }

    //每 15 秒 log 一次 service 是不是還活著
    private Runnable mTask_serviceStatus = new Runnable() {
        @Override
        public void run() {
            mWorker.postDelayed(this, 15000);
            Log.d(TAG, "InService: " + mInService);
        }
    };
    private Handler mWorker = new Handler();

    private void showServiceStatus() {
        //每 15 秒 Log 一次
        mWorker.postDelayed(mTask_serviceStatus, 15000);
    }

    //為了讓 Service 活久一點，只好將 Service 推到前景
    private void foregroundService() {
        Intent intent = new Intent(RadarService.this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
                RadarService.this, 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.pref_positioning))
//                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.))
                .setSmallIcon(R.drawable.ic_hat_notify)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent);

        //android O need a channel for notification
        NotificationChannel radarChannel = null;
        String channelId = "AndroidChannel";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            radarChannel = new NotificationChannel(
                    channelId, "AndroidRadar", NotificationManager.IMPORTANCE_HIGH);
            radarChannel.setBypassDnd(true);
            radarChannel.enableLights(false);
            radarChannel.enableVibration(true);
            builder.setChannelId(channelId);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(radarChannel);
        }
        RadarService.this.startForeground(333, builder.build());
    }

    @Override
    public void onDestroy() {
        if (mClient != null) {
            mClient.removeLocationUpdates(mLocationCallback);
        }
        Log.i(TAG, "onDestroy");
        mWorker.removeCallbacks(mTask_serviceStatus);

        mInService = false;
        RadarService.this.stopForeground(true);
        super.onDestroy();
    }
}
