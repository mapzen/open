package com.mapzen;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
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

public class MapzenApplication extends Application {
    public static final int LOCATION_UPDATE_FREQUENCY = 1000;
    public static final float LOCATION_UPDATE_MIN_DISTANCE = 10.0f;
    public static final int HTTP_REQUEST_TIMEOUT_MS = 500;
    public static final String PELIAS_TEXT = "text";
    public static final double DEFAULT_LATITUDE = 64.133333;
    public static final double DEFAULT_LONGITUDE = -21.933333;
    public static final int DEFAULT_ZOOMLEVEL = 15;
    public static final String LOG_TAG = "Mapzen: ";
    public static final int LOCATION_PRIORITY_COLLECTION_CAPACITY = 3;
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
    private SparseArray<Location> location =
            new SparseArray<Location>(LOCATION_PRIORITY_COLLECTION_CAPACITY);
    public static final int HIGH_PRIORITY_LOCATION = 0;
    public static final int MED_PRIORITY_LOCATION = 1;
    public static final int LOW_PRIORITY_LOCATION = 2;
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

    public void setLocation(int weight, Location loc) {
        Logger.d("Location: setting location: weight: "
                + String.valueOf(weight) + ", time: " + new Date().toString());
        if (loc == null) {
            Logger.d("Location: location is null");
        } else {
            this.location.put(weight, loc);
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
        Location loc = getLocation();
        if (loc != null) {
            mapPosition = new MapPosition(loc.getLatitude(),
                    loc.getLongitude(), Math.pow(2, getStoredZoomLevel()));
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
}
