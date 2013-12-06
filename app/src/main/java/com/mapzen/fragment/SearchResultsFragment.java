package com.mapzen.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mapzen.R;
import com.mapzen.SearchViewAdapter;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Place;

import org.oscim.core.GeoPoint;

import java.util.ArrayList;

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
        adapter = new SearchViewAdapter(getActivity(),
               R.id.results, list);
        assert view != null;
        ListView listView = (ListView) view.findViewById(R.id.results);
        listView.setAdapter(adapter);
        act = (BaseActivity) getActivity();
        assert act != null;
        mapFragment = act.getMapFragment();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv = (TextView) view.findViewById(R.id.big);
                Place place = (Place) tv.getTag();
                mapFragment.centerOn(new GeoPoint(place.getLat(), place.getLon()));
            }
        });

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
        list.clear();
    }

    public void add(Place place) {
        list.add(place);
    }

    public void notifyNewData() {
        adapter.notifyDataSetChanged();
    }
}
