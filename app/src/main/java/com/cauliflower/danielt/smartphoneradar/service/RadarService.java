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
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RadarService extends Service {

    LocationRequest locationRequest;

    public RadarService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void createLocationRequest() {
        String frequency = PositionPreferences.getUpdateFrequency(RadarService.this);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(Integer.valueOf(frequency));
        locationRequest.setFastestInterval(Integer.valueOf(frequency));
        locationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createLocationRequest();
        fuseLocationRequest();
        Toast.makeText(this,"onCreate",Toast.LENGTH_SHORT).show();

    }

    @SuppressLint("MissingPermission")
    private void fuseLocationRequest() {
        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(RadarService.this);
        client.requestLocationUpdates(locationRequest,
                new LocationCallback() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        final Location location = locationResult.getLastLocation();
                        Log.i("local location UPDATE", location.toString());
                        Log.i("local location UPDATE getTime:", String.valueOf(location.getTime()));
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                                new LatLng(location.getLatitude(),
//                                        location.getLongitude())
//                                , 15));
                        new Thread(new Runnable() {
                            @SuppressLint("LongLogTag")
                            @Override
                            public void run() {
                                try {
                                    SimpleDateFormat s = new SimpleDateFormat("yy-MM-dd-HH:mm:ss");
                                    String time = s.format(new Date());
                                    updateLocation(
                                            "daniel", "daniel", time,
                                            location.getLatitude(), location.getLongitude());
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
                , null);
    }

    public String updateLocation(String username, String password, String time, double latitude, double longitude) throws
            UnsupportedEncodingException {

        String params = "account=" + URLEncoder.encode(username, "UTF-8") +
                "&password=" + URLEncoder.encode(password, "UTF-8") +
                "&time=" + URLEncoder.encode(time, "UTF-8") +
                "&latitude=" + URLEncoder.encode(String.valueOf(latitude), "UTF-8") +
                "&longitude=" + URLEncoder.encode(String.valueOf(longitude), "UTF-8") +
                "&action=" + URLEncoder.encode("updateLocation", "UTF-8") +
                "&";
        Log.i("PARAMS", params);

        ConnectDb connectDb = new ConnectDb(RadarService.this);
        String response = connectDb.sendHttpRequest(params);

        Log.i("response", response);
        return response;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this,"onDestroy",Toast.LENGTH_SHORT).show();
    }
}
