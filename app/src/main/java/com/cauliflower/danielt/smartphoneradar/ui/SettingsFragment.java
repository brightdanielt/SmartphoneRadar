
package com.cauliflower.danielt.smartphoneradar.ui;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.data.MainDb;
import com.cauliflower.danielt.smartphoneradar.data.RadarPreferences;
import com.cauliflower.danielt.smartphoneradar.data.RadarContract;
import com.cauliflower.danielt.smartphoneradar.obj.User;
import com.cauliflower.danielt.smartphoneradar.service.NetWatcherJob;
import com.cauliflower.danielt.smartphoneradar.service.RadarService;
import com.cauliflower.danielt.smartphoneradar.network.ConnectServer;
import com.cauliflower.danielt.smartphoneradar.network.NetworkUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

// Create SettingsFragment and extend PreferenceFragment
public class SettingsFragment extends PreferenceFragment implements
        // Implement OnSharedPreferenceChangeListener from SettingsFragment
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();
    private static final int LOADER_ID = 1;
    private FirebaseAuth mAuth;
    String mEmail_targetTracked, mPassword_targetTracked;

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
        mAuth = FirebaseAuth.getInstance();
        /* Register the preference change listener */
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        initPreference();
    }

    // Override onSharedPreferenceChanged to update non SwitchPreferences when they are changed
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (null != preference) {
            if (preference instanceof ListPreference) {
                setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
                showDialogSettingWorkNextTime();
            } else {
                //Start RadarService if turn on the switch
                if (sharedPreferences.getBoolean(key, false)) {
                    //If network not connected ,alert the user that RadarService will not work
                    if (!NetworkUtils.checkNetworkConnected(getActivity())) {
                        new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_ops)
                                .setMessage(R.string.dialog_msg_enablePosition_noInternetConnected)
                                .setCancelable(true)
                                .create().show();
                    }
                    //Check android api level
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        scheduleNetWatcherJob();
                    } else {
                        //For apps targeting M and below
                        RadarPreferences.startRadarService(getActivity());
                    }
                } else {
                    if (RadarService.mInService) {
                        //Stop RadarService if turn off the switch
                        RadarPreferences.stopRadarService(getActivity());
                    }

                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        cancelScheduledNetWatcherJob();
                    }
                }
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if (key.equals(getString(R.string.pref_key_MapsActivity))) {
            if (mEmail_targetTracked != null) {
                Intent i = new Intent();
                i.putExtra(RadarContract.UserEntry.COLUMN_USER_EMAIL, mEmail_targetTracked);
                i.putExtra(RadarContract.UserEntry.COLUMN_USER_PASSWORD, mPassword_targetTracked);
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
        findPreference(getString(R.string.pref_key_AccountActivity)).setEnabled(true);
        findPreference(getString(R.string.pref_key_updateFrequency)).setEnabled(false);

        //MapsActivity
        //向資料庫查詢追蹤目標
        List<User> userList_targetTracked = MainDb.searchUser(getActivity(), RadarContract.UserEntry.USED_FOR_GETLOCATION);
        for (User targetTracked : userList_targetTracked) {
            //存在追蹤目標
            String in_use = targetTracked.getIn_use();
            if (in_use.equals(RadarContract.UserEntry.IN_USE_YES)) {
                mEmail_targetTracked = targetTracked.getEmail();
                mPassword_targetTracked = targetTracked.getPassword();
                Preference p = findPreference(getString(R.string.pref_key_MapsActivity));
                //允許開啟 MapsActivity 以追蹤目標
                p.setEnabled(true);
                setPreferenceSummary(p, mEmail_targetTracked);
                break;
            }
        }

        //定位頻率
        Preference frequency = findPreference(getString(R.string.pref_key_updateFrequency));
        ListPreference listPreference = (ListPreference) frequency;
        listPreference.setValue(RadarPreferences.getUpdateFrequency(getActivity()));
        setPreferenceSummary(frequency, RadarPreferences.getUpdateFrequency(getActivity()));

        //定位開關
        Preference position = findPreference(getString(R.string.pref_key_position));
        SwitchPreference switchPreference = (SwitchPreference) position;
        //設定“定位開關”的開關值
        switchPreference.setChecked(RadarPreferences.getPositionEnable(getActivity()));
//        switchPreference.setChecked(RadarService.inService);

        //取得使用者資訊
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String email = firebaseUser.getEmail().trim();
            //若存在使用者則可設定“定位開關”
            position.setEnabled(true);
            setPreferenceSummary(position, email);
            //若存在使用者則可設定“定位頻率”
            frequency.setEnabled(true);
        }

    }

    private static int NET_WATCHER_JOB_ID = 100;

    /**
     * Schedule a job that register a receiver to monitor network connectivity in background,
     */
    @TargetApi(Build.VERSION_CODES.N)
    private void scheduleNetWatcherJob() {
        JobScheduler jobScheduler = (JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo netWatcherInfo = new JobInfo.Builder(NET_WATCHER_JOB_ID, new ComponentName(getActivity(), NetWatcherJob.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                .setMinimumLatency(1000)
                .setOverrideDeadline(2000)
                //job will be written to disk and loaded at boot
                .setPersisted(true)
                .build();
        if (jobScheduler != null) {
            jobScheduler.schedule(netWatcherInfo);
        } else {
            Log.i(TAG, "Can not get system service: JOB_SCHEDULER_SERVICE while scheduling job");
        }
    }

    /**
     * Cancel the NetWatcher job scheduled
     */
    @TargetApi(Build.VERSION_CODES.N)
    private void cancelScheduledNetWatcherJob() {
        JobScheduler jobScheduler = (JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.cancel(NET_WATCHER_JOB_ID);
        } else {
            Log.i(TAG, "Can not get system service: JOB_SCHEDULER_SERVICE while cancel job");
        }
    }

    //Remind user the modification of setting might work until user start RadarService next time
    private void showDialogSettingWorkNextTime() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.updateSetting)
                .setMessage(R.string.settingWorkNextTime)
                .setCancelable(true)
                .create()
                .show();
    }
}
