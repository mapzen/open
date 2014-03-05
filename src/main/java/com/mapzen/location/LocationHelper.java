package com.mapzen.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.GPS_PROVIDER;

public class LocationHelper {
    private final Context context;
    private final ConnectionCallbacks connectionCallbacks;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private android.location.LocationListener gpsListener;

    public LocationHelper(Context context, ConnectionCallbacks connectionCallbacks) {
        this.context = context;
        this.connectionCallbacks = connectionCallbacks;
    }

    public void connect() {
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        connectionCallbacks.onConnected(null);
    }

    public void disconnect() {
        if (gpsListener != null) {
            locationManager.removeUpdates(gpsListener);
        }

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

    public void requestLocationUpdates(LocationRequest request, LocationListener locationListener) {
        this.locationListener = locationListener;

        gpsListener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LocationHelper.this.locationListener.onLocationChanged(location);
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
        };

        locationManager.requestLocationUpdates(GPS_PROVIDER, request.getFastestInterval(),
                request.getSmallestDisplacement(), gpsListener);
    }

    public static interface ConnectionCallbacks {
        public void onConnected(Bundle connectionHint);
        public void onDisconnected();
    }
}
