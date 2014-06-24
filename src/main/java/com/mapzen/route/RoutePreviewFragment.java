package com.mapzen.route;

import android.view.MotionEvent;
import android.widget.RelativeLayout;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.fragment.BaseFragment;
import com.mapzen.osrm.Route;
import com.mapzen.osrm.Router;
import com.mapzen.widget.DistanceView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
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
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

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

public class RoutePreviewFragment extends BaseFragment
        implements Router.Callback {
    public static final String TAG = RoutePreviewFragment.class.getSimpleName();
    public static final int ROUTE_ZOOM_LEVEL = 19;
    private SimpleFeature destination;
    private boolean reverse = false;
    private Type transportationMode = DRIVING;
    private Route route;
    private Fragment fragment = null;
    private SlidingUpPanelLayout slideLayout;

    @Inject PathLayer path;
    @Inject ItemizedLayer<MarkerItem> markers;

    @Inject Router router;
    @InjectView(R.id.starting_point) TextView startingPointTextView;
    @InjectView(R.id.destination) TextView destinationTextView;
    @InjectView(R.id.destination_preview) TextView destinationPreview;
    @InjectView(R.id.destination_preview_distance) DistanceView destinationPreviewDistance;
    @InjectView(R.id.route_reverse) ImageButton routeReverse;
    @InjectView(R.id.by_car) RadioButton byCar;
    @InjectView(R.id.by_foot) RadioButton byFoot;
    @InjectView(R.id.by_bike) RadioButton byBike;
    @InjectView(R.id.start) TextView startBtn;
    @InjectView(R.id.routing_mode) RadioGroup routingMode;
    @InjectView(R.id.destination_container) RelativeLayout destinationContainer;
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
        getMapController().getMap().layers().remove(markers);
        getMapController().getMap().layers().remove(path);
        mapFragment.updateMap();
        act.enableActionbar();
        act.showActionBar();
        unregisterViewUpdater();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.route_preview, container, false);
        ButterKnife.inject(this, view);
        setOriginAndDestination();
        registerViewUpdater();
        initSlideLayout(view);
        return view;
    }

    private void initSlideLayout(View view) {
        slideLayout = (SlidingUpPanelLayout)  view.findViewById(R.id.sliding_layout);
        slideLayout.setSlidingEnabled(true);
        slideLayout.setVisibility(slideLayout.VISIBLE);
        slideLayout.setDragView(view.findViewById(R.id.destination_preview));
        slideLayout.setSlidingEnabled(false);
        addTouchListener();
        slideLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset < .99) {
                    if (fragment == null) {
                        showDirectionListFragmentInExpanded();
                    }
                }
                if (slideOffset > .99 && fragment != null) {
                    hideDirectionListFragment();
                    slideLayout.collapsePane();
                }
                if (slideOffset == 1.0) {
                    slideLayout.setSlidingEnabled(false);
                }
            }

            @Override
            public void onPanelExpanded(View panel) {

            }

            @Override
            public void onPanelCollapsed(View panel) {
                slideLayout.setSlidingEnabled(false);
            }

            @Override
            public void onPanelAnchored(View panel) {
            }
        });
    }

    private void addTouchListener() {
        destinationContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                slideLayout.setSlidingEnabled(true);
                return false;
            }
        });
    }

    public void collapseSlideLayout() {
        if (slideLayout.isExpanded()) {
            slideLayout.collapsePane();
        }
    }

    public boolean slideLayoutIsExpanded() {
        return slideLayout.isExpanded();
    }

    private void setOriginAndDestination() {
        if (!reverse) {
            startingPointTextView.setText(getString(R.string.current_location));
            destinationTextView.setText(destination.getProperty(NAME));
            destinationPreview.setText(destination.getProperty(NAME));
            startBtn.setText(getString(R.string.start));
        } else {
            startingPointTextView.setText(destination.getProperty(NAME));
            destinationTextView.setText(getString(R.string.current_location));
            destinationPreview.setText(getString(R.string.current_location));
            startBtn.setText(getString(R.string.view));
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

    @SuppressWarnings("unused")
    @OnClick(R.id.start) public void start() {
        if (!reverse) {
            startRouting();
        } else {
            showDirectionListFragment();
        }
    }

    public void createRouteToDestination() {
        mapFragment.clearMarkers();
        mapFragment.updateMap();
        act.showProgressDialog();
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

    private void showDirectionListFragmentInExpanded() {
         fragment = DirectionListFragment.
                newInstance(route.getRouteInstructions(),
                        new DirectionListFragment.DirectionListener() {
                    @Override
                    public void onInstructionSelected(int index) {
                    }
                });
        act.getSupportFragmentManager().beginTransaction()
                .replace(R.id.destination_container, fragment, DirectionListFragment.TAG)
                .disallowAddToBackStack()
                .commit();
    }

    private void showDirectionListFragment() {
       slideLayout.expandPane();
    }

    private void hideDirectionListFragment() {
        if (fragment != null) {
        act.getSupportFragmentManager()
                .beginTransaction()
                .disallowAddToBackStack()
                .remove(fragment)
                .commit();
        }
        fragment = null;
    }

    private void startRouting() {
        RouteFragment routeFragment = RouteFragment.newInstance(act, destination);
        routeFragment.setRoute(route);
        act.getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .add(R.id.routes_container, routeFragment, RouteFragment.TAG)
                .commit();
        act.getSupportFragmentManager().beginTransaction()
                .disallowAddToBackStack()
                .remove(this)
                .commit();
        getMapController().getMap().layers().remove(markers);

    }
}
