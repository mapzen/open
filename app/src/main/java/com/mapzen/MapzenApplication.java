package com.mapzen;

import android.app.Application;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.mapzen.util.Logger;

import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;

import java.util.Date;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static com.mapzen.MapzenApplication.PriorityLocationListener.HIGH_PRIORITY;
import static com.mapzen.MapzenApplication.PriorityLocationListener.LOW_PRIORITY;
import static com.mapzen.MapzenApplication.PriorityLocationListener.MEDIUM_PRIORITY;

public class MapzenApplication extends Application {
    public static final int LOCATION_UPDATE_FREQUENCY = 1000;
    public static final float LOCATION_UPDATE_MIN_DISTANCE = 10.0f;
    public static final int HTTP_REQUEST_TIMEOUT_MS = 500;
    public static final String PELIAS_TEXT = "text";
    public static final double DEFAULT_LATITUDE = 64.133333;
    public static final double DEFAULT_LONGITUDE = -21.933333;
    public static final int DEFAULT_ZOOMLEVEL = 15;
    public static final String LOG_TAG = "Mapzen: ";
    private static MapzenApplication app;
    private static MapPosition mapPosition =
            new MapPosition(DEFAULT_LATITUDE, DEFAULT_LONGITUDE, Math.pow(2, DEFAULT_ZOOMLEVEL));
    private final String[] columns = {
            _ID, PELIAS_TEXT
    };
    private String currentSearchTerm = "";
    private int currentPagerPosition = 0;
    private RequestQueue queue;
    private LocationManager locationManager;
    private SparseArray<Location> location = new SparseArray<Location>(3);
    private LocationListener high_priority_listener = new PriorityLocationListener(HIGH_PRIORITY);
    private LocationListener med_priority_listener = new PriorityLocationListener(MEDIUM_PRIORITY);
    private LocationListener low_priority_listener = new PriorityLocationListener(LOW_PRIORITY);

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
        app.setLocation(PriorityLocationListener.LOW_PRIORITY, app.findBestLocation());
        return app;
    }

    public String[] getColumns() {
        return columns;
    }

    public Location findBestLocation() {
        List<String> providers = locationManager.getProviders(true);
        Logger.d("location: providers" + providers.toString());
        Location bestLocation = null;
        for (String provider : providers) {
            Logger.d("location: provider: " + provider.toString());
            Location loc = locationManager.getLastKnownLocation(provider);
            Logger.d("location: last known location: " + loc.toString());
            if (loc == null) {
                Logger.d("location: is null");
                continue;
            }
            if (bestLocation == null
                    || loc.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = loc;
                Logger.d("location: bestLocation: " + bestLocation.toString());
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return bestLocation;
    }

    public void storeMapPosition(MapPosition pos) {
        mapPosition = pos;
    }

    public double getStoredZoomLevel() {
        return mapPosition.zoomLevel;
    }

    public Location getLocation() {
        Location loc = location.get(0);
        if (loc == null) {
            loc = location.get(1);
        }
        if (loc == null) {
            loc = location.get(2);
        }
        return loc;
    }

    public void setLocation(int weight, Location location) {
        Logger.d("Location: setting location: weight: " +
                String.valueOf(weight) + ", time: " + new Date().toString());
        if (location == null) {
            Logger.d("Location: location is null");
        } else {
            this.location.put(weight, location);
        }
    }

    private String getBestProvider() {
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Logger.d("Location: Best provider: " + provider);
        return provider;
    }

    public void setupLocationUpdates() {
        Logger.d("Location: Requesting updates");
        locationManager.requestLocationUpdates(getBestProvider(),
                LOCATION_UPDATE_FREQUENCY, LOCATION_UPDATE_MIN_DISTANCE,
                high_priority_listener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                LOCATION_UPDATE_FREQUENCY, LOCATION_UPDATE_MIN_DISTANCE,
                med_priority_listener);
        locationManager.requestLocationUpdates(
                LocationManager.PASSIVE_PROVIDER,
                LOCATION_UPDATE_FREQUENCY, LOCATION_UPDATE_MIN_DISTANCE,
                low_priority_listener);
    }

    public void stopLocationUpdates() {
        if (locationManager != null) {
            locationManager.removeUpdates(low_priority_listener);
            locationManager.removeUpdates(med_priority_listener);
            locationManager.removeUpdates(high_priority_listener);
        }
    }

    public GeoPoint getLocationPoint() {
        return getLocationPosition().getGeoPoint();
    }

    public MapPosition getLocationPosition() {
        if (location != null) {
            Logger.d("Location: get location position");
            double lat = getLocation().getLatitude();
            double lon = getLocation().getLongitude();
            mapPosition = new MapPosition(lat, lon, Math.pow(2, getStoredZoomLevel()));
        } else {
            Logger.d("Location: get location position");
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
        Log.d(LOG_TAG, "request: adding " + request.getUrl());
        request.setRetryPolicy(new DefaultRetryPolicy(HTTP_REQUEST_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    public static class PriorityLocationListener implements LocationListener {
        public static final int HIGH_PRIORITY = 0;
        public static final int MEDIUM_PRIORITY = 1;
        public static final int LOW_PRIORITY = 2;
        private int weight = 2;

        public PriorityLocationListener(int weight) {
            this.weight = weight;
        }

        @Override
        public void onLocationChanged(Location location) {
            app.setLocation(weight, location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
