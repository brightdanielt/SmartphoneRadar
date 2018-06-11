package com.cauliflower.danielt.smartphoneradar.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.COLUMN_USER_ACCOUNT;
import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.COLUMN_USER_PASSWORD;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, Updater {

    private GoogleMap mMap;
    private RecyclerView mRecycler_locationList;
    private MyAdapter mAdapter;
    private LinearLayout mLinearLayout_wrapRecyclerView;

    //用於遠端 DB
    private ConnectDb mConnectDb;
    //用於手機 DB
    private MyDbHelper mDbHelper;
    private List<SimpleLocation> mLocationList = new ArrayList<>();
    private boolean mShowNewMarkOnly = false;

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getSupportActionBar().setSubtitle("");
                    }
                }, 7500);
                String time_to_compare = mDbHelper.searchNewTime(mAccount);
                mConnectDb.getLocationFromServer(mAccount, mPassword, time_to_compare);
                //每 15 秒查詢一次座標
                mHandler.postDelayed(mRunnable, 15000);
//                SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
//                sp.parse(new ByteArrayInputStream(response.getBytes()), new HandlerXML(MapsActivity.this));
//            } catch (ParserConfigurationException e) {
//                e.printStackTrace();
//            } catch (SAXException e) {
//                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    private String mAccount, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mConnectDb = new ConnectDb(MapsActivity.this);
        mDbHelper = new MyDbHelper(MapsActivity.this);
        Intent i = getIntent();
        mAccount = i.getStringExtra(COLUMN_USER_ACCOUNT);
        mPassword = i.getStringExtra(COLUMN_USER_PASSWORD);
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

        makeViewWork();

        if (mAccount != null && mPassword != null) {
            //3 秒後執行第一次座標查詢
            mHandler.postDelayed(mRunnable, 3000);
        }
    }

    private void makeViewWork() {
        setUpCluster();

        mRecycler_locationList = findViewById(R.id.recycler_locationList);
        mLinearLayout_wrapRecyclerView = findViewById(R.id.linearLayout_wrapRecyclerView);

        //為避免 adapter 的觀察對象變更，導致 notify 失效，使用 addAll() 防止 locationList 記憶體位置更改
        mLocationList.addAll(mDbHelper.searchAllLocation(mAccount));
        mAdapter = new MyAdapter(mLocationList);
        mRecycler_locationList.setAdapter(mAdapter);
        mRecycler_locationList.setLayoutManager(new LinearLayoutManager(this));

        //該功能原本能夠直接在 MyAdapter 的方法 onBindViewHolder 實現
        //取出 listLocation 物件作為 item 的資料，同時添增標記
        //但在 view 的 params 變動時，recyclerView再次呼叫了方法 onBindViewHolder
        //使得標記重複添加因此改為呼叫該方法 showAllMarks
        showAllMarks();
    }

    private void setUpCluster() {
        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<MyItem>(this, mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setRenderer(new OwnRendering(MapsActivity.this, mMap, mClusterManager));
    }

    //在地圖顯示所有標記
    private void showAllMarks() {
        mClusterManager.clearItems();
        for (SimpleLocation location : mLocationList) {
            MyItem item = new MyItem(null,
                    location.getLatitude(), location.getLongitude(), location.getTime(), null);
            mClusterManager.addItem(item);
        }
    }

    //只顯示最新的標記在地圖上
    private void showNewMarkOnly() {
        mClusterManager.clearItems();
        if (!mLocationList.isEmpty()) {
            int size = mLocationList.size();
            //因為 locationList 是持續更新資料的，最後一筆資料即最新的 SimpleLocation
            SimpleLocation location = mLocationList.get(size - 1);
            MyItem item1 = new MyItem(null,
                    location.getLatitude(), location.getLongitude(), location.getTime(), null);
            mClusterManager.addItem(item1);
        }
    }

    //處理向伺服器查詢得的位置資訊
    @Override
    public void updateData(List<SimpleLocation> locations) {
        if (locations.size() > 0) {
            for (SimpleLocation location : locations) {
                double latitude, longitude;
                String time;

                latitude = location.getLatitude();
                longitude = location.getLongitude();
                time = location.getTime();
                //手機端資料庫新增一筆 Location
                mDbHelper.addLocation(mAccount, latitude, longitude, time);

                //recycler_locationList 新增一筆資料
                mLocationList.add(new SimpleLocation(time, latitude, longitude));

                if (mShowNewMarkOnly) {
                    //移除所有標記，因為得到新標記後，前一個新標即視為舊標記
                    mClusterManager.clearItems();
                }

                //地圖新增一個標記
                mClusterManager.addItem(new MyItem(null, latitude, longitude, time, ""));

                //鏡頭移動至該新座標
                LatLng latLng = new LatLng(latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));

            }
            getSupportActionBar().setSubtitle(null);
            mAdapter.notifyDataSetChanged();
        } else {
            getSupportActionBar().setSubtitle(R.string.get_no_location);
        }

    }

    private class MyItem implements ClusterItem {
        private final LatLng mPosition;
        private BitmapDescriptor icon;
        private String title;
        private String snippet;

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        MyItem(BitmapDescriptor ic, Double lat, Double lng, String title, String sni) {
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

    class OwnRendering extends DefaultClusterRenderer<MyItem> {


        OwnRendering(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            case R.id.action_locationList: {
                if (item.isChecked()) {
                    item.setChecked(false);
                    mLinearLayout_wrapRecyclerView.setLayoutParams(
                            new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 0f
                            )
                    );
                } else {
                    item.setChecked(true);
                    mLinearLayout_wrapRecyclerView.setLayoutParams(
                            new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 3f
                            )
                    );
                }
                break;
            }
            case R.id.action_showNewMarkOnly: {
                if (item.isChecked()) {
                    item.setChecked(false);
                    mShowNewMarkOnly = false;
                    showAllMarks();
                } else {
                    item.setChecked(true);
                    mShowNewMarkOnly = true;
                    showNewMarkOnly();
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maps_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<SimpleLocation> mLocationList;

        MyAdapter(List<SimpleLocation> list) {
            mLocationList = list;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_time, tv_lat, tv_lng;

            private ViewHolder(View v) {
                super(v);
                tv_time = v.findViewById(R.id.time);
                tv_lat = v.findViewById(R.id.lat);
                tv_lng = v.findViewById(R.id.lng);
            }

        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).
                    inflate(R.layout.recycler_view_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            SimpleLocation location = mLocationList.get(position);
            String time = location.getTime();
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            //將座標清單中的所有座標加入標記群集
//            MyItem offsetItem = new MyItem(null, lat, lng, time, "");
//            mClusterManager.addItem(offsetItem);

            holder.tv_time.setText(time);
            holder.tv_lat.setText(String.valueOf(lat));
            holder.tv_lng.setText(String.valueOf(lng));

            final LatLng latLng = new LatLng(lat, lng);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                }
            });
        }

        @Override
        public int getItemCount() {
            return mLocationList.size();
        }
    }
}
