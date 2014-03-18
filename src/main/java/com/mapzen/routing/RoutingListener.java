package com.mapzen.routing;

import android.location.Location;

public interface RoutingListener {
    public void onLost(Location location);
}
