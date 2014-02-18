package com.mapzen.activity;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.entity.Feature;
import com.mapzen.fragment.ListResultsFragment;
import com.mapzen.search.PagerResultsFragment;
import com.mapzen.shadows.ShadowLocationClient;
import com.mapzen.shadows.ShadowVolley;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestBaseActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.tester.android.view.TestMenu;

import java.util.ArrayList;

import static com.mapzen.MapController.getMapController;
import static com.mapzen.support.TestHelper.initBaseActivityWithMenu;
import static com.mapzen.support.TestHelper.initMapFragment;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class BaseActivityTest {
    private BaseActivity activity;
    private ShadowLocationClient shadowLocationClient;
    private TestMenu menu;

    @Before
    public void setUp() throws Exception {
        ShadowVolley.clearMockRequestQueue();
        menu = new TestMenu();
        activity = initBaseActivityWithMenu(menu);
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
    public void onPause_shouldCloseDB() throws Exception {
        activity.onPause();
        assertThat(activity.getDb()).isNotOpen();
    }

    @Test
    public void onResume_shouldReConnectLocationClient() throws Exception {
        shadowLocationClient.disconnect();
        activity.onResume();
        assertThat(shadowLocationClient.isConnected()).isTrue();
    }

    @Test
    public void onResume_shouldGetWritableLocationDatabase() throws Exception {
        assertThat(activity.getDb()).isOpen();
    }

    @Test
    public void onPrepareOptionsMenu_shouldHideSearchWhenResultsVisible() throws Exception {
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(new Feature());
        Fragment fragment = ListResultsFragment.newInstance(activity, features);
        activity.getSupportFragmentManager().beginTransaction()
                .add(fragment, ListResultsFragment.TAG)
                .commit();

        Menu menu = new TestMenu();
        activity.onCreateOptionsMenu(menu);
        activity.onPrepareOptionsMenu(menu);
        assertThat(menu.findItem(R.id.search)).isNotVisible();
    }

    @Test
    public void onMenuItemActionCollapse_shouldPopPagerResultsFragment() throws Exception {
        activity.executeSearchOnMap("query");
        menu.findItem(R.id.search).collapseActionView();
        assertThat(activity.getSupportFragmentManager())
                .doesNotHaveFragmentWithTag(PagerResultsFragment.TAG);
    }

    @Test
    public void onOptionsItemSelected_shouldToggleDebugMode() throws Exception {
        MenuItem menuItem = menu.findItem(R.id.debug_toggle);
        activity.onOptionsItemSelected(menuItem);
        assertThat(activity.isInDebugMode()).isTrue();
        activity.onOptionsItemSelected(menuItem);
        assertThat(activity.isInDebugMode()).isFalse();
    }

    @Test
    public void onOptionsItemSelected_shouldToastDebugModeIndication() throws Exception {
        MenuItem menuItem = menu.findItem(R.id.debug_toggle);
        activity.onOptionsItemSelected(menuItem);
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("debug mode: on");
        activity.onOptionsItemSelected(menuItem);
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("debug mode: off");
    }

    @Test
    public void executeSearchOnMap_shouldCreateNewPagerResultsFragment() throws Exception {
        activity.executeSearchOnMap("query1");
        final PagerResultsFragment fragment1 = (PagerResultsFragment)
                activity.getSupportFragmentManager().findFragmentByTag(PagerResultsFragment.TAG);

        activity.executeSearchOnMap("query2");
        final PagerResultsFragment fragment2 = (PagerResultsFragment)
                activity.getSupportFragmentManager().findFragmentByTag(PagerResultsFragment.TAG);

        assertThat(fragment1).isNotSameAs(fragment2);
    }

    @Test
    public void executeSearchOnMap_shouldReplaceExistingFragment() throws Exception {
        activity.executeSearchOnMap("query1");
        final PagerResultsFragment fragment1 = (PagerResultsFragment)
                activity.getSupportFragmentManager().findFragmentByTag(PagerResultsFragment.TAG);

        activity.executeSearchOnMap("query2");
        final PagerResultsFragment fragment2 = (PagerResultsFragment)
                activity.getSupportFragmentManager().findFragmentByTag(PagerResultsFragment.TAG);

        assertThat(activity.getSupportFragmentManager().findFragmentByTag(PagerResultsFragment.TAG))
                .isNotSameAs(fragment1);
        assertThat(activity.getSupportFragmentManager().findFragmentByTag(PagerResultsFragment.TAG))
                .isSameAs(fragment2);
    }

    @Test
    public void onPoiClick_shouldPagerResultsFragmentCurrentItem() throws Exception {
        PagerResultsFragment pagerResultsFragment = PagerResultsFragment.newInstance(activity);
        startFragment(pagerResultsFragment);
        activity.getSupportFragmentManager().beginTransaction()
                .add(R.id.pager_results_container, pagerResultsFragment,
                        PagerResultsFragment.TAG)
                .commit();

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(new JSONObject());
        jsonArray.put(new JSONObject());
        pagerResultsFragment.setSearchResults(jsonArray);
        pagerResultsFragment.setCurrentItem(0);
        activity.getMapFragment().getOnPoiClickListener().onPoiClick(1, null);
        assertThat(pagerResultsFragment.getCurrentItem()).isEqualTo(1);
    }

    @Test
    public void deactivateMapLocationUpdates_shouldBlockLocationUpdates() throws Exception {
        Location location = new Location("expected");
        Location newLocation = new Location("new expected");
        // TODO activity.getMapFragment().setUserLocation(location);
        activity.getLocationListener().onLocationChanged(location);
        activity.deactivateMapLocationUpdates();
        activity.getLocationListener().onLocationChanged(newLocation);
        // TODO assertThat(activity.getMapFragment().getUserLocation()).isNotEqualTo(newLocation);
    }

    @Test
    public void onLocationChange_shouldUpdateMapController() throws Exception {
        Location expected = new Location("expected");
        activity.getLocationListener().onLocationChanged(expected);
        Location actual = getMapController().getLocation();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void onConnect_shouldUpdateMapController() throws Exception {
        Location expected = new Location("expected");
        shadowLocationClient.setLastLocation(expected);
        GooglePlayServicesClient.ConnectionCallbacks callbacks =
                ((TestBaseActivity) activity).getConnectionCallback();
        callbacks.onConnected(new Bundle());
        Location actual = getMapController().getLocation();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldHaveSuggestionsAdapter() throws Exception {
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        assertThat(searchView.getSuggestionsAdapter()).isNotNull();
    }

    @Test
    public void executeSearchOnMap_shouldRemoveSuggestionsAdapter() throws Exception {
        activity.executeSearchOnMap("query");
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        assertThat(searchView.getSuggestionsAdapter()).isNull();
    }
}
