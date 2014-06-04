package com.mapzen.route;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.fragment.BaseFragment;
import com.mapzen.osrm.Route;
import com.mapzen.osrm.Router;

import javax.inject.Inject;

import static com.mapzen.MapController.geoPointToPair;
import static com.mapzen.MapController.getMapController;
import static com.mapzen.MapController.locationToPair;

public class RoutePreviewFragment extends BaseFragment implements Router.Callback {
    @Inject Router router;
    public static final String TAG = RoutePreviewFragment.class.getSimpleName();

    public static RoutePreviewFragment newInstance(BaseActivity act) {
        final RoutePreviewFragment fragment = new RoutePreviewFragment();
        fragment.setAct(act);
        fragment.setMapFragment(act.getMapFragment());
        fragment.inject();
        return fragment;
    }

    public void createRouteTo(SimpleFeature destination) {
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

    @Override
    public void success(Route route) {
        if (!isAdded()) {
            act.getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.routes_container, this, TAG)
                    .commit();
        }
        act.dismissProgressDialog();
    }

    @Override
    public void failure(int statusCode) {
        onServerError(statusCode);
    }
}
