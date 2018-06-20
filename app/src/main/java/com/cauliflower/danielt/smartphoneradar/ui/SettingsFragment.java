
package com.cauliflower.danielt.smartphoneradar.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.data.PositionPreferences;
import com.cauliflower.danielt.smartphoneradar.data.RadarContract;
import com.cauliflower.danielt.smartphoneradar.obj.User;
import com.cauliflower.danielt.smartphoneradar.data.RadarDbHelper;
import com.cauliflower.danielt.smartphoneradar.tool.ConnectServer;
import com.cauliflower.danielt.smartphoneradar.tool.NetworkUtils;

import java.util.List;

// Create SettingsFragment and extend PreferenceFragment
public class SettingsFragment extends PreferenceFragment implements
        // Implement OnSharedPreferenceChangeListener from SettingsFragment
        SharedPreferences.OnSharedPreferenceChangeListener {
    String mAccount_sendLocation, mPassword_sendLocation;
    String mAccount_getLocation, mPassword_getLocation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }

    // Unregister SettingsFragment (this) as a SharedPreferenceChangedListener in onStop
    @Override
    public void onStop() {
        super.onStop();
        /* Unregister the preference change listener */
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    // Register SettingsFragment (this) as a SharedPreferenceChangedListener in onStart
    @Override
    public void onStart() {
        super.onStart();
        /* Register the preference change listener */
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        new CheckServerOnlineTask().execute();
    }

    // Override onSharedPreferenceChanged to update non SwitchPreferences when they are changed
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (null != preference) {
            if (preference instanceof ListPreference) {
                setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
            } else {
                //Start RadarService if turn on the switch
                if (sharedPreferences.getBoolean(key, false)) {
                    //If network not connected ,alert the user that position feature will not work
                    if (!NetworkUtils.checkNetworkConnected(getActivity())) {
                        new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_ops)
                                .setMessage(R.string.dialog_msg_noInternetConnected)
                                .setCancelable(true)
                                .create().show();
                    }
                    PositionPreferences.startRadarService(getActivity());
                } else {
                    //Stop RadarService if turn off the switch
                    PositionPreferences.stopRadarService(getActivity());
                }
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if (key.equals(getString(R.string.pref_key_MapsActivity))) {
            if (mAccount_getLocation != null) {
                Intent i = new Intent();
                i.putExtra(RadarContract.UserEntry.COLUMN_USER_ACCOUNT, mAccount_getLocation);
                i.putExtra(RadarContract.UserEntry.COLUMN_USER_PASSWORD, mPassword_getLocation);
                i.setClass(getActivity(), MapsActivity.class);
                startActivity(i);
            }
        } else if (key.equals(getString(R.string.pref_key_AccountActivity))) {
            Intent i = new Intent();
            i.setClass(getActivity(), AccountActivity.class);
            startActivity(i);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    // Create a method called setPreferenceSummary that accepts a Preference and
    // an Object and sets the summary of the preference
    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();
//        String key = preference.getKey();

        if (preference instanceof ListPreference) {
            /* For list preferences, look up the correct display value in */
            /* the preference's 'entries' list (since they have separate labels/values). */
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
    }

    //設定 Preference 的 enable、checked、summary
    private void initPreference() {
        RadarDbHelper radarDbHelper = new RadarDbHelper(getActivity());

        //MapsActivity
        List<User> userList_getLocation = radarDbHelper.searchUser(RadarContract.UserEntry.USED_FOR_GETLOCATION);
        for (User user : userList_getLocation) {
            String in_use = user.getIn_use();
            if (in_use.equals(RadarContract.UserEntry.IN_USE_YES)) {
                mAccount_getLocation = user.getAccount();
                mPassword_getLocation = user.getPassword();
                Preference p = findPreference(getString(R.string.pref_key_MapsActivity));
                p.setEnabled(true);
                setPreferenceSummary(p, mAccount_getLocation);
                break;
            }
        }

        //UpdateFrequencyList
        Preference frequency = findPreference(getString(R.string.pref_key_updateFrequency));
        ListPreference listPreference = (ListPreference) frequency;
        listPreference.setValue(PositionPreferences.getUpdateFrequency(getActivity()));
        setPreferenceSummary(frequency, PositionPreferences.getUpdateFrequency(getActivity()));

        //PositionSwitch
        Preference position = findPreference(getString(R.string.pref_key_position));
        SwitchPreference switchPreference = (SwitchPreference) position;
        //設定“定位開關”的開關值
        switchPreference.setChecked(PositionPreferences.getPositionEnable(getActivity()));
//        switchPreference.setChecked(RadarService.inService);
        List<User> userList_sendLocation = radarDbHelper.searchUser(RadarContract.UserEntry.USED_FOR_SENDLOCATION);
        for (User user : userList_sendLocation) {
            mAccount_sendLocation = user.getAccount();
            mPassword_sendLocation = user.getPassword();
            if (mAccount_sendLocation != null) {
                //若存在帳號則可點擊“定位開關”
                position.setEnabled(true);
                setPreferenceSummary(position, mAccount_sendLocation);
                findPreference(getString(R.string.pref_key_updateFrequency)).setEnabled(true);
                break;
            }
        }
        radarDbHelper.close();

        if (!mServerOnline) {
            findPreference(getString(R.string.pref_key_AccountActivity)).setEnabled(false);
            findPreference(getString(R.string.pref_key_MapsActivity)).setEnabled(false);
            findPreference(getString(R.string.pref_key_position)).setEnabled(false);

        }
    }

    private boolean mServerOnline = false;

    /*檢查伺服器連線狀況*/
    class CheckServerOnlineTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog mLoadingDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mServerOnline = false;
            mLoadingDialog = new ProgressDialog(getActivity());
            mLoadingDialog.setMessage(getString(R.string.dialog_msg_loading));
            mLoadingDialog.setCancelable(false);
            mLoadingDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return ConnectServer.checkServerOnline();
        }

        @Override
        protected void onPostExecute(Boolean online) {
            super.onPostExecute(online);
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
            mServerOnline = online;
            //連不上伺服器
            if (!mServerOnline) {
                //提示使用者伺服器關閉中
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.dialog_title_ops)
                        .setMessage(R.string.dialog_msg_server_closed)
                        .setCancelable(true)
                        .create().show();
            }
            initPreference();
        }
    }
}