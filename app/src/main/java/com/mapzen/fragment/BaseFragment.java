package com.mapzen.fragment;

import android.support.v4.app.Fragment;

import com.mapzen.MapzenApplication;
import com.mapzen.activity.BaseActivity;

import org.oscim.map.Map;

public abstract class BaseFragment extends Fragment {
    protected Map map;
    protected BaseActivity act;
    protected MapFragment mapFragment;
    protected MapzenApplication app;

    public void setMap(Map map) {
        this.map = map;
    }

    public void setAct(BaseActivity act) {
        this.act = act;
    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    public void setApp(MapzenApplication app) {
        this.app = app;
    }
}
