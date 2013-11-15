package com.mapzen;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchViewAdapter extends ArrayAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> entries;
    private ViewHolder holder;
    public SearchViewAdapter(Activity a, int textViewResourceId, ArrayList<HashMap<String, String>> ent) {
        super(a, textViewResourceId, ent);
        activity = a;
        entries = ent;
    }

    public static class ViewHolder{
        public TextView item1;
        public TextView lat;
        public TextView lon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi =
                    (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.search_item, null);
            holder = new ViewHolder();
            holder.item1 = (TextView) v.findViewById(R.id.big);
            holder.lat = (TextView) v.findViewById(R.id.lat);
            holder.lon = (TextView) v.findViewById(R.id.lon);
            v.setTag(holder);
        } else {
            holder=(ViewHolder)v.getTag();
        }

        final HashMap<String, String> custom = entries.get(position);
        if (custom != null) {
            holder.item1.setText(custom.get("display_name"));
            holder.lat.setText(custom.get("lat"));
            holder.lon.setText(custom.get("lon"));
        }
        return v;
    }
}
