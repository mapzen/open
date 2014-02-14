package com.mapzen.shadows;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import static com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;

@SuppressWarnings("unused")
@Implements(LocationClient.class)
public class ShadowLocationClient {
    private boolean connected = false;
    private boolean updatesRequested = false;
    private LocationListener locationListener;

    public void __constructor__(Context context,
            ConnectionCallbacks connectionCallbacks,
            OnConnectionFailedListener failedListener) {
    }

    public void clearAll() {
        connected = false;
        updatesRequested = false;
    }

    @Implementation
    public void requestLocationUpdates(LocationRequest request, LocationListener listener) {
        updatesRequested = true;
        this.locationListener = listener;
    }

    @Implementation
    public void removeLocationUpdates(LocationListener listener) {
        updatesRequested = false;
    }

    public LocationListener getLocationListener() {
        return locationListener;
    }

    @Implementation
    public void connect() {
        connected = true;
    }

    @Implementation
    public void disconnect() {
        connected = false;
    }

    @Implementation
    public Location getLastLocation() {
        return new Location("fused");
    }

    @Implementation
    public boolean isConnected() {
        return connected;
    }

    public boolean hasUpdatesRequests() {
        return updatesRequested;
    }
}


