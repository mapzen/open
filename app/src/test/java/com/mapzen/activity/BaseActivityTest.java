package com.mapzen.activity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.widget.SearchView;

import com.mapzen.MapzenApplication;
import com.mapzen.MapzenTestRunner;
import com.mapzen.R;
import com.mapzen.fragment.MapFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscim.map.TestMap;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.tester.android.view.TestMenu;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;

@RunWith(MapzenTestRunner.class)
public class BaseActivityTest {
    private BaseActivity activity;

    @Before
    public void setUp() throws Exception {
        simulateLocation(0, 0);
        activity = Robolectric.buildActivity(BaseActivity.class).create().get();
        activity.registerMapView(new TestMap());
        FragmentManager manager = activity.getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) manager.findFragmentById(R.id.map_fragment);
        mapFragment.setAct(activity);
        mapFragment.setMap(new TestMap());
        mapFragment.onStart();
    }

    @After
    public void tearDown() throws Exception {
        // TODO: Mock network requests so cleanup is not needed.
        ((MapzenApplication) application).cancelAllApiRequests();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(activity).isNotNull();
    }

    @Test
    public void geoIntent_shouldSetCurrentSearchTerm() throws Exception {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("geo:0,0?q=Empire State Building"));
        activity.setIntent(intent);
        activity.onCreateOptionsMenu(new TestMenu());
        String currentSearchTerm = ((MapzenApplication) application).getCurrentSearchTerm();
        assertThat(currentSearchTerm).isEqualTo("Empire State Building");
    }

    @Test
    public void geoIntent_shouldSetQuery() throws Exception {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("geo:0,0?q=Empire State Building"));
        Menu menu = new TestMenu();
        activity.setIntent(intent);
        activity.onCreateOptionsMenu(menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        assertThat(searchView.getQuery().toString()).isEqualTo("Empire State Building");
    }

    @Test
    public void mapsIntent_shouldSetCurrentSearchTerm() throws Exception {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?z=16&"
                        + "q=Empire State Building@40.74828,-73.985565"));
        activity.setIntent(intent);
        activity.onCreateOptionsMenu(new TestMenu());
        String currentSearchTerm = ((MapzenApplication) application).getCurrentSearchTerm();
        assertThat(currentSearchTerm).isEqualTo("Empire State Building");
    }

    @Test
    public void mapsIntent_shouldSetQuery() throws Exception {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?z=16&"
                        + "q=Empire State Building@40.74828,-73.985565"));
        Menu menu = new TestMenu();
        activity.setIntent(intent);
        activity.onCreateOptionsMenu(menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        assertThat(searchView.getQuery().toString()).isEqualTo("Empire State Building");
    }

    public static void simulateLocation(double lng, double lat) {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLongitude(lng);
        location.setLatitude(lat);
        location.setProvider(LocationManager.GPS_PROVIDER);

        LocationManager manager = (LocationManager)
                application.getSystemService(Context.LOCATION_SERVICE);
        ShadowLocationManager shadowManager = Robolectric.shadowOf(manager);
        shadowManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true);
        shadowManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
        shadowManager.setProviderEnabled(LocationManager.PASSIVE_PROVIDER, true);
        shadowManager.simulateLocation(location);
    }
}
