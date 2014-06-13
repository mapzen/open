package com.mapzen.adapters;

import com.mapzen.R;
import com.mapzen.entity.SimpleFeature;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PlaceArrayAdapter extends ArrayAdapter<SimpleFeature> {
    private ArrayList<SimpleFeature> simpleFeatures = new ArrayList<SimpleFeature>();

    public PlaceArrayAdapter(Context context, int textViewResourceId,
            ArrayList<SimpleFeature> objects) {
        super(context, textViewResourceId, objects);
        simpleFeatures = objects;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View v = convertView;
        final SimpleFeature simpleFeature = simpleFeatures.get(position);
        SimpleFeature.ViewHolder holder;
        if (v == null) {
            LayoutInflater vi =
                    (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.search_item, null);
            holder = new SimpleFeature.ViewHolder();
            holder.setTitle((TextView) v.findViewById(R.id.title));
            holder.setAddress((TextView) v.findViewById(R.id.address));
            v.setTag(holder);
        } else {
            holder = (SimpleFeature.ViewHolder) v.getTag();
        }
        holder.setFromFeature(simpleFeature);
        return v;
    }
}

