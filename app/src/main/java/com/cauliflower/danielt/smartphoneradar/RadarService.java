package com.cauliflower.danielt.smartphoneradar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class RadarService extends Service {

    LocationRequest locationRequest;

    public RadarService() {}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createLocationRequest();
        fuseLocationRequest();
    }

    @SuppressLint("MissingPermission")
    private void fuseLocationRequest() {
        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(RadarService.this);
        client.requestLocationUpdates(locationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Location location = locationResult.getLastLocation();
                        Log.i("UPDATE", location.toString());
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                                new LatLng(location.getLatitude(),
//                                        location.getLongitude())
//                                , 15));
                    }
                }
                , null);
    }

}
