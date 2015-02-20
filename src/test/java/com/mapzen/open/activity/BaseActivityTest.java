package com.mapzen.open.activity;

import com.mapzen.android.gson.Feature;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.open.MapController;
import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.TestMapzenApplication;
import com.mapzen.open.core.SettingsFragment;
import com.mapzen.open.entity.SimpleFeature;
import com.mapzen.open.event.RoutePreviewEvent;
import com.mapzen.open.route.RouteFragment;
import com.mapzen.open.route.RoutePreviewFragment;
import com.mapzen.open.search.PagerResultsFragment;
import com.mapzen.open.search.SavedSearch;
import com.mapzen.open.support.MapzenTestRunner;
import com.mapzen.open.support.TestHelper;
import com.mapzen.open.support.TestHelper.ViewUpdateSubscriber;
import com.mapzen.open.util.MapzenNotificationCreator;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.squareup.otto.Bus;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.tester.android.view.TestMenu;
import org.scribe.model.Token;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.mapzen.open.support.TestHelper.getTestFeature;
import static com.mapzen.open.support.TestHelper.getTestSimpleFeature;
import static com.mapzen.open.support.TestHelper.initBaseActivity;
import static com.mapzen.open.support.TestHelper.initBaseActivityWithMenu;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.shadows.ShadowToast.getTextOfLatestToast;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@RunWith(MapzenTestRunner.class)
public class BaseActivityTest {
    private BaseActivity activity;
    private TestMenu menu;
    @Inject LostApiClient locationClient;
    @Inject MixpanelAPI mixpanelAPI;
    @Inject MapController mapController;
    @Inject SavedSearch savedSearch;
    @Inject SQLiteDatabase db;
    @Inject Bus bus;
    @Inject MapzenApplication app;

    @Before
    public void setUp() throws Exception {
        ((TestMapzenApplication) Robolectric.application).inject(this);
        menu = new TestMenu();
        activity = initBaseActivityWithMenu(menu);
        savedSearch.clear();
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
        savedSearch.store("expected");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SavedSearch.TAG, savedSearch.serialize());
        editor.commit();
        savedSearch.clear();
        initBaseActivity();
        assertThat(savedSearch.getIterator().next().getTerm()).isEqualTo("expected");
    }

    @Test
    public void onPause_shouldPersistSavedSearches() throws Exception {
        savedSearch.store("expected");
        activity.onPause();
        savedSearch.clear();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        savedSearch.deserialize(prefs.getString(SavedSearch.TAG, ""));
        assertThat(savedSearch.getIterator().next().getTerm()).isEqualTo("expected");
    }

    @Test
    public void onDestroy_shouldFlushMixpanelApi() throws Exception {
        activity.onDestroy();
        verify(mixpanelAPI).flush();
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
    public void onPause_shouldNotDisconnectLocationClientWhileRouting() throws Exception {
        activity.getSupportFragmentManager().beginTransaction()
                .add(R.id.routes_container, new Fragment(), RouteFragment.TAG)
                .commit();
        activity.onPause();
        assertThat(locationClient.isConnected()).isTrue();
    }

    @Test
    public void onResume_shouldReConnectLocationClient() throws Exception {
        locationClient.disconnect();
        activity.onResume();
        assertThat(locationClient.isConnected()).isTrue();
    }

    @Test
    public void onResume_shouldGetWritableLocationDatabase() throws Exception {
        assertThat(db).isOpen();
    }

    @Test
    public void onMenuItemActionExpand_shouldSetIconifiedFalse() throws Exception {
        menu.findItem(R.id.search).expandActionView();
        assertThat((SearchView) menu.findItem(R.id.search).getActionView()).isNotIconified();
    }

    @Test
    public void onMenuItemActionExpand_shouldShowAutoCompleteListView() throws Exception {
        menu.findItem(R.id.search).expandActionView();
        assertThat(activity.getAutoCompleteListView()).isVisible();
    }

    @Test
    public void onMenuItemActionExpand_shouldSetAutoCompleteAdapter() throws Exception {
        menu.findItem(R.id.search).expandActionView();
        assertThat(((ListView) activity.getAutoCompleteListView()).getAdapter()).isNotNull();
    }

    @Test
    public void onMenuItemActionCollapse_shouldHideAutoCompleteListView() throws Exception {
        activity.getAutoCompleteListView().setVisibility(View.VISIBLE);
        menu.findItem(R.id.search).collapseActionView();
        assertThat(activity.getAutoCompleteListView()).isGone();
    }

    @Test
    public void onMenuItemActionCollapse_shouldPopPagerResultsFragment() throws Exception {
        activity.executeSearchOnMap("query");
        menu.findItem(R.id.search).collapseActionView();
        assertThat(activity.getSupportFragmentManager())
                .doesNotHaveFragmentWithTag(PagerResultsFragment.TAG);
    }

    @Test
    public void onTouch_shouldShowAutoCompleteListView() throws Exception {
        MotionEvent motionEvent = Mockito.mock(MotionEvent.class);
        Mockito.when(motionEvent.getAction()).thenReturn(MotionEvent.ACTION_UP);

        activity.getAutoCompleteListView().setVisibility(View.GONE);
        activity.getQueryAutoCompleteTextView(activity.getSearchView())
                .dispatchTouchEvent(motionEvent);
        assertThat(activity.getAutoCompleteListView()).isVisible();
    }

    @Test
    public void onOptionsItemSelected_shouldLaunchSettingsFragment() throws Exception {
        activity.toggleDebugMode();
        MenuItem menuItem = menu.findItem(R.id.settings);
        activity.onOptionsItemSelected(menuItem);
        assertThat(activity.getFragmentManager()).hasFragmentWithTag(SettingsFragment.TAG);
    }

    @Test
    public void onOptionsItemSelected_shouldOpenAboutPage() throws Exception {
        MenuItem menuItem = menu.findItem(R.id.about);
        activity.onOptionsItemSelected(menuItem);
        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(shadowIntent.getData()).isEqualTo(Uri.parse("https://mapzen.com/open/about"));
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
        assertThat(activity.getSupportActionBar().isShowing()).isFalse();
    }

    @Test
    public void shouldShowActionBarWhenGettingBackFromSettings() throws Exception {
        activity.toggleDebugMode();
        MenuItem menuItem = menu.findItem(R.id.settings);
        activity.onOptionsItemSelected(menuItem);
        activity.onBackPressed();
        assertThat(activity.getSupportActionBar().isShowing()).isTrue();
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
    public void shouldDisplaySavedSearchTermsOnFocus() throws Exception {
        savedSearch.clear();
        savedSearch.store("saved query 1");
        savedSearch.store("saved query 2");
        savedSearch.store("saved query 3");
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        final AutoCompleteTextView autoCompleteTextView =
                (AutoCompleteTextView) searchView.findViewById(searchView.getContext()
                        .getResources().getIdentifier("android:id/search_src_text", null, null));
        autoCompleteTextView.getOnFocusChangeListener().onFocusChange(searchView, true);
        assertThat(((ListView) activity.getAutoCompleteListView()).getAdapter()).hasCount(3);
    }

    @Test
    public void executeSearchOnMap_shouldRemoveSuggestionsAdapter() throws Exception {
        activity.executeSearchOnMap("query");
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        assertThat(searchView.getSuggestionsAdapter()).isNull();
    }

    @Test
    public void openingSearchView_shouldHideOverflow() throws Exception {
        TestMenuWithGroup menu = new TestMenuWithGroup();
        activity.onCreateOptionsMenu(menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.onActionViewExpanded();
        assertThat(menu.group).isEqualTo(R.id.overflow_menu);
        assertThat(menu.visible).isFalse();
    }

    @Test
    public void showActionbar_shouldShowActionbar() throws Exception {
        activity.hideActionBar();
        activity.showActionBar();
        assertThat(activity.getSupportActionBar().isShowing()).isTrue();
    }

    @Test
    public void showActionbar_shouldNotShowActionbar() throws Exception {
        activity.hideActionBar();
        activity.disableActionbar();
        activity.showActionBar();
        assertThat(activity.getSupportActionBar().isShowing()).isFalse();
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
        assertThat(app.getAccessToken()).isNotNull();
        activity.onOptionsItemSelected(menuItem);
        assertThat(app.getAccessToken()).isNull();
    }

    @Test
    public void updateView_shouldPostViewUpdateEvent() throws Exception {
        ViewUpdateSubscriber viewUpdateSubscriber = new ViewUpdateSubscriber();
        bus.register(viewUpdateSubscriber);
        activity.updateView();
        assertThat(viewUpdateSubscriber.getEvent()).isNotNull();
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
        app.deactivateMoveMapToLocation();
        activity.locateButtonAction(activity.findViewById(R.id.locate_button));
        assertThat(app.shouldMoveMapToLocation()).isTrue();
    }

    @Test
    public void onRoutePreviewEvent_shouldStartRoutePreviewFragment() throws Exception {
        activity.onRoutePreviewEvent(new RoutePreviewEvent(getTestSimpleFeature()));
        assertThat(activity.getSupportFragmentManager())
                .hasFragmentWithTag(RoutePreviewFragment.TAG);
    }

    @Test
    public void onRoutePreviewEvent_shouldDisplayGPSPromptIfNotEnabled() throws Exception {
        ShadowLocationManager manager = shadowOf(getLocationManager());
        manager.setProviderEnabled(LocationManager.GPS_PROVIDER, false);
        activity.onRoutePreviewEvent(new RoutePreviewEvent(getTestSimpleFeature()));
        assertThat(activity.getSupportFragmentManager()).hasFragmentWithTag("gps_dialog");
    }

    @Test
    public void onDestroy_shouldSaveCurrentSearchTerm() throws Exception {
        activity.searchMenuItem.expandActionView();
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQuery("query", false);
        activity.onDestroy();
        assertThat(app.getCurrentSearchTerm()).isEqualTo("query");
    }

    @Test
    public void onCreateOptionsMenu_shouldRestoreCurrentSearchTerm() throws Exception {
        app.setCurrentSearchTerm("query");
        activity.onCreateOptionsMenu(menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        assertThat(activity.searchMenuItem.isActionViewExpanded()).isTrue();
        assertThat(searchView.getQuery().toString()).isEqualTo("query");
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

    private LocationManager getLocationManager() {
        return (LocationManager) Robolectric.application.getSystemService(Context.LOCATION_SERVICE);
    }

    private class TestMenuWithGroup extends TestMenu {
        private int group;
        private boolean visible;

        @Override
        public void setGroupVisible(int group, boolean visible) {
            this.group = group;
            this.visible = visible;
        }
    }
}
