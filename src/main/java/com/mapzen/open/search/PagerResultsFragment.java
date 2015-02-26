package com.mapzen.open.search;

import com.mapzen.android.Pelias;
import com.mapzen.android.gson.Feature;
import com.mapzen.android.gson.Result;
import com.mapzen.open.MapController;
import com.mapzen.open.R;
import com.mapzen.open.activity.BaseActivity;
import com.mapzen.open.adapters.SearchViewAdapter;
import com.mapzen.open.entity.SimpleFeature;
import com.mapzen.open.fragment.BaseFragment;
import com.mapzen.open.fragment.ItemFragment;
import com.mapzen.open.util.Logger;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.splunk.mint.Mint;

import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static com.mapzen.open.MapController.DEFAULT_ZOOM_LEVEL;
import static com.mapzen.open.MapController.getMapController;
import static com.mapzen.open.util.MixpanelHelper.Event.PELIAS_SEARCH;
import static com.mapzen.open.util.MixpanelHelper.Payload.PELIAS_TERM;
import static com.mapzen.open.util.MixpanelHelper.Payload.fromHashMap;

public class PagerResultsFragment extends BaseFragment {
    public static final String TAG = PagerResultsFragment.class.getSimpleName();
    private List<ItemFragment> currentCollection = new ArrayList<ItemFragment>();
    private ArrayList<SimpleFeature> simpleFeatures = new ArrayList<SimpleFeature>();
    @Inject MapController mapController;
    @Inject Pelias pelias;
    @Inject MixpanelAPI mixpanelApi;
    @Inject SavedSearch savedSearch;

    @InjectView(R.id.pagination)
    TextView indicator;

    @InjectView(R.id.results)
    ViewPager pager;

    private String searchTermForCurrentResults;

    public static PagerResultsFragment newInstance(BaseActivity act) {
        PagerResultsFragment pagerResultsFragment = new PagerResultsFragment();
        pagerResultsFragment.setAct(act);
        pagerResultsFragment.setMapFragment(act.getMapFragment());
        pagerResultsFragment.inject();
        pagerResultsFragment.setRetainInstance(true);
        return pagerResultsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results,
                container, false);
        ButterKnife.inject(this, view);
        initOnPageChangeListener();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (simpleFeatures != null && !simpleFeatures.isEmpty()) {
            for (SimpleFeature simpleFeature : simpleFeatures) {
                addMarker(simpleFeature);
            }
            displayResults(simpleFeatures.size(), pager.getCurrentItem());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (act != null) {
            act.getAutoCompleteListView().setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearMap();
        ButterKnife.reset(this);
    }

    public void viewAll() {
        final Intent intent = new Intent(getActivity(), ListResultsActivity.class);
        intent.putExtra(ListResultsActivity.EXTRA_FEATURE_LIST, simpleFeatures);
        intent.putExtra(ListResultsActivity.EXTRA_SEARCH_TERM, searchTermForCurrentResults);
        startActivityForResult(intent, 0);
    }

    public void setCurrentItem(int position) {
        pager.setCurrentItem(position);
    }

    public int getCurrentItem() {
        return pager.getCurrentItem();
    }

    private void initOnPageChangeListener() {
        this.pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                centerOnPlace(i, mapController.getZoomScale());
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    private void centerOnPlace(int i) {
        centerOnPlace(i, Math.pow(2, DEFAULT_ZOOM_LEVEL));
    }

    private void centerOnPlace(int i, double zoom) {
        ItemFragment srf = currentCollection.get(i);
        SimpleFeature simpleFeature = srf.getSimpleFeature();
        Logger.d("simpleFeature: " + simpleFeature.toString());
        String indicatorText = getString(R.string.paginate_template, i + 1,
                currentCollection.size());
        indicator.setText(indicatorText);
        mapFragment.repopulatePoiLayer();
        mapFragment.centerOn(simpleFeature, zoom);
    }

    private void hide() {
        act.getSupportFragmentManager().beginTransaction()
                .hide(this)
                .commit();
    }

    public void clearMap() {
        try {
            mapFragment.clearMarkers();
            mapFragment.updateMap();
        } catch (NullPointerException npe) {
            Mint.logException(npe);
        }
    }

    public void clearAll() {
        Logger.d(String.format(Locale.US, "clearing all items: %d", currentCollection.size()));
        ItemizedLayer<MarkerItem> poiLayer = mapFragment.getPoiLayer();
        poiLayer.removeAllItems();
        if (pager != null) {
            pager.setCurrentItem(0);
        }
        currentCollection.clear();
        simpleFeatures.clear();
    }

    public void add(SimpleFeature simpleFeature) {
        Logger.d(simpleFeature.toString());
        addMarker(simpleFeature);
        ItemFragment itemFragment = new ItemFragment();
        itemFragment.setSimpleFeature(simpleFeature);
        itemFragment.setMapFragment(mapFragment);
        itemFragment.setAct(act);
        currentCollection.add(itemFragment);
        simpleFeatures.add(simpleFeature);
    }

    public void update(SimpleFeature simpleFeature) {
        if (pager != null) {
            TextView address = (TextView) pager.findViewById(R.id.address);
            address.setText(String.format(Locale.getDefault(), "%s, %s",
                    simpleFeature.getCity(), simpleFeature.getAdmin()));
        }
    }

    public void setSearchResults(List<Feature> features) {
        clearAll();
        if (features.size() > 0) {
            for (Feature feature: features) {
                SimpleFeature simpleFeature = SimpleFeature.fromFeature(feature);
                add(simpleFeature);
            }

            if (pager != null) {
                displayResults(features.size(), pager.getCurrentItem());
            } else {
                Logger.e("Unable to display search results: pager is null");
            }

        } else {
            hide();
            Toast.makeText(act, "No results were found for: " + act.getSearchView().getQuery(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public boolean executeSearchOnMap(final SearchView view, String query) {
        if (!isConnected()) {
            Toast.makeText(view.getContext(), view.getContext().getString(R.string.no_network),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        act.showLoadingIndicator();
        searchTermForCurrentResults = query;
        savedSearch.store(query);
        trackSearch(query);
        Double lat = getMapController().getMap().getMapPosition().getLatitude();
        Double lon = getMapController().getMap().getMapPosition().getLongitude();
        pelias.search(query, String.valueOf(lat),
                String.valueOf(lon), getSearchCallback(view));
        return true;
    }

    private void trackSearch(String newText) {
        HashMap<String, Object> payload = new HashMap<String, Object>();
        payload.put(PELIAS_TERM, newText);
        mixpanelApi.track(PELIAS_SEARCH, fromHashMap(payload));
    }

    private boolean isConnected() {
        final NetworkInfo networkInfo = ((ConnectivityManager)
                act.getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (networkInfo == null) {
            return false;
        }

        return networkInfo.isConnected();
    }

    private Callback<Result> getSearchCallback(final SearchView view) {
        return new Callback<Result>() {
            @Override
            public void success(Result result, retrofit.client.Response response) {
                setSearchResults(result.getFeatures());
                act.hideLoadingIndicator();
                view.clearFocus();
            }

            @Override
            public void failure(RetrofitError error) {
                onServerError(error);
            }
        };
    }

    public void displayResults(int length, int currentPos) {
        SearchViewAdapter adapter = new SearchViewAdapter(getChildFragmentManager(),
                currentCollection);
        pager.setAdapter(adapter);
        String indicatorText = getString(R.string.paginate_template, 1, length);
        indicator.setText(indicatorText);
        centerOnPlace(currentPos);
        mapFragment.updateMap();
        setMultiResultHeaderVisibility(length);
        getView().findViewById(R.id.results_root).setVisibility(View.VISIBLE);
    }

    private void setMultiResultHeaderVisibility(int length) {
        if (length == 1) {
            indicator.setVisibility(View.GONE);
            ((BaseActivity) getActivity()).hideActionViewAll();
        } else {
            indicator.setVisibility(View.VISIBLE);
            ((BaseActivity) getActivity()).showActionViewAll();
        }
    }

    private void addMarker(SimpleFeature simpleFeature) {
        mapFragment.addPoi(simpleFeature);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            final int index = data.getIntExtra(ListResultsActivity.EXTRA_INDEX, 0);
            pager.setCurrentItem(index, true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (act != null) {
                    final View view = act.getSearchView();
                    if (view != null) {
                        view.clearFocus();
                    }
                }
            }
        });
    }
}
