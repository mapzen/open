package com.mapzen.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.entity.Feature;

import java.util.ArrayList;
import java.util.List;

import static com.mapzen.MapzenApplication.NO_PLACE_PICKED_REQUEST;
import static com.mapzen.MapzenApplication.PICK_PLACE_REQUEST;

public class FullSearchResultsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.full_results_list);
        final ListView listview = (ListView) findViewById(R.id.full_list);
        Intent intent = getIntent();
        final ArrayList<Feature> list = intent.getParcelableArrayListExtra("features");
        final PlaceArrayAdapter adapter = new PlaceArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        if (itemId  == android.R.id.home) {
            setResult(NO_PLACE_PICKED_REQUEST);
            finish();
        }
        return true;
    }

    public static Intent getIntent(Context context, ArrayList<Feature> features) {
        Intent intent = new Intent(context, FullSearchResultsActivity.class);
        intent.putParcelableArrayListExtra("features", features);
        return intent;
    }

    private class PlaceArrayAdapter extends ArrayAdapter<Feature> {
        private List<Feature> features = new ArrayList<Feature>();
        public PlaceArrayAdapter(Context context, int textViewResourceId,
                                 List<Feature> objects) {
            super(context, textViewResourceId, objects);
            features = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            final Feature feature = features.get(position);
            Feature.ViewHolder holder;
            if (v == null) {
                LayoutInflater vi =
                        (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.search_item, null);
                holder = new Feature.ViewHolder();
                holder.title = (TextView) v.findViewById(R.id.place_title);
                holder.address = (TextView) v.findViewById(R.id.place_address);
                v.setTag(holder);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent data = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("feature", feature);
                        data.putExtras(bundle);
                        setResult(PICK_PLACE_REQUEST, data);
                        finish();
                    }
                });
            } else {
                holder = (Feature.ViewHolder) v.getTag();
            }
            holder.setFromFeature(feature);
            return v;
        }
    }
}
