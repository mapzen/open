package com.mapzen.open.event;

import android.location.Location;

/**
 * An event used to deliver location updates.
 */
public class LocationUpdateEvent {
    private Location location;

    public LocationUpdateEvent(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
