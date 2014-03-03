package com.mapzen.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class LocationHelper {
    private Context context;

    public LocationHelper(Context context) {
        this.context = context;
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
}
