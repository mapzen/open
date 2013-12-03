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

public class MapzenApplication extends Application {

    public static String PELIAS_TEXT = "text";
    public static String PELIAS_LAT = "lat";
    public static String PELIAS_LON = "lon";
    public static String PELIAS_PAYLOAD = "payload";

    private static Location location = null;
    private static MapPosition mapPosition =
            new MapPosition(64.133333, -21.933333, Math.pow(2, 15));

    public static String LOG_TAG = "Mapzen: ";

    public static void storeMapPosition(MapPosition pos) {
        mapPosition = pos;
    }

    public static double getStoredZoomLevel() {
        return mapPosition.zoomLevel;
    }

    public static Location getLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        if(location == null) {
            location = locationManager.getLastKnownLocation(provider);
        }
        setupLocationUpdates(locationManager, provider);

        return location;
    }

    private static void setupLocationUpdates(LocationManager locationManager, String provider) {
        locationManager.requestLocationUpdates(provider, 1000, 10.0f, new LocationListener() {
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
        });
    }

    public static GeoPoint getLocationPoint(Context context) {
        return getLocationPosition(context).getGeoPoint();
    }

    public static MapPosition getLocationPosition(Context context) {
        Location location = getLocation(context);
        MapPosition mapPosition;
        if(location != null) {
            double lat = getLocation(context).getLatitude();
            double lon = getLocation(context).getLongitude();
            mapPosition = new MapPosition(lat, lon, Math.pow(2, getStoredZoomLevel()));
        } else {
            mapPosition = new MapPosition(40.67f, -73.94f, Math.pow(2, getStoredZoomLevel()));
        }
        return mapPosition;
    }
}
