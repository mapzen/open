package com.mapzen.route;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.fragment.BaseFragment;
import com.mapzen.osrm.Route;
import com.mapzen.osrm.Router;
import com.mapzen.util.Logger;

import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.core.BoundingBox;
import org.oscim.core.MapPosition;
import org.oscim.layers.PathLayer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
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
import static com.mapzen.util.DouglasPeuckerReducer.reduceWithTolerance;

public class RoutePreviewFragment extends BaseFragment
        implements Router.Callback {
    public static final String TAG = RoutePreviewFragment.class.getSimpleName();
    public static final int ROUTE_ZOOM_LEVEL = 19;
    public static final int REDUCE_TOLERANCE = 100;
    private SimpleFeature destination;
    private boolean reverse = false;
    private Type transportationMode = DRIVING;
    private Route route;

    @Inject PathLayer path;
    @Inject ItemizedLayer<MarkerItem> markers;

    @Inject Router router;
    @InjectView(R.id.starting_point) TextView startingPointTextView;
    @InjectView(R.id.destination) TextView destinationTextView;
    @InjectView(R.id.route_reverse) ImageButton routeReverse;
    @InjectView(R.id.starting_location_icon) ImageView startLocationIcon;
    @InjectView(R.id.destination_location_icon) ImageView destinationLocationIcon;
    @InjectView(R.id.start_location_layout) LinearLayout startLocationLayout;
    @InjectView(R.id.destination_layout) LinearLayout destinationLayout;
    @InjectView(R.id.to_text) TextView toTextView;
    @InjectView(R.id.from_text) TextView fromTextView;
    @InjectView(R.id.routing_circle) ImageButton routingCircle;
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
        app.deactivateMoveMapToLocation();
        act.hideActionBar();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getMapController().getMap().layers().remove(markers);
        getMapController().getMap().layers().remove(path);
        mapFragment.updateMap();
        act.enableActionbar();
        act.showActionBar();
        app.activateMoveMapToLocation();
        unregisterViewUpdater();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.route_preview, container, false);
        ButterKnife.inject(this, view);
        setOriginAndDestination();
        registerViewUpdater();
        return view;
    }

    private void setOriginAndDestination() {
        toTextView.setVisibility(View.VISIBLE);
        fromTextView.setVisibility(View.VISIBLE);

        if (!reverse) {
            startingPointTextView.setText(getString(R.string.current_location));
            destinationTextView.setText(destination.getProperty(NAME));
            startLocationIcon.setVisibility(View.VISIBLE);
            destinationLocationIcon.setVisibility(View.GONE);

        } else {
            startingPointTextView.setText(destination.getProperty(NAME));
            destinationTextView.setText(getString(R.string.current_location));
            startLocationIcon.setVisibility(View.GONE);
            destinationLocationIcon.setVisibility(View.VISIBLE);
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.route_reverse) public void reverse() {
        reverse = !reverse;
        toTextView.setVisibility(View.GONE);
        fromTextView.setVisibility(View.GONE);
        animateDestinationReverse();
        setOriginAndDestination();
        createRouteToDestination();
        setStartButtonImage();
    }

    private void setStartButtonImage() {
        if (reverse) {
            routingCircle.setImageResource(R.drawable.ic_preview_button);
            routingCircle.setTag(getString(R.string.view));
        } else {
            routingCircle.setImageResource(R.drawable.ic_start_button);
            routingCircle.setTag(getString(R.string.start));
        }
    }

    private void animateDestinationReverse() {
        Animation rotateAnimation = AnimationUtils.loadAnimation(act, R.anim.rotate180);
        routeReverse.startAnimation(rotateAnimation);
        Animation moveDown = AnimationUtils.loadAnimation(act, R.anim.move_down);
        Animation moveUp = AnimationUtils.loadAnimation(act, R.anim.move_up);

        startLocationLayout.startAnimation(moveDown);
        destinationLayout.startAnimation(moveUp);
    }

    @SuppressWarnings("unused")
    @OnCheckedChanged(R.id.by_car) public void byCar(boolean active) {
        if (active) {
            transportationMode = DRIVING;
            createRouteToDestination();
        }
    }
    @SuppressWarnings("unused")
    @OnCheckedChanged(R.id.by_bike) public void byBike(boolean active) {
        if (active) {
            transportationMode = BIKING;
            createRouteToDestination();
        }
    }
    @SuppressWarnings("unused")
    @OnCheckedChanged(R.id.by_foot) public void byFoot(boolean active) {
        if (active) {
            transportationMode = WALKING;
            createRouteToDestination();
        }
    }

    @OnClick(R.id.routing_circle)
    public void start() {
        if (!reverse) {
            startRouting();
        } else {
            showDirectionListFragment();
        }
    }

    public void createRouteToDestination() {
        mapFragment.clearMarkers();
        mapFragment.updateMap();
        act.showLoadingIndicator();
        router.clearLocations()
                .setLocation(getOriginPoint())
                .setLocation(getDestinationPoint())
                .setZoomLevel(ROUTE_ZOOM_LEVEL)
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
        act.hideLoadingIndicator();
        List<Location> points = route.getGeometry();
        long time = System.currentTimeMillis();
        Logger.d("RoutePreviewFragment::success Geometry points before: " + points.size());
        if (points.size() > 100) {
            points = reduceWithTolerance(points, REDUCE_TOLERANCE);
        }
        Logger.d("Timing: " + String.valueOf(System.currentTimeMillis() - time));
        Logger.d("RoutePreviewFragment::success Geometry points after: " + points.size());
        path.clearPath();
        double minlat = Integer.MAX_VALUE;
        double minlon = Integer.MAX_VALUE;
        double maxlat = Integer.MIN_VALUE;
        double maxlon = Integer.MIN_VALUE;
        for (Location loc : points) {
            maxlat = Math.max(maxlat, loc.getLatitude());
            maxlon = Math.max(maxlon, loc.getLongitude());
            minlat = Math.min(minlat, loc.getLatitude());
            minlon = Math.min(minlon, loc.getLongitude());
            path.addPoint(locationToGeoPoint(loc));
        }

        BoundingBox bbox = new BoundingBox(minlat, minlon, maxlat, maxlon);
        int w = getMapController().getMap().getWidth();
        int h = getMapController().getMap().getHeight();
        MapPosition position = new MapPosition();
        position.setByBoundingBox(bbox, w, h);

        position.setScale(position.getZoomScale() * 0.85);

        getMapController().getMap().setMapPosition(position);

        if (!getMapController().getMap().layers().contains(path)) {
            getMapController().getMap().layers().add(path);
        }

        if (!getMapController().getMap().layers().contains(markers)) {
            getMapController().getMap().layers().add(markers);
        }
        markers.removeAllItems();
        markers.addItem(getMarkerItem(R.drawable.ic_a, points.get(0)));
        markers.addItem(getMarkerItem(R.drawable.ic_b, points.get(points.size() - 1)));
    }

    @Override
    public void failure(int statusCode) {
        path.clearPath();
        onServerError(statusCode);
    }

    @Override
    public void onViewUpdate() {
        createRouteToDestination();
    }

    private MarkerItem getMarkerItem(int icon, Location loc) {
        MarkerItem markerItem = new MarkerItem("Generic Marker",
                "Generic Description", locationToGeoPoint(loc));
        markerItem.setMarker(new MarkerSymbol(
                AndroidGraphics.drawableToBitmap(app.getResources().getDrawable(icon)),
                MarkerItem.HotspotPlace.BOTTOM_CENTER));
        return markerItem;
    }

    private void showDirectionListFragment() {
        final Fragment fragment = DirectionListFragment.
                newInstance(route.getRouteInstructions(),
                        new DirectionListFragment.DirectionListener() {
                            @Override
                            public void onInstructionSelected(int index) {
                            }
                        }, destination, reverse);
        act.getSupportFragmentManager().beginTransaction()
                .add(R.id.full_list, fragment, DirectionListFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    private void startRouting() {
        hideFragmentContents();
        RouteFragment routeFragment = RouteFragment.newInstance(act, destination);
        routeFragment.setRoute(route);
        act.getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .add(R.id.routes_container, routeFragment, RouteFragment.TAG)
                .commit();
        path.clearPath();
        getMapController().getMap().layers().remove(markers);
    }

    private void hideFragmentContents() {
        act.getSupportFragmentManager().beginTransaction().hide(this).commit();
    }

    public void showFragmentContents() {
        act.getSupportFragmentManager().beginTransaction().show(this).commit();
    }
}
