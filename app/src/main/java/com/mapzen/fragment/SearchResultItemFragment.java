package com.mapzen.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapzen.R;
import com.mapzen.entity.Place;

public class SearchResultItemFragment extends Fragment {
    private Place place;

    public SearchResultItemFragment (Place place) {
        this.place = place;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_item, container, false);
        TextView tv = (TextView) view.findViewById(R.id.place_title);
        tv.setText(place.getDisplayName());
        return view;
    }

    public Place getPlace() {
        return place;
    }
}