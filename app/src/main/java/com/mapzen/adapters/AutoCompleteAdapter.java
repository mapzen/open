package com.mapzen.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.TextView;

import com.mapzen.entity.Feature;
import com.mapzen.fragment.MapFragment;
import com.mapzen.fragment.SearchResultsFragment;

import org.json.JSONException;
import org.json.JSONObject;

import static com.mapzen.MapzenApplication.PELIAS_TEXT;
import static com.mapzen.MapzenApplication.getApp;

public class AutoCompleteAdapter extends CursorAdapter {
    private SearchView searchView;
    private MapFragment map;
    private SearchResultsFragment searchResults;
    private Context context;

    public AutoCompleteAdapter(Context context, Cursor c) {
        super(context, c, 0);
        this.context = context;
    }

    public void setSearchView(SearchView view) {
        searchView = view;
    }

    public void setMap(MapFragment mapFragment) {
        map = mapFragment;
    }

    public void setSearchResults(SearchResultsFragment searchResultsFragment) {
        searchResults = searchResultsFragment;
    }

    @Override
    public View newView(final Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        assert v != null;
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView tv = (TextView) view;
                Feature feature = (Feature) tv.getTag();
                getApp(context).setCurrentSearchTerm("");
                searchView.setQuery("", false);
                searchView.clearFocus();
                searchView.setQuery(tv.getText(), false);
                searchResults.hideResultsWrapper();
                map.centerOn(feature);
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
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = (TextView) view;
        final int textIndex = cursor.getColumnIndex(PELIAS_TEXT);
        Feature feature = null;
        try {
            feature = Feature.fromJson(new JSONObject(cursor.getString(textIndex)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        tv.setTag(feature);
        tv.setText(feature.getDisplayName());
    }
}

