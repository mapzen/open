package com.mapzen.activity;

import com.mapzen.MapController;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.android.gson.Feature;
import com.mapzen.core.OSMOauthFragment;
import com.mapzen.core.SettingsFragment;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.fragment.ListResultsFragment;
import com.mapzen.search.PagerResultsFragment;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestBaseActivity;
import com.mapzen.support.TestHelper;

import com.google.common.io.Files;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.fest.assertions.data.Offset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.tester.android.view.TestMenu;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static com.mapzen.MapController.getMapController;
import static com.mapzen.android.lost.LocationClient.ConnectionCallbacks;
import static com.mapzen.support.TestHelper.enableDebugMode;
import static com.mapzen.support.TestHelper.getTestFeature;
import static com.mapzen.support.TestHelper.initBaseActivityWithMenu;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class BaseActivityTest {
    private BaseActivity activity;
    private TestMenu menu;

    @Before
    public void setUp() throws Exception {
        menu = new TestMenu();
        activity = initBaseActivityWithMenu(menu);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(activity).isNotNull();
    }

    @Test
    public void onCreate_shouldInitializeMapController() throws Exception {
        assertThat(MapController.getMapController().getMap()).isNotNull();
    }

    @Test
    public void onCreate_shouldSetDataUploadServiceAlarm() throws Exception {
        AlarmManager alarmManager =
                (AlarmManager) Robolectric.application.getSystemService(Context.ALARM_SERVICE);
        ShadowAlarmManager shadowAlarmManager = Robolectric.shadowOf(alarmManager);
        assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNotNull();
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
        assertThat(activity.locationHelper.isConnected()).isTrue();
    }

    @Test
    public void onPause_shouldDisconnectLocationClient() throws Exception {
        activity.onPause();
        assertThat(activity.locationHelper.isConnected()).isFalse();
    }

    @Test
    public void onResume_shouldReConnectLocationClient() throws Exception {
        activity.locationHelper.disconnect();
        activity.onResume();
        assertThat(activity.locationHelper.isConnected()).isTrue();
    }

    @Test
    public void onResume_shouldGetWritableLocationDatabase() throws Exception {
        assertThat(activity.getDb()).isOpen();
    }

    @Test
    public void onPrepareOptionsMenu_shouldHideSearchWhenResultsVisible() throws Exception {
        ArrayList<SimpleFeature> simpleFeatures = new ArrayList<SimpleFeature>();
        simpleFeatures.add(new SimpleFeature());
        Fragment fragment = ListResultsFragment.newInstance(activity, simpleFeatures);
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
    public void onOptionsItemSelected_shouldLaunchSettingsFragment() throws Exception {
        MenuItem menuItem = menu.findItem(R.id.settings);
        activity.onOptionsItemSelected(menuItem);
        assertThat(activity.getFragmentManager()).hasFragmentWithTag(SettingsFragment.TAG);
    }

    @Test
    public void onOptionsItemSelected_shouldHideActionBar() throws Exception {
        activity.showActionBar();
        MenuItem menuItem = menu.findItem(R.id.settings);
        activity.onOptionsItemSelected(menuItem);
        assertThat(activity.getActionBar()).isNotShowing();
    }

    @Test
    public void shouldShowActionBarWhenGettingBackFromSettings() throws Exception {
        MenuItem menuItem = menu.findItem(R.id.settings);
        activity.onOptionsItemSelected(menuItem);
        activity.onBackPressed();
        assertThat(activity.getActionBar()).isShowing();
    }

    @Test
    public void onBackPressed_shouldStayInBaseActivityWhenSettingsIsActive() throws Exception {
        MenuItem menuItem = menu.findItem(R.id.settings);
        activity.onOptionsItemSelected(menuItem);
        activity.onBackPressed();
        assertThat(activity.isFinishing()).isFalse();
    }

    @Test
    public void onBackPressed_shouldFinishBaseActivity() throws Exception {
        activity.onBackPressed();
        assertThat(activity.isFinishing()).isTrue();
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

        List<Feature> features = new ArrayList<Feature>();
        features.add(getTestFeature());
        features.add(getTestFeature());
        pagerResultsFragment.setSearchResults(features);
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
        Location expected = initLastLocation();
        ConnectionCallbacks callbacks = ((TestBaseActivity) activity).getConnectionCallback();
        callbacks.onConnected(new Bundle());
        Location actual = getMapController().getLocation();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void onConnect_shouldResetZoomLevel() throws Exception {
        getMapController().setZoomLevel(1);
        initLastLocation();
        invokeOnConnected();
        assertThat(getMapController().getZoomLevel()).isEqualTo(MapController.DEFAULT_ZOOMLEVEL);
    }

    @Test
    public void onConnect_shouldFindMe() throws Exception {
        initLastLocation();
        invokeOnConnected();
        assertThat(activity.getMapFragment().getMap().getMapPosition().getLatitude())
                .isEqualTo(1.0, Offset.offset(0.1));
        assertThat(activity.getMapFragment().getMap().getMapPosition().getLongitude())
                .isEqualTo(2.0, Offset.offset(0.1));
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

    @Test
    public void onCreate_debugViewShouldBeHidden() throws Exception {
        assertThat(activity.findViewById(R.id.debugging)).isNotVisible();
    }

    @Test
    public void onCreate_debugViewShouldBeVisible() throws Exception {
        activity.getDb().close();
        enableDebugMode(Robolectric.application);
        BaseActivity act = TestHelper.initBaseActivity();
        assertThat(act.findViewById(R.id.debugging)).isVisible();
    }

    @Test
    public void showActionbar_shouldShowActionbar() throws Exception {
        activity.hideActionBar();
        activity.showActionBar();
        assertThat(activity.getActionBar()).isShowing();
    }

    @Test
    public void showActionbar_shouldNotShowActionbar() throws Exception {
        activity.hideActionBar();
        activity.disableActionbar();
        activity.showActionBar();
        assertThat(activity.getActionBar()).isNotShowing();
    }

    @Test
    public void onOptionsItemSelected_shouldBeSuccessful() throws Exception {
        final TestBaseActivity testBaseActivity = (TestBaseActivity) activity;
        final String expected = "upload successful!";
        final MockWebServer server = new MockWebServer();
        MockResponse response = new MockResponse().setBody(expected);
        server.enqueue(response);
        server.play();

        testBaseActivity.setDebugDataEndpoint(server.getUrl("/upload.php").toString());
        MenuItem menuItem = menu.findItem(R.id.phone_home);
        testBaseActivity.onOptionsItemSelected(menuItem);

        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(expected);
        server.shutdown();
    }

    @Test
    public void onOptionsItemSelected_shouldNotifyWhenUnsuccessful() throws Exception {
        final TestBaseActivity testBaseActivity = (TestBaseActivity) activity;
        final String expected = "Upload failed, please try again later!";
        final MockWebServer server = new MockWebServer();
        MockResponse response = new MockResponse().setResponseCode(500);
        server.enqueue(response);
        server.play();

        testBaseActivity.setDebugDataEndpoint(server.getUrl("/upload.php").toString());
        MenuItem menuItem = menu.findItem(R.id.phone_home);
        testBaseActivity.onOptionsItemSelected(menuItem);

        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(expected);
        server.shutdown();
    }

    @Test
    public void onOptionsItemSelected_shouldPostDatabaseFile() throws Exception {
        TestBaseActivity testBaseActivity = (TestBaseActivity) activity;
        MockWebServer server = new MockWebServer();
        MockResponse response = new MockResponse();
        server.enqueue(response);
        server.play();

        byte[] expected = Files.toByteArray(new File(testBaseActivity.getDb().getPath()));
        testBaseActivity.setDebugDataEndpoint(server.getUrl("/upload.php").toString());
        MenuItem menuItem = menu.findItem(R.id.phone_home);
        testBaseActivity.onOptionsItemSelected(menuItem);
        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody()).isEqualTo(expected);
        server.shutdown();
    }

    @Test
    public void shouldNotifyUserIfLastLocationNotAvailable() throws Exception {
        LocationManager locationManager = (LocationManager)
                application.getSystemService(LOCATION_SERVICE);
        shadowOf(locationManager).setLastKnownLocation(GPS_PROVIDER, null);
        shadowOf(locationManager).setLastKnownLocation(NETWORK_PROVIDER, null);
        shadowOf(locationManager).setLastKnownLocation(GPS_PROVIDER, null);
        invokeOnConnected();
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Waiting for location");
    }

    @Test
    public void shouldHaveOSMLoginOption() throws Exception {
        Menu menu = new TestMenu();
        activity.onCreateOptionsMenu(menu);
        MenuItem menuItem = menu.findItem(R.id.login);
        assertThat(menuItem).isVisible();
    }

    @Test
    public void shouldNotHaveOSMLoginOption() throws Exception {
        Token token = new Token("stuff", "fun");
        Menu menu = new TestMenu();
        activity.onCreateOptionsMenu(menu);
        activity.setAccessToken(token);
        MenuItem menuItem = menu.findItem(R.id.login);
        assertThat(menuItem).isNotVisible();
    }

    @Test
    public void shouldHaveOSMLogoutOption() throws Exception {
        Token token = new Token("stuff", "fun");
        Menu menu = new TestMenu();
        activity.onCreateOptionsMenu(menu);
        activity.setAccessToken(token);
        MenuItem menuItem = menu.findItem(R.id.logout);
        assertThat(menuItem).isVisible();
    }

    @Test
    public void shouldNotHaveOSMLogoutOption() throws Exception {
        Menu menu = new TestMenu();
        activity.onCreateOptionsMenu(menu);
        MenuItem menuItem = menu.findItem(R.id.logout);
        assertThat(menuItem).isNotVisible();
    }

    @Test
    public void onOptionsItemSelected_shouldLogout() throws Exception {
        Token token = new Token("stuff", "fun");
        Menu menu = new TestMenu();
        activity.onCreateOptionsMenu(menu);
        activity.setAccessToken(token);
        MenuItem menuItem = menu.findItem(R.id.logout);
        assertThat(((MapzenApplication) application).getAccessToken()).isNotNull();
        activity.onOptionsItemSelected(menuItem);
        assertThat(((MapzenApplication) application).getAccessToken()).isNull();
    }

    @Test
    public void onOptionsItemSelected_shouldToggleLoginLogout() throws Exception {
        Token token = new Token("stuff", "fun");
        Menu menu = new TestMenu();
        activity.onCreateOptionsMenu(menu);
        activity.setAccessToken(token);
        MenuItem logoutItem = menu.findItem(R.id.logout);
        activity.onOptionsItemSelected(logoutItem);
        assertThat(logoutItem).isNotVisible();
        MenuItem loginItem = menu.findItem(R.id.login);
        assertThat(loginItem).isVisible();
    }

    @Test
    public void onOptionsItemSelected_shouldStartOSMOauthFragment() throws Exception {
        Menu menu = new TestMenu();
        final TestBaseActivity testBaseActivity = (TestBaseActivity) activity;
        testBaseActivity.onCreateOptionsMenu(menu);
        OAuthService serviceMock = Mockito.mock(OAuthService.class);
        ((MapzenApplication) application).setOsmOauthService(serviceMock);
        MenuItem loginItem = menu.findItem(R.id.login);
        testBaseActivity.onOptionsItemSelected(loginItem);

        assertThat(testBaseActivity.getSupportFragmentManager())
                .hasFragmentWithTag(OSMOauthFragment.TAG);
    }

    private Location initLastLocation() {
        Location location = new Location(GPS_PROVIDER);
        location.setLatitude(1.0);
        location.setLongitude(2.0);
        Robolectric.shadowOf((LocationManager) activity.getSystemService(Context.LOCATION_SERVICE))
                .setLastKnownLocation(GPS_PROVIDER, location);
        return location;
    }

    private void invokeOnConnected() {
        ConnectionCallbacks callbacks = ((TestBaseActivity) activity).getConnectionCallback();
        callbacks.onConnected(new Bundle());
    }
}
