
package com.cauliflower.danielt.smartphoneradar.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.data.PositionPreferences;

//import android.support.v7.preference.CheckBoxPreference;
//import android.support.v7.preference.ListPreference;
//import android.support.v7.preference.Preference;
//import android.support.v7.preference.PreferenceFragmentCompat;
//import android.support.v7.preference.PreferenceScreen;

// Create SettingsFragment and extend PreferenceFragment
public class SettingsFragment extends PreferenceFragment implements
        // Implement OnSharedPreferenceChangeListener from SettingsFragment
        SharedPreferences.OnSharedPreferenceChangeListener {

    // Create a method called setPreferenceSummary that accepts a Preference and an Object and sets the summary of the preference
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


}