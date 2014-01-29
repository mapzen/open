package com.mapzen.fragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.adapters.PlaceArrayAdapter;
import com.mapzen.entity.Feature;

import java.util.ArrayList;

import static com.mapzen.activity.BaseActivity.SEARCH_RESULTS_STACK;

public class ListResultsFragment extends ListFragment {
    private BaseActivity act;
    public static final String FULL_LIST = "full list";
    private static ListResultsFragment listResultsFragment;

    public void setAct(BaseActivity act) {
        this.act = act;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.full_results_list, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        PagerResultsFragment pagerResultsFragment = act.getPagerResultsFragment();
        pagerResultsFragment.setCurrentItem(position);
        act.getSearchView().getSuggestionsAdapter().swapCursor(null);
        detach();
    }

    public static ListResultsFragment newInstance(BaseActivity act, ArrayList<Feature> features) {
        listResultsFragment = new ListResultsFragment();
        PlaceArrayAdapter placeArrayAdapter = new PlaceArrayAdapter(act,
                android.R.layout.simple_list_item_1, features);
        listResultsFragment.setListAdapter(placeArrayAdapter);
        listResultsFragment.setAct(act);
        return listResultsFragment;
    }

    public void attachToContainer(int container) {
        if (isAdded()) {
            show();
        } else {
            add(container);
        }
    }

    private void add(int container) {
        act.getSupportFragmentManager().beginTransaction()
                .addToBackStack(SEARCH_RESULTS_STACK)
                .add(container, this, FULL_LIST)
                .commit();
    }

    private void show() {
        act.getSupportFragmentManager().beginTransaction()
                .addToBackStack(SEARCH_RESULTS_STACK)
                .show(this)
                .commit();
    }

    public void detach() {
        act.getSupportFragmentManager().beginTransaction()
                .addToBackStack(SEARCH_RESULTS_STACK)
                .hide(this)
                .commit();
    }
}
