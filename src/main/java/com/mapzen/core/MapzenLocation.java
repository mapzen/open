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
import android.os.Bundle;
import android.widget.Toast;

import static com.mapzen.MapController.getMapController;

public final class MapzenLocation {
    public static final String KEY_LOCATION = "location";
    public static final String COM_MAPZEN_FIND_ME = "com.mapzen.updates.find_me";

    private MapzenLocation() {
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
            locationRequest.setInterval(BaseActivity.LOCATION_INTERVAL);
            locationClient.requestLocationUpdates(locationRequest,
                    new MapzenLocation.Listener(application));
        }

        @Override
        public void onDisconnected() {
            Logger.d("LocationHelper disconnected.");
        }

    }
}
