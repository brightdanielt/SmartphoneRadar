package com.cauliflower.danielt.smartphoneradar.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.cauliflower.danielt.smartphoneradar.tool.ConnectDb;
import com.cauliflower.danielt.smartphoneradar.interfacer.Updater;
import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.obj.SimpleLocation;
import com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.COLUMN_USER_ACCOUNT;
import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.COLUMN_USER_PASSWORD;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, Updater {

    private GoogleMap mMap;
    //用於遠端 DB
    private ConnectDb connectDb;
    //用於手機 DB
    private MyDbHelper dbHelper;

    private Handler handler;
    private Runnable runnable;

    private String account, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent i = getIntent();
        account = i.getStringExtra(COLUMN_USER_ACCOUNT);
        password = i.getStringExtra(COLUMN_USER_PASSWORD);

        if (account != null && password != null) {
            connectDb = new ConnectDb(MapsActivity.this);
            dbHelper = new MyDbHelper(MapsActivity.this);

            runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        connectDb.getLatLngFromServer(account, password);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            };

            handler = new Handler();
            //每 20 秒查詢一次位置
            handler.postDelayed(runnable, 20000);
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    @Override
    public void updateData(List<SimpleLocation> locations) {
        for (int i = 0; i < locations.size(); i++) {

            double latitude, longitude;
            String time;

            latitude = locations.get(i).getLatitude();
            longitude = locations.get(i).getLongitude();
            time = locations.get(i).getTime();

            LatLng latLng = new LatLng(latitude, longitude);

            dbHelper.addLocation(account, latitude, longitude, time);

            // Add a marker in new latLng and move the camera
            mMap.addMarker(new MarkerOptions().
                    position(latLng).title(time));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

}
