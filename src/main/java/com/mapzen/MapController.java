package com.mapzen;

import android.location.Location;
import org.oscim.core.MapPosition;
import org.oscim.map.Map;

public final class MapController {
    public static final int DEFAULT_ZOOMLEVEL = 15;
    private static MapController mapController;
    private Map map;
    private Location location;
    private MapPosition mapPosition = new MapPosition(1.0, 1.0, Math.pow(2, DEFAULT_ZOOMLEVEL));

    static {
        mapController = new MapController();
    }

    private MapController() {
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public static MapController getMapController() {
        return mapController;
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
