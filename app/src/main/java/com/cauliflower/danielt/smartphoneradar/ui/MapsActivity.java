package com.cauliflower.danielt.smartphoneradar.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.interfacer.Updater;
import com.cauliflower.danielt.smartphoneradar.obj.SimpleLocation;
import com.cauliflower.danielt.smartphoneradar.tool.ConnectDb;
import com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.io.IOException;
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

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                String time_to_compare = dbHelper.searchNewTime(account);
                connectDb.getLocationFromServer(account, password, time_to_compare);
                handler.postDelayed(runnable, 15000);
//                SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
//                sp.parse(new ByteArrayInputStream(response.getBytes()), new HandlerXML(MapsActivity.this));
//            } catch (ParserConfigurationException e) {
//                e.printStackTrace();
//            } catch (SAXException e) {
//                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

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

            //每 15 秒查詢一次位置
            handler.postDelayed(runnable, 15000);
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
        setUpCluster();
        mClusterManager.setRenderer(new OwnRendering(MapsActivity.this, mMap, mClusterManager));
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
//            mMap.addMarker(new MarkerOptions().
//                    position(latLng).title(time));

            MyItem offsetItem = new MyItem(null, latitude, longitude, time, "");
            mClusterManager.addItem(offsetItem);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));

        }
    }

    public class MyItem implements ClusterItem {
        private final LatLng mPosition;
        private BitmapDescriptor icon;
        private String title;
        private String snippet;

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        public MyItem(BitmapDescriptor ic, Double lat, Double lng, String title, String sni) {
            mPosition = new LatLng(lat, lng);
            this.icon = ic;
            this.title = title;
            this.snippet = sni;
        }

        public BitmapDescriptor getIcon() {
            return icon;
        }

        public String getSnippet() {
            return snippet;
        }

        public String getTitle() {
            return title;
        }
    }

    public class OwnRendering extends DefaultClusterRenderer<MyItem> {

        public OwnRendering(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }

        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
            markerOptions.icon(item.getIcon());
            markerOptions.snippet(item.getSnippet());
            markerOptions.title(item.getTitle());
            super.onBeforeClusterItemRendered(item, markerOptions);
        }
    }

    // Declare a variable for the cluster manager.
    private ClusterManager<MyItem> mClusterManager;

    private void setUpCluster() {
        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<MyItem>(this, mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

    }

}
