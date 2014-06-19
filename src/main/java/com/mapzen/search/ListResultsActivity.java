package com.mapzen.search;

import com.mapzen.entity.SimpleFeature;
import com.mapzen.fragment.ListResultsFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import java.util.ArrayList;

public class ListResultsActivity extends FragmentActivity {
    public static final String EXTRA_FEATURE_LIST = "com.mapzen.search.features";
    public static final String EXTRA_INDEX = "com.mapzen.search.index";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ArrayList<SimpleFeature> simpleFeatures =
                getIntent().getParcelableArrayListExtra(EXTRA_FEATURE_LIST);
        final ListResultsFragment fragment = ListResultsFragment.newInstance(simpleFeatures);
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
