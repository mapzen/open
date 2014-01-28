package com.mapzen.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oscim.layers.marker.ItemizedIconLayer;
import org.oscim.layers.marker.MarkerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.mapzen.activity.BaseActivity.SEARCH_RESULTS_STACK;

public class PagerResultsFragment extends BaseFragment {
    public static final String PAGER_RESULTS = "results";
    private SearchViewAdapter adapter;
    private List<ItemFragment> currentCollection =
            new ArrayList<ItemFragment>();
    private TextView indicator;
    private ViewPager pager;
    private ArrayList<Feature> features = new ArrayList<Feature>();
    private static final String PAGINATE_TEMPLATE = "%2d of %2d RESULTS";
    private static PagerResultsFragment pagerResultsFragment;
    private MapzenProgressDialog progressDialog;

    public static PagerResultsFragment newInstance(BaseActivity act) {
        pagerResultsFragment = new PagerResultsFragment();
        pagerResultsFragment.setAct(act);
        pagerResultsFragment.initializeAdapter();
        pagerResultsFragment.initializeProgressDialog();
        pagerResultsFragment.setMapFragment(act.getMapFragment());
        return pagerResultsFragment;
    }

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearMap();
    }

    private void setViewAll(Button viewAll) {
        viewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                act.getSearchView().clearFocus();
                ListResultsFragment.newInstance(act, features).attachToContainer(R.id.full_list);
            }
        });
    }

    public void setCurrentItem(int position) {
        pager.setCurrentItem(position);
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

    public void initializeAdapter() {
        this.adapter = new SearchViewAdapter(act.getSupportFragmentManager());
    }

    private void centerOnPlace(int i) {
        ItemFragment srf = currentCollection.get(i);
        Feature feature = srf.getFeature();
        Logger.d("feature: " + feature.toString());
        String indicatorText = String.format(Locale.getDefault(), PAGINATE_TEMPLATE, i + 1, currentCollection.size());
        indicator.setText(indicatorText);
        mapFragment.centerOn(feature);
    }

    private void hide() {
        act.getSupportFragmentManager().beginTransaction()
                .hide(this)
                .commit();
    }

    public void attachToContainer() {
        if (isAdded()) {
            show();
        } else {
            add();
        }
        act.getSearchView().clearFocus();
    }

    private void add() {
        act.getSupportFragmentManager().beginTransaction()
                .addToBackStack(SEARCH_RESULTS_STACK)
                .add(R.id.pager_results_container, this, PAGER_RESULTS)
                .commit();
    }

    private void show() {
        act.getSupportFragmentManager().beginTransaction()
                .addToBackStack(SEARCH_RESULTS_STACK)
                .show(this)
                .commit();
    }

    public void clearMap() {
        mapFragment.clearMarkers();
        mapFragment.updateMap();
    }

    public void flipTo(Feature feature) {
        int pos = features.indexOf(feature);
        pager.setCurrentItem(pos);
    }

    public void clearAll() {
        Logger.d(String.format(
                Locale.getDefault(), "clearing all items: %d", currentCollection.size()));
        ItemizedIconLayer<MarkerItem> poiLayer = mapFragment.getPoiLayer();
        poiLayer.removeAllItems();
        pager.setCurrentItem(0);
        adapter.clearFragments();
        currentCollection.clear();
        features.clear();
    }

    public void add(Feature feature) {
        Logger.d(feature.toString());
        addMarker(feature);
        ItemFragment itemFragment = new ItemFragment();
        itemFragment.setFeature(feature);
        itemFragment.setMapFragment(mapFragment);
        itemFragment.setAct(act);
        currentCollection.add(itemFragment);
        features.add(feature);
        adapter.addFragment(itemFragment);
    }

    public void notifyNewData() {
        adapter.notifyDataSetChanged();
    }

    public void setSearchResults(JSONArray jsonArray) {
        clearAll();
        if (jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                Feature feature = new Feature();
                try {
                    feature.buildFromJSON(jsonArray.getJSONObject(i));
                } catch (JSONException e) {
                    Logger.e(e.toString());
                }
                add(feature);
            }
            displayResults(jsonArray.length(), pager.getCurrentItem());
        } else {
            hide();
            Toast.makeText(act, "No results where found for: " + act.getSearchView().getQuery(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public boolean executeSearchOnMap(final SearchView view, String query) {
        app.cancelAllApiRequests();
        attachToContainer();
        progressDialog.show();
        app.setCurrentSearchTerm(query);
        JsonObjectRequest jsonObjectRequest =
                Feature.search(mapFragment.getMap(), query,
                        getSearchListener(view), getErrorListener());
        app.enqueueApiRequest(jsonObjectRequest);
        return true;
    }

    public MapzenProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public void initializeProgressDialog() {
        progressDialog = new MapzenProgressDialog(act);
    }

    private Response.ErrorListener getErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                onServerError(progressDialog, volleyError);
            }
        };
    }

    private Response.Listener<JSONObject> getSearchListener(final SearchView view) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Logger.d("Search Results: " + jsonObject.toString());
                JSONArray jsonArray = new JSONArray();
                try {
                    jsonArray = jsonObject.getJSONArray("features");
                } catch (JSONException e) {
                    Logger.e(e.toString());
                }
                setSearchResults(jsonArray);
                progressDialog.dismiss();
                view.clearFocus();
            }
        };
    }

    private void displayResults(int length, int currentPos) {
        notifyNewData();
        String initialIndicatorText = String.format(Locale.getDefault(), PAGINATE_TEMPLATE, 1, length);
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
