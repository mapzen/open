package com.mapzen.core;

import com.mapzen.MapController;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.android.lost.LocationClient;
import com.mapzen.android.lost.LocationListener;
import com.mapzen.android.lost.LocationRequest;
import com.mapzen.util.Logger;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import static com.mapzen.MapController.getMapController;

public final class MapzenLocation {
    public static final String KEY_LOCATION = "location";
    public static final String COM_MAPZEN_FIND_ME = "com.mapzen.updates.find_me";
    public static final int DEFAULT_LOCATION_INTERVAL = 1000;

    private MapzenLocation() {
    }

    public static class Util {
        public static Location getDistancePointFromBearing(Location originalLocation,
                int distanceMeters, int bearing) {
            double orgLat = originalLocation.getLatitude();
            double orgLng = originalLocation.getLongitude();
            double dist = distanceMeters / 1000.0 / 6371.0;
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

        public Listener(MapzenApplication application) {
            this.application = application;
        }

        public void onLocationChanged(Location location) {
            if (application.shouldMoveMapToLocation()) {
                getMapController().setLocation(location);
                Intent findMe = new Intent(COM_MAPZEN_FIND_ME);
                application.sendBroadcast(findMe);
            }
            Intent toBroadcast = new Intent(BaseActivity.COM_MAPZEN_UPDATES_LOCATION);
            toBroadcast.putExtra(KEY_LOCATION, location);
            application.sendBroadcast(toBroadcast);
        }
    }

    public static class ConnectionCallbacks implements LocationClient.ConnectionCallbacks {
        private MapzenApplication application;
        private LocationClient locationClient;

        public ConnectionCallbacks(MapzenApplication application) {
            this.application = application;
        }

        public void setLocationClient(LocationClient locationClient) {
            this.locationClient = locationClient;
        }

        @Override
        public void onConnected(Bundle bundle) {
            getMapController().setZoomLevel(MapController.DEFAULT_ZOOMLEVEL);
            final Location location = locationClient.getLastLocation();
            Logger.d("Last known location: " + location);

            if (location != null) {
                getMapController().setLocation(location);
                Intent findMe = new Intent("findMe");
                application.sendBroadcast(findMe);
            } else {
                Toast.makeText(application, application.getString(R.string.waiting),
                        Toast.LENGTH_LONG).show();
            }

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(getLocationUpdateIntervalPreference());
            locationClient.requestLocationUpdates(locationRequest,
                    new MapzenLocation.Listener(application));
        }

        private int getLocationUpdateIntervalPreference() {
            return PreferenceManager.getDefaultSharedPreferences(application)
                    .getInt(application.getString(R.string.settings_location_update_interval_key),
                            DEFAULT_LOCATION_INTERVAL);
        }

        @Override
        public void onDisconnected() {
            Logger.d("LocationClient disconnected.");
        }
    }
}
