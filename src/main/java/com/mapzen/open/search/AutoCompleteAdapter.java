package com.mapzen.open.search;

import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.activity.BaseActivity;
import com.mapzen.android.Pelias;
import com.mapzen.android.gson.Feature;
import com.mapzen.android.gson.Result;
import com.mapzen.open.entity.SimpleFeature;
import com.mapzen.open.fragment.MapFragment;
import com.mapzen.open.util.Highlighter;
import com.mapzen.open.util.Logger;
import com.mapzen.open.util.ParcelableUtil;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Typeface;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.HashMap;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;

import static com.mapzen.open.MapController.getMapController;
import static com.mapzen.open.MapzenApplication.PELIAS_BLOB;
import static com.mapzen.open.entity.SimpleFeature.CREATOR;
import static com.mapzen.open.entity.SimpleFeature.TEXT;
import static com.mapzen.open.search.SavedSearch.SEARCH_TERM;
import static com.mapzen.open.search.SavedSearch.getSavedSearch;
import static com.mapzen.open.util.MixpanelHelper.Event.PELIAS_SUGGEST;
import static com.mapzen.open.util.MixpanelHelper.Payload.PELIAS_TERM;
import static com.mapzen.open.util.MixpanelHelper.Payload.fromHashMap;

public class AutoCompleteAdapter extends CursorAdapter implements SearchView.OnQueryTextListener {
    public static final int AUTOCOMPLETE_THRESHOLD = 3;
    private SearchView searchView;
    private MapFragment mapFragment;
    private BaseActivity act;
    private MapzenApplication app;
    private FragmentManager fragmentManager;
    @Inject Typeface typeface;
    @Inject Pelias pelias;
    @Inject MixpanelAPI mixpanelApi;

    public AutoCompleteAdapter(Context context, BaseActivity act, String[] columns,
            FragmentManager fragmentManager) {
        super(context, new MatrixCursor(columns), 0);
        this.act = act;
        this.app = (MapzenApplication) act.getApplication();
        this.fragmentManager = fragmentManager;
        app.inject(this);
    }

    public void setSearchView(SearchView view) {
        this.searchView = view;
    }

    public SearchView getSearchView() {
        return searchView;
    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    @Override
    public View newView(final Context c, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(c);
        final TextView textView =
                (TextView) inflater.inflate(R.layout.search_dropdown_item, parent, false);
        textView.setTypeface(typeface);
        assert textView != null;
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView tv = (TextView) view;
                SimpleFeature simpleFeature = (SimpleFeature) tv.getTag();
                getSavedSearch().store(tv.getText().toString());
                app.setCurrentSearchTerm("");
                searchView.setQuery("", false);
                searchView.clearFocus();
                searchView.setQuery(tv.getText(), false);
                mapFragment.clearMarkers();
                mapFragment.updateMap();

                if (simpleFeature != null) {
                    app.setCurrentSearchTerm(simpleFeature.getHint());
                    mapFragment.centerOn(simpleFeature);
                    PagerResultsFragment pagerResultsFragment =
                            PagerResultsFragment.newInstance(act);
                    fragmentManager.beginTransaction()
                            .replace(R.id.pager_results_container, pagerResultsFragment,
                                    PagerResultsFragment.TAG).commit();
                    fragmentManager.executePendingTransactions();
                    pagerResultsFragment.add(simpleFeature);
                    pagerResultsFragment.displayResults(1, 0);
                } else {
                    searchView.setQuery(tv.getText().toString(), true);
                }

                act.getQueryAutoCompleteTextView(searchView).setSelection(0);
            }
        });

        parent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                dismissKeyboard();
                return false;
            }
        });

        return textView;
    }

    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) act
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }

    @Override
    public void bindView(View view, Context c, Cursor cursor) {
        final TextView tv = (TextView) view;
        if (cursor.getColumnName(1).equals(SEARCH_TERM)) {
            tv.setText(cursor.getString(1));
        } else {
            final int blobIndex = cursor.getColumnIndex(PELIAS_BLOB);
            byte[] bytes = cursor.getBlob(blobIndex);
            SimpleFeature simpleFeature = ParcelableUtil.unmarshall(bytes, CREATOR);
            tv.setTextColor(app.getResources().getColor(R.color.light_gray));

            final Highlighter highlighter = initAutoCompleteHighlighter(simpleFeature);
            tv.setTag(simpleFeature);
            tv.setText(highlighter.highlight());
        }
    }

    private Highlighter initAutoCompleteHighlighter(SimpleFeature simpleFeature) {
        final Highlighter highlighter = new Highlighter(simpleFeature.getProperty(TEXT),
                app.getResources().getColor(R.color.red));
        final String query  = searchView.getQuery().toString().trim();
        final String[] terms = TextUtils.split(query, " ");
        for (String term : terms) {
            highlighter.addTerm(term);
        }

        return highlighter;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (query.equals(app.getString(R.string.secret_phrase))) {
            act.toggleDebugMode();
            searchView.clearFocus();
            return false;
        }

        act.getQueryAutoCompleteTextView(searchView).setSelection(0);
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
            Double lat = getMapController().getMap().getMapPosition().getLatitude();
            Double lon = getMapController().getMap().getMapPosition().getLongitude();
            trackSuggest(newText);
            pelias.suggest(newText, String.valueOf(lat),
                    String.valueOf(lon), getPeliasCallback());
            Logger.d("search: autocomplete request enqueued");
        }
        return true;
    }

    private void trackSuggest(String newText) {
        HashMap<String, Object> payload = new HashMap<String, Object>();
        payload.put(PELIAS_TERM, newText);
        mixpanelApi.track(PELIAS_SUGGEST, fromHashMap(payload));
    }

    private Callback<Result> getPeliasCallback() {
        final MatrixCursor cursor = new MatrixCursor(app.getColumns());
        return new Callback<Result>() {
            @Override
            public void success(Result result, retrofit.client.Response response) {
                int i = 0;
                for (Feature feature : result.getFeatures()) {
                    SimpleFeature simpleFeature = SimpleFeature.fromFeature(feature);
                    byte[] data = ParcelableUtil.marshall(simpleFeature);
                    cursor.addRow(new Object[]{i, data});
                    i++;
                }
                Logger.d("search: swapping cursor");
                swapCursor(cursor);
            }

            @Override
            public void failure(RetrofitError error) {
                Logger.e("request: error: " + error.toString());
            }
        };
    }

    public void resetCursor() {
        swapCursor(new MatrixCursor(app.getColumns()));
    }

    public void loadSavedSearches() {
        changeCursor(getSavedSearch().getCursor());
    }
}
