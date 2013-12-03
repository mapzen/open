package com.mapzen;

import android.app.Application;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

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
        if(location == null) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            location = locationManager.getLastKnownLocation(provider);
        }

        return location;
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
