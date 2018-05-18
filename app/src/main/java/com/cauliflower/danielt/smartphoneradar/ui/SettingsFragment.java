
package com.cauliflower.danielt.smartphoneradar.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import com.cauliflower.danielt.smartphoneradar.AccountActivity;
import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.data.PositionPreferences;
import com.cauliflower.danielt.smartphoneradar.obj.User;
import com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper;

import java.util.List;

//import android.support.v7.preference.CheckBoxPreference;
//import android.support.v7.preference.ListPreference;
//import android.support.v7.preference.Preference;
//import android.support.v7.preference.PreferenceFragmentCompat;
//import android.support.v7.preference.PreferenceScreen;

// Create SettingsFragment and extend PreferenceFragment
public class SettingsFragment extends PreferenceFragment implements
        // Implement OnSharedPreferenceChangeListener from SettingsFragment
        SharedPreferences.OnSharedPreferenceChangeListener {
    String account_sendLocation, password_sendLocation;
    String account_getLocation, password_getLocation;

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

        setPreferenceEnable();

        //Set default value for enable positioning
        Preference position = findPreference(getString(R.string.pref_key_position));
        SwitchPreference switchPreference = (SwitchPreference) position;
        //The default value depends on RadarService is inService or not
//        switchPreference.setChecked(RadarService.inService);
        switchPreference.setChecked(PositionPreferences.getPositionEnable(getActivity()));

        //Set default value for updateFrequency
        Preference frequency = findPreference(getString(R.string.pref_key_updateFrequency));
        ListPreference listPreference = (ListPreference) frequency;
        listPreference.setValue(PositionPreferences.getUpdateFrequency(getActivity()));
        setPreferenceSummary(frequency, PositionPreferences.getUpdateFrequency(getActivity()));
    }
    // Override onSharedPreferenceChanged to update non SwitchPreferences when they are changed

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (null != preference) {
            if (preference instanceof ListPreference) {
                setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
            } else {
                //Start and stop RadarService

                if (sharedPreferences.getBoolean(key, false)) {
                    PositionPreferences.startRadarService(getActivity());
                } else {
                    PositionPreferences.stopRadarService(getActivity());
                }
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if (key.equals(getString(R.string.pref_key_MapsActivity))) {
            if (account_getLocation != null) {
                Intent i = new Intent();
                i.putExtra(MyDbHelper.COLUMN_USER_ACCOUNT, account_getLocation);
                i.putExtra(MyDbHelper.COLUMN_USER_PASSWORD, password_getLocation);
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

    private void setPreferenceEnable() {
        //若存在該帳號則可點擊該 Preference
        MyDbHelper myDbHelper = new MyDbHelper(getActivity());

        List<User> userList_sendLocation = myDbHelper.searchUser(MyDbHelper.VALUE_USER_USEDFOR_SENDLOCATION);
        for (User user : userList_sendLocation) {
            account_sendLocation = user.getAccount();
            password_sendLocation = user.getPassword();
            if (account_sendLocation != null) {
                Preference p = findPreference(getString(R.string.pref_key_position));
                p.setEnabled(true);
                setPreferenceSummary(p, account_sendLocation);
                findPreference(getString(R.string.pref_key_updateFrequency)).setEnabled(true);
                break;
            }
        }
        List<User> userList_getLocation = myDbHelper.searchUser(MyDbHelper.VALUE_USER_USEDFOR_GETLOCATION);
        for (User user : userList_getLocation) {
            String in_use = user.getIn_use();
            if (in_use.equals(MyDbHelper.VALUE_USER_IN_USE_YES)) {
                account_getLocation = user.getAccount();
                password_getLocation = user.getPassword();
                Preference p = findPreference(getString(R.string.pref_key_MapsActivity));
                p.setEnabled(true);
                setPreferenceSummary(p, account_getLocation);
                break;
            }
        }
        myDbHelper.close();
    }
}