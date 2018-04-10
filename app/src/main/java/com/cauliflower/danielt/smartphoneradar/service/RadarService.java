package com.cauliflower.danielt.smartphoneradar.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.cauliflower.danielt.smartphoneradar.data.PositionPreferences;
import com.cauliflower.danielt.smartphoneradar.tool.ConnectDb;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RadarService extends Service {

    private String TAG = RadarService.class.getSimpleName();
    public static boolean inService = false;

    private FusedLocationProviderClient client;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private ConnectDb connectDb;

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
        inService = true;
        connectDb = new ConnectDb(RadarService.this);
        createLocationRequest();
        createLocationCallback();
        fuseLocationRequest();
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();

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
                SimpleDateFormat s = new SimpleDateFormat("yy-MM-dd-HH:mm:ss");
                String time = s.format(new Date());
                try {
                    connectDb.sendLocationToServer("daniel", "daniel",
                            time, location.getLatitude(), location.getLongitude());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        };
    }

    @SuppressLint("MissingPermission")
    private void fuseLocationRequest() {
        client = LocationServices.getFusedLocationProviderClient(RadarService.this);
        client.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onDestroy() {
        client.removeLocationUpdates(locationCallback);
        super.onDestroy();
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
    }

}
