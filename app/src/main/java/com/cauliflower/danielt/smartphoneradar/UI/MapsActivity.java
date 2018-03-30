package com.cauliflower.danielt.smartphoneradar.UI;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.cauliflower.danielt.smartphoneradar.Tool.ConnectDb;
import com.cauliflower.danielt.smartphoneradar.Tool.HandlerXML;
import com.cauliflower.danielt.smartphoneradar.Interface.Updater;
import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.Obj.SimpleLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, Updater {

    private GoogleMap mMap;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    getLatLngFromServer();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        };

        handler = new Handler();
        //每 20 秒查詢一次位置
        handler.postDelayed(runnable, 20000);
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

    private void getLatLngFromServer() throws UnsupportedEncodingException {
        ConnectDb connectDb = new ConnectDb();
        String params = "account=" + URLEncoder.encode("", "UTF-8") +
                "&password=" + URLEncoder.encode("", "UTF-8") +
                "&time=" + URLEncoder.encode("", "UTF-8") +
                "&action=" + URLEncoder.encode("getLocation", "UTF-8") +
                "&";

        Log.i("PARAMS", params);
        String response = connectDb.sendHttpRequest(params);

        try {
            SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
            sp.parse(new ByteArrayInputStream(response.getBytes()), new HandlerXML(MapsActivity.this));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void updateData(List<SimpleLocation> locations) {
        for (int i = 0; i < locations.size(); i++) {
            LatLng latLng = new LatLng(
                    locations.get(i).getLatitude(),
                    locations.get(i).getLongitude());

            // Add a marker in new latLng and move the camera
            mMap.addMarker(new MarkerOptions().
                    position(latLng).
                    title(locations.get(i).getTime()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }
}
