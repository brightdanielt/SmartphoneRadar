package com.cauliflower.danielt.smartphoneradar;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.cauliflower.danielt.smartphoneradar.tool.ConnectDb;
import com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper;
import com.cauliflower.danielt.smartphoneradar.tool.MyDialogBuilder;
import com.cauliflower.danielt.smartphoneradar.tool.ResponseCode;
import com.cauliflower.danielt.smartphoneradar.ui.MapsActivity;
import com.cauliflower.danielt.smartphoneradar.ui.SettingsActivity;

import java.io.UnsupportedEncodingException;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.cauliflower.danielt.smartphoneradar.MainActivity.LoadingTask.TASK_LOGIN_TO_GET_LOCATION;
import static com.cauliflower.danielt.smartphoneradar.MainActivity.LoadingTask.TASK_LOGIN_TO_SEND_LOCATION;
import static com.cauliflower.danielt.smartphoneradar.MainActivity.LoadingTask.TASK_SIGN_UP;
import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.COLUMN_USER_ACCOUNT;
import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.COLUMN_USER_PASSWORD;
import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.COLUMN_USER_USEDFOR;
import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.TABLE_USER;
import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.VALUE_USER_USEDFOR_GETLOCATION;
import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.VALUE_USER_USEDFOR_SENDLOCATION;

public class MainActivity extends AppCompatActivity {
    private MyDbHelper dbHelper;
    private ConnectDb connectDb;
    private ResponseCode responseCode;

    ProgressDialog dialog_loading;
    private EditText edTxt_account, edTxt_password;
    private Button btn_enter;
    private RadioButton radioBtn_logIn_to_sendLocation, radioBtn_logIn_to_getLocation, radioBtn_signUp;

    private String account, password;
    private String imei, model;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new MyDbHelper(MainActivity.this);
        connectDb = new ConnectDb(MainActivity.this);
        responseCode = new ResponseCode(MainActivity.this);

        //先查詢是否已註冊
        Cursor cursor = dbHelper.getReadableDatabase().query(
                TABLE_USER, null, COLUMN_USER_USEDFOR + "=?", new String[]{VALUE_USER_USEDFOR_SENDLOCATION},
                null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int index_account = cursor.getColumnIndex(COLUMN_USER_ACCOUNT);
            int index_password = cursor.getColumnIndex(COLUMN_USER_PASSWORD);
            account = cursor.getString(index_account);
            password = cursor.getString(index_password);

            //存在帳密，已註冊
            if (account != null && password != null) {
                new LoadingTask().execute(TASK_LOGIN_TO_SEND_LOCATION);
            }
        } else {
            //若未註冊，檢查是否查詢過其他手機位置
            Cursor cursor_getLocation = dbHelper.getReadableDatabase().query(
                    TABLE_USER, null, COLUMN_USER_USEDFOR + "=?", new String[]{VALUE_USER_USEDFOR_GETLOCATION},
                    null, null, null);
            if (cursor_getLocation.getCount() > 0) {
                cursor_getLocation.moveToFirst();
                int index_account = cursor_getLocation.getColumnIndex(COLUMN_USER_ACCOUNT);
                int index_password = cursor_getLocation.getColumnIndex(COLUMN_USER_PASSWORD);
                account = cursor_getLocation.getString(index_account);
                password = cursor_getLocation.getString(index_password);
                //查詢過其他手機位置
                if (account != null && password != null) {
                    new LoadingTask().execute(TASK_LOGIN_TO_GET_LOCATION);
                }
            }else {
                //該帳密不存在，應重新輸入帳密以登入、查詢手機位置或註冊
                setContentView(R.layout.activity_main);
                makeViewWork();
            }
        }
    }

    private void makeViewWork() {
        radioBtn_logIn_to_sendLocation = findViewById(R.id.radioBtn_logIn);
        radioBtn_signUp = findViewById(R.id.radioBtn_signUp);
        radioBtn_logIn_to_getLocation = findViewById(R.id.radioBtn_getLocation);

        edTxt_account = findViewById(R.id.edTxt_account);
        edTxt_password = findViewById(R.id.edTxt_password);
        btn_enter = findViewById(R.id.btn_enter);

        btn_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                account = edTxt_account.getText().toString();
                password = edTxt_password.getText().toString();

                if (account.equals("") || password.equals("")) {
                    Toast.makeText(MainActivity.this, "請輸入帳密", Toast.LENGTH_SHORT).show();
                } else {
                    if (radioBtn_logIn_to_sendLocation.isChecked()) {
                        //登入以定位該手機
                        new LoadingTask().execute(TASK_LOGIN_TO_SEND_LOCATION);
                    } else if (radioBtn_signUp.isChecked()) {
                        //向 Server 註冊該帳密，並定位該手機
                        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
                            }
                            return;
                        } else {
//                            imei = telephonyManager.getDeviceId();
                            imei = "000006";
                            model = Build.MODEL;
                            new LoadingTask().execute(TASK_SIGN_UP);
                        }

                    } else if (radioBtn_logIn_to_getLocation.isChecked()) {
                        //登入以查詢綁定該帳號的手機位置
                        new LoadingTask().execute(TASK_LOGIN_TO_GET_LOCATION);
                    }
                }
            }
        });
    }

    public class LoadingTask extends AsyncTask<Integer, Object, String> {
        public static final int TASK_SIGN_UP = 101;
        public static final int TASK_LOGIN_TO_SEND_LOCATION = 102;
        public static final int TASK_LOGIN_TO_GET_LOCATION = 103;
        private int whichTask = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog_loading = ProgressDialog.show(MainActivity.this, "",
                    "Loading. Please wait...", true);
        }

        @Override
        protected String doInBackground(Integer... tasks) {
            whichTask = tasks[0];
            String response = null;
            switch (whichTask) {
                case TASK_SIGN_UP: {
                    try {
                        //向 Server 註冊該組帳密
                        response = connectDb.signUp(account, password, model, imei);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case TASK_LOGIN_TO_SEND_LOCATION:
                case TASK_LOGIN_TO_GET_LOCATION: {
                    try {
                        //要求 Server 驗證該組帳密
                        response = connectDb.logIn(account, password);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response.contains("Exception")) {
                //應該是網路或伺服器有問題，跳出對話筐，要求稍後再試試
                MyDialogBuilder dialogBuilder = new MyDialogBuilder(MainActivity.this, response);
                dialogBuilder.setPositiveButton("好了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
            } else if (responseCode.checkCode(response)) {
                //根據回傳值，得知目的成功與否
                switch (whichTask) {
                    case TASK_SIGN_UP: {
                        dbHelper.addUser(account, password, VALUE_USER_USEDFOR_SENDLOCATION);
                        Intent i = new Intent();
                        i.setClass(MainActivity.this, SettingsActivity.class);
                        i.putExtra(COLUMN_USER_ACCOUNT, account);
                        i.putExtra(COLUMN_USER_PASSWORD, password);
                        startActivity(i);
                        finish();
                        break;
                    }
                    case TASK_LOGIN_TO_SEND_LOCATION: {
                        //要判斷是否已存在該筆資料
                        //資料結構可能要調整
                        dbHelper.addUser(account, password, VALUE_USER_USEDFOR_SENDLOCATION);
                        //帳密存在，轉跳追蹤設定頁面
                        Intent i = new Intent();
                        i.setClass(MainActivity.this, SettingsActivity.class);
                        i.putExtra(COLUMN_USER_ACCOUNT, account);
                        i.putExtra(COLUMN_USER_PASSWORD, password);
                        startActivity(i);
                        finish();
                        break;
                    }
                    case TASK_LOGIN_TO_GET_LOCATION: {
                        dbHelper.addUser(account, password, VALUE_USER_USEDFOR_GETLOCATION);
                        //帳密存在，轉跳 Google Map 追蹤頁面
                        //顯示綁定該帳號的手機位置
                        Intent i = new Intent();
                        i.setClass(MainActivity.this, MapsActivity.class);
                        i.putExtra(COLUMN_USER_ACCOUNT, account);
                        i.putExtra(COLUMN_USER_PASSWORD, password);
                        startActivity(i);
                        finish();
                        break;
                    }
                }

            } else {
                //該帳密不存在，應重新輸入帳密以登入、查詢手機位置或註冊
                setContentView(R.layout.activity_main);
                makeViewWork();
            }
            dialog_loading.dismiss();

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog_loading != null && dialog_loading.isShowing()) {
            dialog_loading.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PERMISSION_GRANTED) {
//                            imei = telephonyManager.getDeviceId();
                imei = "000007";
                model = Build.MODEL;
                new LoadingTask().execute(TASK_SIGN_UP);
            }
        }
    }
}
