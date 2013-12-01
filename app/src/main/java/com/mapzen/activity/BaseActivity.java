package com.mapzen.activity;

import android.app.*;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.*;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.mapzen.R;
import com.mapzen.entity.Place;
import com.mapzen.fragment.SearchResultsFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;

import static com.mapzen.MapzenApplication.LOG_TAG;

public class BaseActivity extends Activity {
    private SlidingMenu slidingMenu;
    private GeoNamesAdapter geoNamesAdapter;
    private RequestQueue queue;

    private static final String[] COLUMNS = {
        BaseColumns._ID,
        SearchManager.SUGGEST_COLUMN_TEXT_1,
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(getApplicationContext());
        setContentView(R.layout.base);

        setupActionbar();
        setupSlidingMenu();
    }

    private void setupActionbar() {
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
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
        final SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        final SearchResultsFragment searchResultsFragment =
                (SearchResultsFragment) getFragmentManager().findFragmentById(R.id.search_results_fragment);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String baseUrl = "http://open.mapquestapi.com/nominatim/v1/search.php?format=json&q=";
                MapView mapView;
                mapView = (MapView)findViewById(R.id.map);
                BoundingBoxE6 boundingBox = mapView.getBoundingBox();
                double[] box = {
                        ((double) boundingBox.getLonWestE6()) / 1E6,
                        ((double) boundingBox.getLatNorthE6()) / 1E6,
                        ((double) boundingBox.getLonEastE6()) / 1E6,
                        ((double) boundingBox.getLatSouthE6()) / 1E6,
                };
                String url = baseUrl + query + "&bounded=1&viewbox=" + Double.toString(box[0]) +
                        "," + Double.toString(box[1]) + "," + Double.toString(box[2]) + "," + Double.toString(box[3]);
                Log.v(LOG_TAG, url);
                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        Log.v(LOG_TAG, jsonArray.toString());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Place place = null;
                            try {
                                place = Place.fromJson(jsonArray.getJSONObject(i));
                            } catch (JSONException e) {
                                Log.e(LOG_TAG, e.toString());
                            }
                            Log.v(LOG_TAG, place.getDisplayName());
                            searchResultsFragment.add(place);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                    }
                });
                queue.add(jsonArrayRequest);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                final MatrixCursor cursor = new MatrixCursor(COLUMNS);

                String autocompleteUrl = "http://api-pelias-test.mapzen.com/suggest/" + newText;
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
        if (geoNamesAdapter == null) {
            MatrixCursor cursor = new MatrixCursor(COLUMNS);
            geoNamesAdapter = new GeoNamesAdapter(getActionBar().getThemedContext(), cursor);
        }
        searchView.setSuggestionsAdapter(geoNamesAdapter);
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