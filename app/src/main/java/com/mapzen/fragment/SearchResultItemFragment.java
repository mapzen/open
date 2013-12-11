package com.mapzen.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Feature;

public class SearchResultItemFragment extends Fragment {
    private Feature feature;

    public SearchResultItemFragment(Feature feature) {
        this.feature = feature;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_item, container, false);
        TextView tv = (TextView) view.findViewById(R.id.place_title);
        tv.setText(feature.getDisplayName());
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity) getActivity()).showPlace(feature);
            }
        });
        return view;
    }

    public Feature getFeature() {
        return feature;
    }
}
