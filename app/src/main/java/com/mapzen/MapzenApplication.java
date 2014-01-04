package com.mapzen;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.Volley;

import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.map.Map;

import java.util.List;

import static android.provider.BaseColumns._ID;

public class MapzenApplication extends Application implements LocationListener {
    public static final int LOCATION_UPDATE_FREQUENCY = 1000;
    public static final float LOCATION_UPDATE_MIN_DISTANCE = 10.0f;
    private static MapzenApplication app;
    private Location location = null;
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
        app.setLocation(app.findBestLocation());
        return app;
    }

    public Location findBestLocation() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location loc = locationManager.getLastKnownLocation(provider);
            if (loc == null) {
                continue;
            }
            if (bestLocation == null
                    || loc.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = loc;
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
        return location;
    }

    public void setLocation(Location location) {
        Log.v(LOG_TAG, "Location: setting location");
        if(location == null) {
            Log.v(LOG_TAG, "Location: location is null");
        }
        this.location = location;
    }

    private String getBestProvider() {
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Log.v(LOG_TAG, "Location: Best provider: " + provider);
        return provider;
    }

    public void setupLocationUpdates() {
        Log.v(LOG_TAG, "Location: Requesting updates");
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
        Log.v(LOG_TAG, "Location: setting location: ");
        setLocation(loc);
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
            Log.v(LOG_TAG, "Location: get location position");
            double lat = getLocation().getLatitude();
            double lon = getLocation().getLongitude();
            mapPosition = new MapPosition(lat, lon, Math.pow(2, getStoredZoomLevel()));
        } else {
            Log.v(LOG_TAG, "Location: get location position");
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
        request.setRetryPolicy(new DefaultRetryPolicy(500,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                Log.d(LOG_TAG, "request: cancelling " + request.getUrl());
                return true;
            }
        });
        queue.add(request);
    }
}
