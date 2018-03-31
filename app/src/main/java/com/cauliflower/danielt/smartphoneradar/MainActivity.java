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

import static com.cauliflower.danielt.smartphoneradar.Tool.MyDbHelper.COLUMN_USER_ACCOUNT;
import static com.cauliflower.danielt.smartphoneradar.Tool.MyDbHelper.COLUMN_USER_PASSWORD;
import static com.cauliflower.danielt.smartphoneradar.Tool.MyDbHelper.COLUMN_USER_USEDFOR;
import static com.cauliflower.danielt.smartphoneradar.Tool.MyDbHelper.TABLE_USER;
import static com.cauliflower.danielt.smartphoneradar.Tool.MyDbHelper.VALUE_USER_USEDFOR_GETLOCATION;
import static com.cauliflower.danielt.smartphoneradar.Tool.MyDbHelper.VALUE_USER_USEDFOR_SENDLOCATION;

public class MainActivity extends AppCompatActivity {
    private MyDbHelper dbHelper;
    private ConnectDb connectDb;
    private EditText edTxt_account, edTxt_password;
    private Button btn_enter;
    private RadioButton radioBtn_logIn, radioBtn_signUp, radioBtn_getLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectDb = new ConnectDb(MainActivity.this);

        dbHelper = new MyDbHelper(MainActivity.this);
        //查詢是否已註冊
        Cursor cursor = dbHelper.getReadableDatabase().query(
                TABLE_USER, null, COLUMN_USER_USEDFOR + "=?", new String[]{VALUE_USER_USEDFOR_SENDLOCATION},
                null, null, null);
        int index_account = cursor.getColumnIndex(COLUMN_USER_ACCOUNT);
        int index_password = cursor.getColumnIndex(COLUMN_USER_PASSWORD);
        String account = cursor.getString(index_account);
        String password = cursor.getString(index_password);

        //存在帳密，已註冊
        if (account != null && password != null) {
            try {
                //要求 Server 驗證該組帳密
                String code = connectDb.logIn(account, password);
                ResponseCode responseCode = new ResponseCode(MainActivity.this, code);
                //根據回傳值，得知目的成功與否
                if (responseCode.checkCode()) {
                    //帳密存在，轉跳追蹤設定頁面
                } else {
                    //帳密不存在，應重新輸入帳密以登入、查詢手機位置或註冊
                    findView();
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
                //要求 Server 驗證該組帳密
                String code = connectDb.logIn(account_getLocation, password_getLocation);
                ResponseCode responseCode = new ResponseCode(MainActivity.this, code);
                //根據回傳值，得知目的成功與否
                if (responseCode.checkCode()) {
                    //帳密存在，轉跳google Map追蹤頁面
                    Intent i = new Intent();
                    i.putExtra(COLUMN_USER_ACCOUNT, account_getLocation);
                    i.putExtra(COLUMN_USER_PASSWORD, password_getLocation);
                    i.setClass(MainActivity.this, MapsActivity.class);
                    startActivity(i);
                } else {
                    //帳密不存在，應重新輸入帳密以登入、查詢手機位置或註冊
                    findView();
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
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
                            String response_code = connectDb.logIn(account, password);
                            ResponseCode responseCode = new ResponseCode(MainActivity.this, response_code);
                            //根據回傳值，得知目的成功與否
                            if (responseCode.checkCode()) {
                                dbHelper.addUser(account, password, VALUE_USER_USEDFOR_SENDLOCATION);
                            } else {
                                //驗證帳密失敗
                            }
                        } else if (radioBtn_signUp.isSelected()) {
                            //向 Server 註冊該帳密
                            String response_code = connectDb.signUp(account, password);
                            ResponseCode responseCode = new ResponseCode(MainActivity.this, response_code);
                            //根據回傳值，得知目的成功與否
                            if (responseCode.checkCode()) {
                                dbHelper.addUser(account, password, VALUE_USER_USEDFOR_SENDLOCATION);
                            } else {
                                //註冊帳密失敗
                            }
                        } else if (radioBtn_getLocation.isSelected()) {
                            //向 Server 驗證該帳密
                            String response_code = connectDb.logIn(account, password);
                            ResponseCode responseCode = new ResponseCode(MainActivity.this, response_code);
                            //根據回傳值，得知目的成功與否
                            if (responseCode.checkCode()) {
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
