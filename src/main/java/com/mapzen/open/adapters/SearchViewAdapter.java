package com.mapzen.open.adapters;

import com.mapzen.open.R;
import com.mapzen.open.entity.SimpleFeature;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class SearchViewAdapter extends PagerAdapter {
    private final Context context;
    private final List<SimpleFeature> features;

    public SearchViewAdapter(Context context, List<SimpleFeature> features) {
        this.context = context;
        this.features = features;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View view = View.inflate(context, R.layout.search_result_pager_item, null);
        final TextView title = (TextView) view.findViewById(R.id.title);
        final TextView address = (TextView) view.findViewById(R.id.address);
        final SimpleFeature feature = features.get(position);
        title.setText(feature.getProperty(SimpleFeature.TEXT));
        address.setText(String.format("%s, %s", feature.getCity(), feature.getAdmin()));
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return features.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
