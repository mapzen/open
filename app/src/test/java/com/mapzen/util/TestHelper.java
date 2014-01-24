package com.mapzen.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentManager;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.fragment.MapFragment;

import org.oscim.map.TestMap;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.tester.android.view.TestMenu;

public class TestHelper {
    public static void simulateLocation(double lat, double lon) {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setProvider(LocationManager.GPS_PROVIDER);

        LocationManager manager = (LocationManager)
                Robolectric.application.getSystemService(Context.LOCATION_SERVICE);
        ShadowLocationManager shadowManager = Robolectric.shadowOf(manager);
        shadowManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true);
        shadowManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
        shadowManager.setProviderEnabled(LocationManager.PASSIVE_PROVIDER, true);
        shadowManager.simulateLocation(location);
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
