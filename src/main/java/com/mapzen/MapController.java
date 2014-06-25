package com.mapzen;

import com.mapzen.activity.BaseActivity;
import com.mapzen.osrm.Instruction;

import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.map.Map;

import static com.mapzen.route.RouteFragment.ROUTE_ZOOM_LEVEL;

public final class MapController {
    public static final int DEFAULT_ZOOMLEVEL = 15;
    public static final String DEBUG_LOCATION = "fixed_debug_location";
    private static MapController mapController;
    private Map map;
    private Location location;
    private MapPosition mapPosition = new MapPosition(1.0, 1.0, Math.pow(2, DEFAULT_ZOOMLEVEL));
    private BaseActivity activity;

    static {
        mapController = new MapController();
    }

    public static double[] locationToPair(Location loc) {
        return new double[] { loc.getLatitude(), loc.getLongitude() };
    }

    public static GeoPoint locationToGeoPoint(Location loc) {
        return new GeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    public static double[] geoPointToPair(GeoPoint point) {
        return new double[] { point.getLatitude(), point.getLongitude() };
    }

    private MapController() {
    }

    public void setActivity(BaseActivity activity) {
        this.activity = activity;
        this.map = activity.getMap();
    }

    public static MapController getMapController() {
        return mapController;
    }

    public Map getMap() {
        return map;
    }

    public Location getLocation() {
        if (!isFixedLocationEnabled()) {
            return location;
        } else {
            return getFixedLocation();
        }
    }

    public MapController setLocation(Location location) {
        this.location = location;
        mapPosition.setPosition(location.getLatitude(), location.getLongitude());
        return this;
    }

    public MapController centerOn(Location location) {
        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
        map.animator().animateTo(point);
        return this;
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
        map.setMapPosition(mapPosition);
    }

    public void setMapPerspectiveForInstruction(Instruction instruction) {
        Location location = instruction.getLocation();
        map.setMapPosition(location.getLatitude(), location.getLongitude(),
                Math.pow(2, ROUTE_ZOOM_LEVEL));
        setRotation(instruction.getRotationBearing());
        map.updateMap(true);
    }

    public void setRotation(float rotation) {
        map.viewport().setRotation(rotation);
        map.updateMap(true);
    }

    public void setPosition(Location location) {
        map.setMapPosition(location.getLatitude(), location.getLongitude(),
                Math.pow(2, ROUTE_ZOOM_LEVEL));
        map.updateMap(true);
    }

    public double getZoomScale() {
        return mapPosition.scale;
    }

    private boolean isFixedLocationEnabled() {
        String fixedLocationLey =
                activity.getString(R.string.settings_key_enable_fixed_location);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getBoolean(fixedLocationLey, false);
    }

    private boolean isFixedLocationValid(String[] values) {
        String pattern = "\\s?-?\\d+\\.\\d+?";
        return values.length == 2 && values[0].matches(pattern) && values[1].matches(pattern);
    }

    private Location getFixedLocation() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String latLng = prefs.getString(activity.getString(R.string.settings_fixed_location_key),
                activity.getString(R.string.settings_fixed_location_default_value));
        String[] latLngValues = latLng.split(",");
        if (!isFixedLocationValid(latLngValues)) {
            Toast.makeText(activity, activity.getString(R.string.toast_fixed_location_is_malformed),
                    Toast.LENGTH_LONG).show();
            return location;
        } else {
            Location fixedLocation = new Location(DEBUG_LOCATION);
            fixedLocation.setLatitude(Double.valueOf(latLngValues[0]));
            fixedLocation.setLongitude(Double.valueOf(latLngValues[1]));
            return fixedLocation;
        }
    }
}

