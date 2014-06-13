package com.mapzen.core;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends PreferenceFragment {
    public static final String TAG = SettingsFragment.class.getSimpleName();

    private BaseActivity activity;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    public static SettingsFragment newInstance(BaseActivity activity) {
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.setActivity(activity);
        return settingsFragment;
    }

    public void setActivity(BaseActivity activity) {
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.preference_list_fragment, container, false);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onStart() {
        super.onStart();
        activity.hideActionBar();
        updateDisplayValues();
        initSharedPrefsListener();
    }

    private void updateDisplayValues() {
        displayValue(R.string.settings_zoom_walking_key, R.integer.zoom_walking);
        displayValue(R.string.settings_zoom_biking_key, R.integer.zoom_biking);
        displayValue(R.string.settings_zoom_driving_0to15_key, R.integer.zoom_driving_0to15);
        displayValue(R.string.settings_zoom_driving_0to15_key, R.integer.zoom_driving_0to15);
        displayValue(R.string.settings_zoom_driving_15to25_key, R.integer.zoom_driving_15to25);
        displayValue(R.string.settings_zoom_driving_25to35_key, R.integer.zoom_driving_25to35);
        displayValue(R.string.settings_zoom_driving_35to50_key, R.integer.zoom_driving_35to50);
        displayValue(R.string.settings_zoom_driving_over50_key, R.integer.zoom_driving_over50);

        displayValue(R.string.settings_turn_walking_key, R.integer.turn_walking);
        displayValue(R.string.settings_turn_biking_key, R.integer.turn_biking);
        displayValue(R.string.settings_turn_driving_0to15_key, R.integer.turn_driving_0to15);
        displayValue(R.string.settings_turn_driving_0to15_key, R.integer.turn_driving_0to15);
        displayValue(R.string.settings_turn_driving_15to25_key, R.integer.turn_driving_15to25);
        displayValue(R.string.settings_turn_driving_25to35_key, R.integer.turn_driving_25to35);
        displayValue(R.string.settings_turn_driving_35to50_key, R.integer.turn_driving_35to50);
        displayValue(R.string.settings_turn_driving_over50_key, R.integer.turn_driving_over50);
    }

    private void initSharedPrefsListener() {
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                updateDisplayValues();
            }
        };

        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(listener);
    }

    private void displayValue(int keyId, int defaultValueId) {
        final String key = getString(keyId);
        final Preference pref = findPreference(key);
        final int defaultValue = getResources().getInteger(defaultValueId);
        final int value = getPreferenceManager().getSharedPreferences().getInt(key, defaultValue);

        pref.setSummary(Integer.toString(value));
    }

    @Override
    public void onStop() {
        super.onStop();
        activity.showActionBar();
    }
}

