package com.cauliflower.danielt.smartphoneradar.UI;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.Service.RadarService;
/*
* 考慮使用 FB 做為登入媒介
* 但 FB 也是綁定手機的，當手機遺失時，在其他手機登入 FB，需要原手機取得驗證，
* 因此暫不考慮使用 FB 登入*/


public class SignUpActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            startRadar();
        }

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
