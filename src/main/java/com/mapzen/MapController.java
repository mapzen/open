package com.mapzen;

import android.content.Context;
import android.location.Location;

import com.mapzen.activity.BaseActivity;

import org.oscim.core.MapPosition;
import org.oscim.map.Map;

public class MapController {
    private static MapController mapController;
    private Map map;
    private MapzenApplication app;
    private Location location;
    private MapPosition mapPosition = new MapPosition(1.0, 1.0, Math.pow(2, 15));

    public MapController(Context context) {
        this.app = (MapzenApplication) context.getApplicationContext();
        this.map = ((BaseActivity) context).getMap();
    }

    public static MapController getInstance(Context context) {
        if (mapController == null) {
            mapController = new MapController(context);
        }
        return mapController;
    }

    public MapzenApplication getApp() {
        return app;
    }

    public Map getMap() {
        return map;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
        mapPosition.setPosition(location.getLatitude(), location.getLongitude());
    }

    public MapPosition getMapPosition() {
        return mapPosition;
    }

    public void setMapPosition(MapPosition mapPosition) {
        this.mapPosition = mapPosition;
    }

    public double getZoomLevel() {
        return mapPosition.zoomLevel;
    }

    public void setZoomLevel(int zoomLevel) {
        mapPosition.setZoomLevel(zoomLevel);
    }
}
