package com.mapzen.search;

import com.mapzen.R;
import com.mapzen.adapters.PlaceArrayAdapter;
import com.mapzen.entity.SimpleFeature;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class   ListResultsFragment extends ListFragment {
    @InjectView(R.id.term) TextView termTextView;
    private ArrayList<SimpleFeature> features;
    private String currentSearchTerm;

    public static ListResultsFragment newInstance(ArrayList<SimpleFeature> simpleFeatures,
            String currentSearchTerm) {
        final ListResultsFragment listResultsFragment = new ListResultsFragment();
        listResultsFragment.features = simpleFeatures;
        listResultsFragment.currentSearchTerm = currentSearchTerm;
        return listResultsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.full_results_list, container, false);
        ButterKnife.inject(this, view);
        termTextView.setText("\"" + currentSearchTerm + "\"");
        setListAdapter(new PlaceArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_1, features));
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Intent resultIntent = new Intent();
        resultIntent.putExtra(ListResultsActivity.EXTRA_INDEX, position);
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
        getActivity().finish();
    }
}