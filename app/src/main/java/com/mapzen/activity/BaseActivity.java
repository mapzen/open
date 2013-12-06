package com.mapzen.activity;

import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.entity.Place;
import com.mapzen.fragment.MapFragment;
import com.mapzen.fragment.SearchResultsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oscim.android.MapActivity;
import org.oscim.android.canvas.AndroidBitmap;
import org.oscim.core.BoundingBox;
import org.oscim.core.MapPosition;
import org.oscim.layers.marker.ItemizedIconLayer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.map.Map;

import java.io.InputStream;
import java.util.ArrayList;

import static android.provider.BaseColumns._ID;
import static com.mapzen.MapzenApplication.LOG_TAG;
import static com.mapzen.MapzenApplication.PELIAS_LAT;
import static com.mapzen.MapzenApplication.PELIAS_LON;
import static com.mapzen.MapzenApplication.PELIAS_PAYLOAD;
import static com.mapzen.MapzenApplication.PELIAS_TEXT;

public class BaseActivity extends MapActivity
        implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
    private GeoNamesAdapter geoNamesAdapter;
    private RequestQueue queue;
    private MenuItem menuItem;
    private MapzenApplication app;
    private FragmentManager fragmentManager;
    private MapFragment mapFragment;
    private SearchResultsFragment searchResultsFragment;

    final String[] COLUMNS = {
        _ID, PELIAS_TEXT, PELIAS_LAT, PELIAS_LON
    };

    public Map getMap() {
        return mMap;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = MapzenApplication.getApp(this);
        queue = Volley.newRequestQueue(getApplicationContext());
        fragmentManager = getFragmentManager();
        setContentView(R.layout.base);
    }

    public MapFragment getMapFragment() {
        mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map_fragment);
        return mapFragment;
    }

    public SearchResultsFragment getSearchResultsFragment() {
        searchResultsFragment = (SearchResultsFragment) fragmentManager.findFragmentById(R.id.search_results_fragment);
        return searchResultsFragment;
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
        JsonArrayRequest jsonArrayRequest =
                Place.search(mMap, query, getSearchSuccessResponseListener(),
                        getSearchErrorResponseListener());
        queue.add(jsonArrayRequest);
        return true;
    }

    private Response.ErrorListener getSearchErrorResponseListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        };
    }

    private Response.Listener<JSONArray> getSearchSuccessResponseListener() {
        return new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                Log.v(LOG_TAG, jsonArray.toString());
                searchResultsFragment = getSearchResultsFragment();
                searchResultsFragment.clearAll();
                MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
                assert mapFragment != null;
                ItemizedIconLayer<MarkerItem> poiLayer = mapFragment.getPoiLayer();
                poiLayer.removeAllItems();
                for (int i = 0; i < jsonArray.length(); i++) {
                    Place place = null;
                    try {
                        place = Place.fromJson(jsonArray.getJSONObject(i));
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.toString());
                    }
                    Log.v(LOG_TAG, place.getDisplayName());
                    MarkerItem m = place.getMarker();
                    m.setMarker(mapFragment.getDefaultMarkerSymbol());
                    m.setMarkerHotspot(MarkerItem.HotspotPlace.CENTER);
                    poiLayer.addItem(place.getMarker());

                    searchResultsFragment.add(place);
                }
                searchResultsFragment.notifyNewData();
                searchResultsFragment.showResultsWrapper();
                mMap.render();
                final SearchView searchView = (SearchView) menuItem.getActionView();
                assert searchView != null;
                searchView.clearFocus();
            }
        };
    }

    private void clearSearchText() {
        final SearchView searchView = (SearchView) menuItem.getActionView();
        assert searchView != null;
        searchView.setQuery("", false);
        searchView.clearFocus();
    }

    private Response.Listener<JSONArray> getAutocompleteSuccessResponseListener() {
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

    private Response.ErrorListener getAutocompleteErrorResponseListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        };
    }

    public boolean onQueryTextChange(String newText) {
        JsonArrayRequest jsonArrayRequest = Place.suggest(newText,
                    getAutocompleteSuccessResponseListener(), getAutocompleteErrorResponseListener());
        queue.add(jsonArrayRequest);
        return true;
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
                    clearSearchText();
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
            MapPosition position = new MapPosition(lat, lon, Math.pow(2, app.getStoredZoomLevel()));
            tv.setTag(position);
            tv.setText(cursor.getString(textIndex));
        }
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem menuItem) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
        searchResultsFragment.hideResultsWrapper();
        return true;
    }
}
