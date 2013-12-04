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

    public static String PELIAS_TEXT = "text";
    public static String PELIAS_LAT = "lat";
    public static String PELIAS_LON = "lon";
    public static String PELIAS_PAYLOAD = "payload";

    private static MapzenApplication app;
    private static Location location = null;
    private static MapPosition mapPosition =
            new MapPosition(64.133333, -21.933333, Math.pow(2, 15));

    private LocationManager locationManager;
    private Context context;

    public static String LOG_TAG = "Mapzen: ";

    public MapzenApplication() {
        super();
    }

    private MapzenApplication(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public static MapzenApplication getApp(Context context) {
        if(app == null) {
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
        if(location == null) {
            location = locationManager.getLastKnownLocation(getBestProvider());
        }
        return location;
    }

    private String getBestProvider() {
        Criteria criteria = new Criteria();
        return locationManager.getBestProvider(criteria, true);
    }

    public void setupLocationUpdates() {
        locationManager.requestLocationUpdates(getBestProvider(), 1000, 10.0f, this);
    }

    public void stopLocationUpdates() {
        if(locationManager != null) {
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
        Location location = getLocation();
        MapPosition mapPosition;
        if(location != null) {
            double lat = getLocation().getLatitude();
            double lon = getLocation().getLongitude();
            mapPosition = new MapPosition(lat, lon, Math.pow(2, getStoredZoomLevel()));
        } else {
            mapPosition = new MapPosition(40.67f, -73.94f, Math.pow(2, getStoredZoomLevel()));
        }
        return mapPosition;
    }
}
