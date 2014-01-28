package com.mapzen.shadows;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.LocationClient;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import static com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;

@SuppressWarnings("unused")
@Implements(LocationClient.class)
public class ShadowLocationClient {
    private boolean connected = false;

    public void __constructor__(Context context,
                                ConnectionCallbacks connectionCallbacks,
                                OnConnectionFailedListener failedListener) {
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
}


