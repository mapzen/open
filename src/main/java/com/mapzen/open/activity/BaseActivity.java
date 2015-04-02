package com.mapzen.open.activity;

import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.open.MapController;
import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.core.DataUploadService;
import com.mapzen.open.core.MapzenLocation;
import com.mapzen.open.core.SettingsFragment;
import com.mapzen.open.event.RoutePreviewEvent;
import com.mapzen.open.event.ViewUpdateEvent;
import com.mapzen.open.fragment.MapFragment;
import com.mapzen.open.route.RouteFragment;
import com.mapzen.open.route.RoutePreviewFragment;
import com.mapzen.open.search.AutoCompleteAdapter;
import com.mapzen.open.search.AutoCompleteListView;
import com.mapzen.open.search.OnPoiClickListener;
import com.mapzen.open.search.PagerResultsFragment;
import com.mapzen.open.search.PeliasSearchView;
import com.mapzen.open.search.SavedSearch;
import com.mapzen.open.util.Logger;
import com.mapzen.open.util.MapzenGPSPromptDialogFragment;
import com.mapzen.open.util.MapzenNotificationCreator;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.splunk.mint.Mint;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.oscim.android.AndroidMap;
import org.oscim.android.MapView;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.map.Map;
import org.scribe.model.Token;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.Calendar;

import javax.inject.Inject;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class BaseActivity extends ActionBarActivity {
    @Inject LostApiClient locationClient;
    private Menu activityMenu;
    private AutoCompleteAdapter autoCompleteAdapter;
    @Inject MapzenApplication app;
    private MapFragment mapFragment;
    private MapzenGPSPromptDialogFragment gpsPromptDialogFragment;
    private AutoCompleteListView autoCompleteListView;
    @Inject MixpanelAPI mixpanelAPI;
    @Inject MapController mapController;
    @Inject SavedSearch savedSearch;
    @Inject Bus bus;

    protected boolean enableActionbar = true;

    MenuItem searchMenuItem;

    private boolean exitNavigationIntentReceived;

    private Runnable hideImeRunnable = new Runnable() {
        public void run() {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getSearchView().getWindowToken(), 0);
                searchMenuItem.getActionView().clearFocus();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MapzenApplication) getApplication()).inject(this);
        Mint.initAndStartSession(this, "ebfa8fd7");
        Mint.addExtraData("OEM", Build.MANUFACTURER);
        setContentView(R.layout.base);
        initMapFragment();
        gpsPromptDialogFragment = new MapzenGPSPromptDialogFragment();
        initMapController();
        initAlarm();
        initSavedSearches();
        bus.register(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getBooleanExtra(MapzenNotificationCreator.EXIT_NAVIGATION, false)) {
            exitNavigationIntentReceived = true;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (exitNavigationIntentReceived) {
            getSupportFragmentManager().popBackStack(); // Pop RouteFragment
            getSupportFragmentManager().popBackStack(); // Pop RoutePreviewFragment
            exitNavigationIntentReceived = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getMap() != null) {
            ((AndroidMap) getMap()).pause(true);
        }
        if (getSupportFragmentManager().findFragmentByTag(RouteFragment.TAG) == null) {
            locationClient.disconnect();
        }
        persistSavedSearches();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getMap() != null) {
            ((AndroidMap) getMap()).pause(false);
        }
        locationClient.connect();
        MapzenLocation.onLocationServicesConnected(MapController.getMapController(),
                LocationServices.FusedLocationApi, app);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.disconnect();

        if (getMap() != null) {
            getMap().destroy();
        }
        clearNotifications();
        mixpanelAPI.flush();
        bus.unregister(this);
        saveCurrentSearchTerm();
        app.setAutoCompleteVisibility(getAutoCompleteListView().getVisibility());
    }

    private void clearNotifications() {
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
    }

    public boolean isInDebugMode() {
        SharedPreferences prefs = getDefaultSharedPreferences(this);
        return prefs.getBoolean(getString(R.string.settings_key_debug), false);
    }

    public void toggleDebugMode() {
        SharedPreferences prefs = getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(getString(R.string.settings_key_debug), !isInDebugMode());
        editor.commit();
        MenuItemCompat.collapseActionView(searchMenuItem);
        supportInvalidateOptionsMenu();
        if (isInDebugMode()) {
            Toast.makeText(this, getString(R.string.debug_settings_on), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getString(R.string.debug_settings_off), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                final PreferenceFragment settingsFragment = SettingsFragment.newInstance(this);
                getFragmentManager().beginTransaction()
                        .add(R.id.settings, settingsFragment, SettingsFragment.TAG)
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.logout:
                SharedPreferences prefs = getSharedPreferences("OAUTH", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove("token");
                editor.remove("forced_login");
                editor.commit();
                startActivity(new Intent(this, InitialActivity.class));
                finish();
                return true;
            case R.id.upload_traces:
                uploadTraces();
                return true;
            case R.id.about:
                final Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://mapzen.com/open/about"));
                startActivity(intent);
                return true;
            case R.id.action_view_all:
                final PagerResultsFragment pagerResultsFragment = getPagerResultsFragment();
                if (pagerResultsFragment != null) {
                    pagerResultsFragment.viewAll();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showLoadingIndicator() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMapFragment().showProgress();
                findViewById(R.id.locate_button).setVisibility(View.GONE);
                findViewById(R.id.attribution).setVisibility(View.GONE);
            }
        });
    }

    public void hideLoadingIndicator() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMapFragment().hideProgress();
                findViewById(R.id.locate_button).setVisibility(View.VISIBLE);
                findViewById(R.id.attribution).setVisibility(View.VISIBLE);
            }
        });
    }

    public void showGPSPromptDialog() {
        if (!gpsPromptDialogFragment.isAdded()) {
            gpsPromptDialogFragment.show(getSupportFragmentManager(), "gps_dialog");
        }
    }

    public void promptForGPSIfNotEnabled() {
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSPromptDialog();
        }
    }

    private void initMapFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map_fragment);
        mapFragment.setAct(this);
        mapFragment.setRetainInstance(true);
        mapFragment.setOnPoiClickListener(new OnPoiClickListener() {
            @Override
            public void onPoiClick(int index, MarkerItem item) {
                final PagerResultsFragment pagerResultsFragment = getPagerResultsFragment();
                pagerResultsFragment.setCurrentItem(index);
            }
        });
    }

    private PagerResultsFragment getPagerResultsFragment() {
        return (PagerResultsFragment) getSupportFragmentManager()
                .findFragmentByTag(PagerResultsFragment.TAG);
    }

    public MapFragment getMapFragment() {
        return mapFragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        activityMenu = menu;
        getMenuInflater().inflate(R.menu.options_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.search);
        final PeliasSearchView searchView =
                (PeliasSearchView) MenuItemCompat.getActionView(searchMenuItem);
        setupAdapter(searchView);

        MenuItemCompat.setOnActionExpandListener(searchMenuItem,
                new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                hideOptionsMenu();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                resetSearchView();
                showOptionsMenu();
                removePagerResultsFragment();
                return true;
            }
        });

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(autoCompleteAdapter);
        restoreCurrentSearchTerm();
        searchView.setAutoCompleteListView(getAutoCompleteListView());
        getAutoCompleteListView().setVisibility(app.getAutoCompleteVisibility());
        if (getAutoCompleteListView().getVisibility() == View.GONE) {
            new Handler().postDelayed(hideImeRunnable, 100);
        }

        Uri data = getIntent().getData();
        if (data != null) {
            Logger.d("data = " + data.toString());
            if (data.toString().contains("geo:")) {
                handleGeoIntent(searchView, data);
            } else if (data.toString().contains("maps.google.com")) {
                handleMapsIntent(searchView, data);
            }
        }
        return true;
    }

    private void removePagerResultsFragment() {
        final PagerResultsFragment pagerResultsFragment = getPagerResultsFragment();
        if (pagerResultsFragment != null && pagerResultsFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().remove(pagerResultsFragment)
                    .commit();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final boolean debug = isInDebugMode();
        menu.findItem(R.id.settings).setVisible(debug);
        menu.findItem(R.id.upload_traces).setVisible(debug);
        return true;
    }

    public void hideOptionsMenu() {
        if (activityMenu != null) {
            activityMenu.setGroupVisible(R.id.overflow_menu, false);
        }
    }

    public void showOptionsMenu() {
        if (activityMenu != null) {
            activityMenu.setGroupVisible(R.id.overflow_menu, true);
        }
    }

    public void hideActionViewAll() {
        if (activityMenu != null) {
            activityMenu.findItem(R.id.action_view_all).setVisible(false);
        }
    }

    public void showActionViewAll() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override public void run() {
                if (activityMenu != null) {
                    activityMenu.findItem(R.id.action_view_all).setVisible(true);
                }
            }
        }, 100);
    }

    private void resetSearchView() {
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQuery("", false);
        searchView.clearFocus();
        searchView.setIconified(true);
        autoCompleteAdapter.resetCursor();
        autoCompleteAdapter.loadSavedSearches();
    }

    @Override
    public void onBackPressed() {
        if (settingsFragmentOnBack()) {
            return;
        }
        if (routingFragmentOnBack()) {
            return;
        }
        super.onBackPressed();
        clearNotifications();
    }

    private boolean settingsFragmentOnBack() {
        SettingsFragment settingsFragment = (SettingsFragment) getFragmentManager()
                .findFragmentByTag(SettingsFragment.TAG);
        if (settingsFragment != null && settingsFragment.isAdded()) {
            getFragmentManager().beginTransaction()
                    .detach(settingsFragment)
                    .commit();
            return true;
        }
        return false;
    }

    private boolean routingFragmentOnBack() {
        RouteFragment routeFragment = (RouteFragment)
                getSupportFragmentManager().findFragmentByTag(RouteFragment.TAG);
        if (routeFragment != null && routeFragment.isAdded()) {
            boolean shouldClose = routeFragment.onBackAction();
            if (shouldClose) {
                ((RoutePreviewFragment) getSupportFragmentManager()
                        .findFragmentByTag(RoutePreviewFragment.TAG)).showFragmentContents();
                clearNotifications();
                super.onBackPressed();
            }
            return true;
        }
        return false;
    }

    private void handleGeoIntent(SearchView searchView, Uri data) {
        if (data.toString().contains("q=")) {
            searchMenuItem.expandActionView();
            String queryString = Uri.decode(data.toString().split("q=")[1]);
            searchView.setQuery(queryString, true);
        }
    }

    private void handleMapsIntent(SearchView searchView, Uri data) {
        if (data.toString().contains("q=")) {
            searchMenuItem.expandActionView();
            String queryString = Uri.decode(data.toString().split("q=")[1].split("@")[0]);
            searchView.setQuery(queryString, true);
        }
    }

    public MapView getMapView() {
        if (mapFragment == null || mapFragment.getView() == null) {
            return null;
        }

        return (MapView) mapFragment.getView().findViewById(R.id.map);
    }

    public Map getMap() {
        if (getMapView() == null) {
            return null;
        }

        return getMapView().map();
    }

    public PeliasSearchView getSearchView() {
        if (searchMenuItem != null) {
            return (PeliasSearchView) searchMenuItem.getActionView();
        }

        return null;
    }

    public MenuItem getSearchMenu() {
        return searchMenuItem;
    }

    public void setupAdapter(SearchView searchView) {
        if (autoCompleteAdapter == null) {
            autoCompleteAdapter = new AutoCompleteAdapter(getSupportActionBar().getThemedContext(),
                    this, app.getColumns(), getSupportFragmentManager());
            autoCompleteAdapter.setSearchView(searchView);
            autoCompleteAdapter.setMapFragment(mapFragment);
        }

        getAutoCompleteListView().setAdapter(autoCompleteAdapter);
    }

    private void initMapController() {
        mapController.setActivity(this);
    }

    public void hideActionBar() {
        collapseSearchView();
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    public void collapseSearchView() {
        MenuItem searchMenu = getSearchMenu();
        if (searchMenu != null) {
            searchMenu.collapseActionView();
        }
    }

    public void enableActionbar() {
        enableActionbar = true;
    }

    public void disableActionbar() {
        enableActionbar = false;
    }

    public void showActionBar() {
        if (!getSupportActionBar().isShowing() && enableActionbar) {
            getSupportActionBar().show();
        }
    }

    public boolean executeSearchOnMap(String query) {
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setSuggestionsAdapter(null);

        PagerResultsFragment pagerResultsFragment = PagerResultsFragment.newInstance(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.pager_results_container, pagerResultsFragment,
                        PagerResultsFragment.TAG)
                .commit();
        return pagerResultsFragment.executeSearchOnMap(getSearchView(), query);
    }

    public void setAccessToken(Token accessToken) {
        app.setAccessToken(accessToken);
    }

    public void updateView() {
        bus.post(new ViewUpdateEvent());
    }

    private void initAlarm() {
        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(this, DataUploadService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

        int hourInMillis = 60 * 60 * 1000;
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), hourInMillis, pintent);
    }

    private void uploadTraces() {
        Intent uploadIntent = new Intent(this, DataUploadService.class);
        startService(uploadIntent);
    }

    private void initSavedSearches() {
        SharedPreferences prefs = getDefaultSharedPreferences(this);
        savedSearch.deserialize(prefs.getString(SavedSearch.TAG, ""));
    }

    private void persistSavedSearches() {
        SharedPreferences prefs = getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SavedSearch.TAG, savedSearch.serialize());
        editor.commit();
    }

    public void locateButtonAction(View view) {
        app.activateMoveMapToLocation();
        mapFragment.centerOnCurrentLocation();
    }

    @Subscribe
    public void onRoutePreviewEvent(RoutePreviewEvent event) {
        final RoutePreviewFragment routePreviewFragment =
                RoutePreviewFragment.newInstance(this, event.getSimpleFeature());
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .add(R.id.routes_preview_container, routePreviewFragment, RoutePreviewFragment.TAG)
                .commitAllowingStateLoss();
        promptForGPSIfNotEnabled();
    }

    public AutoCompleteListView getAutoCompleteListView() {
        if (autoCompleteListView == null) {
            autoCompleteListView = (AutoCompleteListView) findViewById(R.id.auto_complete);
            autoCompleteListView.setEmptyView(findViewById(android.R.id.empty));
        }

        return autoCompleteListView;
    }

    private void restoreCurrentSearchTerm() {
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        if (app.getCurrentSearchTerm() != null) {
            searchMenuItem.expandActionView();
            searchView.setQuery(app.getCurrentSearchTerm(), false);
        }
    }

    private void saveCurrentSearchTerm() {
        if (searchMenuItem == null) {
            return;
        }

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        if (searchView != null) {
            if (searchMenuItem.isActionViewExpanded()) {
                app.setCurrentSearchTerm(searchView.getQuery().toString());
            } else {
                app.setCurrentSearchTerm(null);
            }
        }
    }
}
