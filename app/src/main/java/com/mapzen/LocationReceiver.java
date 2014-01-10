package com.mapzen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.mapzen.fragment.MapFragment;
import com.mapzen.util.Logger;

public class LocationReceiver extends BroadcastReceiver {
    private MapFragment mapFragment;

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Location location = (Location) bundle.get(LocationManager.KEY_LOCATION_CHANGED);
        String priority = intent.getAction();
        int prioCode = MapzenApplication.LOW_PRIORITY_LOCATION; //set the default
        if (priority.contains("HIGH")) {
            prioCode = MapzenApplication.HIGH_PRIORITY_LOCATION;
        } else if (priority.contains("MED")) {
            prioCode = MapzenApplication.MED_PRIORITY_LOCATION;
        }
        Logger.d("LOOKK location update with " + String.valueOf(prioCode));
        if (!mapFragment.isDetached()) {
            mapFragment.setUserLocation(location);
        }
    }
}
