package com.cauliflower.danielt.smartphoneradar.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.data.MainDb;
import com.cauliflower.danielt.smartphoneradar.data.RadarContract;
import com.cauliflower.danielt.smartphoneradar.data.RadarPreferences;
import com.cauliflower.danielt.smartphoneradar.firebase.RadarFirestore;
import com.cauliflower.danielt.smartphoneradar.obj.SimpleLocation;
import com.cauliflower.danielt.smartphoneradar.obj.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.common.collect.Maps;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import static com.cauliflower.danielt.smartphoneradar.firebase.RadarFirestore.FIRESTORE_FIELD_LATITUDE;
import static com.cauliflower.danielt.smartphoneradar.firebase.RadarFirestore.FIRESTORE_FIELD_LONGITUDE;
import static com.cauliflower.danielt.smartphoneradar.firebase.RadarFirestore.FIRESTORE_FIELD_PASSWORD;
import static com.cauliflower.danielt.smartphoneradar.firebase.RadarFirestore.FIRESTORE_FIELD_TIME;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private RecyclerView mRecyclerView_location;
    private LocationAdapter mLocationAdapter;
    private LinearLayout mLinearLayout_wrapRecyclerView;
    private ListenerRegistration mLocationListenerRg;

    //用於手機 DB
    private List<SimpleLocation> mLocationList = new ArrayList<>();

    private String mEmail, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //查詢所有追蹤對象
        List<User> trackingList = MainDb.searchUser(MapsActivity.this, RadarContract.UserEntry.USED_FOR_GETLOCATION);
        for (User targetTracked : trackingList) {
            //找出正在追蹤的對象
            if (targetTracked.getIn_use().equals(RadarContract.UserEntry.IN_USE_YES)) {
                //取得信箱與密碼
                mEmail = targetTracked.getEmail().trim();
                mPassword = targetTracked.getPassword().trim();
                break;
            }
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
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        makeViewWork();

        //判斷是否查得正在追蹤對象
        if (mEmail != null && mPassword != null) {
            //監聽該追蹤對象的座標更新
            mLocationListenerRg = RadarFirestore.setOnLocationUpdateListener(mEmail, mPassword, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                    //監聽失敗
                    if (e != null) {
                        Log.d(TAG, "setOnLocationUpdateListener failed", e);
                        return;
                    }
                    //Document has local changes that haven't been written to the backend yet
                    /*if (value.getMetadata().hasPendingWrites()) {
                        //代表監聽到 local 的更新，而非來自 firestore 的更新
                        //這是延遲補償的設計所導致的，但監聽座標不需要該功能
                        return;
                    }*/
                    //監聽成功
                    if (value != null) {
                        //處理座標資料
                        handleLocation(value);
                    }
                }
            });
        }
    }

    private void makeViewWork() {
        //設定標記集合
        setUpCluster();

        mRecyclerView_location = findViewById(R.id.recycler_locationList);
        mLinearLayout_wrapRecyclerView = findViewById(R.id.linearLayout_wrapRecyclerView);

        //為避免 adapter 的觀察對象變更，導致 notify 失效，使用 addAll() 防止 locationList 記憶體位置更改
        mLocationList.addAll(MainDb.searchAllLocation(MapsActivity.this, mEmail));
        mLocationAdapter = new LocationAdapter();
        //item 大小固定
        mRecyclerView_location.setHasFixedSize(true);
        mRecyclerView_location.setAdapter(mLocationAdapter);
        mRecyclerView_location.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView_location.scrollToPosition(mLocationList.size() - 1);
        setItemTouchHelper();

        //Load preference to decide how to show list and mark
        boolean showNewMark = RadarPreferences.getShowNewMarkOnly(MapsActivity.this);
        showNewMarkOnly(showNewMark);
        boolean showList = RadarPreferences.getShowLocationList(MapsActivity.this);
        showList(showList);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    /**
     * 為 location recyclerView 設置 item 滑動
     */
    private void setItemTouchHelper() {
        //只接受左右滑動
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //左右滑動後，從清單移除該項目
                int position = viewHolder.getAdapterPosition();
                //刪除內存資料庫的該筆資料
                MainDb.deleteLocation(MapsActivity.this,
                        (SimpleLocation) viewHolder.itemView.getTag());
                mLocationList.remove(position);
                mLocationAdapter.notifyItemRemoved(position);
            }
        });
        //指定 RecyclerView 對象
        helper.attachToRecyclerView(mRecyclerView_location);
    }


    /**
     * 當地圖上固定範圍內的標記太多時，會以標記集合的方式呈現
     */
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

    //顯示最新或全部瘩的標記在地圖上
    private void showNewMarkOnly(boolean showNewMark) {
        mClusterManager.clearItems();
        if (showNewMark) {
            if (!mLocationList.isEmpty()) {
                int size = mLocationList.size();
                //因為 locationList 是持續更新資料的，最後一筆資料即最新的 SimpleLocation
                SimpleLocation location = mLocationList.get(size - 1);
                MyItem item1 = new MyItem(null,
                        location.getLatitude(), location.getLongitude(), location.getTime(), null);
                //加入最新的座標
                mClusterManager.addItem(item1);
            }
        } else {
            for (SimpleLocation location : mLocationList) {
                MyItem item = new MyItem(null,
                        location.getLatitude(), location.getLongitude(), location.getTime(), null);
                mClusterManager.addItem(item);
            }
        }
    }

    //是否顯示座標清單
    private void showList(boolean show) {
        if (show) {
            mLinearLayout_wrapRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mLinearLayout_wrapRecyclerView.setVisibility(View.GONE);
        }
    }

    //驗應資料身份、將座標儲存到手機資料庫、顯示於清單
    private void handleLocation(QuerySnapshot value) {
        //取出座標文件
        for (QueryDocumentSnapshot doc : value) {
            //驗證身份
            if (mPassword.equals(doc.getString(FIRESTORE_FIELD_PASSWORD))) {
                final double latitude, longitude;
                Date dateFromServer;
                //檢查資料是否齊全
                if (doc.getDouble(FIRESTORE_FIELD_LATITUDE) != null
                        && doc.getDouble(FIRESTORE_FIELD_LONGITUDE) != null
                        && doc.getDate(FIRESTORE_FIELD_TIME) != null) {
                    latitude = doc.getDouble(FIRESTORE_FIELD_LATITUDE);
                    longitude = doc.getDouble(FIRESTORE_FIELD_LONGITUDE);
                    dateFromServer = doc.getDate(FIRESTORE_FIELD_TIME);
                } else {
                    //資料不齊全
                    Log.w(TAG, "handleLocation，遺失座標資料");
                    break;
                }
                if (latitude == 0) {
                    Log.i(TAG, "查到初始化座標，略過該筆資料");
                    return;
                }
                //因為註冊監聽時會回傳舊的座標，所以必須判斷回傳座標是新的還是舊的
                if (isExpiredDateFromServer(dateFromServer)) {
                    //server傳來舊座標，直接略過
                    return;
                }
                //格式化字串
                final String time = DateFormat.getDateTimeInstance().format(dateFromServer);

                //手機端資料庫新增一筆 Location
                SimpleLocation simpleLocation = new SimpleLocation(mEmail, time, latitude, longitude);
                MainDb.addLocation(MapsActivity.this, simpleLocation);
                //座標清單，新增一筆資料
                mLocationAdapter.addNewLocation(simpleLocation);

                if (RadarPreferences.getShowNewMarkOnly(MapsActivity.this)) {
                    //移除所有標記，因為得到新標記後，前一個新標即視為舊標記
                    mClusterManager.clearItems();
                }

                //地圖新增一個標記
                mClusterManager.addItem(new MyItem(null, latitude, longitude, time, ""));

                //鏡頭移動至該新座標
                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                                //zoom level 2 to 21
                                new LatLng(latitude, longitude), 20));

                //繪製路線
                drawRoute();

                //提示使用者有新的座標
                getSupportActionBar().setSubtitle(time);
            } else {
                Log.w(TAG, "handleLocation，身份驗證失敗");
            }
        }
    }

    /**
     * 注意！！！ Date 格式化成字串後，如下：
     * String dateStr = DateFormat.getDateTimeInstance().format(date)
     * 若再將字串轉回 Date 如下：
     * Date date2 = DateFormat.getDateTimeInstance().parse(dateFromDbStr);
     * 原 date 跟轉回的 date2 比較，結果可能不是是相等的，因為格式化過程中，milliseconds 後三位數字不在格式的範圍，
     * 而轉回的 date2 在沒有後三位的情況下自動給予 0 的值
     * 所以要就統一格式化，若不格式化，也可以 milliseconds 為判斷基礎，最不容易出錯
     */
    private boolean isExpiredDateFromServer(Date dateFromServer) {
        String dateFromDbStr = MainDb.searchNewTime(MapsActivity.this, mEmail);
        if (dateFromDbStr == null) {
            //尚無資料可比較
            return false;
        }
        try {
            String dateFromServerStr = DateFormat.getDateTimeInstance().format(dateFromServer);
            Date dateFromDb = DateFormat.getDateTimeInstance().parse(dateFromDbStr);
            dateFromServer = DateFormat.getDateTimeInstance().parse(dateFromServerStr);

            /*int j = dateFromDb.compareTo(dateFromServer);
            boolean a = dateFromDb.before(dateFromServer);
            boolean c = dateFromDb.equals(dateFromServer);*/
            if (dateFromDb.after(dateFromServer) || dateFromDb.equals(dateFromServer)) {
                //是舊(過期)的時間
                Log.i(TAG, "Ignore this location," + dateFromDb + " >= " + dateFromServer);
                return true;
            }
        } catch (ParseException e) {
//            SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.dateFormat));
            Log.d(TAG, "DateFormat parse error", e);
            return true;
        }
        return false;
    }

    /**
     * Draw polyline between last location and new location
     */
    private void drawRoute() {
        if (mLocationList.size() > 1) {
            int size = mLocationList.size();
            LatLng lastLocation = new LatLng(mLocationList.get(size - 2).getLatitude(),
                    mLocationList.get(size - 2).getLongitude());
            LatLng newLatLng = new LatLng(mLocationList.get(size - 1).getLatitude(),
                    mLocationList.get(size - 1).getLongitude());

            PolylineOptions options = new PolylineOptions()
                    .add(lastLocation)
                    .add(newLatLng)
                    .color(Color.GREEN)
                    .width(10)
                    .clickable(true)
                    .geodesic(true)
                    .jointType(JointType.BEVEL)
                    .startCap(new RoundCap());
            mMap.addPolyline(options);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maps_menu, menu);
        boolean showList = RadarPreferences.getShowLocationList(MapsActivity.this);
        menu.findItem(R.id.action_locationList).setChecked(showList);
        boolean showNewMarkOnly = RadarPreferences.getShowNewMarkOnly(MapsActivity.this);
        menu.findItem(R.id.action_showNewMarkOnly).setChecked(showNewMarkOnly);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            case R.id.action_locationList: {
                boolean newChecked = !item.isChecked();
                item.setChecked(newChecked);
                RadarPreferences.setShowLocationList(MapsActivity.this, newChecked);
                showList(newChecked);
                break;
            }
            case R.id.action_showNewMarkOnly: {
                boolean newChecked = !item.isChecked();
                item.setChecked(newChecked);
                RadarPreferences.setShowNewMarkOnly(MapsActivity.this, newChecked);
                showNewMarkOnly(newChecked);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Stop listening location update
        if (mLocationListenerRg != null) {
            mLocationListenerRg.remove();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
        LocationAdapter() {
        }

        class LocationViewHolder extends RecyclerView.ViewHolder {
            TextView tv_time, tv_lat, tv_lng;

            private LocationViewHolder(View v) {
                super(v);
                tv_time = v.findViewById(R.id.time);
                tv_lat = v.findViewById(R.id.lat);
                tv_lng = v.findViewById(R.id.lng);
            }

        }

        @Override
        public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).
                    inflate(R.layout.recycler_view_item, parent, false);
            return new LocationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(LocationViewHolder holder, final int position) {
            SimpleLocation location = mLocationList.get(position);
            String time = location.getTime();
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            holder.itemView.setTag(location);
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

        public void addNewLocation(SimpleLocation simpleLocation) {
            if (mLocationList != null) {
                mLocationList.add(simpleLocation);
                //刷新座標清單
                LocationAdapter.this.notifyDataSetChanged();
                mRecyclerView_location.scrollToPosition(mLocationList.size() - 1);
            }
        }
    }
}
