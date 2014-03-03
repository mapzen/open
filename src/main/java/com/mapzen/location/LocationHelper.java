package com.mapzen.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class LocationHelper {
    private final Context context;
    private final ConnectionCallbacks connectionCallbacks;
    private LocationManager locationManager;

    public LocationHelper(Context context, ConnectionCallbacks connectionCallbacks) {
        this.context = context;
        this.connectionCallbacks = connectionCallbacks;
    }

    public void connect() {
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        connectionCallbacks.onConnected(null);
    }

    public void disconnect() {
        connectionCallbacks.onDisconnected();
    }

    public Location getLastLocation() {
        if (locationManager == null) {
            throw new IllegalStateException("Not connected. "
                    + "Call connect() and wait for onConnected() to be called.");
        }

        final List<String> providers = locationManager.getAllProviders();
        Location bestLocation = null;
        for (String provider : providers) {
            final Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = location;
                }
            }
        }

        return bestLocation;
    }

    public static interface ConnectionCallbacks {
        public void onConnected(Bundle connectionHint);
        public void onDisconnected();
    }
}
