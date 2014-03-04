package com.mapzen.location;

public final class LocationRequest {
    private LocationRequest() {
    }

    public static LocationRequest create() {
        return new LocationRequest();
    }
}
