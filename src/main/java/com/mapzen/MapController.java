package com.mapzen;

import com.mapzen.activity.BaseActivity;

import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.oscim.core.MapPosition;
import org.oscim.map.Map;

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
        if(!isFixedLocationEnabled()) {
            return location;
        } else {
            return getFixedLocation();
        }
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

    public double getZoomScale() {
        return mapPosition.scale;
    }

    private boolean isFixedLocationEnabled() {
        String fixed_location_key =
                activity.getString(R.string.settings_key_enable_fixed_location);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getBoolean(fixed_location_key, false);
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
        if(!isFixedLocationValid(latLngValues)) {
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
