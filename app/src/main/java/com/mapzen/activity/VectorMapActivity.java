package com.mapzen.activity;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.oscim.android.MapActivity;
import org.oscim.android.MapView;
import org.oscim.core.MapPosition;
import org.oscim.layers.tile.vector.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.theme.InternalRenderTheme;
import org.oscim.tiling.source.TileSource;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

import static android.provider.BaseColumns._ID;
import static com.mapzen.MapzenApplication.LOG_TAG;
import static com.mapzen.MapzenApplication.PELIAS_LAT;
import static com.mapzen.MapzenApplication.PELIAS_LON;
import static com.mapzen.MapzenApplication.PELIAS_PAYLOAD;
import static com.mapzen.MapzenApplication.PELIAS_TEXT;
import static com.mapzen.MapzenApplication.getLocationPosition;
import static com.mapzen.MapzenApplication.getStoredZoomLevel;
import static com.mapzen.MapzenApplication.storeMapPosition;

public class VectorMapActivity extends MapActivity implements SearchView.OnQueryTextListener {
    private SlidingMenu slidingMenu;
    private GeoNamesAdapter geoNamesAdapter;
    private RequestQueue queue;
    private MapView mMapView;
    private VectorTileLayer mBaseLayer;
    private MenuItem menuItem;

    final String[] COLUMNS = {
        _ID, PELIAS_TEXT, PELIAS_LAT, PELIAS_LON
    };

    public Map getMap() {
        return mMap;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(getApplicationContext());
        setContentView(R.layout.base);
        setupActionbar();
        setupSlidingMenu();
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
        menuItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        setupAdapter(searchView);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    private void setupAdapter(SearchView searchView) {
        if (geoNamesAdapter == null) {
            MatrixCursor cursor = new MatrixCursor(COLUMNS);
            geoNamesAdapter = new GeoNamesAdapter(getActionBar().getThemedContext(), cursor);
        }
        searchView.setSuggestionsAdapter(geoNamesAdapter);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    private Response.Listener<JSONArray> autocompleteSuccessResponseListener() {
        final MatrixCursor cursor = new MatrixCursor(COLUMNS);
        return new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                Log.v(LOG_TAG, jsonArray.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject obj = (JSONObject) jsonArray.get(i);
                        JSONObject payload = (JSONObject) obj.get(PELIAS_PAYLOAD);
                        cursor.addRow(new String[] {
                                String.valueOf(i),
                                obj.getString(PELIAS_TEXT),
                                payload.getString(PELIAS_LAT),
                                payload.getString(PELIAS_LON)
                        });
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.toString());
                    }
                }
                geoNamesAdapter.swapCursor(cursor);
            }
        };
    }

    private Response.ErrorListener autocompleteErrorResponseListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        };
    }

    public boolean onQueryTextChange(String newText) {
        String autocompleteUrl = getString(R.string.pelias_test_suggest_url) + Uri.encode(newText);
        Log.v(LOG_TAG, autocompleteUrl);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(autocompleteUrl,
                    autocompleteSuccessResponseListener(), autocompleteErrorResponseListener());
        queue.add(jsonArrayRequest);
        return true;
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

    private class GeoNamesAdapter extends CursorAdapter {
        public GeoNamesAdapter(Context context, Cursor c) {
            super(context, c, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            assert v != null;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final SearchView searchView = (SearchView) menuItem.getActionView();
                    assert searchView != null;
                    searchView.setQuery("", false);
                    searchView.clearFocus();
                    MapPosition mapPosition = (MapPosition) view.getTag();
                    mMap.setMapPosition(mapPosition);
                }
            });
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView tv = (TextView) view;
            final int textIndex = cursor.getColumnIndex(PELIAS_TEXT);
            double lat =
                    Double.parseDouble(cursor.getString(cursor.getColumnIndex(PELIAS_LAT)));
            double lon =
                    Double.parseDouble(cursor.getString(cursor.getColumnIndex(PELIAS_LON)));
            MapPosition position = new MapPosition(lat, lon, Math.pow(2, getStoredZoomLevel()));
            tv.setTag(position);
            tv.setText(cursor.getString(textIndex));
        }
    }
}
