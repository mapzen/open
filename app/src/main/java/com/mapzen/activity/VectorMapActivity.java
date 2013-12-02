package com.mapzen.activity;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.mapzen.MapzenApplication;
import com.mapzen.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.oscim.android.MapActivity;
import org.oscim.android.MapView;
import org.oscim.layers.tile.vector.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.theme.InternalRenderTheme;
import org.oscim.tiling.source.TileSource;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

import static com.mapzen.MapzenApplication.LOG_TAG;

public class VectorMapActivity extends MapActivity {
    private SlidingMenu slidingMenu;
    private GeoNamesAdapter geoNamesAdapter;
    private RequestQueue queue;

    MapView mMapView;
    VectorTileLayer mBaseLayer;

    final String[] COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(getApplicationContext());
        setContentView(R.layout.vector);
        setupActionbar();
        setupSlidingMenu();
        setupMap();
    }

    @Override
    public void onBackPressed() {
        if ( slidingMenu.isMenuShowing() ) {
            slidingMenu.toggle();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            this.slidingMenu.toggle();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.slidingMenu.toggle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        setupAdapter(searchView);
        setupAutocompleteSearch(searchView);
        return true;
    }

    private void setupAdapter(SearchView searchView) {
        if (geoNamesAdapter == null) {
            MatrixCursor cursor = new MatrixCursor(COLUMNS);
            geoNamesAdapter = new GeoNamesAdapter(getActionBar().getThemedContext(), cursor);
        }
        searchView.setSuggestionsAdapter(geoNamesAdapter);
    }

    private void setupAutocompleteSearch(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                final MatrixCursor cursor = new MatrixCursor(COLUMNS);

                String autocompleteUrl = getString(R.string.pelias_test_suggest_url) + newText;
                Log.v(LOG_TAG, autocompleteUrl);
                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(autocompleteUrl, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        Log.v(LOG_TAG, jsonArray.toString());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj;
                            try {
                                obj = (JSONObject) jsonArray.get(i);
                                cursor.addRow(new String[] {String.valueOf(i), obj.getString("text")});
                            } catch (Exception e) {
                            }
                        }
                        geoNamesAdapter.swapCursor(cursor);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                    }
                });
                queue.add(jsonArrayRequest);
                return true;
            }
        });
    }

    private void setupSlidingMenu() {
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        slidingMenu.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.slidingmenu_shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        slidingMenu.setMenu(R.layout.slidingmenu);
    }

    private void setupActionbar() {
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setupMap() {
        mMapView = (MapView) findViewById(R.id.map);
        TileSource tileSource = new OSciMap4TileSource();
        tileSource.setOption(getString(R.string.tiles_source_url_key), getString(R.string.tiles_source_url));
        mBaseLayer = mMap.setBaseMap(tileSource);
        mMap.getLayers().add(new BuildingLayer(mMap, mBaseLayer.getTileLayer()));
        mMap.getLayers().add(new LabelLayer(mMap, mBaseLayer.getTileLayer()));
        mMap.setTheme(InternalRenderTheme.DEFAULT);
        mMap.setMapPosition(MapzenApplication.getLocationPosition(this));
    }

private class GeoNamesAdapter extends CursorAdapter {
    public GeoNamesAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = (TextView) view;
        final int textIndex = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
        tv.setText(cursor.getString(textIndex));
    }
}
}
