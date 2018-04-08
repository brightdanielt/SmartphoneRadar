package com.cauliflower.danielt.smartphoneradar.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.service.RadarService;
/*
* 考慮使用 FB 做為登入媒介
* 但 FB 也是綁定手機的，當手機遺失時，在其他手機登入 FB，需要原手機取得驗證，
* 因此暫不考慮使用 FB 登入*/


public class SignUpActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 1;
    private EditText edTxt_account, edTxt_password;
    private Button btn_signUp;

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
        edTxt_account = (EditText) findViewById(R.id.edTxt_account);
        edTxt_password = (EditText) findViewById(R.id.edTxt_password);
        btn_signUp = (Button) findViewById(R.id.btn_signUp);

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String account = edTxt_account.getText().toString();
                String password = edTxt_password.getText().toString();

                if (account == null || password == null) {
                    Toast.makeText(SignUpActivity.this, "請輸入帳密", Toast.LENGTH_SHORT).show();
                } else {
                    //向 Server 註冊該帳密

                }
            }
        });
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
