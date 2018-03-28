package com.cauliflower.danielt.smartphoneradar;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.cauliflower.danielt.smartphoneradar.Tool.ConnectDb;
import com.cauliflower.danielt.smartphoneradar.Tool.MyDbHelper;
import com.cauliflower.danielt.smartphoneradar.Tool.ResponseCode;
import com.cauliflower.danielt.smartphoneradar.UI.SignUpActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    private MyDbHelper dbHelper;

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

        if (account != null && password != null) {
            try {
                //要求 Server 驗證該組帳密
                String code = login(account, password);
                ResponseCode responseCode = new ResponseCode(MainActivity.this, code);
                //根據回傳值，得知目的成功與否
                if (responseCode.checkCode()) {
                    //帳密存在
                } else {
                    //帳密不存在，轉跳註冊頁面
                    Intent i =new Intent();
                    i.setClass(MainActivity.this,SignUpActivity.class);
                    startActivity(i);
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {

        }

    }

    public String login(String account, String password) throws
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
