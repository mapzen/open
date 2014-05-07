package com.mapzen.core;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends PreferenceFragment {
    public static final String TAG = SettingsFragment.class.getSimpleName();
    BaseActivity activity;

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
    }

    @Override
    public void onStop() {
        super.onStop();
        activity.showActionBar();
    }
}
