package com.mapzen.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Feature;

public class ItemFragment extends BaseFragment {
    private Feature feature;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_item, container, false);
        Feature.ViewHolder holder;
        if (view != null) {
            holder = new Feature.ViewHolder();
            holder.setTitle((TextView) view.findViewById(R.id.place_title));
            holder.setAddress((TextView) view.findViewById(R.id.place_address));
            ImageButton button = (ImageButton) view.findViewById(R.id.btn_route_go);
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RouteFragment routeFragment = new RouteFragment();
                    routeFragment.setFrom(app.getLocationPoint());
                    routeFragment.setFeature(feature);
                    routeFragment.setMapFragment(mapFragment);
                    routeFragment.setAct(act);
                    routeFragment.attachToActivity();
                }
            });
            view.setTag(holder);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((BaseActivity) getActivity()).showPlace(feature, true);
                }
            });
        } else {
            holder = (Feature.ViewHolder) view.getTag();
        }

        holder.setFromFeature(feature);
        return view;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Feature getFeature() {
        return feature;
    }
}
