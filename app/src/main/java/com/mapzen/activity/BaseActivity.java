package com.mapzen.activity;

import android.app.SearchManager;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.adapters.AutoCompleteAdapter;
import com.mapzen.entity.Feature;
import com.mapzen.fragment.ListResultsFragment;
import com.mapzen.fragment.MapFragment;
import com.mapzen.fragment.PagerResultsFragment;
import com.mapzen.util.Logger;

import org.oscim.android.MapActivity;
import org.oscim.map.Map;

import static com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import static com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class BaseActivity extends MapActivity
        implements MenuItem.OnActionExpandListener {
    public static final int LOCATION_INTERVAL = 5000;
    public static final String PLAY_SERVICE_FAIL_MESSAGE = "Your device cannot be located";
    private AutoCompleteAdapter autoCompleteAdapter;
    private MenuItem menuItem;
    private MapzenApplication app;
    private MapFragment mapFragment;
    private PagerResultsFragment pagerResultsFragment;
    public static final String SEARCH_RESULTS_STACK = "search_results_stack";
    public static final String ROUTE_STACK = "route_stack";
    private LocationClient locationClient;
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Logger.d("Location: receiving new location: " + location.toString());
            mapFragment.setUserLocation(location);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        app = (MapzenApplication) getApplication();
        setContentView(R.layout.base);
        initMapFragment();
        initLocationClient();
        pagerResultsFragment = PagerResultsFragment.newInstance(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationClient.connect();
    }

    private ConnectionCallbacks connectionCallback = new ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            Location location = locationClient.getLastLocation();
            mapFragment.setUserLocation(location);
            app.setLocation(location);
            Logger.d("Location: last location: " + location.toString());
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(LOCATION_INTERVAL);
            locationRequest.setPriority(PRIORITY_HIGH_ACCURACY);
            locationClient.requestLocationUpdates(locationRequest, locationListener);
        }

        @Override
        public void onDisconnected() {
            locationClient.removeLocationUpdates(locationListener);
        }
    };

    private OnConnectionFailedListener onConnectionFailedListener = new OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Toast.makeText(getApplicationContext(), PLAY_SERVICE_FAIL_MESSAGE, Toast.LENGTH_LONG).show();
        }
    };

    private void initLocationClient() {
        Logger.d("Location: initializing");
        locationClient = new LocationClient(this, connectionCallback, onConnectionFailedListener);
        locationClient.connect();
    }

    private void initMapFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map_fragment);
        mapFragment.setAct(this);
        mapFragment.setMap(getMap());
    }

    public MapFragment getMapFragment() {
        return mapFragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        menuItem = menu.findItem(R.id.search);
        menuItem.setOnActionExpandListener(this);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        setupAdapter(searchView);
        searchView.setOnQueryTextListener(autoCompleteAdapter);

        if (!app.getCurrentSearchTerm().isEmpty()) {
            menuItem.expandActionView();
            Logger.d("search: " + app.getCurrentSearchTerm());
            searchView.setQuery(app.getCurrentSearchTerm(), false);
            searchView.clearFocus();
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

    private void handleGeoIntent(SearchView searchView, Uri data) {
        if (data.toString().contains("q=")) {
            menuItem.expandActionView();
            String queryString = Uri.decode(data.toString().split("q=")[1]);
            app.setCurrentSearchTerm(queryString);
            searchView.setQuery(queryString, true);
        }
    }

    private void handleMapsIntent(SearchView searchView, Uri data) {
        if (data.toString().contains("q=")) {
            menuItem.expandActionView();
            String queryString = Uri.decode(data.toString().split("q=")[1].split("@")[0]);
            app.setCurrentSearchTerm(queryString);
            searchView.setQuery(queryString, true);
        }
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    private PagerResultsFragment getActivePagerResults() {
        return (PagerResultsFragment) getSupportFragmentManager()
                .findFragmentByTag(PagerResultsFragment.PAGER_RESULTS);
    }

    private ListResultsFragment getActiveListResults() {
        return (ListResultsFragment) getSupportFragmentManager()
                .findFragmentByTag(ListResultsFragment.FULL_LIST);
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        if (getActiveListResults() != null || getActivePagerResults() != null) {
            onBackPressed();
        }
        return true;
    }

    public Map getMap() {
        return mMap;
    }

    public SearchView getSearchView() {
        return (SearchView) menuItem.getActionView();
    }

    public MenuItem getSearchMenu() {
        return menuItem;
    }

    private void setupAdapter(SearchView searchView) {
        if (autoCompleteAdapter == null) {
            autoCompleteAdapter =
                    new AutoCompleteAdapter(getActionBar().getThemedContext(),
                            this, app.getColumns());
            autoCompleteAdapter.setSearchView(searchView);
            autoCompleteAdapter.setMapFragment(mapFragment);
            autoCompleteAdapter.setPagerResultsFragment(pagerResultsFragment);
        }
        searchView.setSuggestionsAdapter(autoCompleteAdapter);
    }

    private void clearSearchText() {
        app.setCurrentSearchTerm("");
        final SearchView searchView = (SearchView) menuItem.getActionView();
        assert searchView != null;
        searchView.setQuery("", false);
        searchView.clearFocus();
    }

    public void showPlace(Feature feature, boolean clearSearch) {
        pagerResultsFragment.flipTo(feature);
        if (clearSearch) {
            clearSearchText();
        }
        mapFragment.centerOn(feature);
    }

    public PagerResultsFragment getPagerResultsFragment() {
        return pagerResultsFragment;
    }

    public void hideActionBar() {
        blurSearchMenu();
        getActionBar().hide();
    }

    public void blurSearchMenu() {
        MenuItem searchMenu = getSearchMenu();
        if (searchMenu != null) {
            searchMenu.collapseActionView();
        }
    }

    public void showActionBar() {
        if (!getActionBar().isShowing()) {
            getActionBar().show();
        }
    }
}
