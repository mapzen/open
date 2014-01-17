package com.mapzen.adapters;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Feature;

import java.util.ArrayList;

public class PlaceArrayAdapter extends ArrayAdapter<Feature> {
    private ArrayList<Feature> features = new ArrayList<Feature>();
    private Context context;

    public PlaceArrayAdapter(Context context, int textViewResourceId,
                             ArrayList<Feature> objects) {
        super(context, textViewResourceId, objects);
        features = objects;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        final Feature feature = features.get(position);
        Feature.ViewHolder holder;
        if (v == null) {
            LayoutInflater vi =
                    (LayoutInflater) context.getApplicationContext().
                            getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.search_item, null);
            holder = new Feature.ViewHolder();
            holder.setTitle((TextView) v.findViewById(R.id.place_title));
            holder.setAddress((TextView) v.findViewById(R.id.place_address));
            v.setTag(holder);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), BaseActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("feature", feature);
                    bundle.putParcelableArrayList("features", features);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });
        } else {
            holder = (Feature.ViewHolder) v.getTag();
        }
        holder.setFromFeature(feature);
        return v;
    }
}
