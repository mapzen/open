package com.mapzen.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Feature;

public class ItemFragment extends BaseFragment {
    private Feature feature;

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_item, container, false);
        Feature.ViewHolder holder;
        if (view != null) {
            holder = new Feature.ViewHolder();
            holder.setTitle((TextView) view.findViewById(R.id.place_title));
            holder.setAddress((TextView) view.findViewById(R.id.place_address));
            Button button = (Button) view.findViewById(R.id.btn_route_go);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RouteFragment routeFragment = new RouteFragment();
                    routeFragment.setFrom(app.getLocationPoint());
                    routeFragment.setDestination(feature.getGeoPoint());
                    routeFragment.setMapFragment(mapFragment);
                    routeFragment.setApp(app);
                    routeFragment.attachTo((BaseActivity) getActivity());
                }
            });
            holder.setButton(button);
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

    public Feature getFeature() {
        return feature;
    }
}
