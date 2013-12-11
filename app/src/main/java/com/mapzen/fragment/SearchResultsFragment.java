package com.mapzen.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapzen.R;
import com.mapzen.SearchViewAdapter;
import com.mapzen.activity.BaseActivity;
import com.mapzen.activity.FullSearchResultsActivity;
import com.mapzen.entity.Feature;

import org.json.JSONArray;
import org.json.JSONException;
import org.oscim.layers.marker.ItemizedIconLayer;
import org.oscim.layers.marker.MarkerItem;

import java.util.ArrayList;
import java.util.List;

import static com.mapzen.MapzenApplication.LOG_TAG;
import static com.mapzen.MapzenApplication.PICK_PLACE_REQUEST;

public class SearchResultsFragment extends Fragment {
    private SearchViewAdapter adapter;
    private LinearLayout wrapper;
    private BaseActivity act;
    private MapFragment mapFragment;
    private List<SearchResultItemFragment> currentCollection =
            new ArrayList<SearchResultItemFragment>();
    private TextView indicator;
    private static final String PAGINATE_TEMPLATE = "%2d of %2d RESULTS";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results,
                container, false);
        assert view != null;
        act = (BaseActivity) getActivity();
        assert act != null;
        indicator = (TextView) view.findViewById(R.id.pagination);
        Button viewAll = (Button) view.findViewById(R.id.view_all);
        viewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Feature> features = new ArrayList<Feature>(currentCollection.size());
                for (SearchResultItemFragment fragment : currentCollection) {
                    act.getSearchView().clearFocus();
                    features.add(fragment.getFeature());
                }
                startActivityForResult(FullSearchResultsActivity.getIntent(getActivity(), features), PICK_PLACE_REQUEST);
            }
        });
        ViewPager pager = (ViewPager) view.findViewById(R.id.results);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        adapter = new SearchViewAdapter(act, act.getSupportFragmentManager());
        pager.setAdapter(adapter);
        wrapper = (LinearLayout) view.findViewById(R.id.results_wrapper);
        return view;
    }

    public void setMapFragment(MapFragment map) {
        mapFragment = map;
    }

    private void centerOnPlace(int i) {
        SearchResultItemFragment srf = currentCollection.get(i);
        Feature feature = srf.getFeature();
        Log.v(LOG_TAG, "feature: " + feature.toString());
        String indicatorText = String.format(PAGINATE_TEMPLATE, i + 1, currentCollection.size());
        indicator.setText(indicatorText);
        mapFragment.centerOn(feature.getMarker().getPoint());
    }

    public void showResultsWrapper() {
        mapFragment.pullUp();
        wrapper.setVisibility(View.VISIBLE);
    }

    public void hideResultsWrapper() {
        wrapper.setVisibility(View.GONE);
        mapFragment.pullDown();
        mapFragment.getPoiLayer().removeAllItems();
        mapFragment.updateMap();
    }

    public void clearAll() {
        ItemizedIconLayer<MarkerItem> poiLayer = mapFragment.getPoiLayer();
        poiLayer.removeAllItems();
        adapter.clearFragments();
        currentCollection.clear();
    }

    public void add(Feature feature) {
        SearchResultItemFragment searchResultItemFragment = new SearchResultItemFragment(feature);
        currentCollection.add(searchResultItemFragment);
        adapter.addFragment(searchResultItemFragment);
    }

    public void notifyNewData() {
        adapter.notifyDataSetChanged();
    }

    public void setSearchResults(JSONArray jsonArray) {
        ItemizedIconLayer<MarkerItem> poiLayer = mapFragment.getPoiLayer();
        clearAll();
        for (int i = 0; i < jsonArray.length(); i++) {
            Feature feature = null;
            try {
                feature = Feature.fromJson(jsonArray.getJSONObject(i));
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.toString());
            }
            Log.v(LOG_TAG, feature.getDisplayName());
            MarkerItem m = feature.getMarker();
            m.setMarker(mapFragment.getDefaultMarkerSymbol());
            m.setMarkerHotspot(MarkerItem.HotspotPlace.CENTER);
            poiLayer.addItem(feature.getMarker());
            add(feature);
        }
        notifyNewData();
        String initialIndicatorText = String.format(PAGINATE_TEMPLATE, 1, jsonArray.length());
        indicator.setText(initialIndicatorText);
        showResultsWrapper();
        mapFragment.updateMap();
    }
}
