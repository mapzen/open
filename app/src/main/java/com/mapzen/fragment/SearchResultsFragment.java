package com.mapzen.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.SearchViewAdapter;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Place;

import java.util.ArrayList;

import static com.mapzen.MapzenApplication.LOG_TAG;

public class SearchResultsFragment extends Fragment {
    private SearchViewAdapter adapter;
    private LinearLayout wrapper;
    private BaseActivity act;
    private MapFragment mapFragment;
    private final ArrayList<Place> list = new ArrayList<Place>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results,
                container, false);
        assert view != null;
        act = (BaseActivity) getActivity();
        assert act != null;
        ViewPager pager = (ViewPager) view.findViewById(R.id.results);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                Log.v(LOG_TAG, "onPageScrolled");
            }

            @Override
            public void onPageSelected(int i) {
                Log.v(LOG_TAG, "onPageSelected");
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                Log.v(LOG_TAG, "onPageScrollStateChanged");
            }
        });
        adapter = new SearchViewAdapter(act, act.getSupportFragmentManager());
        pager.setAdapter(adapter);
        mapFragment = act.getMapFragment();
        wrapper = (LinearLayout) view.findViewById(R.id.results_wrapper);
        return view;
    }

    public void showResultsWrapper() {
        wrapper.setVisibility(View.VISIBLE);
    }

    public void hideResultsWrapper() {
        wrapper.setVisibility(View.GONE);
        mapFragment.getPoiLayer().removeAllItems();
        mapFragment.updateMap();
    }

    public void clearAll() {
        adapter.clearFragments();
    }

    public void add(Place place) {
        adapter.addFragment(new SearchResultItemFragment(place));
    }

    public void notifyNewData() {
        adapter.notifyDataSetChanged();
    }
}
