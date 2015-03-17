package com.mapzen.open.adapters;

import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.entity.SimpleFeature;
import com.mapzen.open.event.RoutePreviewEvent;

import com.squareup.otto.Bus;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

public class SearchViewAdapter extends PagerAdapter {
    private final Context context;
    private final List<SimpleFeature> features;

    @Inject Bus bus;

    public SearchViewAdapter(Context context, List<SimpleFeature> features) {
        this.context = context;
        this.features = features;
        ((MapzenApplication) context.getApplicationContext()).inject(this);
        bus.register(this);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View view = View.inflate(context, R.layout.search_result_pager_item, null);
        final TextView title = (TextView) view.findViewById(R.id.title);
        final TextView address = (TextView) view.findViewById(R.id.address);
        final ImageButton start = (ImageButton) view.findViewById(R.id.start);
        final SimpleFeature feature = features.get(position);
        title.setText(feature.getTitle());
        address.setText(feature.getAddress());
        start.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                bus.post(new RoutePreviewEvent(feature));
            }
        });
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
