package com.mapzen.open.search;

import com.mapzen.open.R;
import com.mapzen.open.adapters.PlaceArrayAdapter;
import com.mapzen.open.entity.SimpleFeature;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListResultsFragment extends ListFragment {
    private ArrayList<SimpleFeature> features;
    private String currentSearchTerm;

    public static ListResultsFragment newInstance(ArrayList<SimpleFeature> simpleFeatures,
            String currentSearchTerm) {
        final ListResultsFragment listResultsFragment = new ListResultsFragment();
        listResultsFragment.features = simpleFeatures;
        listResultsFragment.currentSearchTerm = currentSearchTerm;
        listResultsFragment.setRetainInstance(true);
        return listResultsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.full_results_list, container, false);
        final View header = View.inflate(getActivity(), R.layout.full_results_list_header, null);
        final TextView term = (TextView) header.findViewById(R.id.term);
        final ListView list = (ListView) view.findViewById(android.R.id.list);
        term.setText(Html.fromHtml("&ldquo;" + currentSearchTerm + "&rdquo;"));
        list.addHeaderView(header);
        list.setHeaderDividersEnabled(false);
        setListAdapter(new PlaceArrayAdapter(getActivity(), features));
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Intent resultIntent = new Intent();
        resultIntent.putExtra(ListResultsActivity.EXTRA_INDEX, position - 1);
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
        getActivity().finish();
    }
}
