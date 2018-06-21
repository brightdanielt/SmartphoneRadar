/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cauliflower.danielt.smartphoneradar.ui;

import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.tool.RequestPermission;

/**
 * SettingsActivity is responsible for displaying the {@link SettingsFragment}. It is also
 * responsible for orchestrating proper navigation when the up button is clicked. When the up
 * button is clicked from the SettingsActivity, we want to navigate to the Activity that the user
 * came from to get to the SettingsActivity.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 100;
    private static final int REQUEST_CODE_LOCATION_SETTING = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_activity_menu, menu);
        return true;
    }

    /**
     * 點擊Menu item時呼叫 SettingsFragment的
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refreshAll) {
            FragmentManager manager = getFragmentManager();
            SettingsFragment settingsFragment = (SettingsFragment) manager.findFragmentById(R.id.radar_settings_fragment);
//        settingsFragment.onOptionsItemSelected(item);
            settingsFragment.refreshAll();
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        RequestPermission.accessFineLocation(this, REQUEST_CODE_ACCESS_FINE_LOCATION);
        RequestPermission.displayLocationSettingsRequest(this, REQUEST_CODE_LOCATION_SETTING);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
//                        PositionPreferences.getPositionEnable(SettingsActivity.this)==true
                        ) {
                    //使用者允許權限
                    break;
                } else {
                    //使用者拒絕授權
                    finish();
                }
                break;
        }
    }
}