package com.mapzen.activity;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import com.crashlytics.android.Crashlytics;
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

public class BaseActivity extends MapActivity
        implements MenuItem.OnActionExpandListener {
    private AutoCompleteAdapter autoCompleteAdapter;
    private MenuItem menuItem;
    private MapzenApplication app;
    private MapFragment mapFragment;
    private PagerResultsFragment pagerResultsFragment;
    public static final String SEARCH_RESULTS_STACK = "search_results_stack";
    public static final String ROUTE_STACK = "route_stack";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        app = MapzenApplication.getApp(this);
        setContentView(R.layout.base);
        initMapFragment();
        pagerResultsFragment = PagerResultsFragment.newInstance(this);
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
        return true;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    private PagerResultsFragment getActivePagerResults() {
        return (PagerResultsFragment) getSupportFragmentManager()
                .findFragmentByTag(PagerResultsFragment.PAGER_RESULTS);
    }

    private ListResultsFragment getActiveListRestults() {
        return (ListResultsFragment) getSupportFragmentManager()
                .findFragmentByTag(ListResultsFragment.FULL_LIST);
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        if (getActiveListRestults() != null || getActivePagerResults() != null) {
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
