package com.cauliflower.danielt.smartphoneradar.UI;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.Service.RadarService;
import com.cauliflower.danielt.smartphoneradar.Tool.ConnectDb;
import com.cauliflower.danielt.smartphoneradar.Tool.MyDbHelper;
import com.cauliflower.danielt.smartphoneradar.Tool.ResponseCode;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
/*
* 考慮使用 FB 做為登入媒介
* 但 FB 也是綁定手機的，當手機遺失時，在其他手機登入 FB，需要原手機取得驗證，
* 因此暫不考慮使用 FB 登入*/


public class SignUpActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 1;
    private EditText edTxt_account, edTxt_password;
    private Button btn_signUp;

    private MyDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(
//                    this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_LOCATION);
//        } else {
//            startRadar();
//        }

        setContentView(R.layout.activity_sign_up);
        findView();

    }

    private void findView() {
        edTxt_account = findViewById(R.id.edTxt_account);
        edTxt_password = findViewById(R.id.edTxt_password);
        btn_signUp = findViewById(R.id.btn_signUp);

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String account = edTxt_account.getText().toString();
                String password = edTxt_password.getText().toString();

                if (account == null || password == null) {
                    Toast.makeText(SignUpActivity.this, "請輸入帳密", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        //向 Server 註冊該帳密
                        String response_code = signUp(account, password);

                        ResponseCode responseCode = new ResponseCode(SignUpActivity.this, response_code);
                        //根據回傳值，得知目的成功與否
                        if (responseCode.checkCode()) {
                            ContentValues values = new ContentValues();
                            values.put("account", account);
                            values.put("password", password);
                            MyDbHelper dbHelper = new MyDbHelper(
                                    SignUpActivity.this, "SmartphoneRadar.db", null, 1);
                            long id = dbHelper.getWritableDatabase().insert("user", null, values);
                            Log.i("Insert user", id + "");
                        } else {
                            //註冊帳密失敗
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public String signUp(String account, String password) throws
            UnsupportedEncodingException {

        String params = "account=" + URLEncoder.encode(account, "UTF-8") +
                "&password=" + URLEncoder.encode(password, "UTF-8") +
                "&action=" + URLEncoder.encode("signUp", "UTF-8") +
                "&";
        Log.i("PARAMS", params);

        ConnectDb connectDb = new ConnectDb();
        String response = connectDb.sendHttpRequest(params);

        Log.i("response", response);
        return response;

    }


    private void startRadar() {
        Intent i = new Intent();
        i.setClass(this, RadarService.class);

        startService(i);
        this.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    //使用者允許權限
                    startRadar();
                } else {
                    //使用者拒絕授權, 停用MyLocation功能
                }
                break;
        }
    }
}
