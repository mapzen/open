package com.mapzen.open.activity;

import com.mapzen.open.MapController;
import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.TestMapzenApplication;
import com.mapzen.android.gson.Feature;
import com.mapzen.android.lost.LocationClient;
import com.mapzen.open.core.MapzenLocation;
import com.mapzen.open.core.SettingsFragment;
import com.mapzen.open.entity.SimpleFeature;
import com.mapzen.open.route.RouteFragment;
import com.mapzen.open.route.RoutePreviewFragment;
import com.mapzen.open.search.PagerResultsFragment;
import com.mapzen.open.search.SavedSearch;
import com.mapzen.open.support.MapzenTestRunner;
import com.mapzen.open.support.TestBaseActivity;
import com.mapzen.open.support.TestHelper;
import com.mapzen.open.util.MapzenNotificationCreator;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;

import com.google.common.io.Files;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.tester.android.view.TestMenu;
import org.scribe.model.Token;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.SearchView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static com.mapzen.open.search.SavedSearch.getSavedSearch;
import static com.mapzen.open.support.TestHelper.getTestFeature;
import static com.mapzen.open.support.TestHelper.initBaseActivity;
import static com.mapzen.open.support.TestHelper.initBaseActivityWithMenu;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.shadows.ShadowToast.getTextOfLatestToast;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class BaseActivityTest {
    private BaseActivity activity;
    private TestMenu menu;
    @Inject LocationClient locationClient;
    @Inject MixpanelAPI mixpanelAPI;
    @Inject MapController mapController;

    @Before
    public void setUp() throws Exception {
        ((TestMapzenApplication) Robolectric.application).inject(this);
        menu = new TestMenu();
        activity = initBaseActivityWithMenu(menu);
        getSavedSearch().clear();
    }

    @After
    public void tearDown() {
        activity.finish();
        mapController.setActivity(new BaseActivity());
    }

    @Test
    public void toggleDebugMode_shouldToggleSettings() {
        activity.onPrepareOptionsMenu(menu);
        assertThat(menu.findItem(R.id.settings)).isNotVisible();
        activity.toggleDebugMode();
        activity.onPrepareOptionsMenu(menu);
        assertThat(menu.findItem(R.id.settings)).isVisible();
    }

    @Test
    public void toggleDebugMode_shouldToggleSubmitData() {
        activity.onPrepareOptionsMenu(menu);
        assertThat(menu.findItem(R.id.phone_home)).isNotVisible();
        activity.toggleDebugMode();
        activity.onPrepareOptionsMenu(menu);
        assertThat(menu.findItem(R.id.phone_home)).isVisible();
    }

    @Test
    public void toggleDebugMode_shouldToggleUploadGPSTraces() {
        activity.onPrepareOptionsMenu(menu);
        assertThat(menu.findItem(R.id.upload_traces)).isNotVisible();
        activity.toggleDebugMode();
        activity.onPrepareOptionsMenu(menu);
        assertThat(menu.findItem(R.id.upload_traces)).isVisible();
    }

    @Test
    public void toggleDebugMode_shouldCollapseActionView() throws Exception {
        menu.findItem(R.id.search).expandActionView();
        activity.toggleDebugMode();
        assertThat(menu.findItem(R.id.search)).isActionViewCollapsed();
    }

    @Test
    public void toggleDebugMode_shouldToastState() throws Exception {
        activity.toggleDebugMode();
        assertThat(getTextOfLatestToast())
                .isEqualTo(activity.getString(R.string.debug_settings_on));
        activity.toggleDebugMode();
        assertThat(getTextOfLatestToast())
                .isEqualTo(activity.getString(R.string.debug_settings_off));
    }

    @Test
    public void toggleDebugMode_shouldFlipDebugMode() {
        Boolean debugMode = activity.isInDebugMode();
        activity.toggleDebugMode();
        assertThat(activity.isInDebugMode()).isNotEqualTo(debugMode);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(activity).isNotNull();
    }

    @Test
    public void onCreate_shouldInitializeMapController() throws Exception {
        assertThat(mapController.getMap()).isNotNull();
    }

    @Test
    public void onCreate_shouldSetDataUploadServiceAlarm() throws Exception {
        Token token = new Token("yo", "yo");
        activity.setAccessToken(token);
        activity = initBaseActivityWithMenu(menu);
        AlarmManager alarmManager =
                (AlarmManager) Robolectric.application.getSystemService(Context.ALARM_SERVICE);
        ShadowAlarmManager shadowAlarmManager = Robolectric.shadowOf(alarmManager);
        assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNotNull();
    }

    @Test
    public void onCreate_shouldInitializeSavedSearches() throws Exception {
        getSavedSearch().store("expected");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SavedSearch.TAG, getSavedSearch().serialize());
        editor.commit();
        getSavedSearch().clear();
        initBaseActivity();
        assertThat(getSavedSearch().get().next()).isEqualTo("expected");
    }

    @Test
    public void onPause_shouldPersistSavedSearches() throws Exception {
        getSavedSearch().store("expected");
        activity.onPause();
        getSavedSearch().clear();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        getSavedSearch().deserialize(prefs.getString(SavedSearch.TAG, ""));
        assertThat(getSavedSearch().get().next()).isEqualTo("expected");
    }

    @Test
    public void onDestroy_shouldFlushMixpanelApi() throws Exception {
        activity.onDestroy();
        verify(mixpanelAPI).flush();
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
        assertThat(locationClient.isConnected()).isTrue();
    }

    @Test
    public void onPause_shouldDisconnectLocationClient() throws Exception {
        activity.onPause();
        assertThat(locationClient.isConnected()).isFalse();
    }

    @Test
    public void onResume_shouldReConnectLocationClient() throws Exception {
        locationClient.disconnect();
        activity.onResume();
        assertThat(locationClient.isConnected()).isTrue();
    }

    @Test
    public void onResume_shouldGetWritableLocationDatabase() throws Exception {
        assertThat(activity.getDb()).isOpen();
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
        activity.toggleDebugMode();
        MenuItem menuItem = menu.findItem(R.id.settings);
        activity.onOptionsItemSelected(menuItem);
        assertThat(activity.getFragmentManager()).hasFragmentWithTag(SettingsFragment.TAG);
    }

    @Test
    public void onOptionsItemSelected_shouldLaunchDataUploadService() throws Exception {
        activity.toggleDebugMode();
        MenuItem menuItem = menu.findItem(R.id.upload_traces);
        activity.onOptionsItemSelected(menuItem);
        Intent serviceIntent = Robolectric.getShadowApplication().peekNextStartedService();
        String serviceStarted = serviceIntent.getComponent().getClassName();
        assertThat(serviceStarted).isEqualTo("com.mapzen.open.core.DataUploadService");
    }

    @Test
    public void onOptionsItemSelected_shouldHideActionBar() throws Exception {
        activity.showActionBar();
        activity.toggleDebugMode();
        MenuItem menuItem = menu.findItem(R.id.settings);
        activity.onOptionsItemSelected(menuItem);
        assertThat(activity.getActionBar()).isNotShowing();
    }

    @Test
    public void shouldShowActionBarWhenGettingBackFromSettings() throws Exception {
        activity.toggleDebugMode();
        MenuItem menuItem = menu.findItem(R.id.settings);
        activity.onOptionsItemSelected(menuItem);
        activity.onBackPressed();
        assertThat(activity.getActionBar()).isShowing();
    }

    @Test
    public void onBackPressed_shouldStayInBaseActivityWhenSettingsIsActive() throws Exception {
        activity.toggleDebugMode();
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

        ShadowLocationManager manager = shadowOf(locationClient.getLocationManager());
        manager.simulateLocation(location);
        ((MapzenApplication) application).deactivateMoveMapToLocation();
        manager.simulateLocation(location);
        // TODO assertThat(activity.getMapFragment().getUserLocation()).isNotEqualTo(newLocation);
    }

    @Test
    public void onConnect_shouldUpdateMapController() throws Exception {
        Location expected = initLastLocation();
        invokeOnConnected();
        Location actual = mapController.getLocation();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void onConnect_shouldResetZoomLevel() throws Exception {
        mapController.setZoomLevel(1);
        initLastLocation();
        invokeOnConnected();
        assertThat(mapController.getZoomLevel()).isEqualTo(MapController.DEFAULT_ZOOM_LEVEL);
    }

    @Test
    public void shouldHaveSuggestionsAdapter() throws Exception {
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        assertThat(searchView.getSuggestionsAdapter()).isNotNull();
    }

    @Test
    public void shouldDisplaySavedSearchTermsOnFocus() throws Exception {
        getSavedSearch().clear();
        getSavedSearch().store("saved query 1");
        getSavedSearch().store("saved query 2");
        getSavedSearch().store("saved query 3");
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        final AutoCompleteTextView autoCompleteTextView =
                (AutoCompleteTextView) searchView.findViewById(searchView.getContext()
                        .getResources().getIdentifier("android:id/search_src_text", null, null));
        autoCompleteTextView.getOnFocusChangeListener().onFocusChange(searchView, true);
        assertThat(searchView.getSuggestionsAdapter().getCursor()).hasCount(3);
    }

    @Test
    public void executeSearchOnMap_shouldRemoveSuggestionsAdapter() throws Exception {
        activity.executeSearchOnMap("query");
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        assertThat(searchView.getSuggestionsAdapter()).isNull();
    }

    @Test
    public void openingSearchView_shouldHideOverflow() throws Exception {
        Menu spy = spy(activity.getActivityMenu());
        activity.toggleDebugMode();
        activity.onCreateOptionsMenu(spy);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.onActionViewExpanded();
        verify(spy).setGroupVisible(R.id.overflow_menu, false);
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
        activity.toggleDebugMode();
        MenuItem menuItem = menu.findItem(R.id.phone_home);
        testBaseActivity.onOptionsItemSelected(menuItem);

        assertThat(getTextOfLatestToast()).isEqualTo(expected);
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

        activity.toggleDebugMode();
        testBaseActivity.setDebugDataEndpoint(server.getUrl("/upload.php").toString());
        MenuItem menuItem = menu.findItem(R.id.phone_home);
        testBaseActivity.onOptionsItemSelected(menuItem);

        assertThat(getTextOfLatestToast()).isEqualTo(expected);
        server.shutdown();
    }

    @Test
    public void onOptionsItemSelected_shouldPostDatabaseFile() throws Exception {
        TestBaseActivity testBaseActivity = (TestBaseActivity) activity;
        MockWebServer server = new MockWebServer();
        MockResponse response = new MockResponse();
        server.enqueue(response);
        server.play();

        activity.toggleDebugMode();
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
        assertThat(getTextOfLatestToast()).isEqualTo("Waiting for location");
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
    public void updateView_shouldSendUpdateViewBroadcast() throws Exception {
        activity.updateView();
        List<Intent> intents = Robolectric.getShadowApplication().getBroadcastIntents();
        assertThat(intents).contains(new Intent(BaseActivity.COM_MAPZEN_UPDATE_VIEW));
    }

    @Test
    public void getSearchQueryTextView_shouldReturnAutoCompleteTextView() throws Exception {
        SearchView searchView = activity.getSearchView();
        AutoCompleteTextView textView = activity.getQueryAutoCompleteTextView(searchView);
        LinearLayout linearLayout1 = (LinearLayout) activity.getSearchView().getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        assertThat(linearLayout3.indexOfChild(textView)).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void getSearchView_shouldReturnNullIfMenuItemNotAvailable() throws Exception {
        activity.searchMenuItem = null;
        assertThat(activity.getSearchView()).isNull();
    }

    @Test
    public void showLoadingIndicator_shouldHideFindMe() throws Exception {
        activity.showLoadingIndicator();
        Robolectric.runUiThreadTasks();
        assertThat(activity.findViewById(R.id.locate_button)).isNotVisible();
    }

    @Test
    public void showLoadingIndicator_shouldHideCopyright() throws Exception {
        activity.showLoadingIndicator();
        Robolectric.runUiThreadTasks();
        assertThat(activity.findViewById(R.id.attribution)).isNotVisible();
    }

    @Test
    public void hideLoadingIndicator_shouldShowFindMe() throws Exception {
        activity.showLoadingIndicator();
        activity.hideLoadingIndicator();
        Robolectric.runUiThreadTasks();
        assertThat(activity.findViewById(R.id.locate_button)).isVisible();
    }

    @Test
    public void hideLoadingIndicator_shouldShowCopyright() throws Exception {
        activity.showLoadingIndicator();
        activity.hideLoadingIndicator();
        Robolectric.runUiThreadTasks();
        assertThat(activity.findViewById(R.id.attribution)).isVisible();
    }

    @Test
    public void onPostResume_shouldPopRouteFragmentAndRoutePreviewFragment() throws Exception {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        addFragmentToBackStack(RoutePreviewFragment.newInstance(activity, new SimpleFeature()),
                RoutePreviewFragment.TAG);
        RouteFragment routeFragment = RouteFragment.newInstance(activity, new SimpleFeature());
        routeFragment.setRoute(getRouteMock());

        addFragmentToBackStack(routeFragment,
                RouteFragment.TAG);

        Intent intent = new Intent();
        intent.putExtra(MapzenNotificationCreator.EXIT_NAVIGATION, true);
        activity.onNewIntent(intent);
        activity.onPostResume();
        assertThat(fragmentManager.findFragmentByTag(RouteFragment.TAG)).isNull();
        assertThat(fragmentManager.findFragmentByTag(RoutePreviewFragment.TAG)).isNull();
    }

    private void addFragmentToBackStack(Fragment fragment, String tag) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    @Test
    public void locateButtonAction_shouldActivateMoveMapToLocation() throws Exception {
        MapzenApplication app = (MapzenApplication) application;
        app.deactivateMoveMapToLocation();
        activity.locateButtonAction(activity.findViewById(R.id.locate_button));
        assertThat(app.shouldMoveMapToLocation()).isTrue();
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
        MapzenLocation.ConnectionCallbacks connectionCallbacks =
                new MapzenLocation.ConnectionCallbacks((MapzenApplication) application);
        connectionCallbacks.setLocationClient(locationClient);
        connectionCallbacks.onConnected(new Bundle());
    }

    private Route getRouteMock() throws JSONException {
        Route route = mock(Route.class);
        Mockito.when(route.foundRoute()).thenReturn(true);
        Mockito.when(route.getRawRoute()).thenReturn(new JSONObject("{}"));
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(TestHelper.getTestInstruction());
        Mockito.when(route.getRouteInstructions()).thenReturn(instructions);
        Mockito.when(route.getStartCoordinates()).thenReturn(TestHelper.getTestLocation(0, 0));
        return route;
    }

}
