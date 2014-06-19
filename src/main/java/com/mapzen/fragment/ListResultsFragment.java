package com.mapzen.fragment;

import com.mapzen.R;
import com.mapzen.adapters.PlaceArrayAdapter;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.search.ListResultsActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

public class ListResultsFragment extends ListFragment {
    private ArrayList<SimpleFeature> features;

    public static ListResultsFragment newInstance(ArrayList<SimpleFeature> simpleFeatures) {
        final ListResultsFragment listResultsFragment = new ListResultsFragment();
        listResultsFragment.features = simpleFeatures;
        return listResultsFragment;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Intent resultIntent = new Intent();
        resultIntent.putExtra(ListResultsActivity.EXTRA_INDEX, position);
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
        getActivity().finish();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setListAdapter(new PlaceArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_1, features));
        return inflater.inflate(R.layout.full_results_list, container, false);
    }
}
