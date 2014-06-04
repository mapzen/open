package com.mapzen.fragment;

import com.mapzen.R;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.route.RoutePreviewFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ItemFragment extends BaseFragment {
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.address)
    TextView address;
    @InjectView(R.id.start)
    TextView startButton;
    private SimpleFeature simpleFeature;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.search_item, container, false);
        ButterKnife.inject(this, view);
        initView();
        return view;
    }

    private void initView() {
        SimpleFeature.ViewHolder holder = new SimpleFeature.ViewHolder();
        holder.setTitle(title);
        holder.setAddress(address);
        holder.setFromFeature(simpleFeature);
    }

    @OnClick(R.id.start)
    public void start() {
        final RoutePreviewFragment routePreviewFragment =
                RoutePreviewFragment.newInstance(act, simpleFeature);
        routePreviewFragment.createRouteToDestination();
        act.promptForGPSIfNotEnabled();
    }

    public SimpleFeature getSimpleFeature() {
        return simpleFeature;
    }

    public void setSimpleFeature(SimpleFeature simpleFeature) {
        this.simpleFeature = simpleFeature;
    }
}

