package com.mapzen.util;

import android.support.v4.app.FragmentManager;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.fragment.MapFragment;

import org.oscim.map.TestMap;
import org.robolectric.Robolectric;
import org.robolectric.tester.android.view.TestMenu;

import org.apache.commons.io.FileUtils;

import java.io.File;

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

    public static String getFixture(String name) {
        String basedir = System.getProperty("user.dir");
        File file = new File(basedir + "/src/test/fixtures/" + name + ".fixture");
        String fixture = "";
        try {
            fixture = FileUtils.readFileToString(file, "UTF-8");
        } catch (Exception e) {
            fixture = "not found";
        }
        return fixture;
    }
}
