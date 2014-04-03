package com.mapzen.fragment;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.adapters.PlaceArrayAdapter;
import com.mapzen.entity.GeoFeature;
import com.mapzen.search.PagerResultsFragment;

import java.util.ArrayList;

public class ListResultsFragment extends ListFragment {
    public static final String TAG = ListResultsFragment.class.getSimpleName();
    private static ListResultsFragment listResultsFragment;
    private BaseActivity act;

    public void setAct(BaseActivity act) {
        this.act = act;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.full_results_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ActionBar actionBar = act.getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.results_title);
        }

        act.supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            act.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        PagerResultsFragment pagerResultsFragment = (PagerResultsFragment)
                act.getSupportFragmentManager().findFragmentByTag(PagerResultsFragment.TAG);

        pagerResultsFragment.setCurrentItem(position);
        act.getSearchView().getSuggestionsAdapter().swapCursor(null);
        act.onBackPressed();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        final ActionBar actionBar = act.getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(R.string.application_name);
        }

        act.supportInvalidateOptionsMenu();
        act.getSearchView().setQuery(((MapzenApplication) act.getApplication())
                .getCurrentSearchTerm(), false);
    }

    public static ListResultsFragment newInstance(BaseActivity act,
            ArrayList<GeoFeature> geoFeatures) {
        listResultsFragment = new ListResultsFragment();
        PlaceArrayAdapter placeArrayAdapter = new PlaceArrayAdapter(act,
                android.R.layout.simple_list_item_1, geoFeatures);
        listResultsFragment.setListAdapter(placeArrayAdapter);
        listResultsFragment.setAct(act);
        return listResultsFragment;
    }
}
