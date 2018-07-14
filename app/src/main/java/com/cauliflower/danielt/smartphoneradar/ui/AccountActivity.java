package com.cauliflower.danielt.smartphoneradar.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.data.MainDb;
import com.cauliflower.danielt.smartphoneradar.data.RadarContract;
import com.cauliflower.danielt.smartphoneradar.data.RadarPreferences;
import com.cauliflower.danielt.smartphoneradar.firebase.RadarAuthentication;
import com.cauliflower.danielt.smartphoneradar.firebase.RadarFirestore;
import com.cauliflower.danielt.smartphoneradar.obj.User;
import com.cauliflower.danielt.smartphoneradar.service.RadarService;
import com.cauliflower.danielt.smartphoneradar.tool.MyDialogBuilder;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.UserEntry;

public class AccountActivity extends AppCompatActivity {

    public static final String TAG = AccountActivity.class.getSimpleName();
    private static final int REQUEST_CODE_READ_PHONE_STATE = 101;
    private static final int REQUEST_CODE_SIGNIN_FIREBASE_AUTH = 102;

    private String mIMEI, mModel;

    private Button mBtn_signIn, mBtn_addTragetTracked;
    private static final int MAX_DOCUMENT_LOCATIION = 1;
    private ImageView imgView_userPhoto;
    private TextView mTv_userInfo, mTv_hintTargetTracked;
    private ListView mListView_targetTracked;
    private List<User> mTargetTrackedList = new ArrayList<>();
    private TargetTrackedAdapter mAdapter_targetTracked;
    private ProgressDialog mDialog_loading;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        makeViewWork();

    }

    @Override
    protected void onStart() {
        super.onStart();
        getPhoneInfo();
        mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null) and update UI accordingly.
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            RadarFirestore.checkUserExists(currentUser.getEmail(), new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String resultEmail = document.getString(RadarFirestore.FIRESTORE_FIELD_EMAIL);
                            if (!currentUser.getEmail().equals(resultEmail)) {
                                //使用者還沒建立，代表非法登入，強制登出
                                RadarAuthentication.signOut(AccountActivity.this, null);
                                updateAuthInfo(null);
                            } else {
                                //使用者還已經建立，代表合法登入，更新使用者資訊
                                updateAuthInfo(currentUser);
                            }
                        }
                    }
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog_loading != null && mDialog_loading.isShowing()) {
            mDialog_loading.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void makeViewWork() {
        mDialog_loading = ProgressDialog.show(AccountActivity.this, "",
                getString(R.string.loading), true);
        mDialog_loading.dismiss();
        imgView_userPhoto = findViewById(R.id.imgView_photo);
        mBtn_signIn = findViewById(R.id.btn_singIn);
        mBtn_addTragetTracked = findViewById(R.id.btn_addTargetTracked);
        mTv_userInfo = findViewById(R.id.tv_userInfo);
        mTv_hintTargetTracked = findViewById(R.id.tv_hintTargetTracked);
        mListView_targetTracked = findViewById(R.id.listView_targetTracked);

        mTargetTrackedList.addAll(MainDb.searchUser(this, UserEntry.USED_FOR_GETLOCATION));
        mAdapter_targetTracked = new TargetTrackedAdapter(mTargetTrackedList);
        mListView_targetTracked.setAdapter(mAdapter_targetTracked);

        updateTrackList();
    }

    private void updateTrackList() {
        mTargetTrackedList.clear();
        mTargetTrackedList.addAll(MainDb.searchUser(AccountActivity.this, UserEntry.USED_FOR_GETLOCATION));
        if (mTargetTrackedList.size() > 0) {
            mTv_hintTargetTracked.setVisibility(View.GONE);
        }
        mAdapter_targetTracked.notifyDataSetChanged();
        mDialog_loading.dismiss();
    }

    /**
     * Refresh user info from FirebaseUser.
     */
    private void updateAuthInfo(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            mTv_userInfo.setText(firebaseUser.getEmail());
            new LoadBitmapFromUri().execute(firebaseUser.getPhotoUrl());
            mBtn_signIn.setText(R.string.signOut);
        } else {
            mTv_userInfo.setText(R.string.notSignIn);
            imgView_userPhoto.setImageBitmap(null);
            mBtn_signIn.setText(R.string.signIn);
        }
        mDialog_loading.dismiss();
    }

    public void clickBtn(View view) {
        switch (view.getId()) {
            case R.id.btn_singIn: {
                if (mAuth.getCurrentUser() != null) {
                    //登出
                    RadarAuthentication.signOut(AccountActivity.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                updateAuthInfo(null);
                                //停止定位服務
                                if (RadarService.mInService) {
                                    stopService(new Intent(AccountActivity.this, RadarService.class));
                                }
//                                RadarPreferences.setPositionCheck(AccountActivity.this,false);
                            } else {
                                Log.d(TAG, "Error Firebase auth sign out", task.getException());
                            }
                        }
                    });
                } else {
                    RadarAuthentication.signIn(AccountActivity.this, REQUEST_CODE_SIGNIN_FIREBASE_AUTH);
                }
                break;
            }
            case R.id.btn_addTargetTracked: {
                final MyDialogBuilder builder = new MyDialogBuilder(AccountActivity.this, R.string.addTargetTracked);
                final AlertDialog dialogAddTargetTracked = builder.create();
                builder.setOnButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v.getId() == R.id.dialog_btn_ok) {
                            final String email = builder.getEmail();
                            final String password = builder.getPassword();
                            mDialog_loading.show();
                            RadarFirestore.checkRightToReadUser(email, password, new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful() && task.getResult().size() > 0) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                            MainDb.addUser(AccountActivity.this, email, password,
                                                    UserEntry.USED_FOR_GETLOCATION, UserEntry.IN_USE_NO);
                                            updateTrackList();
                                        }
                                    } else if (task.isSuccessful() && task.getResult().size() < 1) {
                                        Toast.makeText(AccountActivity.this, R.string.wrong_user, Toast.LENGTH_SHORT).show();
                                        Log.i(TAG, "找不到該使用者，可能信箱或密碼打錯");
                                        mDialog_loading.dismiss();
                                    } else {
                                        Log.d(TAG, "Error listing documents: ", task.getException());
                                        mDialog_loading.dismiss();
                                    }

                                }
                            });
                            dialogAddTargetTracked.dismiss();
                        } else {
                            dialogAddTargetTracked.dismiss();
                        }
                    }
                });
                dialogAddTargetTracked.show();
                break;
            }
        }
    }

    /**
     * 取得 手機型號與 imei
     */
    private void getPhoneInfo() {
        String manufacturer = Build.MANUFACTURER;
        mModel = Build.MODEL;
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_READ_PHONE_STATE);
            }

            return;
        }
        //若未取得權限，getDeviceId 回傳 null
        mIMEI = telephonyManager.getDeviceId();
    }

    //Create user to firestore
    private void showDialogCreateUser(final FirebaseUser user) {
        final EditText ed_password = new EditText(AccountActivity.this);
        ed_password.setHint(R.string.fui_password_hint);
        ed_password.setMaxLines(1);
        ed_password.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        //要求使用者建立一組密碼，用於加強定位的安全性
        new AlertDialog.Builder(AccountActivity.this)
                .setCancelable(false)
                .setTitle("別擔心")
                .setMessage("為了加強您的定位安全，請設定一組密碼，任何人皆需要該組密碼，才能將您列為為追蹤目標")
                .setView(ed_password)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        //取得輸入的密碼
                        final String password = ed_password.getText().toString().trim();
                        //檢查密碼
                        if (!password.equals("")) {
                            mDialog_loading.show();
                            //在 firestore 建立使用者
                            RadarFirestore.createUser(user.getEmail(), password, mIMEI, mModel, user.getUid(), new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //建立使用者成功
                                    Toast.makeText(AccountActivity.this, R.string.createUser_success, Toast.LENGTH_SHORT).show();
                                    //todo  寫入手機ＤＢ使用者信箱密碼 ，日後用於新建位置、查詢位置
                                    MainDb.addUser(AccountActivity.this, user.getEmail(), password,
                                            UserEntry.USED_FOR_SENDLOCATION, UserEntry.IN_USE_YES);
                                    //更新使用者資訊
                                    updateAuthInfo(mAuth.getCurrentUser());
                                    //關閉對話筐
                                    dialog.dismiss();
                                    //初始化座標文件
                                    initFirestoreLocation();
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //建立使用者失敗
                                    //以資料結構設計正確為前提，不會建立失敗，除非是網路或 firestore 有問題
                                    Toast.makeText(AccountActivity.this, R.string.signIn_failed, Toast.LENGTH_SHORT).show();
                                    RadarAuthentication.signOut(AccountActivity.this, null);
                                    updateAuthInfo(null);
                                    Log.d(TAG, "Error create user", e);
                                    //關閉對話筐
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                }).create().show();
    }

    private class LoadBitmapFromUri extends AsyncTask<Uri, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Uri... uris) {
            Log.i(TAG, uris[0].toString());
            try {
                URL url = new URL(uris[0].toString());
                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    return bitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imgView_userPhoto.setImageBitmap(bitmap);
            }
            super.onPostExecute(bitmap);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_PHONE_STATE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getPhoneInfo();
        }
    }

    private void initFirestoreLocation() {
        String password = "";
        List<User> userList = MainDb.searchUser(AccountActivity.this, UserEntry.USED_FOR_SENDLOCATION);
        for (User user : userList) {
            password = user.getPassword();
        }
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser.getEmail() != null && !password.trim().equals("")) {

        }
        //初始化 1 個座標
        for (int documentId = 1; documentId <= MAX_DOCUMENT_LOCATIION; documentId++) {
            RadarFirestore.createLocation(String.valueOf(documentId), firebaseUser.getEmail(),
                    password, firebaseUser.getUid(),
                    mIMEI, 0, 0, null, null
            );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //result from firebase auth sign in
        if (requestCode == REQUEST_CODE_SIGNIN_FIREBASE_AUTH) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.i(TAG, "Name: " + user.getDisplayName() + "\n" +
                        "UID: " + user.getUid());
                mDialog_loading.show();
                //檢查是否第一次登入
                RadarFirestore.checkUserExists(user.getEmail(), new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String resultEmail = document.getString(RadarFirestore.FIRESTORE_FIELD_EMAIL);
                                String resultImei = document.getString(RadarFirestore.FIRESTORE_FIELD_IMEI);
                                String resultUid = document.getString(RadarFirestore.FIRESTORE_FIELD_UID);
                                if (user.getUid().equals(resultUid) && user.getEmail().equals(resultEmail)
                                        && mIMEI.equals(resultImei)) {
                                    //Firestore 存在該使用者
                                    updateAuthInfo(user);
                                } else {
                                    //可能是在非綁定的裝置登入
                                    Log.i(TAG, "該帳號已綁定於其他裝置.");
                                    //提示使用者該帳號已綁定於其他裝置
                                    new AlertDialog.Builder(AccountActivity.this)
                                            .setTitle(R.string.dialog_title_ops)
                                            .setMessage(R.string.userHasBindToOtherDevice)
                                            .setCancelable(true)
                                            .create().show();
                                    //強制登出
                                    RadarAuthentication.signOut(AccountActivity.this, null);
                                    updateAuthInfo(null);
                                }
                            }
                        } else if (task.isSuccessful() && task.getResult().size() < 1) {
                            mDialog_loading.dismiss();
                            //Firestore 不存在該使用者，代表第一次登入，向 firestore 新增使用者
                            showDialogCreateUser(user);
                        } else {
                            //不應該發生的錯誤
                            Log.i(TAG, "Task is not successful when checkUserExists.");
                            //強制登出
                            RadarAuthentication.signOut(AccountActivity.this, null);
                            updateAuthInfo(null);
                        }
                    }
                });
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Toast.makeText(AccountActivity.this, R.string.signIn_failed, Toast.LENGTH_SHORT).show();
                int errorCode = response.getError().getErrorCode();
                String errorMsg = response.getError().getMessage();
                Log.i(TAG, "ErrorCode: " + errorCode + "Msg: " + errorMsg);
            }
        }

    }

    public class TargetTrackedAdapter extends BaseAdapter {
        List<User> userList;

        TargetTrackedAdapter(List<User> userList) {
            this.userList = userList;
        }

        @Override
        public int getCount() {
            return userList.size();
        }

        @Override
        public Object getItem(int position) {
            return userList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = LayoutInflater.from(AccountActivity.this).inflate(R.layout.list_view_item, null);
            TextView tv_account = v.findViewById(R.id.ckBox_account);
            final String account = userList.get(position).getEmail();
            final String password = userList.get(position).getPassword();
            final String usedFor = userList.get(position).getUsedFor();
            String in_use = userList.get(position).getIn_use();
            tv_account.setText(account);
            if (in_use.equals(UserEntry.IN_USE_YES)) {
                tv_account.append(" 追蹤對象");
            }
            convertView = v;

            if (usedFor.equals(UserEntry.USED_FOR_GETLOCATION)) {
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(AccountActivity.this)
                                .setTitle("問問你～")
                                .setMessage(Html.fromHtml("選擇 <font color=\"blue\">" + account +
                                        "</font> 為目前追蹤對象嗎？"))
                                .setCancelable(false)
                                .setPositiveButton("是的", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //要求 RadarDbHelper 更改該 User 的 in_use 為 yes
                                        MainDb.updateUser_in_use(AccountActivity.this, account);
                                        updateTrackList();
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();

                    }
                });
            }
            return convertView;
        }
    }


}
