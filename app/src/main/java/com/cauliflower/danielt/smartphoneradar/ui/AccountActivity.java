package com.cauliflower.danielt.smartphoneradar.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.obj.User;
import com.cauliflower.danielt.smartphoneradar.tool.ConnectServer;
import com.cauliflower.danielt.smartphoneradar.data.RadarDbHelper;
import com.cauliflower.danielt.smartphoneradar.tool.MyDialogBuilder;
import com.cauliflower.danielt.smartphoneradar.tool.ResponseCode;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.UserEntry;

import static com.cauliflower.danielt.smartphoneradar.ui.AccountActivity.LoadingTask.TASK_LOGIN_TO_GET_LOCATION;
import static com.cauliflower.danielt.smartphoneradar.ui.AccountActivity.LoadingTask.TASK_LOGIN_TO_SEND_LOCATION;
import static com.cauliflower.danielt.smartphoneradar.ui.AccountActivity.LoadingTask.TASK_SEND_VERIFICATION_CODE;
import static com.cauliflower.danielt.smartphoneradar.ui.AccountActivity.LoadingTask.TASK_SIGN_UP;
import static com.cauliflower.danielt.smartphoneradar.ui.AccountActivity.LoadingTask.TASK_UPDATE_PASSWORD;


public class AccountActivity extends AppCompatActivity {

    public static final String TAG = AccountActivity.class.getSimpleName();
    private static final int REQUEST_CODE_READ_PHONE_STATE = 101;
    private RadarDbHelper mDbHelper;
    private ConnectServer mConnectServer;
    private ResponseCode mResponseCode;

    private ProgressDialog mDialog_loading;
    private MyDialogBuilder mDialogBuilder_signUp_logIn;
    private AlertDialog mDialog_signUp_logIn = null;

    private String mAccount_sendLocation, mPassword_sendLocation,
            mAccount_getLocation, mPassword_getLocation,
            mAccount_forgetPassword;

    private String mIMEI, mModel, mEmail;
    private int mVerification_code;

    private Button mBtn_sendLocation_logIn, mBtn_sendLocation_signUp, mBtn_getLocation_logIn;
    private Button mBtn_forgetPassword_ok, mBtn_forgetPassword_cancel, mBtn_forgetPassword_send_verification_code;
    private EditText mEdTxt_forgetPassword_account, mEdTxt_forgetPassword_email, mEdTxt_forgetPassword_verificationCode;
    private TextView mTv_hint_sendLocation, mTv_hint_getLocation;
    private ListView mListView_sendLocation, mListView_getLocation;
    private List<User> mUserList_sendLocation = new ArrayList<>();
    private List<User> mUserList_getLocation = new ArrayList<>();
    private AccountAdapter mAdapter_sendLocation, mAdapter_getLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDbHelper = new RadarDbHelper(AccountActivity.this);
        mConnectServer = new ConnectServer(AccountActivity.this);
        mResponseCode = new ResponseCode(AccountActivity.this);
        makeViewWork();

    }

    @Override
    protected void onStart() {
        super.onStart();
        getPhoneInfo();
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

        mBtn_sendLocation_logIn = findViewById(R.id.btn_sendLocation_logIn);
        mBtn_sendLocation_signUp = findViewById(R.id.btn_sendLocation_signUp);
        mBtn_getLocation_logIn = findViewById(R.id.btn_getLocation_logIn);
        //按鈕註冊監聽器
        mBtn_sendLocation_logIn.setOnClickListener(new MyButtonClickListener(R.string.title_logIn));
        mBtn_sendLocation_signUp.setOnClickListener(new MyButtonClickListener(R.string.title_signUp));
        mBtn_getLocation_logIn.setOnClickListener(new MyButtonClickListener(R.string.title_logIn));

        mTv_hint_sendLocation = findViewById(R.id.tv_hint_sendLocation);
        mTv_hint_getLocation = findViewById(R.id.tv_hint_getLocation);

        mListView_sendLocation = findViewById(R.id.listView_sendLocation);
        mListView_getLocation = findViewById(R.id.listView_getLocation);

        mUserList_sendLocation.addAll(mDbHelper.searchUser(UserEntry.USED_FOR_SENDLOCATION));
        mUserList_getLocation.addAll(mDbHelper.searchUser(UserEntry.USED_FOR_GETLOCATION));
        mAdapter_sendLocation = new AccountAdapter(mUserList_sendLocation);
        mAdapter_getLocation = new AccountAdapter(mUserList_getLocation);
        mListView_sendLocation.setAdapter(mAdapter_sendLocation);
        mListView_getLocation.setAdapter(mAdapter_getLocation);

        updateView();
    }

    private void updateView() {
        mUserList_sendLocation.clear();
        mUserList_getLocation.clear();
        mUserList_sendLocation.addAll(mDbHelper.searchUser(UserEntry.USED_FOR_SENDLOCATION));
        mUserList_getLocation.addAll(mDbHelper.searchUser(UserEntry.USED_FOR_GETLOCATION));

        if (mUserList_sendLocation.size() > 0) {
            mTv_hint_sendLocation.setVisibility(View.GONE);
            mBtn_sendLocation_logIn.setVisibility(View.INVISIBLE);
            mBtn_sendLocation_signUp.setVisibility(View.INVISIBLE);
        } else {
            //綁定帳號處，顯示登入或註冊按鈕
            mBtn_sendLocation_logIn.setVisibility(View.VISIBLE);
            mBtn_sendLocation_signUp.setVisibility(View.VISIBLE);
        }
        if (mUserList_getLocation.size() > 0) {
            mTv_hint_getLocation.setVisibility(View.GONE);
        }
        mAdapter_sendLocation.notifyDataSetChanged();
        mAdapter_getLocation.notifyDataSetChanged();

    }

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

    /**
     * 與伺服器溝通，可溝通多種內容：
     * 1.註冊帳號
     * 2.登入定位帳號
     * 3.登入查詢帳號
     * 4.忘記密碼時，要求Server傳送驗證碼到指定信箱
     * 5.更新密碼（驗證碼正確時，驗證碼做為密碼）
     */
    public class LoadingTask extends AsyncTask<Integer, Object, String> {
        public static final int TASK_SIGN_UP = 101;
        public static final int TASK_LOGIN_TO_SEND_LOCATION = 102;
        public static final int TASK_LOGIN_TO_GET_LOCATION = 103;
        public static final int TASK_SEND_VERIFICATION_CODE = 104;
        public static final int TASK_UPDATE_PASSWORD = 105;
        private int whichTask = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog_loading = ProgressDialog.show(AccountActivity.this, "",
                    getString(R.string.loading), true);
        }

        @Override
        protected String doInBackground(Integer... tasks) {
            whichTask = tasks[0];
            String response = "";
            switch (whichTask) {
                case TASK_SIGN_UP: {
                    try {
                        //向 Server 註冊該組帳密
                        response = mConnectServer.signUp(mAccount_sendLocation, mPassword_sendLocation, mModel, mIMEI);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case TASK_LOGIN_TO_SEND_LOCATION: {
                    try {
                        //要求 Server 驗證該組帳密
                        response = mConnectServer.logIn_sendLocation(
                                mAccount_sendLocation, mPassword_sendLocation, mModel, mIMEI);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case TASK_LOGIN_TO_GET_LOCATION: {
                    try {
                        //要求 Server 驗證該組帳密
                        response = mConnectServer.logIn_getLocation(mAccount_getLocation, mPassword_getLocation);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case TASK_SEND_VERIFICATION_CODE: {
                    try {
                        //要求 Server 發送驗證碼到 Email
                        Random random = new Random();
                        mVerification_code = 1 + random.nextInt(10000);
                        response = mConnectServer.sendVerificationCodeToEmail(
                                mAccount_forgetPassword, mModel, mIMEI, mEmail, String.valueOf(mVerification_code));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case TASK_UPDATE_PASSWORD: {
                    try {
                        //更改密碼
                        response = mConnectServer.updatePassword(
                                mAccount_forgetPassword, String.valueOf(mVerification_code));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            return response;
        }


        /**
         * 根據回傳值執行相對應的後續任務
         * 回傳值分為兩種情況：
         * 1.Exception，可能原因：裝置網路未連接、伺服器未開啟
         * 2.Success、failed、error：代表與伺服器連接成功，詳細代碼請對照 {@link ResponseCode}
         * 當出現第三種情況，代表伺服器與裝置端網路通訊的程式碼有問題，因為預期上，我們能夠處理任何回傳值
         */
        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            //應該是網路或伺服器有問題，跳出對話筐，要求稍後再試試
            if (response.contains("Exception")) {
                MyDialogBuilder dialogBuilder = new MyDialogBuilder(AccountActivity.this, response);
                dialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //再執行一次該任務
                        switch (whichTask) {
                            case TASK_SIGN_UP: {
                                new LoadingTask().execute(TASK_SIGN_UP);
                                break;
                            }
                            case TASK_LOGIN_TO_SEND_LOCATION: {
                                new LoadingTask().execute(TASK_LOGIN_TO_SEND_LOCATION);
                                break;
                            }
                            case TASK_LOGIN_TO_GET_LOCATION: {
                                new LoadingTask().execute(TASK_LOGIN_TO_GET_LOCATION);
                                break;
                            }
                        }
                    }
                }).show();
            } else if (mResponseCode.checkCode(response)) {
                //根據回傳值，得知目的成功與否
                switch (whichTask) {
                    case TASK_SIGN_UP: {
                        mDbHelper.addUser(mAccount_sendLocation, mPassword_sendLocation, UserEntry.USED_FOR_SENDLOCATION, UserEntry.IN_USE_YES);
                        Toast.makeText(AccountActivity.this, mAccount_sendLocation + getString(R.string.sighUp_sendLocation_success), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case TASK_LOGIN_TO_SEND_LOCATION: {
                        mDbHelper.addUser(mAccount_sendLocation, mPassword_sendLocation, UserEntry.USED_FOR_SENDLOCATION, UserEntry.IN_USE_NO);
                        Toast.makeText(AccountActivity.this, mAccount_sendLocation + getString(R.string.logIn_sendLocation_success), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case TASK_LOGIN_TO_GET_LOCATION: {
                        mDbHelper.addUser(mAccount_getLocation, mPassword_getLocation, UserEntry.USED_FOR_GETLOCATION, UserEntry.IN_USE_NO);
                        Toast.makeText(AccountActivity.this, mAccount_getLocation + getString(R.string.logIn_getLocation_success), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case TASK_SEND_VERIFICATION_CODE: {
                        mEdTxt_forgetPassword_account.setEnabled(false);
                        mEdTxt_forgetPassword_email.setEnabled(false);
                        mEdTxt_forgetPassword_verificationCode.setEnabled(true);
                        mBtn_forgetPassword_send_verification_code.setEnabled(false);
                        mBtn_forgetPassword_ok.setEnabled(true);
                        Toast.makeText(AccountActivity.this, getString(R.string.please_receive_verification_code), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case TASK_UPDATE_PASSWORD: {
                        mAccount_sendLocation = mAccount_forgetPassword;
                        mPassword_sendLocation = String.valueOf(mVerification_code);
                        mDbHelper.addUser(mAccount_sendLocation, mPassword_sendLocation, UserEntry.USED_FOR_SENDLOCATION, UserEntry.IN_USE_YES);
                        mDbHelper.updatePassword(mAccount_forgetPassword, String.valueOf(mVerification_code));
                        Toast.makeText(AccountActivity.this, mAccount_sendLocation + getString(R.string.update_password_success), Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                updateView();
            } else {
                Log.d(TAG, "Unexpected response in loadingTask.");
            }
            mDialog_loading.dismiss();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog_loading != null && mDialog_loading.isShowing()) {
            mDialog_loading.dismiss();
        }
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_PHONE_STATE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getPhoneInfo();
        }
    }

    public class AccountAdapter extends BaseAdapter {
        List<User> userList;

        public AccountAdapter(List<User> userList) {
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
            final String account = userList.get(position).getAccount();
            final String password = userList.get(position).getPassword();
            final String usedFor = userList.get(position).getUsedFor();
            String in_use = userList.get(position).getIn_use();
            tv_account.setText(account);
            if (in_use.equals(UserEntry.IN_USE_YES)) {
                tv_account.append("  已登入");
            }
            convertView = v;

            if (usedFor.equals(UserEntry.USED_FOR_GETLOCATION)) {
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(AccountActivity.this)
                                .setTitle("問問你～")
                                .setMessage(Html.fromHtml("以 <font color=\"blue\">" + account + "</font> 的身份登入嗎？"))
                                .setCancelable(false)
                                .setPositiveButton("是的", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //要求 RadarDbHelper 更改該 User 的 in_use 為 yes
                                        mDbHelper.updateUser_in_use(account);
                                        updateView();
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

    //AccountActivity的登入、註冊按鈕監聽器
    class MyButtonClickListener implements View.OnClickListener {
        int resId_title;

        MyButtonClickListener(int resId_dialogTitle) {
            resId_title = resId_dialogTitle;
        }

        @Override
        public void onClick(View v) {
            mDialogBuilder_signUp_logIn = new MyDialogBuilder(AccountActivity.this, resId_title);
            switch (v.getId()) {
                //點擊定位用的註冊按鈕
                case R.id.btn_sendLocation_signUp: {
                    getPhoneInfo();
                    mDialogBuilder_signUp_logIn.setOnButtonClickListener(
                            new DialogButtonListener(TASK_SIGN_UP));
                    break;
                }
                //點擊定位用的登入按鈕
                case R.id.btn_sendLocation_logIn: {
                    getPhoneInfo();
                    mDialogBuilder_signUp_logIn.setBtn_forgetPasswordVisibility(View.VISIBLE);
                    mDialogBuilder_signUp_logIn.setOnButtonClickListener(
                            new DialogButtonListener(TASK_LOGIN_TO_SEND_LOCATION));
                    break;
                }
                //點擊查詢用的登入按鈕
                case R.id.btn_getLocation_logIn: {
                    mDialogBuilder_signUp_logIn.setOnButtonClickListener(
                            new DialogButtonListener(TASK_LOGIN_TO_GET_LOCATION));
                    break;
                }
            }
            mDialog_signUp_logIn = mDialogBuilder_signUp_logIn.create();
            mDialog_signUp_logIn.show();
        }
    }

    //mDialog_signUp_logIn 的按鈕監聽器
    class DialogButtonListener implements View.OnClickListener {
        int task;

        public DialogButtonListener(int task) {
            this.task = task;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.dialog_btn_cancel) {
                mDialog_signUp_logIn.dismiss();
            } else if (v.getId() == R.id.dialog_btn_ok) {
                switch (task) {
                    case TASK_SIGN_UP: {
                        //註冊定位用的帳號
                        mAccount_sendLocation = mDialogBuilder_signUp_logIn.getAccount();
                        mPassword_sendLocation = mDialogBuilder_signUp_logIn.getPassword();
                        if (mAccount_sendLocation != null && mPassword_sendLocation != null
                                && mIMEI != null) {
                            mDialog_signUp_logIn.dismiss();
                            new LoadingTask().execute(TASK_SIGN_UP);
                        } else if (mAccount_sendLocation == null || mPassword_sendLocation == null) {
                            Toast.makeText(AccountActivity.this,
                                    getString(R.string.please_complete_the_fields), Toast.LENGTH_SHORT).show();
                        } else if (mIMEI == null) {
                            Toast.makeText(AccountActivity.this,
                                    getString(R.string.give_me_read_phone_state_permission), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    case TASK_LOGIN_TO_SEND_LOCATION: {
                        //登入定位用的帳號
                        mAccount_sendLocation = mDialogBuilder_signUp_logIn.getAccount();
                        mPassword_sendLocation = mDialogBuilder_signUp_logIn.getPassword();
                        if (mAccount_sendLocation != null && mPassword_sendLocation != null
                                && mIMEI != null) {
//                            mDialog_signUp_logIn.dismiss();
                            new LoadingTask().execute(TASK_LOGIN_TO_SEND_LOCATION);
                        } else if (mAccount_sendLocation == null || mPassword_sendLocation == null) {
                            Toast.makeText(AccountActivity.this,
                                    getString(R.string.please_complete_the_fields), Toast.LENGTH_SHORT).show();
                        } else if (mIMEI == null) {
                            Toast.makeText(AccountActivity.this,
                                    getString(R.string.give_me_read_phone_state_permission), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    case TASK_LOGIN_TO_GET_LOCATION: {
                        //登入定查詢的帳號
                        mAccount_getLocation = mDialogBuilder_signUp_logIn.getAccount();
                        mPassword_getLocation = mDialogBuilder_signUp_logIn.getPassword();
                        if (mAccount_getLocation != null && mPassword_getLocation != null) {
                            mDialog_signUp_logIn.dismiss();
                            new LoadingTask().execute(TASK_LOGIN_TO_GET_LOCATION);
                        } else if (mAccount_getLocation == null || mPassword_getLocation == null) {
                            Toast.makeText(AccountActivity.this,
                                    getString(R.string.please_complete_the_fields), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                }
            } else if (v.getId() == R.id.dialog_btn_forgetPassword) {
                mDialog_signUp_logIn.dismiss();
                final AlertDialog dialog_forgetPassword;
                View view = LayoutInflater.from(
                        AccountActivity.this).inflate(R.layout.dialog_forget_password, null);
                mEdTxt_forgetPassword_account = view.findViewById(R.id.dialog_forgetPassword_edTxt_account);
                mEdTxt_forgetPassword_email = view.findViewById(R.id.dialog_forgetPassword_edTxt_email);
                mEdTxt_forgetPassword_verificationCode = view.findViewById(R.id.dialog_forgetPassword_edTxt_verificationCode);
                mBtn_forgetPassword_ok = view.findViewById(R.id.dialog_forgetPassword_btn_ok);
                mBtn_forgetPassword_cancel = view.findViewById(R.id.dialog_forgetPassword_btn_cancel);
                mBtn_forgetPassword_send_verification_code = view.findViewById(R.id.dialog_forgetPassword_btn_send_verification_code);
                AlertDialog.Builder dialogBuilder_forgetPassword = new AlertDialog.Builder(AccountActivity.this);
                dialogBuilder_forgetPassword.setView(view);
                dialogBuilder_forgetPassword.setTitle(R.string.title_forget_password);
                dialogBuilder_forgetPassword.setCancelable(false);
                dialog_forgetPassword = dialogBuilder_forgetPassword.create();
                mBtn_forgetPassword_send_verification_code.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAccount_forgetPassword = mEdTxt_forgetPassword_account.getText().toString();
                        mEmail = mEdTxt_forgetPassword_email.getText().toString();
                        if (mAccount_forgetPassword.trim().equals("") || mEmail.trim().equals("")) {
                            Toast.makeText(AccountActivity.this,
                                    getString(R.string.please_complete_the_fields), Toast.LENGTH_SHORT).show();
                        } else if (mModel == null || mIMEI == null) {
                            Toast.makeText(AccountActivity.this,
                                    getString(R.string.give_me_read_phone_state_permission), Toast.LENGTH_SHORT).show();
                        } else if (!(mAccount_forgetPassword.trim()).equals("") && !(mEmail.trim()).equals("")
                                && !(mModel.trim()).equals("") && !(mIMEI.trim()).equals("")) {
                            //送出 HttpRequest，使用者請到 Email 收驗證碼
                            new LoadingTask().execute(TASK_SEND_VERIFICATION_CODE);
                        }
                    }
                });
                mBtn_forgetPassword_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String verification_code = mEdTxt_forgetPassword_verificationCode.getText().toString();
                        if (!(verification_code.trim()).equals("")) {
                            if (verification_code.equals(verification_code)) {
                                new LoadingTask().execute(TASK_UPDATE_PASSWORD);
                                dialog_forgetPassword.dismiss();
                            }
                        }
                    }
                });
                mBtn_forgetPassword_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog_forgetPassword.dismiss();
                        mEdTxt_forgetPassword_account = null;
                        mEdTxt_forgetPassword_email = null;
                        mEdTxt_forgetPassword_verificationCode = null;
                        mBtn_forgetPassword_send_verification_code = null;
                        mBtn_forgetPassword_cancel = null;
                        mBtn_forgetPassword_ok = null;
                    }
                });
                dialog_forgetPassword.show();
            }
            mDialog_signUp_logIn.dismiss();
            mDialog_signUp_logIn = null;
            mDialogBuilder_signUp_logIn = null;
        }
    }
}
