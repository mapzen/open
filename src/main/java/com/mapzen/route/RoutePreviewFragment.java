package com.mapzen.route;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.fragment.BaseFragment;
import com.mapzen.osrm.Route;
import com.mapzen.osrm.Router;
import com.mapzen.widget.DistanceView;

import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.core.BoundingBox;
import org.oscim.core.MapPosition;
import org.oscim.layers.PathLayer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;

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
import static com.mapzen.osrm.Router.Type;
import static com.mapzen.osrm.Router.Type.BIKING;
import static com.mapzen.osrm.Router.Type.DRIVING;
import static com.mapzen.osrm.Router.Type.WALKING;

public class RoutePreviewFragment extends BaseFragment implements Router.Callback {
    public static final String TAG = RoutePreviewFragment.class.getSimpleName();
    private SimpleFeature destination;
    private boolean reverse = false;
    private Type transportationMode = DRIVING;
    private Route route;

    @Inject PathLayer path;
    @Inject ItemizedLayer<MarkerItem> markers;

    @Inject Router router;
    @InjectView(R.id.starting_point) TextView startingPointTextView;
    @InjectView(R.id.destination) TextView destinationTextView;
    @InjectView(R.id.destination_preview) TextView destinationPreview;
    @InjectView(R.id.destination_preview_distance) DistanceView destinationPreviewDistance;
    @InjectView(R.id.route_reverse) ImageButton routeReverse;
    @InjectView(R.id.by_car) ImageButton byCar;
    @InjectView(R.id.by_foot) ImageButton byFoot;
    @InjectView(R.id.by_bike) ImageButton byBike;
    @InjectView(R.id.start) TextView startBtn;

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
        act.enableActionbar();
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
            destinationPreview.setText(destination.getProperty(NAME));
            startBtn.setText("Start");
        } else {
            startingPointTextView.setText(destination.getProperty(NAME));
            destinationTextView.setText("Current Location");
            destinationPreview.setText("Current Location");
            startBtn.setText("View");
        }
        if (route != null) {
            destinationPreviewDistance.setDistance(route.getTotalDistance());
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
        transportationMode = DRIVING;
        createRouteToDestination();
    }
    @SuppressWarnings("unused")
    @OnClick(R.id.by_bike) public void byBike() {
        transportationMode = BIKING;
        createRouteToDestination();
    }
    @SuppressWarnings("unused")
    @OnClick(R.id.by_foot) public void byFoot() {
        transportationMode = WALKING;
        createRouteToDestination();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.start) public void start() {
        if (!reverse) {
            RouteFragment routeFragment = RouteFragment.newInstance(act, destination);
            routeFragment.setRoute(route);
            act.getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.routes_container, routeFragment, RouteFragment.TAG)
                    .commit();
            getMapController().getMap().layers().remove(markers);
        }
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
        if (transportationMode.equals(DRIVING)) {
            router.setDriving();
        } else if (transportationMode.equals(WALKING)) {
            router.setWalking();
        } else if (transportationMode.equals(BIKING)) {
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
        this.route = route;
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

        if (!getMapController().getMap().layers().contains(markers)) {
            getMapController().getMap().layers().add(markers);
        }
        markers.removeAllItems();
        markers.addItem(getMarkerItem(points.get(0)));
        markers.addItem(getMarkerItem(points.get(points.size() - 1)));
    }

    @Override
    public void failure(int statusCode) {
        path.clearPath();
        onServerError(statusCode);
    }

    private MarkerItem getMarkerItem(Location loc) {
        MarkerItem markerItem = new MarkerItem("Generic Marker",
                "Generic Description", locationToGeoPoint(loc));
        markerItem.setMarker(new MarkerSymbol(
                AndroidGraphics.drawableToBitmap(app.getResources().getDrawable(R.drawable.ic_a)),
                MarkerItem.HotspotPlace.BOTTOM_CENTER));
        return markerItem;
    }
}
