package com.mapzen.route;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.fragment.BaseFragment;
import com.mapzen.osrm.Route;
import com.mapzen.osrm.Router;

import org.oscim.backend.canvas.Color;
import org.oscim.core.BoundingBox;
import org.oscim.core.MapPosition;
import org.oscim.layers.PathLayer;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.mapzen.MapController.geoPointToPair;
import static com.mapzen.MapController.getMapController;
import static com.mapzen.MapController.locationToGeoPoint;
import static com.mapzen.MapController.locationToPair;
import static com.mapzen.entity.SimpleFeature.NAME;

public class RoutePreviewFragment extends BaseFragment implements Router.Callback {
    public static final String TAG = RoutePreviewFragment.class.getSimpleName();
    private SimpleFeature destination;
    private boolean reverse = false;
    private String transportationMode = "c";

    @Inject PathLayer path;

    @Inject Router router;
    @InjectView(R.id.starting_point) TextView startingPointTextView;
    @InjectView(R.id.destination) TextView destinationTextView;
    @InjectView(R.id.route_reverse) ImageButton routeReverse;
    @InjectView(R.id.by_car) ImageButton byCar;
    @InjectView(R.id.by_foot) ImageButton byFoot;
    @InjectView(R.id.by_bike) ImageButton byBike;

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
        setOriginAndDestination();
        return view;
    }

    private void setOriginAndDestination() {
        if (!reverse) {
            startingPointTextView.setText("Current Location");
            destinationTextView.setText(destination.getProperty(NAME));
        } else {
            startingPointTextView.setText(destination.getProperty(NAME));
            destinationTextView.setText("Current Location");
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.route_reverse) public void reverse() {
        reverse = !reverse;
        setOriginAndDestination();
        createRouteToDestination();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.by_car) public void byCar() {
        transportationMode = "c";
        createRouteToDestination();
    }
    @SuppressWarnings("unused")
    @OnClick(R.id.by_bike) public void byBike() {
        transportationMode = "b";
        createRouteToDestination();
    }
    @SuppressWarnings("unused")
    @OnClick(R.id.by_foot) public void byFoot() {
        transportationMode = "w";
        createRouteToDestination();
    }

    public void createRouteToDestination() {
        mapFragment.clearMarkers();
        mapFragment.updateMap();
        act.showProgressDialog();
        router.clearLocations()
                .setLocation(getOriginPoint())
                .setLocation(getDestinationPoint())
                .setZoomLevel(getRouteZoomLevel())
                .setCallback(this);
        if (transportationMode.equals("c")) {
            router.setDriving();
        } else if (transportationMode.equals("w")) {
            router.setWalking();
        } else if (transportationMode.equals("b")) {
            router.setBiking();
        }
        router.fetch();
    }

    private double getRouteZoomLevel() {
        return getMapController().getZoomLevel();
    }

    private double[] getDestinationPoint() {
        return reverse ? locationToPair(getMapController().getLocation()) :
            geoPointToPair(destination.getGeoPoint());
    }

    private double[] getOriginPoint() {
        return !reverse ? locationToPair(getMapController().getLocation()) :
            geoPointToPair(destination.getGeoPoint());
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
        ArrayList<Location> points = route.getGeometry();
        path.clearPath();
        double minlat = points.get(0).getLatitude();
        double minlon = points.get(0).getLongitude();
        double maxlat = points.get(0).getLatitude();
        double maxlon = points.get(0).getLongitude();
        for (Location loc : points) {
            if (maxlat < loc.getLatitude()) {
                maxlat = loc.getLatitude();
            }
            if (maxlon < loc.getLongitude()) {
                maxlon = loc.getLongitude();
            }
            if (minlat > loc.getLatitude()) {
                minlat = loc.getLatitude();
            }
            if (minlon > loc.getLongitude()) {
                minlon = loc.getLongitude();
            }
            path.addPoint(locationToGeoPoint(loc));
        }

        BoundingBox bbox = new BoundingBox(minlat, minlon, maxlat, maxlon);
        int w = getMapController().getMap().getWidth();
        int h = getMapController().getMap().getHeight();
        MapPosition position = new MapPosition();
        position.setByBoundingBox(bbox, w, h);

        getMapController().getMap().setMapPosition(position);
        if (!getMapController().getMap().layers().contains(path)) {
            getMapController().getMap().layers().add(path);
        }
    }

    @Override
    public void failure(int statusCode) {
        path.clearPath();
        onServerError(statusCode);
    }
}
