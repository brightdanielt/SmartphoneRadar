package com.cauliflower.danielt.smartphoneradar;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {
    private MyDbHelper dbHelper;
    private EditText edTxt_account, edTxt_password;
    private Button btn_enter;
    private RadioButton radioBtn_logIn, radioBtn_signUp, radioBtn_getLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new MyDbHelper(
                MainActivity.this, "SmartphoneRadar.db", null, 1);
        //查詢是否已註冊
        Cursor cursor = dbHelper.getReadableDatabase().query(
                "user", null, "_id=?", new String[]{"1"}, null, null, null);
        int index_account = cursor.getColumnIndex("account");
        int index_password = cursor.getColumnIndex("password");
        String account = cursor.getString(index_account);
        String password = cursor.getString(index_password);

        //存在帳密，已註冊
        if (account != null && password != null) {
            try {
                //要求 Server 驗證該組帳密
                String code = logIn(account, password);
                ResponseCode responseCode = new ResponseCode(MainActivity.this, code);
                //根據回傳值，得知目的成功與否
                if (responseCode.checkCode()) {
                    //帳密存在，轉跳追蹤設定頁面
                } else {
                    //帳密不存在，應登入、查詢手機位置或註冊

                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {

        }

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

                if (account == null || password == null) {
                    Toast.makeText(MainActivity.this, "請輸入帳密", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        if (radioBtn_logIn.isSelected()) {
                            //向 Server 驗證該帳密
                            String response_code = logIn(account, password);
                            ResponseCode responseCode = new ResponseCode(MainActivity.this, response_code);
                            //根據回傳值，得知目的成功與否
                            if (responseCode.checkCode()) {
                                ContentValues values = new ContentValues();
                                values.put("account", account);
                                values.put("password", password);
                                MyDbHelper dbHelper = new MyDbHelper(
                                        MainActivity.this, "SmartphoneRadar.db", null, 1);
                                long id = dbHelper.getWritableDatabase().insert("user", null, values);
                                Log.i("Insert user", id + "");

                            } else {
                                //驗證帳密失敗
                            }
                        } else if (radioBtn_signUp.isSelected()) {
                            //向 Server 註冊該帳密
                            String response_code = signUp(account, password);
                            ResponseCode responseCode = new ResponseCode(MainActivity.this, response_code);
                            //根據回傳值，得知目的成功與否
                            if (responseCode.checkCode()) {
                                ContentValues values = new ContentValues();
                                values.put("account", account);
                                values.put("password", password);
                                MyDbHelper dbHelper = new MyDbHelper(
                                        MainActivity.this, "SmartphoneRadar.db", null, 1);
                                long id = dbHelper.getWritableDatabase().insert("user", null, values);
                                Log.i("Insert user", id + "");
                            } else {
                                //註冊帳密失敗
                            }
                        } else if (radioBtn_getLocation.isSelected()) {
                            //向 Server 驗證該帳密
                            String response_code = logIn(account, password);
                            ResponseCode responseCode = new ResponseCode(MainActivity.this, response_code);
                            //根據回傳值，得知目的成功與否
                            if (responseCode.checkCode()) {
                                ContentValues values = new ContentValues();
                                values.put("account", account);
                                values.put("password", password);
                                MyDbHelper dbHelper = new MyDbHelper(
                                        MainActivity.this, "SmartphoneRadar.db", null, 1);
                                long id = dbHelper.getWritableDatabase().insert("user", null, values);
                                Log.i("Insert user", id + "");

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

    //logIn 用於驗證該組帳密是否存在
    public String logIn(String account, String password) throws
            UnsupportedEncodingException {


        String params = "account=" + URLEncoder.encode(account, "UTF-8") +
                "&password=" + URLEncoder.encode(password, "UTF-8") +
                "&action=" + URLEncoder.encode("login", "UTF-8") +
                "&";
        Log.i("PARAMS", params);

        ConnectDb connectDb = new ConnectDb();
        String response = connectDb.sendHttpRequest(params);

        Log.i("response", response);
        return response;

    }
}
