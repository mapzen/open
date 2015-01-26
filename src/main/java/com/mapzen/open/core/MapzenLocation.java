package com.mapzen.open.core;

import com.mapzen.android.lost.api.FusedLocationProviderApi;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.open.MapController;
import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.event.LocationUpdateEvent;
import com.mapzen.open.util.Logger;

import com.squareup.otto.Bus;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

import javax.inject.Inject;

public final class MapzenLocation {
    public static final String COM_MAPZEN_FIND_ME = "com.mapzen.updates.find_me";
    public static final int DEFAULT_LOCATION_INTERVAL = 1000;

    private MapzenLocation() {
    }

    public static class Util {

        public static final double EARTH_RADIUS = 6371.0;
        public static final double KM = 1000.0;

        public static Location getDistancePointFromBearing(Location originalLocation,
                int distanceMeters, int bearing) {
            double orgLat = originalLocation.getLatitude();
            double orgLng = originalLocation.getLongitude();
            double dist = distanceMeters / KM / EARTH_RADIUS;
            double brng = Math.toRadians(bearing);
            double lat1 = Math.toRadians(orgLat);
            double lon1 = Math.toRadians(orgLng);

            double lat2 = Math.asin(
                    Math.sin(lat1) * Math.cos(dist)
                            + Math.cos(lat1) * Math.sin(dist) * Math.cos(brng));
            double a = Math.atan2(
                    Math.sin(brng) * Math.sin(dist) * Math.cos(lat1),
                    Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2));
            double lon2 = lon1 + a;

            lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(Math.toDegrees(lat2));
            location.setLongitude(Math.toDegrees(lon2));

            return location;
        }
    }

    public static class Listener implements LocationListener {
        private MapzenApplication application;
        @Inject MapController mapController;
        @Inject Bus bus;

        public Listener(MapzenApplication application) {
            this.application = application;
            application.inject(this);
        }

        public void onLocationChanged(Location location) {
            if (application.shouldMoveMapToLocation()) {
                mapController.setLocation(location);
                Intent findMe = new Intent(COM_MAPZEN_FIND_ME);
                application.sendBroadcast(findMe);
            }

            bus.post(new LocationUpdateEvent(location));
        }
    }

    public static void onLocationServicesConnected(MapController mapController,
            FusedLocationProviderApi api, MapzenApplication app) {
        mapController.setZoomLevel(MapController.DEFAULT_ZOOM_LEVEL);
        final Location location = api.getLastLocation();
        Logger.d("Last known location: " + location);

        if (location != null) {
            mapController.setLocation(location);
            if (app.shouldMoveMapToLocation()) {
                Intent findMe = new Intent(COM_MAPZEN_FIND_ME);
                app.sendBroadcast(findMe);
            }
        } else {
            if (mapController.getMap() != null) {
                Toast.makeText(app, app.getString(R.string.waiting), Toast.LENGTH_LONG).show();
            }
        }

        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(getLocationUpdateIntervalPreference(app));
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        api.requestLocationUpdates(locationRequest, new Listener(app));
    }

    private static int getLocationUpdateIntervalPreference(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(context.getString(R.string.settings_location_update_interval_key),
                        DEFAULT_LOCATION_INTERVAL);
    }
}
