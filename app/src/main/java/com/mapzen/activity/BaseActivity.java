package com.mapzen.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.mapzen.util.MapzenProgressDialogFragment;

import org.oscim.android.MapActivity;
import org.oscim.map.Map;

import static com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import static com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class BaseActivity extends MapActivity {
    public static final int LOCATION_INTERVAL = 5000;
    public static final String PLAY_SERVICE_FAIL_MESSAGE = "Your device cannot be located";
    public static final String COM_MAPZEN_UPDATES_LOCATION = "com.mapzen.updates.location";
    private AutoCompleteAdapter autoCompleteAdapter;
    private MenuItem menuItem;
    private MapzenApplication app;
    private MapFragment mapFragment;
    private PagerResultsFragment pagerResultsFragment;
    public static final String SEARCH_RESULTS_STACK = "search_results_stack";
    public static final String ROUTE_STACK = "route_stack";
    private MapzenProgressDialogFragment progressDialogFragment;
    private LocationClient locationClient;
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mapFragment.setUserLocation(location);
            Intent toBroadcast = new Intent(COM_MAPZEN_UPDATES_LOCATION);
            toBroadcast.putExtra("location", location);
            sendBroadcast(toBroadcast);
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
        progressDialogFragment = new MapzenProgressDialogFragment();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            final Fragment fragment = getSupportFragmentManager()
                    .findFragmentByTag(ListResultsFragment.TAG);
            if (fragment != null) {
                return fragment.onOptionsItemSelected(item);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void showProgressDialog() {
        progressDialogFragment.show(getSupportFragmentManager(), "dialog");
    }

    public void dismissProgressDialog() {
        progressDialogFragment.dismiss();
    }

    public MapzenProgressDialogFragment getProgressDialogFragment() {
        return progressDialogFragment;
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
        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                ListResultsFragment listResultsFragment = (ListResultsFragment)
                        getSupportFragmentManager().findFragmentByTag(ListResultsFragment.TAG);
                if (pagerResultsFragment != null && pagerResultsFragment.isAdded()
                        && listResultsFragment == null) {
                    onBackPressed();
                }
                return true;
            }
        });

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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ListResultsFragment.TAG);
        if (fragment != null && fragment.isAdded()) {
            collapseSearchView();
            menu.findItem(R.id.search).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
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
            autoCompleteAdapter = new AutoCompleteAdapter(getActionBar().getThemedContext(),
                    this, app.getColumns());
            autoCompleteAdapter.setSearchView(searchView);
            autoCompleteAdapter.setMapFragment(mapFragment);
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
        collapseSearchView();
        getActionBar().hide();
    }

    public void collapseSearchView() {
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

    public LocationClient getLocationClient() {
        return locationClient;
    }

    public boolean executeSearchOnMap(String query) {
        if (pagerResultsFragment == null) {
            pagerResultsFragment = PagerResultsFragment.newInstance(this);
        }

        if (!pagerResultsFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.pager_results_container, pagerResultsFragment,
                            PagerResultsFragment.TAG)
                    .commit();
        }

        return pagerResultsFragment.executeSearchOnMap(getSearchView(), query);
    }
}
