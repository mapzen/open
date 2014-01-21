package com.mapzen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.mapzen.fragment.MapFragment;

public class LocationReceiver extends BroadcastReceiver {
    private MapFragment mapFragment;

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Location location = (Location) bundle.get(LocationManager.KEY_LOCATION_CHANGED);
        if (!mapFragment.isDetached()) {
            mapFragment.setUserLocation(location);
        }
    }
}
