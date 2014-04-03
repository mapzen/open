package com.mapzen.adapters;

import com.mapzen.R;
import com.mapzen.entity.GeoFeature;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PlaceArrayAdapter extends ArrayAdapter<GeoFeature> {
    private ArrayList<GeoFeature> geoFeatures = new ArrayList<GeoFeature>();

    public PlaceArrayAdapter(Context context, int textViewResourceId,
            ArrayList<GeoFeature> objects) {
        super(context, textViewResourceId, objects);
        geoFeatures = objects;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View v = convertView;
        final GeoFeature geoFeature = geoFeatures.get(position);
        GeoFeature.ViewHolder holder;
        if (v == null) {
            LayoutInflater vi =
                    (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.search_item, null);
            holder = new GeoFeature.ViewHolder();
            holder.setTitle((TextView) v.findViewById(R.id.title));
            holder.setAddress((TextView) v.findViewById(R.id.address));
            v.setTag(holder);
        } else {
            holder = (GeoFeature.ViewHolder) v.getTag();
        }
        holder.setFromFeature(geoFeature);
        return v;
    }
}
