package com.mapzen.activity;

import com.mapzen.MapController;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.android.ps.location.LocationClient;
import com.mapzen.android.ps.location.LocationListener;
import com.mapzen.android.ps.location.LocationRequest;
import com.mapzen.core.SettingsFragment;
import com.mapzen.fragment.ListResultsFragment;
import com.mapzen.fragment.MapFragment;
import com.mapzen.search.AutoCompleteAdapter;
import com.mapzen.search.OnPoiClickListener;
import com.mapzen.search.PagerResultsFragment;
import com.mapzen.util.DatabaseHelper;
import com.mapzen.util.DebugDataSubmitter;
import com.mapzen.util.Logger;
import com.mapzen.util.MapzenProgressDialogFragment;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.OkHttpClient;

import org.oscim.android.MapActivity;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.map.Map;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import static com.mapzen.MapController.getMapController;
import static com.mapzen.android.ps.location.LocationClient.ConnectionCallbacks;

public class BaseActivity extends MapActivity {
    public static final int LOCATION_INTERVAL = 1000;
    public static final String PLAY_SERVICE_FAIL_MESSAGE = "Your device cannot be located";
    public static final String COM_MAPZEN_UPDATES_LOCATION = "com.mapzen.updates.location";
    public static final String DEBUG_DATA_ENDPOINT = "http://snitchmedia.com/upload.php";
    protected DatabaseHelper dbHelper;
    protected DebugDataSubmitter debugDataSubmitter;
    LocationClient locationHelper;
    private AutoCompleteAdapter autoCompleteAdapter;
    private MenuItem menuItem;
    private MapzenApplication app;
    private MapFragment mapFragment;
    private MapzenProgressDialogFragment progressDialogFragment;
    private boolean updateMapLocation = true;
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (updateMapLocation) {
                getMapController().setLocation(location);
                mapFragment.findMe();
            }
            Intent toBroadcast = new Intent(COM_MAPZEN_UPDATES_LOCATION);
            toBroadcast.putExtra("location", location);
            sendBroadcast(toBroadcast);
        }
    };
    protected ConnectionCallbacks connectionCallback = new ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            getMapController().setZoomLevel(MapController.DEFAULT_ZOOMLEVEL);
            final Location location = locationHelper.getLastLocation();
            Logger.d("Last known location: " + location);

            if (location != null) {
                getMapController().setLocation(location);
                mapFragment.findMe();
            } else {
                Toast.makeText(BaseActivity.this, getString(R.string.waiting),
                        Toast.LENGTH_LONG).show();
            }

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(LOCATION_INTERVAL);
            locationHelper.requestLocationUpdates(locationRequest, locationListener);
        }

        @Override
        public void onDisconnected() {
            Logger.d("LocationHelper disconnected.");
        }
    };
    private SQLiteDatabase db;
    private TextView debugView;
    private boolean enableActionbar = true;

    public void deactivateMapLocationUpdates() {
        updateMapLocation = false;
    }

    public void activateMapLocationUpdates() {
        updateMapLocation = true;
    }

    public LocationListener getLocationListener() {
        return locationListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        app = (MapzenApplication) getApplication();
        setContentView(R.layout.base);
        initMapFragment();
        progressDialogFragment = new MapzenProgressDialogFragment();
        dbHelper = new DatabaseHelper(this);
        initMapController();
        initLocationClient();
        initDebugView();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationHelper.disconnect();
        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationHelper.connect();
        db = dbHelper.getWritableDatabase();
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public boolean isInDebugMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(getString(R.string.settings_key_debug), false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            final Fragment fragment = getSupportFragmentManager()
                    .findFragmentByTag(ListResultsFragment.TAG);
            if (fragment != null) {
                return fragment.onOptionsItemSelected(item);
            }
        } else if (item.getItemId() == R.id.settings) {
            final PreferenceFragment fragment = SettingsFragment.newInstance(this);
            getFragmentManager().beginTransaction()
                    .add(R.id.settings, fragment, SettingsFragment.TAG)
                    .addToBackStack(null)
                    .commit();
        } else if (item.getItemId() == R.id.phone_home) {
            initDebugDataSubmitter();
            debugDataSubmitter.run();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showProgressDialog() {
        if (!progressDialogFragment.isAdded()) {
            progressDialogFragment.show(getSupportFragmentManager(), "dialog");
        }
    }

    public void dismissProgressDialog() {
        progressDialogFragment.dismiss();
    }

    public MapzenProgressDialogFragment getProgressDialogFragment() {
        return progressDialogFragment;
    }

    private void initLocationClient() {
        locationHelper = new LocationClient(this, connectionCallback);
    }

    private void initMapFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map_fragment);
        mapFragment.setAct(this);
        mapFragment.setMap(getMap());
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
                final PagerResultsFragment pagerResultsFragment = getPagerResultsFragment();
                final ListResultsFragment listResultsFragment = (ListResultsFragment)
                        getSupportFragmentManager().findFragmentByTag(ListResultsFragment.TAG);
                if (pagerResultsFragment != null && pagerResultsFragment.isAdded()
                        && listResultsFragment == null) {
                    getSupportFragmentManager().beginTransaction().remove(pagerResultsFragment)
                            .commit();
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
    public void onBackPressed() {
        SettingsFragment fragment = (SettingsFragment) getFragmentManager()
                .findFragmentByTag(SettingsFragment.TAG);
        if (fragment != null && fragment.isAdded()) {
            getFragmentManager().beginTransaction()
                    .detach(fragment)
                    .commit();
        } else {
            super.onBackPressed();
        }
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

    public void setupAdapter(SearchView searchView) {
        if (autoCompleteAdapter == null) {
            autoCompleteAdapter = new AutoCompleteAdapter(getActionBar().getThemedContext(),
                    this, app.getColumns(), getSupportFragmentManager());
            autoCompleteAdapter.setSearchView(searchView);
            autoCompleteAdapter.setMapFragment(mapFragment);
        }

        searchView.setSuggestionsAdapter(autoCompleteAdapter);
    }

    private void initMapController() {
        MapController mapController = getMapController();
        mapController.setActivity(this);
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

    public void enableActionbar() {
        enableActionbar = true;
    }

    public void disableActionbar() {
        enableActionbar = false;
    }

    public void showActionBar() {
        if (!getActionBar().isShowing() && enableActionbar) {
            getActionBar().show();
        }
    }

    public boolean executeSearchOnMap(String query) {
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSuggestionsAdapter(null);

        PagerResultsFragment pagerResultsFragment = PagerResultsFragment.newInstance(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.pager_results_container, pagerResultsFragment,
                        PagerResultsFragment.TAG)
                .commit();

        return pagerResultsFragment.executeSearchOnMap(getSearchView(), query);
    }

    private void initDebugView() {
        debugView = (TextView) findViewById(R.id.debugging);
        if (isInDebugMode()) {
            debugView.setVisibility(View.VISIBLE);
        }
    }

    public void writeToDebugView(String msg) {
        debugView.setText(msg);
    }

    public void appendToDebugView(String msg) {
        String fullText = debugView.getText().toString() + "," + msg;
        debugView.setText(fullText);
    }

    public void initDebugDataSubmitter() {
        debugDataSubmitter = new DebugDataSubmitter(this);
        debugDataSubmitter.setClient(new OkHttpClient());
        debugDataSubmitter.setEndpoint(DEBUG_DATA_ENDPOINT);
        debugDataSubmitter.setFile(new File(getDb().getPath()));
    }
}
