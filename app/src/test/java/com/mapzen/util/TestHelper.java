package com.mapzen.util;

import android.support.v4.app.FragmentManager;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.fragment.MapFragment;

import org.oscim.map.TestMap;
import org.robolectric.Robolectric;
import org.robolectric.tester.android.view.TestMenu;

public final class TestHelper {
    private TestHelper() {
    }

    public static BaseActivity initBaseActivity() {
        BaseActivity activity = Robolectric.buildActivity(BaseActivity.class).create().get();
        activity.onCreateOptionsMenu(new TestMenu());
        activity.registerMapView(new TestMap());
        return activity;
    }

    public static MapFragment initMapFragment(BaseActivity activity) {
        FragmentManager manager = activity.getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) manager.findFragmentById(R.id.map_fragment);
        mapFragment.setAct(activity);
        mapFragment.setMap(new TestMap());
        mapFragment.onStart();
        return mapFragment;
    }
}
