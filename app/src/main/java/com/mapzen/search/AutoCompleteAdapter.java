package com.mapzen.search;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Feature;
import com.mapzen.fragment.MapFragment;
import com.mapzen.util.Logger;
import com.mapzen.util.VolleyHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.mapzen.MapzenApplication.PELIAS_TEXT;
import static com.mapzen.entity.Feature.FEATURES;
import static com.mapzen.entity.Feature.NAME;

public class AutoCompleteAdapter extends CursorAdapter implements SearchView.OnQueryTextListener {
    public static final int AUTOCOMPLETE_THRESHOLD = 3;
    private SearchView searchView;
    private MapFragment mapFragment;
    private BaseActivity act;
    private MapzenApplication app;
    private FragmentManager fragmentManager;

    public AutoCompleteAdapter(Context context, BaseActivity act, String[] columns,
            FragmentManager fragmentManager) {
        super(context, new MatrixCursor(columns), 0);
        this.act = act;
        this.app = (MapzenApplication) act.getApplication();
        this.fragmentManager = fragmentManager;
    }

    public void setSearchView(SearchView view) {
        this.searchView = view;
    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    @Override
    public View newView(final Context c, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(c);
        View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        assert v != null;
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView tv = (TextView) view;
                Feature feature = (Feature) tv.getTag();
                app.setCurrentSearchTerm("");
                searchView.setQuery("", false);
                searchView.clearFocus();
                searchView.setQuery(tv.getText(), false);
                mapFragment.clearMarkers();
                mapFragment.updateMap();
                mapFragment.centerOn(feature);
                PagerResultsFragment pagerResultsFragment = PagerResultsFragment.newInstance(act);
                fragmentManager.beginTransaction()
                        .replace(R.id.pager_results_container, pagerResultsFragment,
                                PagerResultsFragment.TAG).commit();
                fragmentManager.executePendingTransactions();
                pagerResultsFragment.add(feature);
                pagerResultsFragment.displayResults(1, 0);
            }
        });
        parent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                dismissKeyboard();
                return false;
            }
        });
        return v;
    }

    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) act
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }

    @Override
    public void bindView(View view, Context c, Cursor cursor) {
        TextView tv = (TextView) view;
        final int textIndex = cursor.getColumnIndex(PELIAS_TEXT);
        Feature feature = new Feature();
        try {
            feature.buildFromJSON(new JSONObject(cursor.getString(textIndex)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        tv.setTag(feature);
        tv.setText(feature.getProperty(NAME));
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return act.executeSearchOnMap(query);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        act.setupAdapter(searchView);

        Logger.d("onQueryTextChange: text" + newText);
        if (newText.length() < AUTOCOMPLETE_THRESHOLD) {
            Logger.d("search: newText shorter than 3 "
                    + "was:" + String.valueOf(newText.length()));
            return true;
        }

        Logger.d("search: term newText: " + newText);
        if (!newText.isEmpty()) {
            Logger.d("search: autocomplete starts");
            JsonObjectRequest jsonObjectRequest = Feature.suggest(newText,
                    getAutoCompleteSuccessResponseListener(),
                    getAutoCompleteErrorResponseListener());
            app.enqueueApiRequest(jsonObjectRequest);
            Logger.d("search: autocomplete request enqueued");
        }
        return true;
    }

    private Response.Listener<JSONObject> getAutoCompleteSuccessResponseListener() {
        final MatrixCursor cursor = new MatrixCursor(app.getColumns());
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Logger.d("search: autocomplete request returned");
                Logger.d("request: success" + jsonObject.toString());
                JSONArray jsonArray = new JSONArray();
                try {
                    jsonArray = jsonObject.getJSONArray(FEATURES);
                } catch (JSONException e) {
                    Logger.e(e.toString());
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        cursor.addRow(new Object[]{i, jsonArray.getJSONObject(i)});
                    } catch (JSONException e) {
                        Logger.e(e.toString());
                    }
                }
                Logger.d("search: swapping cursor");
                swapCursor(cursor);
            }
        };
    }

    private Response.ErrorListener getAutoCompleteErrorResponseListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                String errorMsg = VolleyHelper.Error.getMessage(volleyError, act);
                Logger.e("request: error: " + errorMsg);
            }
        };
    }
}
