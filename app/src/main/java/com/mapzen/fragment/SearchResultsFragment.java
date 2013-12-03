package com.mapzen.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.mapzen.R;
import com.mapzen.SearchViewAdapter;
import com.mapzen.entity.Place;

import java.util.ArrayList;

public class SearchResultsFragment extends Fragment {
    SearchViewAdapter adapter;
    LinearLayout wrapper;
    private final ArrayList<Place> list = new ArrayList<Place>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results,
                container, false);
        adapter = new SearchViewAdapter(getActivity(),
               R.id.results, list);
        ListView listView = (ListView) view.findViewById(R.id.results);
        listView.setAdapter(adapter);
        wrapper = (LinearLayout) view.findViewById(R.id.results_wrapper);
        return view;
    }

    public void showResultsWrapper() {
        wrapper.setVisibility(View.VISIBLE);
    }

    public void hideResultsWrapper() {
        wrapper.setVisibility(View.GONE);
    }

    public void add(Place place) {
        list.add(place);
    }

    public void notifyNewData() {
        adapter.notifyDataSetChanged();
    }
}
