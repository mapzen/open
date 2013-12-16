package com.mapzen.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.mapzen.AutoCompleteCursor;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.adapters.AutoCompleteAdapter;
import com.mapzen.entity.Feature;
import com.mapzen.fragment.MapFragment;
import com.mapzen.fragment.SearchResultsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oscim.android.MapActivity;
import org.oscim.map.Map;

import java.util.ArrayList;

import static android.provider.BaseColumns._ID;
import static com.mapzen.MapzenApplication.LOG_TAG;
import static com.mapzen.MapzenApplication.PELIAS_TEXT;

public class BaseActivity extends MapActivity
        implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
    private AutoCompleteAdapter autoCompleteAdapter;
    private RequestQueue queue;
    private MenuItem menuItem;
    private MapzenApplication app;
    private FragmentManager fragmentManager;
    private MapFragment mapFragment;
    private SearchResultsFragment searchResultsFragment;

    private final String[] columns = {
        _ID, PELIAS_TEXT
    };

    public Map getMap() {
        return mMap;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        app = MapzenApplication.getApp(this);
        queue = Volley.newRequestQueue(getApplicationContext());
        fragmentManager = getSupportFragmentManager();
        setContentView(R.layout.base);
        mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map_fragment);
        searchResultsFragment = (SearchResultsFragment) fragmentManager.findFragmentById(R.id.search_results_fragment);
        // TODO remove fugly HACK
        searchResultsFragment.setMapFragment(mapFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                ArrayList<Feature> features = bundle.getParcelableArrayList("features");
                int pos = app.getCurrentPagerPosition();
                searchResultsFragment.setSearchResults(features, pos);
                Feature feature = bundle.getParcelable("feature");
                if (feature != null) {
                    showPlace(feature, false);
                }
            }
        }
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
        searchView.setOnQueryTextListener(this);
        if (!app.getCurrentSearchTerm().isEmpty()) {
            menuItem.expandActionView();
            Log.v(LOG_TAG, "search: " + app.getCurrentSearchTerm());
            searchView.setQuery(app.getCurrentSearchTerm(), false);
            searchView.clearFocus();
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void setupAdapter(SearchView searchView) {
        if (autoCompleteAdapter == null) {
            AutoCompleteCursor cursor = new AutoCompleteCursor(columns);
            autoCompleteAdapter = new AutoCompleteAdapter(getActionBar().getThemedContext(), cursor);
            autoCompleteAdapter.setSearchView(searchView);
            autoCompleteAdapter.setMap(mapFragment);
            autoCompleteAdapter.setSearchResults(searchResultsFragment);
        }
        searchView.setSuggestionsAdapter(autoCompleteAdapter);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        final SearchView searchView = (SearchView) menuItem.getActionView();
        return searchResultsFragment.executeSearchOnMap(getMap(), searchView, query);
    }

    private void clearSearchText() {
        app.setCurrentSearchTerm("");
        final SearchView searchView = (SearchView) menuItem.getActionView();
        assert searchView != null;
        searchView.setQuery("", false);
        searchView.clearFocus();
    }

    public SearchView getSearchView() {
        return (SearchView) menuItem.getActionView();
    }

    private Response.Listener<JSONObject> getAutocompleteSuccessResponseListener() {
        final AutoCompleteCursor cursor = new AutoCompleteCursor(columns);
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.v(LOG_TAG, jsonObject.toString());
                JSONArray jsonArray = new JSONArray();
                try {
                    jsonArray = jsonObject.getJSONArray("features");
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.toString());
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        cursor.addRow(new Object[]{ i, jsonArray.getJSONObject(i)});
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.toString());
                    }
                }
                autoCompleteAdapter.swapCursor(cursor);
            }
        };
    }

    private Response.ErrorListener getAutocompleteErrorResponseListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        };
    }

    public boolean onQueryTextChange(String newText) {
        Log.v(LOG_TAG, "search: " + app.getCurrentSearchTerm());
        if (!newText.isEmpty()) {
            JsonObjectRequest jsonObjectRequest = Feature.suggest(newText,
                    getAutocompleteSuccessResponseListener(), getAutocompleteErrorResponseListener());
            queue.add(jsonObjectRequest);
        }
        return true;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        searchResultsFragment.hideResultsWrapper();
        return true;
    }

    public void showPlace(Feature feature, boolean clearSearch) {
        searchResultsFragment.flipTo(feature);
        if (clearSearch) {
            clearSearchText();
        }
        mapFragment.centerOn(feature);
    }

    public void enqueueApiRequest(Request<?> request) {
        queue.add(request);
    }
}
