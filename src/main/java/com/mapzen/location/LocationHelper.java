package com.mapzen.location;

import com.google.android.gms.common.ConnectionResult;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class LocationHelper {
    private final Context context;
    private final ConnectionCallbacks connectionCallbacks;
    private final OnConnectionFailedListener onConnectionFailedListener;

    public LocationHelper(Context context, ConnectionCallbacks connectionCallbacks,
            OnConnectionFailedListener onConnectionFailedListener) {
        this.context = context;
        this.connectionCallbacks = connectionCallbacks;
        this.onConnectionFailedListener = onConnectionFailedListener;
    }

    public Location getLastLocation() {
        final LocationManager manager = (LocationManager)
                context.getSystemService(LOCATION_SERVICE);
        final List<String> providers = manager.getAllProviders();

        Location bestLocation = null;
        for (String provider : providers) {
            final Location location = manager.getLastKnownLocation(provider);
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

    public static interface OnConnectionFailedListener {
        public void onConnectionFailed(ConnectionResult result);
    }
}
