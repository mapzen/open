package com.mapzen;

import android.app.Application;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;

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

    private LocationManager locationManager;
    private Context context;

    public static final String LOG_TAG = "Mapzen: ";

    public MapzenApplication() {
        super();
    }

    private MapzenApplication(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(getBestProvider());
    }

    public static MapzenApplication getApp(Context context) {
        if (app == null) {
            app = new MapzenApplication(context);
        }
        return app;
    }

    public void storeMapPosition(MapPosition pos) {
        mapPosition = pos;
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
}
