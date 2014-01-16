package com.mapzen;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.mapzen.util.Logger;

import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;

import java.util.List;

import static android.provider.BaseColumns._ID;

public class MapzenApplication extends Application {
    public static final int LOCATION_UPDATE_FREQUENCY = 1000;
    public static final float LOCATION_UPDATE_MIN_DISTANCE = 10.0f;
    public static final int HTTP_REQUEST_TIMEOUT_MS = 500;
    public static final String PELIAS_TEXT = "text";
    private final String[] columns = {
            _ID, PELIAS_TEXT
    };
    public static final double DEFAULT_LATITUDE = 64.133333;
    public static final double DEFAULT_LONGITUDE = -21.933333;
    public static final int DEFAULT_ZOOMLEVEL = 15;
    private static MapPosition mapPosition =
            new MapPosition(DEFAULT_LATITUDE, DEFAULT_LONGITUDE, Math.pow(2, DEFAULT_ZOOMLEVEL));
    public static final String LOG_TAG = "Mapzen: ";
    public static final int HIGH_PRIORITY_LOCATION = 0;
    public static final int MED_PRIORITY_LOCATION = 1;
    public static final int LOW_PRIORITY_LOCATION = 2;
    private static MapzenApplication app;
    private String currentSearchTerm = "";
    private int currentPagerPosition = 0;
    private RequestQueue queue;
    private LocationManager locationManager;
    private PendingIntent highPriorityLocationIntent;
    private PendingIntent medPriorityLocationIntent;
    private PendingIntent lowPriorityLocationIntent;

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
        app.highPriorityLocationIntent = PendingIntent.getBroadcast(context, HIGH_PRIORITY_LOCATION,
                new Intent("com.mapzen.updates.location.HIGH"), 0);
        app.medPriorityLocationIntent = PendingIntent.getBroadcast(context, MED_PRIORITY_LOCATION,
                new Intent("com.mapzen.updates.location.MED"), 0);
        app.lowPriorityLocationIntent = PendingIntent.getBroadcast(context, LOW_PRIORITY_LOCATION,
                new Intent("com.mapzen.updates.location.LOW"), 0);
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
            if (loc == null) {
                Logger.d("location: is null");
                continue;
            }
            Logger.d("location: last known location: " + loc.toString());
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

    public void setupLocationUpdates() {
        Logger.d("Location: Requesting updates");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                LOCATION_UPDATE_FREQUENCY, LOCATION_UPDATE_MIN_DISTANCE,
                highPriorityLocationIntent);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                LOCATION_UPDATE_FREQUENCY, LOCATION_UPDATE_MIN_DISTANCE,
                medPriorityLocationIntent);
        locationManager.requestLocationUpdates(
                LocationManager.PASSIVE_PROVIDER,
                LOCATION_UPDATE_FREQUENCY, LOCATION_UPDATE_MIN_DISTANCE,
                lowPriorityLocationIntent);
    }

    public void stopLocationUpdates() {
        if (locationManager != null) {
            locationManager.removeUpdates(highPriorityLocationIntent);
        }
    }

    public GeoPoint getLocationPoint() {
        return getLocationPosition().getGeoPoint();
    }

    public MapPosition getLocationPosition() {
        Location location = findBestLocation();
        if (location != null) {
            mapPosition =
                    new MapPosition(location.getLatitude(), location.getLongitude(),
                            Math.pow(2, getStoredZoomLevel()));
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
        Log.d(LOG_TAG, "request: adding " + request.getUrl());
        request.setRetryPolicy(new DefaultRetryPolicy(HTTP_REQUEST_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }
}
