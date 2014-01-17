package com.mapzen.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import com.mapzen.R;
import com.mapzen.adapters.PlaceArrayAdapter;
import com.mapzen.entity.Feature;

import java.util.ArrayList;

public class FullSearchResultsActivity extends Activity {
    private ArrayList<Feature> list;
    private String currentSearchTerm;
    private int currentPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.full_results_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        list = intent.getParcelableArrayListExtra("features");
        currentSearchTerm = intent.getStringExtra("savedSearchTerm");
        currentPos = intent.getIntExtra("pagePos", 0);
        final ListView listview = (ListView) findViewById(R.id.full_list);
        final PlaceArrayAdapter adapter = new PlaceArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            Intent intent = new Intent(this, BaseActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("features", list);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        return true;
    }

    public static Intent getIntent(Context context, ArrayList<Feature> features) {
        Intent intent = new Intent(context, FullSearchResultsActivity.class);
        intent.putParcelableArrayListExtra("features", features);
        return intent;
    }

}
