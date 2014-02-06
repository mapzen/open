package com.mapzen.support;

import android.support.v4.app.FragmentManager;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Feature;
import com.mapzen.fragment.MapFragment;

import org.apache.commons.io.FileUtils;
import org.oscim.map.TestMap;
import org.robolectric.tester.android.view.TestMenu;

import java.io.File;

import static com.mapzen.entity.Feature.NAME;
import static org.robolectric.Robolectric.buildActivity;

public final class TestHelper {

    public static final String MOCK_ROUTE_JSON = TestHelper.getFixture("basic_route");

    private TestHelper() {
    }

    public static TestBaseActivity initBaseActivity() {
        return initBaseActivity(new TestMenu());
    }

    public static TestBaseActivity initBaseActivity(TestMenu menu) {
        TestBaseActivity activity = buildActivity(TestBaseActivity.class).create().visible().get();
        activity.onCreateOptionsMenu(menu);
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

    public static Feature getTestFeature() {
        Feature feature = new Feature();
        feature.setLat(1.0);
        feature.setLon(1.0);
        feature.setProperty(NAME, "test feature");
        return feature;
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
