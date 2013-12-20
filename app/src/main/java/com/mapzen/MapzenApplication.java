package com.mapzen;

import android.app.Application;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;

import static android.provider.BaseColumns._ID;

public class MapzenApplication extends Application implements LocationListener {
    public static final int LOCATION_UPDATE_FREQUENCY = 1000;
    public static final float LOCATION_UPDATE_MIN_DISTANCE = 10.0f;
    private static MapzenApplication app;
    private static Location location = null;
    public static final String PELIAS_TEXT = "text";
    public static final double DEFAULT_LATITUDE = 64.133333;
    public static final double DEFAULT_LONGITUDE = -21.933333;
    public static final int DEFAULT_ZOOMLEVEL = 15;
    private static MapPosition mapPosition =
            new MapPosition(DEFAULT_LATITUDE, DEFAULT_LONGITUDE, Math.pow(2, DEFAULT_ZOOMLEVEL));
    private String currentSearchTerm = "";
    private int currentPagerPosition = 0;
    private RequestQueue queue;

    private LocationManager locationManager;

    private final String[] columns = {
        _ID, PELIAS_TEXT
    };

    public String[] getColumns() {
        return columns;
    }

    public static final String LOG_TAG = "Mapzen: ";

    public MapzenApplication() {
        super();
        app = this;
    }

    public static MapzenApplication getApp(Context context) {
        if (app == null) {
            app = new MapzenApplication();
        }
        app.queue = Volley.newRequestQueue(context);
        app.locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        app.location = app.locationManager.getLastKnownLocation(app.getBestProvider());
        return app;
    }

    public void storeMapPosition(MapPosition pos) {
        mapPosition = pos;
    }

    public RequestQueue getQueue() {
        return queue;
    }

    public double getStoredZoomLevel() {
        return mapPosition.zoomLevel;
    }

    public Location getLocation() {
        return location;
    }

    private String getBestProvider() {
        Criteria criteria = new Criteria();
        return locationManager.getBestProvider(criteria, true);
    }

    public void setupLocationUpdates() {
        locationManager.requestLocationUpdates(getBestProvider(),
                LOCATION_UPDATE_FREQUENCY, LOCATION_UPDATE_MIN_DISTANCE, this);
    }

    public void stopLocationUpdates() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(Location loc) {
        location = loc;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    public GeoPoint getLocationPoint() {
        return getLocationPosition().getGeoPoint();
    }

    public MapPosition getLocationPosition() {
        if (location != null) {
            double lat = getLocation().getLatitude();
            double lon = getLocation().getLongitude();
            mapPosition = new MapPosition(lat, lon, Math.pow(2, getStoredZoomLevel()));
        } else {
            mapPosition =
                    new MapPosition(DEFAULT_LATITUDE, DEFAULT_LONGITUDE, Math.pow(2, getStoredZoomLevel()));
        }
        return mapPosition;
    }

    public String getCurrentSearchTerm() {
        return currentSearchTerm;
    }

    public void setCurrentSearchTerm(String currentSearchTerm) {
        this.currentSearchTerm = currentSearchTerm;
    }

    public int getCurrentPagerPosition() {
        return currentPagerPosition;
    }

    public void setCurrentPagerPosition(int currentPagerPosition) {
        this.currentPagerPosition = currentPagerPosition;
    }

    public void enqueueApiRequest(Request<?> request) {
        queue.add(request);
    }
}
