package com.mapzen.search;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.adapters.SearchViewAdapter;
import com.mapzen.android.gson.Feature;
import com.mapzen.android.gson.Result;
import com.mapzen.entity.GeoFeature;
import com.mapzen.fragment.BaseFragment;
import com.mapzen.fragment.ItemFragment;
import com.mapzen.fragment.ListResultsFragment;
import com.mapzen.util.ApiHelper;
import com.mapzen.util.Logger;

import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.mapzen.MapController.DEFAULT_ZOOMLEVEL;
import static com.mapzen.MapController.getMapController;
import static com.mapzen.android.Pelias.getPelias;

public class PagerResultsFragment extends BaseFragment {
    public static final String TAG = PagerResultsFragment.class.getSimpleName();
    private List<ItemFragment> currentCollection = new ArrayList<ItemFragment>();
    private ArrayList<GeoFeature> geoFeatures = new ArrayList<GeoFeature>();
    private static final String PAGINATE_TEMPLATE = "%2d of %2d RESULTS";

    @InjectView(R.id.multi_result_header)
    View multiResultHeader;

    @InjectView(R.id.pagination)
    TextView indicator;

    @InjectView(R.id.view_all)
    Button viewAll;

    @InjectView(R.id.results)
    ViewPager pager;

    public static PagerResultsFragment newInstance(BaseActivity act) {
        PagerResultsFragment pagerResultsFragment = new PagerResultsFragment();
        pagerResultsFragment.setAct(act);
        pagerResultsFragment.setMapFragment(act.getMapFragment());
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
    public void onDestroy() {
        super.onDestroy();
        clearMap();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.view_all)
    @SuppressWarnings("unused")
    public void onClickViewAll() {
        final Fragment fragment = ListResultsFragment.newInstance(act, geoFeatures);
        act.getSupportFragmentManager().beginTransaction()
                .add(R.id.full_list, fragment, ListResultsFragment.TAG)
                .addToBackStack(null)
                .commit();
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
                centerOnPlace(i, getMapController().getZoomScale());
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    private void centerOnPlace(int i) {
        centerOnPlace(i, Math.pow(2, DEFAULT_ZOOMLEVEL));
    }

    private void centerOnPlace(int i, double zoom) {
        ItemFragment srf = currentCollection.get(i);
        GeoFeature geoFeature = srf.getGeoFeature();
        Logger.d("geoFeature: " + geoFeature.toString());
        String indicatorText = String.format(Locale.getDefault(), PAGINATE_TEMPLATE, i + 1,
                currentCollection.size());
        indicator.setText(indicatorText);
        mapFragment.centerOn(geoFeature, zoom);
    }

    private void hide() {
        act.getSupportFragmentManager().beginTransaction()
                .hide(this)
                .commit();
    }

    public void clearMap() {
        mapFragment.clearMarkers();
        mapFragment.updateMap();
    }

    public void flipTo(GeoFeature geoFeature) {
        int pos = geoFeatures.indexOf(geoFeature);
        pager.setCurrentItem(pos);
    }

    public void clearAll() {
        Logger.d(String.format(Locale.US, "clearing all items: %d", currentCollection.size()));
        ItemizedLayer<MarkerItem> poiLayer = mapFragment.getPoiLayer();
        poiLayer.removeAllItems();
        if (pager != null) {
            pager.setCurrentItem(0);
        }
        currentCollection.clear();
        geoFeatures.clear();
    }

    public void add(GeoFeature geoFeature) {
        Logger.d(geoFeature.toString());
        addMarker(geoFeature);
        ItemFragment itemFragment = new ItemFragment();
        itemFragment.setGeoFeature(geoFeature);
        itemFragment.setMapFragment(mapFragment);
        itemFragment.setAct(act);
        currentCollection.add(itemFragment);
        geoFeatures.add(geoFeature);
    }

    public void setSearchResults(List<Feature> features) {
        clearAll();
        if (features.size() > 0) {
            for (Feature feature: features) {
                GeoFeature geoFeature = GeoFeature.fromFeature(feature);
                add(geoFeature);
            }
            displayResults(features.size(), pager.getCurrentItem());
        } else {
            hide();
            Toast.makeText(act, "No results where found for: " + act.getSearchView().getQuery(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public boolean executeSearchOnMap(final SearchView view, String query) {
        app.cancelAllApiRequests();
        act.showProgressDialog();
        app.setCurrentSearchTerm(query);
        getPelias().search(query, ApiHelper.getViewBox(mapFragment.getMap()),
                getSearchCallback(view));
        return true;
    }

    private Callback<Result> getSearchCallback(final SearchView view) {
        return new Callback<Result>() {
            @Override
            public void success(Result result, retrofit.client.Response response) {
                setSearchResults(result.getFeatures());
                act.dismissProgressDialog();
                view.clearFocus();
            }

            @Override
            public void failure(RetrofitError error) {
                onServerError(error);
            }
        };
    }

    public void displayResults(int length, int currentPos) {
        SearchViewAdapter adapter = new SearchViewAdapter(getFragmentManager(), currentCollection);
        pager.setAdapter(adapter);
        String indicatorText = String.format(Locale.getDefault(), PAGINATE_TEMPLATE, 1, length);
        indicator.setText(indicatorText);
        centerOnPlace(currentPos);
        mapFragment.updateMap();
        setMultiResultHeaderVisibility(length);
    }

    private void setMultiResultHeaderVisibility(int length) {
        if (length == 1) {
            multiResultHeader.setVisibility(View.GONE);
        } else {
            multiResultHeader.setVisibility(View.VISIBLE);
        }
    }

    private void addMarker(GeoFeature geoFeature) {
        mapFragment.addPoi(geoFeature);
    }
}
