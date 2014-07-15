package com.mapzen.adapters;

import com.mapzen.R;
import com.mapzen.entity.SimpleFeature;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class PlaceArrayAdapter extends ArrayAdapter<SimpleFeature> {
    private List<SimpleFeature> simpleFeatures;

    public PlaceArrayAdapter(Context context, List<SimpleFeature> simpleFeatures) {
        super(context, 0, simpleFeatures);
        this.simpleFeatures = simpleFeatures;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final SimpleFeature simpleFeature = simpleFeatures.get(position);
        SimpleFeature.ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.search_result_list_item, null);
            holder = new SimpleFeature.ViewHolder();
            holder.setTitle((TextView) convertView.findViewById(R.id.title));
            holder.setAddress((TextView) convertView.findViewById(R.id.address));
            convertView.setTag(holder);
        } else {
            holder = (SimpleFeature.ViewHolder) convertView.getTag();
        }

        holder.setFromFeature(simpleFeature);
        return convertView;
    }
}
