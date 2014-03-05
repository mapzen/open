package com.mapzen.location;

public final class LocationRequest {
    static final long DEFAULT_INTERVAL_IN_MS = 3600000;
    static final long DEFAULT_FASTEST_INTERVAL_IN_MS = 600000;
    static final float DEFAULT_SMALLEST_DISPLACEMENT_IN_METERS = 0.0f;

    private long interval = DEFAULT_INTERVAL_IN_MS;
    private long fastestInterval = DEFAULT_FASTEST_INTERVAL_IN_MS;
    private float smallestDisplacement = DEFAULT_SMALLEST_DISPLACEMENT_IN_METERS;

    private LocationRequest() {
    }

    public static LocationRequest create() {
        return new LocationRequest();
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long millis) {
        interval = millis;

        if (interval < fastestInterval) {
            fastestInterval = interval;
        }
    }

    public long getFastestInterval() {
        return fastestInterval;
    }

    public void setFastestInterval(long millis) {
        fastestInterval = millis;
    }

    public float getSmallestDisplacement() {
        return smallestDisplacement;
    }

    public void setSmallestDisplacement(float meters) {
        smallestDisplacement = meters;
    }
}
