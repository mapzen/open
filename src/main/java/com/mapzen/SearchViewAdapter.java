package com.mapzen;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.mapzen.entity.Place;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

import static com.mapzen.MapzenApplication.LOG_TAG;

public class SearchViewAdapter extends ArrayAdapter {
    private Activity activity;
    private ArrayList<Place> entries;
    private ViewHolder holder;
    public SearchViewAdapter(Activity a, int textViewResourceId, ArrayList<Place> ent) {
        super(a, textViewResourceId, ent);
        activity = a;
        entries = ent;
    }

    public static class ViewHolder{
        public TextView item1;
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
            v.setTag(R.string.tag_viewholder, holder);
        } else {
            holder=(ViewHolder)v.getTag(R.string.tag_viewholder);
        }

        final Place custom = entries.get(position);
        if (custom != null) {
            holder.item1.setText(custom.getDisplayName());
            v.setTag(R.string.tag_placeholder, custom);
        }
        return v;
    }
}
