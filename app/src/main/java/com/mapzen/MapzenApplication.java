package com.mapzen;

import android.app.Application;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;

public class MapzenApplication extends Application {
    private static Location location = null;

    // default to Reykjavik
    public static double[] DEFAULT_COORDINATES =  {
            Double.parseDouble("64.133333") * 1E6,
            Double.parseDouble("-21.933333") * 1E6
    };

    public static String LOG_TAG = "Mapzen: ";

    public static int getZoomLevel() {
        return zoomLevel;
    }

    public static void setZoomLevel(int zoomLevel) {
        MapzenApplication.zoomLevel = zoomLevel;
    }

    public static int zoomLevel = 15;

    public static Location getLocation(Context context) {
        if(location == null) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            location = locationManager.getLastKnownLocation(provider);
        }
        return location;
    }

    public static MapPosition getLocationPosition(Context context) {
        Location location = getLocation(context);
        MapPosition mapPosition = null;
        if(location != null) {
            double lat = getLocation(context).getLatitude();
            double lon = getLocation(context).getLongitude();
            mapPosition = new MapPosition(lat, lon, Math.pow(2, zoomLevel));
        } else {
            mapPosition = new MapPosition(40.67f, -73.94f, Math.pow(2, zoomLevel));
        }

        return mapPosition;
    }
}
