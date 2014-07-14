package com.mapzen.search;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.adapters.SearchViewAdapter;
import com.mapzen.android.gson.Feature;
import com.mapzen.android.gson.Result;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.fragment.BaseFragment;
import com.mapzen.fragment.ItemFragment;
import com.mapzen.util.ApiHelper;
import com.mapzen.util.Logger;

import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;

import static com.mapzen.MapController.DEFAULT_ZOOMLEVEL;
import static com.mapzen.MapController.getMapController;
import static com.mapzen.android.Pelias.getPelias;
import static com.mapzen.search.SavedSearch.getSavedSearch;

public class PagerResultsFragment extends BaseFragment {
    public static final String TAG = PagerResultsFragment.class.getSimpleName();
    private List<ItemFragment> currentCollection = new ArrayList<ItemFragment>();
    private ArrayList<SimpleFeature> simpleFeatures = new ArrayList<SimpleFeature>();
    private static final String PAGINATE_TEMPLATE = "Viewing %d of %d results";

    @InjectView(R.id.multi_result_header)
    View multiResultHeader;

    @InjectView(R.id.pagination)
    TextView indicator;

    @InjectView(R.id.view_all)
    Button viewAll;

    @InjectView(R.id.results)
    ViewPager pager;

    private String searchTermForCurrentResults;

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (act != null) {
            initOnFocusChangeListener();
            initSearchCloseButton();
        }
    }

    private void initOnFocusChangeListener() {
        act.getSearchView().setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    act.getSearchView().setQuery(app.getCurrentSearchTerm(), false);
                }
            }
        });
    }

    private void initSearchCloseButton() {
        final SearchView searchView = act.getSearchView();
        final AutoCompleteAdapter adapter = (AutoCompleteAdapter) searchView
                .getSuggestionsAdapter();
        final AutoCompleteTextView textView = act.getQueryAutoCompleteTextView(searchView);
        final ImageView closeButton = (ImageView) act.getSearchView().findViewById(getResources()
                .getIdentifier("android:id/search_close_btn", null, null));
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter != null) {
                    adapter.loadSavedSearches();
                }

                textView.requestFocus();
                searchView.setQuery("", false);
                app.setCurrentSearchTerm("");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearMap();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.view_all) @SuppressWarnings("unused") public void onClickViewAll() {
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
        SimpleFeature simpleFeature = srf.getSimpleFeature();
        Logger.d("simpleFeature: " + simpleFeature.toString());
        String indicatorText = String.format(Locale.getDefault(), PAGINATE_TEMPLATE, i + 1,
                currentCollection.size());
        indicator.setText(indicatorText);
        mapFragment.centerOn(simpleFeature, zoom);
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

    public void setSearchResults(List<Feature> features) {
        clearAll();
        if (features.size() > 0) {
            for (Feature feature: features) {
                SimpleFeature simpleFeature = SimpleFeature.fromFeature(feature);
                add(simpleFeature);
            }
            displayResults(features.size(), pager.getCurrentItem());
        } else {
            hide();
            Toast.makeText(act, "No results where found for: " + act.getSearchView().getQuery(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public boolean executeSearchOnMap(final SearchView view, String query) {
        act.showProgressDialog();
        app.setCurrentSearchTerm(query);
        searchTermForCurrentResults = query;
        getSavedSearch().store(query);
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

    private void addMarker(SimpleFeature simpleFeature) {
        mapFragment.addPoi(simpleFeature);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            final int index = data.getIntExtra(ListResultsActivity.EXTRA_INDEX, 0);
            pager.setCurrentItem(index);
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
