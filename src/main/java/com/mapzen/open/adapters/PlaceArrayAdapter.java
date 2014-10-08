package com.mapzen.open.adapters;

import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.entity.SimpleFeature;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

public class PlaceArrayAdapter extends ArrayAdapter<SimpleFeature> {
    private final List<SimpleFeature> features;
    @Inject Typeface typeface;

    public PlaceArrayAdapter(Context context, List<SimpleFeature> features) {
        super(context, 0, features);
        this.features = features;
        ((MapzenApplication) context.getApplicationContext()).inject(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.search_result_list_item, null);
            ((TextView) convertView).setTypeface(typeface);
        }

        ((TextView) convertView).setText(features.get(position).getSingleLine());
        return convertView;
    }
}
