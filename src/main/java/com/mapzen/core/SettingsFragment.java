package com.mapzen.core;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.util.MapzenTheme;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class SettingsFragment extends PreferenceFragment {
    public static final String TAG = SettingsFragment.class.getSimpleName();
    BaseActivity activity;
    ArrayList<String> styleEntries = new ArrayList<String>();
    ArrayList<String> styleValues = new ArrayList<String>();

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
        initStyleListPreferences();
        initStyleListener();
    }

    private void initStyleListPreferences() {
        ListPreference listPreference =
                (ListPreference) findPreference(getString(R.string.settings_key_mapstyle));
        if (listPreference != null) {
            for (MapzenTheme value : MapzenTheme.values()) {
                styleEntries.add(value.toString());
                styleValues.add(MapzenTheme.valueOf(value.toString()).toString());
            }
            listPreference.setEntries(getStyleEntries());
            listPreference.setEntryValues(getStyleValues());
        }
    }

    private void initStyleListener() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        prefs.registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                            String key) {
                        if (key.equals(getString(R.string.settings_key_mapstyle))) {
                            String stylesheet = sharedPreferences.getString(key,
                                    getString(R.string.settings_default_mapstyle));
                            activity.getMapFragment().setTheme(MapzenTheme.valueOf(stylesheet));
                        }
                    }
                });
    }

    private CharSequence[] getStyleEntries() {
        return styleEntries.toArray(new CharSequence[styleEntries.size()]);
    }

    private CharSequence[] getStyleValues() {
        return styleValues.toArray(new CharSequence[styleValues.size()]);
    }

    @Override
    public void onStart() {
        super.onStart();
        activity.hideActionBar();
    }

    @Override
    public void onStop() {
        super.onStop();
        activity.showActionBar();
    }
}
