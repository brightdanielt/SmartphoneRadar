package com.cauliflower.danielt.smartphoneradar.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.data.RadarPreferences;
import com.cauliflower.danielt.smartphoneradar.firebase.RadarAuthentication;
import com.cauliflower.danielt.smartphoneradar.firebase.RadarFirestore;
import com.cauliflower.danielt.smartphoneradar.data.RadarUser;
import com.cauliflower.danielt.smartphoneradar.service.RadarService;
import com.cauliflower.danielt.smartphoneradar.tool.MyDialogBuilder;
import com.cauliflower.danielt.smartphoneradar.viewmodel.UserViewModel;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.UserEntry.USED_FOR_GETLOCATION;
import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.UserEntry.USED_FOR_SENDLOCATION;

import com.cauliflower.danielt.smartphoneradar.databinding.ActivityAccountBinding;

/**
 * 請注意: FirebaseAuth.getInstance().getCurrentUser() 存在使用者時，
 * 只代表已透過 facebook 登入，不一定代表使用者通過驗證，
 * 當 UserViewModel.getObservableAuthUser() 存在使用者時，才代表通過驗證，
 * 而驗證 FirebaseUser 的進入點是 getUserFromFirestore(FirebaseUser)
 */
public class AccountActivity extends AppCompatActivity {

    public static final String TAG = AccountActivity.class.getSimpleName();
    private static final int REQUEST_CODE_READ_PHONE_STATE = 101;
    private static final int REQUEST_CODE_SIGNIN_FIREBASE_AUTH = 102;
    private boolean debug = false;

    private String mIMEI, mModel;

    private static final int MAX_DOCUMENT_LOCATION = 1;
    private TargetTrackedAdapter mTargetTrackedAdapter;
    private ProgressDialog mDialog_loading;
    private boolean hasSignIn = false;

    private ActivityAccountBinding mBinding;
    private UserViewModel mUserViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initUI();

        getPhoneInfo();

        mUserViewModel = ViewModelProviders.of(AccountActivity.this).get(UserViewModel.class);

        subscribeToUserModel();

        if (mUserViewModel.getObservableAuthUser().getValue() == null) {
            getUserFromFirestore(FirebaseAuth.getInstance().getCurrentUser());
        }
    }

    private void initUI() {
        mDialog_loading = new ProgressDialog(AccountActivity.this);
        mDialog_loading.setMessage(getString(R.string.loading));
        mDialog_loading.setIndeterminate(true);
        mDialog_loading.setCancelable(false);

        mBinding = DataBindingUtil.setContentView(
                AccountActivity.this, R.layout.activity_account);

        mTargetTrackedAdapter = new TargetTrackedAdapter(radarUser -> {
            radarUser.setInUse(!radarUser.getInUse());
            //todo 這個功能應該由偏好設定達成，而且要放在 MapsActivity
            //RadarUser.inUse 欄位可以去掉
            RadarPreferences.setTrackingTargetEmail(AccountActivity.this, radarUser.getEmail());
        });
        mBinding.recyclerViewTargetTracked.setAdapter(mTargetTrackedAdapter);
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

    private void subscribeToUserModel() {
        //內存
        mUserViewModel.getTargetsTracked().observe(AccountActivity.this,
                radarUsers -> {
                    if (radarUsers != null) {
                        mTargetTrackedAdapter.setUserList(radarUsers);
                        mBinding.executePendingBindings();
                    }
                }
        );

        //firebase
        observeUserFromFirebaseAuth();
        observeUserFromFirestore();
    }

    /**
     * 只有在 FirebaseAuth 登入成功，
     * 並在 Firestore 新增使用者成功，或比對 Firestore 使用者成功，firebaseAuth.getCurrentUser() 才不是 null
     * 因為此外的狀況我們強制登出 FirebaseAuth
     */
    private void observeUserFromFirebaseAuth() {
        mUserViewModel.getObservableAuthUser().observe(AccountActivity.this, (firebaseUser) -> {
            hasSignIn = firebaseUser != null;
            updateAuthInfo(firebaseUser);
        });
    }

    private void observeUserFromFirestore() {
        mUserViewModel.getObservableFirestoreUser().observe(AccountActivity.this, queryDocumentSnapshot -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                //沒登入 firebaseAuth
                mUserViewModel.getObservableAuthUser().postValue(null);
                return;
            }
            if (queryDocumentSnapshot == null) {
                //第一次登入，顯示對話筐，要求輸入定位密碼
                showDialogInputLocationPassword(FirebaseAuth.getInstance().getCurrentUser());
            } else {
                //再次登入，比較資料是否一致
                boolean same = compareUsers(FirebaseAuth.getInstance().getCurrentUser(), queryDocumentSnapshot);
                if (same) {
                    //新增使用者至內存
                    /*String email = queryDocumentSnapshot.getString(RadarFirestore.FIRESTORE_FIELD_EMAIL);
                    String password = queryDocumentSnapshot.getString(RadarFirestore.FIRESTORE_FIELD_PASSWORD);*/
                    /*MainDb.addUser(AccountActivity.this,
                            new RadarUser(email, password, USED_FOR_SENDLOCATION, IN_USE_YES));*/
                    mUserViewModel.getObservableAuthUser().postValue(FirebaseAuth.getInstance().getCurrentUser());
                } else {
                    //可能是在非綁定的裝置登入
                    Log.w(TAG, "該帳號已綁定於其他裝置.");
                    //提示使用者該帳號已綁定於其他裝置
                    new AlertDialog.Builder(AccountActivity.this)
                            .setTitle(R.string.dialog_title_ops)
                            .setMessage(R.string.userHasBindToOtherDevice)
                            .setCancelable(true)
                            .create().show();
                    //強制登出
                    RadarAuthentication.signOut(AccountActivity.this, null);
                    mUserViewModel.getObservableAuthUser().postValue(null);
                }
            }
        });
    }

    /**
     * 透過 FirebaseAuth 登入後，還要檢查 Firestore 有沒有這個使用者
     * 這個方法用於查詢 Firestore 中，相同 email 的使用者
     */
    private void getUserFromFirestore(FirebaseUser currentUser) {
        //連 FirebaseAuth 都還沒登入
        if (currentUser == null) {
            mUserViewModel.getObservableAuthUser().postValue(null);
            return;
        }
        //FirebaseAuth 存在使用者
        mDialog_loading.show();
        //檢查 Firestore 有沒有建立使用者
        RadarFirestore.checkUserExists(currentUser.getEmail(), task -> {
            mDialog_loading.dismiss();
            if (task.isSuccessful() && task.getResult().size() > 0) {
                //存在同 email
                for (QueryDocumentSnapshot document : task.getResult()) {
                    //這裡還沒驗證使用者，後續請查看 observeUserFromFirestore()
                    mUserViewModel.getObservableFirestoreUser().postValue(document);
                }
            } else if (task.isSuccessful() && task.getResult().size() < 1) {
                //Firestore 不存在該 email，代表第一次登入
                mUserViewModel.getObservableFirestoreUser().postValue(null);
            } else {
                //不應該發生的錯誤
                Log.d(TAG, "Task is not successful when getUserFromFirestore.");
                //強制登出
                RadarAuthentication.signOut(AccountActivity.this, null);
                mUserViewModel.getObservableAuthUser().postValue(null);
            }
        });
    }

    /**
     * Compare firebaseUser with the user queried from firestore
     *
     * @param currentUser FirebaseAuth.getInstance().getCurrentUser()
     * @param document    Query result from checkUserExists
     * @return true if they are the same.
     */
    private boolean compareUsers(FirebaseUser currentUser, QueryDocumentSnapshot document) {
        String resultEmail = document.getString(RadarFirestore.FIRESTORE_FIELD_EMAIL);
        String resultPassword = document.getString(RadarFirestore.FIRESTORE_FIELD_PASSWORD);
        String resultImei = document.getString(RadarFirestore.FIRESTORE_FIELD_IMEI);
        String resultUid = document.getString(RadarFirestore.FIRESTORE_FIELD_UID);
        return currentUser.getUid().equals(resultUid) && currentUser.getEmail().equals(resultEmail)
                && mIMEI.equals(resultImei);
    }

    //顯示對話筐，輸入定位密碼
    private void showDialogInputLocationPassword(final FirebaseUser user) {
        final EditText ed_password = new EditText(AccountActivity.this);
        ed_password.setHint(R.string.fui_password_hint);
        ed_password.setMaxLines(1);
        ed_password.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        //要求使用者建立一組密碼，用於加強定位的安全性
        new AlertDialog.Builder(AccountActivity.this)
                .setCancelable(false)
                .setTitle(getString(R.string.dialogTitle_positionSafe))
                .setMessage(getString(R.string.dialogMsg_pleaseSetPassword))
                .setView(ed_password)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    //取得輸入的密碼
                    String password = ed_password.getText().toString().trim();
                    //檢查密碼
                    if (!password.equals("")) {
                        mDialog_loading.show();
                        //在 firestore 建立使用者
                        createUserInFirestore(user, dialog, password);
                    } else {
                        showDialogInputLocationPassword(user);
                    }
                }).create().show();
    }

    private void createUserInFirestore(FirebaseUser user, DialogInterface dialog, String password) {
        RadarFirestore.createUser(user.getEmail(), password, mIMEI, mModel, user.getUid(), aVoid -> {
            mDialog_loading.dismiss();
            //關閉對話筐
            dialog.dismiss();
            //建立使用者成功
            Toast.makeText(AccountActivity.this, R.string.createUser_success, Toast.LENGTH_SHORT).show();
            //寫入手機 database 使用者信箱、密碼 ，日後用於新建、查詢、更新位置
            /*MainDb.addUser(AccountActivity.this,
                    new RadarUser(user.getEmail(), password, USED_FOR_SENDLOCATION, IN_USE_YES));*/
            //因為 firestore 新增使用者成功，完成所有登入與驗證，所以更新該值
            mUserViewModel.getObservableAuthUser().postValue(FirebaseAuth.getInstance().getCurrentUser());
            //初始化座標文件
            initFirestoreLocation(user, password);
        }, e -> {
            //建立使用者失敗
            //以資料結構設計正確為前提，不會建立失敗，除非是網路或 firestore 有問題
            mDialog_loading.dismiss();
            //關閉對話筐
            dialog.dismiss();
            RadarAuthentication.signOut(AccountActivity.this, null);
            mUserViewModel.getObservableAuthUser().postValue(null);
            Toast.makeText(AccountActivity.this, R.string.signIn_failed, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Error create user", e);
        });
    }

    /**
     * 建立使用者成功後，為使用者初始化 firestore 座標文件
     */
    private void initFirestoreLocation(FirebaseUser firebaseUser, String password) {
        if (firebaseUser == null) {
            RadarAuthentication.signOut(AccountActivity.this, null);
            return;
        }
        String email = firebaseUser.getEmail();
        if (email == null || email.equals("")) {
            showNoEmailDialog();
            RadarAuthentication.signOut(AccountActivity.this, null);
            return;
        }
        if (!password.equals("")) {
            //初始化 1 個座標
            for (int documentId = 1; documentId <= MAX_DOCUMENT_LOCATION; documentId++) {
                RadarFirestore.createLocation(String.valueOf(documentId), email, password,
                        firebaseUser.getUid(), mIMEI, 0, 0,
                        null, null
                );
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUserViewModel.getObservableFirestoreUser().getValue() == null) {
            //activity 銷毀時，如果 UserViewModel.getObservableFirestoreUser() 值是 null，代表驗證失敗
            // 登出 FirebaseAuth
            RadarAuthentication.signOut(AccountActivity.this, null);
        }
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

    /**
     * Refresh user info depending on verified FirebaseUser.
     */
    private void updateAuthInfo(FirebaseUser firebaseUser) {
        //todo 使用 DataBinding
        mBinding.setAuthUser(firebaseUser);
        new LoadBitmapFromUri().execute(RadarAuthentication.getDifferentPhotoSize(
                firebaseUser, 200));
    }

    public void clickPhoto(View view) {
        if (hasSignIn) {
            //登出
            showDialogSignOut();
        } else {
            //登入
            signIn();
        }
    }

    public void signIn() {
        RadarAuthentication.signIn(AccountActivity.this, REQUEST_CODE_SIGNIN_FIREBASE_AUTH);
    }

    public void showDialogSignOut() {
        new AlertDialog.Builder(AccountActivity.this)
                .setMessage(R.string.signOut)
                .setCancelable(true)
                .setPositiveButton(R.string.sure, (dialog, which) -> {
                    //登出
                    RadarAuthentication.signOut(AccountActivity.this, task -> {
                        if (task.isSuccessful()) {
                            mUserViewModel.getObservableAuthUser().postValue(null);
                            mUserViewModel.getObservableFirestoreUser().postValue(null);
                            if (RadarService.mInService) {
                                //停止定位服務
                                stopService(new Intent(AccountActivity.this, RadarService.class));
                            }
                        } else {
                            Log.d(TAG, "Error Firebase auth sign out", task.getException());
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .create().show();
    }


    //提醒使用者 Facebook 帳號尚未設定 Email
    private void showNoEmailDialog() {
        new AlertDialog.Builder(AccountActivity.this)
                .setCancelable(true)
                .setTitle(R.string.dialog_title_ops)
                .setMessage(R.string.userFbHasNoEmail)
                .create().show();
    }

    //檢查使用者 facebook 有沒有 email
    private boolean userHasEmail(FirebaseUser user) {
        if (debug) {
            Log.d(TAG, "Name: " + user.getDisplayName() + "\n" + "email: " + user.getEmail());
        }
        if (user == null) {
            return false;
        }
        String email = user.getEmail();
        if (email == null || email.trim().equals("")) {
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //result from firebase auth sign in
        if (requestCode == REQUEST_CODE_SIGNIN_FIREBASE_AUTH) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (!userHasEmail(firebaseUser)) {
                    showNoEmailDialog();
                    RadarAuthentication.signOut(AccountActivity.this, null);
                    return;
                }
                mDialog_loading.show();
                //檢查是否已在 Firestore 註冊
                getUserFromFirestore(firebaseUser);
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Toast.makeText(AccountActivity.this, R.string.signIn_failed, Toast.LENGTH_SHORT).show();
                int errorCode = response.getError().getErrorCode();
                String errorMsg = response.getError().getMessage();
                Log.d(TAG, "ErrorCode: " + errorCode + "Msg: " + errorMsg);
            }
        }
    }

    private class LoadBitmapFromUri extends AsyncTask<Uri, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog_loading.show();
        }

        @Override
        protected Bitmap doInBackground(Uri... uris) {
            if (uris[0] == null) {
                return null;
            }
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
            if (bitmap == null) {
                mBinding.imgViewPhoto.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_add));
            } else {
                mBinding.imgViewPhoto.setImageBitmap(bitmap);
            }
            mDialog_loading.dismiss();
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

    //添加追蹤對象
    public void addTargetTracked(View view) {
        //顯示對話筐，輸入追蹤對象的 email 與 password
        MyDialogBuilder builder = new MyDialogBuilder(AccountActivity.this, R.string.addTargetTracked);
        AlertDialog dialogAddTargetTracked = builder.create();
        dialogAddTargetTracked.show();
        builder.setOnButtonClickListener(v -> {
            if (v.getId() == R.id.dialog_btn_ok) {
                String email = builder.getEmail();
                String password = builder.getPassword();
                if (email == null || password == null) {
                    return;
                }
                mDialog_loading.show();
                //檢查使用者權限
                RadarFirestore.checkRightToReadUser(email, password, task -> {
                    mDialog_loading.dismiss();
                    if (task.isSuccessful() && task.getResult().size() > 0) {
                        //成功
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            mUserViewModel.insertUsers(new RadarUser(email, password, USED_FOR_GETLOCATION, false));
                        }
                    } else if (task.isSuccessful() && task.getResult().size() < 1) {
                        //與 firestore 溝通成功，但找不到該使用者
                        Toast.makeText(AccountActivity.this, R.string.wrong_user, Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "找不到該使用者，可能信箱或密碼打錯");
                    } else {
                        Log.d(TAG, "Error listing documents: ", task.getException());
                    }
                });
            }
            dialogAddTargetTracked.dismiss();
        });
    }
}
