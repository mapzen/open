package com.mapzen.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mapzen.R;
import com.mapzen.entity.Place;

import java.util.ArrayList;
import java.util.List;

public class FullSearchResultsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.full_results_list);
        final ListView listview = (ListView) findViewById(R.id.full_list);
        Intent intent = getIntent();
        final ArrayList<Place> list = intent.getParcelableArrayListExtra("places");
        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
    }

    public static Intent getIntent(Context context, ArrayList<Place> places) {
        Intent intent = new Intent(context, FullSearchResultsActivity.class);
        intent.putParcelableArrayListExtra("places", places);
        return intent;
    }

    private class StableArrayAdapter extends ArrayAdapter<Place> {
        List<Place> places = new ArrayList<Place>();
        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<Place> objects) {
            super(context, textViewResourceId, objects);
            places = objects;
        }

        public class ViewHolder{
            public TextView item1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder holder;
            if (v == null) {
                LayoutInflater vi =
                        (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.search_item, null);
                holder = new ViewHolder();
                holder.item1 = (TextView) v.findViewById(R.id.big);
                v.setTag(holder);
            }
            else
                holder=(ViewHolder)v.getTag();

            final Place custom = places.get(position);
            if (custom != null) {
                holder.item1.setText(custom.getDisplayName());
            }
            return v;
        }
    }
}