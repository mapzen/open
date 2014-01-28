package com.mapzen.activity;

import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.widget.SearchView;

import com.mapzen.MapzenApplication;
import com.mapzen.MapzenTestRunner;
import com.mapzen.R;
import com.mapzen.shadows.ShadowLocationClient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.tester.android.view.TestMenu;

import static com.mapzen.util.TestHelper.initBaseActivity;
import static com.mapzen.util.TestHelper.initMapFragment;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;

@RunWith(MapzenTestRunner.class)
public class BaseActivityTest {
    private BaseActivity activity;
    private ShadowLocationClient shadowLocationClient;

    @Before
    public void setUp() throws Exception {
        activity = initBaseActivity();
        shadowLocationClient = Robolectric.shadowOf_(activity.getLocationClient());
        initMapFragment(activity);
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

    @Test
    public void onCreate_shouldConnectLocationClient() throws Exception {
        assertThat(shadowLocationClient.isConnected()).isTrue();
    }

    @Test
    public void onPause_shouldDisconnectLocationClient() throws Exception {
        activity.onPause();
        assertThat(shadowLocationClient.isConnected()).isFalse();
    }

    @Test
    public void onResume_shouldReConnectLocationClient() throws Exception {
        shadowLocationClient.disconnect();
        activity.onResume();
        assertThat(shadowLocationClient.isConnected()).isTrue();
    }
}
