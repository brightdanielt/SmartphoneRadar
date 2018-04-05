package com.cauliflower.danielt.smartphoneradar;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.cauliflower.danielt.smartphoneradar.Tool.ConnectDb;
import com.cauliflower.danielt.smartphoneradar.Tool.MyDbHelper;
import com.cauliflower.danielt.smartphoneradar.Tool.ResponseCode;
import com.cauliflower.danielt.smartphoneradar.UI.MapsActivity;
import com.cauliflower.danielt.smartphoneradar.UI.SignUpActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.cauliflower.danielt.smartphoneradar.Tool.MyDbHelper.COLUMN_USER_ACCOUNT;
import static com.cauliflower.danielt.smartphoneradar.Tool.MyDbHelper.COLUMN_USER_PASSWORD;
import static com.cauliflower.danielt.smartphoneradar.Tool.MyDbHelper.COLUMN_USER_USEDFOR;
import static com.cauliflower.danielt.smartphoneradar.Tool.MyDbHelper.TABLE_USER;
import static com.cauliflower.danielt.smartphoneradar.Tool.MyDbHelper.VALUE_USER_USEDFOR_GETLOCATION;
import static com.cauliflower.danielt.smartphoneradar.Tool.MyDbHelper.VALUE_USER_USEDFOR_SENDLOCATION;

public class MainActivity extends AppCompatActivity {
    private MyDbHelper dbHelper;
    private ConnectDb connectDb;
    private ResponseCode responseCode;
    private EditText edTxt_account, edTxt_password;
    private Button btn_enter;
    private RadioButton radioBtn_logIn, radioBtn_signUp, radioBtn_getLocation;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectDb = new ConnectDb(MainActivity.this);

        dbHelper = new MyDbHelper(MainActivity.this);

        responseCode = new ResponseCode(MainActivity.this);

        //先查詢是否已註冊
        Cursor cursor = dbHelper.getReadableDatabase().query(
                TABLE_USER, null, COLUMN_USER_USEDFOR + "=?", new String[]{VALUE_USER_USEDFOR_SENDLOCATION},
                null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int index_account = cursor.getColumnIndex(COLUMN_USER_ACCOUNT);
            int index_password = cursor.getColumnIndex(COLUMN_USER_PASSWORD);
            String account = cursor.getString(index_account);
            String password = cursor.getString(index_password);

            //存在帳密，已註冊
            if (account != null && password != null) {
                try {
                    //要求 Server 驗證該組帳密
                    String code = connectDb.logIn(account, password);
                    //根據回傳值，得知目的成功與否
                    if (responseCode.checkCode(code)) {
                        //帳密存在，轉跳追蹤設定頁面
                        Toast.makeText(MainActivity.this, "帳密存在，轉跳追蹤設定頁面", Toast.LENGTH_SHORT).show();
                    } else {
                        //帳密不存在，應重新輸入帳密以登入、查詢手機位置或註冊
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                //若未註冊，檢查是否查詢過其他手機位置
                Cursor cursor_getLocation = dbHelper.getReadableDatabase().query(
                        TABLE_USER, null, COLUMN_USER_USEDFOR + "=?", new String[]{VALUE_USER_USEDFOR_GETLOCATION},
                        null, null, null);
                int index_account_getLocation = cursor_getLocation.getColumnIndex(COLUMN_USER_ACCOUNT);
                int index_password_getLocation = cursor_getLocation.getColumnIndex(COLUMN_USER_PASSWORD);
                String account_getLocation = cursor_getLocation.getString(index_account_getLocation);
                String password_getLocation = cursor_getLocation.getString(index_password_getLocation);
                try {
                    //查詢過其他手機位置
                    if (account_getLocation != null && password_getLocation != null) {
                        //要求 Server 驗證該組帳密
                        String code = connectDb.logIn(account_getLocation, password_getLocation);
                        //根據回傳值，得知驗證成功與否
                        if (responseCode.checkCode(code)) {
                            //帳密存在，轉跳google Map追蹤頁面
                            Intent i = new Intent();
                            i.putExtra(COLUMN_USER_ACCOUNT, account_getLocation);
                            i.putExtra(COLUMN_USER_PASSWORD, password_getLocation);
                            i.setClass(MainActivity.this, MapsActivity.class);
                            startActivity(i);
                        } else {
                            //帳密不存在，應重新輸入帳密以登入、查詢手機位置或註冊
                        }
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        findView();

    }

    private void findView() {
        radioBtn_logIn = findViewById(R.id.radioBtn_logIn);
        radioBtn_signUp = findViewById(R.id.radioBtn_signUp);
        radioBtn_getLocation = findViewById(R.id.radioBtn_getLocation);

        edTxt_account = findViewById(R.id.edTxt_account);
        edTxt_password = findViewById(R.id.edTxt_password);
        btn_enter = findViewById(R.id.btn_enter);

        btn_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String account = edTxt_account.getText().toString();
                String password = edTxt_password.getText().toString();

                if (account.equals("") || password.equals("")) {
                    Toast.makeText(MainActivity.this, "請輸入帳密", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        if (radioBtn_logIn.isChecked()) {
                            //向 Server 驗證該帳密
                            String code = connectDb.logIn(account, password);
                            //根據回傳值，得知目的成功與否
                            if (responseCode.checkCode(code)) {
                                dbHelper.addUser(account, password, VALUE_USER_USEDFOR_SENDLOCATION);
                            } else {
                                //驗證帳密失敗
                            }
                        } else if (radioBtn_signUp.isChecked()) {
                            //向 Server 註冊該帳密
                            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
                                }
                                return;
                            } else {
//                                String imei = telephonyManager.getDeviceId();
                                String imei = "000004";
                                String model = Build.MODEL;

                                String code = null;
                                try {
                                    code = connectDb.signUp(account, password, model, imei);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                //根據回傳值，得知目的成功與否
                                if (responseCode.checkCode(code)) {
                                    dbHelper.addUser(account, password, VALUE_USER_USEDFOR_SENDLOCATION);
                                } else {
                                    //註冊帳密失敗
                                }
                            }

                        } else if (radioBtn_getLocation.isChecked()) {
                            //向 Server 驗證該帳密
                            String code = connectDb.logIn(account, password);
                            //根據回傳值，得知目的成功與否
                            if (responseCode.checkCode(code)) {
                                dbHelper.addUser(account, password, VALUE_USER_USEDFOR_GETLOCATION);
                                startActivity(new Intent(MainActivity.this, MapsActivity.class));
                            } else {
                                //驗證帳密失敗
                            }
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
