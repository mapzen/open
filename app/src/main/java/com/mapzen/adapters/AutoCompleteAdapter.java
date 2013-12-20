package com.mapzen.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
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
import com.mapzen.AutoCompleteCursor;
import com.mapzen.MapzenApplication;
import com.mapzen.entity.Feature;
import com.mapzen.fragment.MapFragment;
import com.mapzen.fragment.SearchResultsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.mapzen.MapzenApplication.LOG_TAG;
import static com.mapzen.MapzenApplication.PELIAS_TEXT;
import static com.mapzen.MapzenApplication.getApp;
import static com.mapzen.entity.Feature.FEATURES;
import static com.mapzen.entity.Feature.TITLE;

public class AutoCompleteAdapter extends CursorAdapter implements SearchView.OnQueryTextListener {
    private SearchView searchView;
    private MapFragment mapFragment;
    private SearchResultsFragment searchResultsFragment;
    private Context context;

    public AutoCompleteAdapter(Context context) {
        super(context, new AutoCompleteCursor(getApp(context).getColumns()), 0);
        this.context = context;
    }

    public void setSearchView(SearchView view) {
        this.searchView = view;
    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    public void setSearchResultsFragment(SearchResultsFragment searchResultsFragment) {
        this.searchResultsFragment = searchResultsFragment;
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
                getApp(c).setCurrentSearchTerm("");
                searchView.setQuery("", false);
                searchView.clearFocus();
                searchView.setQuery(tv.getText(), false);
                searchResultsFragment.hideResultsWrapper();
                mapFragment.centerOn(feature);
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
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
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
        tv.setText(feature.getProperty(TITLE));
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return searchResultsFragment.executeSearchOnMap(searchView, query);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.v(LOG_TAG, "search: " + MapzenApplication.getApp(context).getCurrentSearchTerm());
        if (!newText.isEmpty()) {
            JsonObjectRequest jsonObjectRequest = Feature.suggest(newText,
                    getAutoCompleteSuccessResponseListener(), getAutoCompleteErrorResponseListener());
            getApp(context).getQueue().add(jsonObjectRequest);
        }
        return true;
    }

    private Response.Listener<JSONObject> getAutoCompleteSuccessResponseListener() {
        final AutoCompleteCursor cursor = new AutoCompleteCursor(getApp(context).getColumns());
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.v(LOG_TAG, jsonObject.toString());
                JSONArray jsonArray = new JSONArray();
                try {
                    jsonArray = jsonObject.getJSONArray(FEATURES);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.toString());
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        cursor.addRow(new Object[] { i, jsonArray.getJSONObject(i)});
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.toString());
                    }
                }
                swapCursor(cursor);
            }
        };
    }

    private Response.ErrorListener getAutoCompleteErrorResponseListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        };
    }
}

