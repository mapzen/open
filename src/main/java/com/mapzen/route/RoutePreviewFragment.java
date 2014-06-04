package com.mapzen.route;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.fragment.BaseFragment;
import com.mapzen.osrm.Route;
import com.mapzen.osrm.Router;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.mapzen.MapController.geoPointToPair;
import static com.mapzen.MapController.getMapController;
import static com.mapzen.MapController.locationToPair;
import static com.mapzen.entity.SimpleFeature.NAME;

public class RoutePreviewFragment extends BaseFragment implements Router.Callback {
    public static final String TAG = RoutePreviewFragment.class.getSimpleName();
    private SimpleFeature destination;

    @Inject Router router;
    @InjectView(R.id.starting_point) TextView startingPointTextView;
    @InjectView(R.id.destination) TextView destinationTextView;

    public static RoutePreviewFragment newInstance(BaseActivity act,
            SimpleFeature destination) {
        final RoutePreviewFragment fragment = new RoutePreviewFragment();
        fragment.setAct(act);
        fragment.setMapFragment(act.getMapFragment());
        fragment.inject();
        fragment.setDestination(destination);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        act.hideActionBar();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        act.showActionBar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.route_preview, container, false);
        ButterKnife.inject(this, view);
        startingPointTextView.setText("Current Location");
        destinationTextView.setText(destination.getProperty(NAME));
        return view;
    }

    public void createRouteToDestination() {
        mapFragment.clearMarkers();
        mapFragment.updateMap();
        act.showProgressDialog();
        router.clearLocations()
                .setLocation(locationToPair(getMapController().getLocation()))
                .setLocation(geoPointToPair(destination.getGeoPoint()))
                .setZoomLevel(getMapController().getZoomLevel())
                .setDriving()
                .setCallback(this)
                .fetch();
    }

    public void setDestination(SimpleFeature destination) {
        this.destination = destination;
    }


    @Override
    public void success(Route route) {
        if (!isAdded()) {
            act.getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.routes_preview_container, this, TAG)
                    .commit();
        }
        act.dismissProgressDialog();
    }

    @Override
    public void failure(int statusCode) {
        onServerError(statusCode);
    }
}
