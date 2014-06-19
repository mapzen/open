package com.mapzen.fragment;

import com.mapzen.R;
import com.mapzen.adapters.PlaceArrayAdapter;
import com.mapzen.entity.SimpleFeature;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ListResultsFragment extends ListFragment {
    private ArrayList<SimpleFeature> features;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setListAdapter(new PlaceArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_1, features));
        return inflater.inflate(R.layout.full_results_list, container, false);
    }

    public static ListResultsFragment newInstance(ArrayList<SimpleFeature> simpleFeatures) {
        final ListResultsFragment listResultsFragment = new ListResultsFragment();
        listResultsFragment.features = simpleFeatures;
        return listResultsFragment;
    }
}
