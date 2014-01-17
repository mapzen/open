package com.mapzen.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.adapters.SearchViewAdapter;
import com.mapzen.entity.Feature;
import com.mapzen.util.Logger;
import com.mapzen.util.MapzenProgressDialog;
import com.mapzen.util.VolleyHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oscim.layers.marker.ItemizedIconLayer;
import org.oscim.layers.marker.MarkerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.mapzen.MapzenApplication.LOG_TAG;

public class ResultsFragment extends BaseFragment {
    private SearchViewAdapter adapter;
    private FrameLayout wrapper;
    private List<ItemFragment> currentCollection =
            new ArrayList<ItemFragment>();
    private TextView indicator;
    private ViewPager pager;
    private ArrayList<Feature> features = new ArrayList<Feature>();
    private static final String PAGINATE_TEMPLATE = "%2d of %2d RESULTS";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results,
                container, false);
        indicator = (TextView) view.findViewById(R.id.pagination);
        setViewAll((Button) view.findViewById(R.id.view_all));
        setPager((ViewPager) view.findViewById(R.id.results));
        return view;
    }

    private void setViewAll(Button viewAll) {
        viewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                act.getSearchView().clearFocus();
            }
        });
    }

    private void setPager(ViewPager pager) {
        this.pager = pager;

        this.pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                centerOnPlace(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
        this.pager.setAdapter(adapter);
    }

    public void setAdapter(SearchViewAdapter adapter) {
        this.adapter = adapter;
    }

    private void centerOnPlace(int i) {
        ItemFragment srf = currentCollection.get(i);
        app.setCurrentPagerPosition(i);
        Feature feature = srf.getFeature();
        Logger.d("feature: " + feature.toString());
        String indicatorText = String.format(Locale.ENGLISH, PAGINATE_TEMPLATE, i + 1, currentCollection.size());
        indicator.setText(indicatorText);
        mapFragment.centerOn(feature);
    }

    public void attachTo(BaseActivity activity) {
        this.act = activity;
        act.getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.top_container, this, "results")
                .commit();
        wrapper = (FrameLayout) act.findViewById(R.id.top_container);
    }

    public void showResultsWrapper() {
        wrapper.setVisibility(View.VISIBLE);
    }

    public void hideResultsWrapper() {
        if (wrapper != null) {
            wrapper.setVisibility(View.GONE);
        }
        mapFragment.clearMarkers();
        mapFragment.updateMap();
    }

    public void flipTo(Feature feature) {
        int pos = features.indexOf(feature);
        pager.setCurrentItem(pos);
    }

    public void clearAll() {
        ItemizedIconLayer<MarkerItem> poiLayer = mapFragment.getPoiLayer();
        poiLayer.removeAllItems();
        pager.setCurrentItem(0);
        adapter.clearFragments();
        currentCollection.clear();
        features.clear();
        hideResultsWrapper();
    }

    public void add(Feature feature) {
        Logger.d(feature.toString());
        addMarker(feature);
        ItemFragment itemFragment = new ItemFragment();
        itemFragment.setFeature(feature);
        itemFragment.setMapFragment(mapFragment);
        itemFragment.setApp(app);
        itemFragment.setAct(act);
        currentCollection.add(itemFragment);
        features.add(feature);
        adapter.addFragment(itemFragment);
    }

    public void notifyNewData() {
        adapter.notifyDataSetChanged();
    }

    public void setSearchResults(ArrayList<Feature> items, int pos) {
        clearAll();
        // TODO we shouldn't have to loop here since the we have all the features already
        for (int i = 0; i < items.size(); i++) {
            add(items.get(i));
        }
        displayResults(features.size(), pos);
    }

    public void setSearchResults(JSONArray jsonArray) {
        clearAll();
        if (jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                Feature feature = new Feature();
                try {
                    feature.buildFromJSON(jsonArray.getJSONObject(i));
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.toString());
                }
                add(feature);
            }
            displayResults(jsonArray.length(), pager.getCurrentItem());
        } else {
            Toast.makeText(act, "No results where found for: " + act.getSearchView().getQuery(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public boolean executeSearchOnMap(final SearchView view, String query) {
        attachTo(act);
        final MapzenProgressDialog progressDialog = new MapzenProgressDialog(act);
        progressDialog.show();
        app.setCurrentSearchTerm(query);
        JsonObjectRequest jsonObjectRequest =
                Feature.search(mapFragment.getMap(), query,
                        getSearchListener(view, progressDialog), getErrorListener());
        app.enqueueApiRequest(jsonObjectRequest);
        return true;
    }

    private Response.ErrorListener getErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                String errorMsg = VolleyHelper.Error.getMessage(volleyError, act);
                Log.e(LOG_TAG, "request: error: " + errorMsg);
            }
        };
    }

    private Response.Listener<JSONObject> getSearchListener(final SearchView view, final MapzenProgressDialog dialog) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Logger.d("Search Results: " + jsonObject.toString());
                JSONArray jsonArray = new JSONArray();
                try {
                    jsonArray = jsonObject.getJSONArray("features");
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.toString());
                }
                setSearchResults(jsonArray);
                dialog.dismiss();
                view.clearFocus();
            }
        };
    }

    private void displayResults(int length, int currentPos) {
        showResultsWrapper();
        notifyNewData();
        String initialIndicatorText = String.format(Locale.ENGLISH, PAGINATE_TEMPLATE, 1, length);
        indicator.setText(initialIndicatorText);
        centerOnPlace(currentPos);
        mapFragment.updateMap();
    }

    private void addMarker(Feature feature) {
        ItemizedIconLayer<MarkerItem> poiLayer = mapFragment.getPoiLayer();
        MarkerItem m = feature.getMarker();
        m.setMarker(mapFragment.getDefaultMarkerSymbol());
        m.setMarkerHotspot(MarkerItem.HotspotPlace.CENTER);
        poiLayer.addItem(feature.getMarker());
    }
}
