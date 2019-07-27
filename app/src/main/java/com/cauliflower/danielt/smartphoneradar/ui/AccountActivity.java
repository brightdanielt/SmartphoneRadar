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
import com.cauliflower.danielt.smartphoneradar.RadarApp;
import com.cauliflower.danielt.smartphoneradar.data.RadarUser;
import com.cauliflower.danielt.smartphoneradar.databinding.ActivityAccountBinding;
import com.cauliflower.danielt.smartphoneradar.firebase.RadarAuthentication;
import com.cauliflower.danielt.smartphoneradar.firebase.RadarFirestore;
import com.cauliflower.danielt.smartphoneradar.tool.MyDialogBuilder;
import com.cauliflower.danielt.smartphoneradar.viewmodel.UserViewModel;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.UserEntry.USED_FOR_GETLOCATION;

/**
 * 請注意: Profile.getCurrentProfile() 存在使用者時，
 * 只代表已透過 facebook 登入，不一定代表使用者通過驗證，
 * 當 ui 的 email 顯示時，才代表通過驗證
 */
public class AccountActivity extends AppCompatActivity {

    public static final String TAG = AccountActivity.class.getSimpleName();
    private static final int REQUEST_CODE_READ_PHONE_STATE = 101;

    private String mIMEI, mModel;

    private static final int MAX_DOCUMENT_LOCATION = 1;
    private TargetTrackedAdapter mTargetTrackedAdapter;
    private ProgressDialog mDialog_loading;

    private ActivityAccountBinding mBinding;
    private UserViewModel mUserViewModel;
    private CallbackManager mCallbackManager;
    private RadarApp mApp;
    private AccessTokenTracker mTokenTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mApp = (RadarApp) getApplication();

        initUI();

        getPhoneInfo();

        mTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    updateUI(null, null);
                }
            }
        };

        mUserViewModel = ViewModelProviders.of(AccountActivity.this).get(UserViewModel.class);

        subscribeToUserModel();

    }

    private void initUI() {
        mDialog_loading = new ProgressDialog(AccountActivity.this);
        mDialog_loading.setMessage(getString(R.string.loading));
        mDialog_loading.setIndeterminate(true);
        mDialog_loading.setCancelable(false);

        mBinding = DataBindingUtil.setContentView(
                AccountActivity.this, R.layout.activity_account);

        mTargetTrackedAdapter = new TargetTrackedAdapter(null);
        mBinding.recyclerViewTargetTracked.setAdapter(mTargetTrackedAdapter);

        mCallbackManager = CallbackManager.Factory.create();
        mBinding.btnLoginFb.setReadPermissions("email");
        mBinding.btnLoginFb.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mApp.getFbRepository().getEmail(loginResult.getAccessToken())
                        .observe(AccountActivity.this, email -> {
                            if (email != null) {
                                //檢查是否已在 Firestore 註冊
                                getUserFromFirestoreAndVerify(Profile.getCurrentProfile(), email);
                            } else {
                                showNoEmailDialog();
                                LoginManager.getInstance().logOut();
                            }
                        });
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(AccountActivity.this, R.string.signIn_failed, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Msg: " + error.getMessage());
            }
        });
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
        mUserViewModel.emailFromFb().observe(AccountActivity.this, email -> {
            if (email != null) {
                getUserFromFirestoreAndVerify(Profile.getCurrentProfile(), email);
            }
        });
        //內存
        mUserViewModel.getTargets().observe(AccountActivity.this,
                radarUsers -> {
                    if (radarUsers != null) {
                        mTargetTrackedAdapter.setUserList(radarUsers);
                        mBinding.executePendingBindings();
                    }
                }
        );

    }

    /**
     * 透過 FB 登入後，再檢查 Firestore 是否存在使用者
     * 這個方法用於查詢 Firestore 中，相同 email 的使用者
     */
    private void getUserFromFirestoreAndVerify(Profile profile, String email) {
        mDialog_loading.show();
        //檢查 Firestore 有沒有建立使用者
        RadarFirestore.checkUserExists(email, task -> {
            mDialog_loading.dismiss();
            if (task.isSuccessful() && task.getResult().size() == 0) {
                //Firestore 不存在該 email，代表在註冊中
                //顯示對話筐，要求輸入定位密碼
                askLocationPassword(profile, email);
            } else if (task.isSuccessful() && task.getResult().size() > 0) {
                //Firestore 存在該 email，代表是登入
                QueryDocumentSnapshot snapshot = null;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    snapshot = document;
                }
                //驗證資料是否一致
                boolean verifiedUser = compareUsers(Profile.getCurrentProfile(), email, snapshot);
                if (verifiedUser) {
                    //驗證成功，更新ＵＩ
                    updateUI(Profile.getCurrentProfile(), email);
                } else {
                    //驗證失敗
                    Log.w(TAG, "該帳號已綁定於其他裝置.");
                    //提示該帳號已綁定其他裝置
                    new AlertDialog.Builder(AccountActivity.this)
                            .setTitle(R.string.dialog_title_ops)
                            .setMessage(R.string.userHasBindToOtherDevice)
                            .setCancelable(true)
                            .create().show();
                    //強制登出
                    LoginManager.getInstance().logOut();
                }
            } else {
                //不應該發生的錯誤
                Log.d(TAG, "Task: checkUserExists failed.");
                //強制登出
                LoginManager.getInstance().logOut();
            }
        });
    }

    /**
     * Compare firebaseUser with the user queried from firestore
     *
     * @param profile  Profile.getCurrentProfile()
     * @param document Query result from checkUserExists
     * @return true if they are the same user and same device.
     */
    private boolean compareUsers(Profile profile, String email, QueryDocumentSnapshot document) {
        String resultEmail = document.getString(RadarFirestore.FIRESTORE_FIELD_EMAIL);
        String resultPassword = document.getString(RadarFirestore.FIRESTORE_FIELD_PASSWORD);
        String resultImei = document.getString(RadarFirestore.FIRESTORE_FIELD_IMEI);
        String resultUid = document.getString(RadarFirestore.FIRESTORE_FIELD_UID);
        return profile.getId().equals(resultUid) && email.equals(resultEmail)
                && mIMEI.equals(resultImei);
    }

    //顯示對話筐，輸入定位密碼
    private void askLocationPassword(Profile profile, String email) {
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
                        createUserInFirestore(profile, email, dialog, password);
                    } else {
                        askLocationPassword(profile, email);
                    }
                }).create().show();
    }

    private void createUserInFirestore(Profile profile, String email, DialogInterface dialog, String password) {
        RadarFirestore.createUser(email, password, mIMEI, mModel, profile.getId(), aVoid -> {
            mDialog_loading.dismiss();
            //關閉對話筐
            dialog.dismiss();
            //建立使用者成功
            Toast.makeText(AccountActivity.this, R.string.createUser_success, Toast.LENGTH_SHORT).show();
            //寫入手機 database 使用者信箱、密碼 ，日後用於新建、查詢、更新位置
            /*MainDb.addUser(AccountActivity.this,
                    new RadarUser(user.emailFromFb(), password, USED_FOR_SENDLOCATION, IN_USE_YES));*/
            //因為 firestore 新增使用者成功，完成所有登入與驗證，所以更新該值
            updateUI(Profile.getCurrentProfile(), email);
            //初始化座標文件
            initFirestoreLocation(profile, email, password);
        }, e -> {
            //建立使用者失敗
            //以資料結構設計正確為前提，不會建立失敗，除非是網路或 firestore 有問題
            mDialog_loading.dismiss();
            //關閉對話筐
            dialog.dismiss();
            LoginManager.getInstance().logOut();
            Toast.makeText(AccountActivity.this, R.string.signIn_failed, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Error create user", e);
        });
    }

    /**
     * 建立使用者成功後，為使用者初始化 firestore 座標文件
     */
    private void initFirestoreLocation(Profile profile, String email, String password) {
        if (profile == null) {
            LoginManager.getInstance().logOut();
            return;
        }
        if (email == null || email.equals("")) {
            showNoEmailDialog();
            RadarAuthentication.signOut(AccountActivity.this, null);
            return;
        }
        if (!password.equals("")) {
            //初始化 1 個座標
            for (int documentId = 1; documentId <= MAX_DOCUMENT_LOCATION; documentId++) {
                RadarFirestore.createLocation(String.valueOf(documentId), email, password,
                        profile.getId(), mIMEI, 0, 0,
                        null, null
                );
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBinding.tvUserEmail.getText() == null) {
            //activity 銷毀時，如果 ui 的 email 是 null，代表驗證失敗或尚未成功
            // 登出 FB
            LoginManager.getInstance().logOut();
        }
        if (mDialog_loading != null && mDialog_loading.isShowing()) {
            mDialog_loading.dismiss();
        }
        mTokenTracker.stopTracking();
        mTokenTracker = null;
        mBinding.btnLoginFb.unregisterCallback(mCallbackManager);
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
     * Refresh user info depending on facebook profile
     */
    private void updateUI(Profile profile, String email) {
        mBinding.setFacebookProfile(profile);
        mBinding.tvUserEmail.setText(email);
    }

    //提醒使用者 Facebook 帳號尚未設定 Email
    private void showNoEmailDialog() {
        new AlertDialog.Builder(AccountActivity.this)
                .setCancelable(true)
                .setTitle(R.string.dialog_title_ops)
                .setMessage(R.string.userFbHasNoEmail)
                .create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //result from fb login
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
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
                            mUserViewModel.insertUsers(new RadarUser(email, password, USED_FOR_GETLOCATION));
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
