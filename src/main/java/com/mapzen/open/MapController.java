package com.mapzen.open;

import com.mapzen.open.activity.BaseActivity;
import com.mapzen.osrm.Instruction;

import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.layers.Layer;
import org.oscim.layers.PathLayer;
import org.oscim.map.Map;

import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.widget.Toast;
import org.oscim.map.ViewController;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public final class MapController {
    public static final String KEY_STORED_MAPPOSITION = "stored_mapposition";
    public static final String KEY_TILT = "tilt";
    public static final String KEY_LATITUDE = "lat";
    public static final String KEY_LONGITUDE = "lng";
    public static final String KEY_MAP_SCALE = "scale";
    public static final String KEY_BEARING = "rotation";

    public static final int DEFAULT_ZOOM_LEVEL = 15;
    public static final String DEBUG_LOCATION = "fixed_debug_location";

    private static MapController mapController;
    private Map map;
    private Location location;
    private MapPosition mapPosition = new MapPosition(1.0, 1.0, Math.pow(2, DEFAULT_ZOOM_LEVEL));
    private BaseActivity activity;
    private SharedPreferences preferences;

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
        this.preferences = activity.getSharedPreferences(KEY_STORED_MAPPOSITION, MODE_PRIVATE);
    }

    public static MapController getMapController() {
        return mapController;
    }

    public Map getMap() {
        return map;
    }

    public void clearLines() {
        for (Layer layer: map.layers()) {
            if (layer.getClass().equals(PathLayer.class)) {
                map.layers().remove(layer);
            }
        }
    }

    public void clearLinesExcept(ArrayList<PathLayer> layers) {
        for (Layer layer: map.layers()) {
            if (layer.getClass().equals(PathLayer.class)) {
                if (!layers.contains(layer)) {
                    map.layers().remove(layer);
                }
            }
        }
    }

    public void moveToTop(Class<?> klass) {
        for (Layer layer: map.layers()) {
            if (layer.getClass().equals(klass)) {
                map.layers().remove(layer);
                map.layers().add(layer);
            }
        }
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

    public MapController quarterOn(Location location, double bearing) {
        ViewController v = map.viewport();
        Float tilt = map.getMapPosition().getTilt();
        final int zoom = map.getMapPosition().getZoomLevel();
        MapPosition position = new MapPosition(location.getLatitude(), location.getLongitude(),
                Math.pow(2, zoom));
        v.setMapPosition(position);
        map.updateMap(true);
        float[] ext = new float[8];
        v.getMapExtents(ext, 0);
        position.setBearing((float) bearing);
        v.setMapPosition(position);
        v.moveMap(0, ext[1] / 2);
        v.getMapPosition(position);
        position.setTilt(tilt);
        map.setMapPosition(position);
        map.updateMap(true);
        return this;
    }

    public MapPosition getMapPosition() {
        return mapPosition;
    }

    public void storeMapPosition(MapPosition mapPosition) {
        this.mapPosition = mapPosition;
    }

    public double getZoomLevel() {
        return mapPosition.getZoomLevel();
    }

    public void setZoomLevel(int zoomLevel) {
        mapPosition.setZoomLevel(zoomLevel);
        if (map != null) {
            map.setMapPosition(mapPosition);
        }
    }

    public void resetZoomAndPointNorth() {
        setZoomLevel(DEFAULT_ZOOM_LEVEL);
        mapPosition.setBearing(0.0f);
    }

    public void setMapPerspectiveForInstruction(Instruction instruction) {
        quarterOn(instruction.getLocation(), instruction.getRotationBearing());
    }

    public void setRotation(float rotation) {
        map.viewport().setRotation(rotation);
        map.updateMap(true);
    }

    public double getZoomScale() {
        return mapPosition.scale;
    }

    public void saveLocation() {
        SharedPreferences.Editor editor = preferences.edit();
        MapPosition mapPosition = map.getMapPosition();
        GeoPoint geoPoint = mapPosition.getGeoPoint();
        editor.putFloat(KEY_TILT, mapPosition.getTilt());
        editor.putInt(KEY_LATITUDE, geoPoint.latitudeE6);
        editor.putInt(KEY_LONGITUDE, geoPoint.longitudeE6);
        editor.putFloat(KEY_MAP_SCALE, (float) mapPosition.scale);
        editor.putFloat(KEY_BEARING, mapPosition.getBearing());
        editor.commit();
    }

    private boolean hasStoredMapPosition() {
        return preferences.contains(KEY_LATITUDE)
                && preferences.contains(KEY_LONGITUDE)
                && preferences.contains(KEY_MAP_SCALE)
                && preferences.contains(KEY_BEARING)
                && preferences.contains(KEY_TILT);
    }

    public void restoreFromSavedLocation() {
        if (!hasStoredMapPosition()) {
            ((MapzenApplication) activity.getApplication()).activateMoveMapToLocation();
            return;
        } else {
            ((MapzenApplication) activity.getApplication()).deactivateMoveMapToLocation();
        }
        int latitudeE6 = preferences.getInt(KEY_LATITUDE, 0);
        int longitudeE6 = preferences.getInt(KEY_LONGITUDE, 0);
        float scale = preferences.getFloat(KEY_MAP_SCALE,
                (float) Math.pow(2, DEFAULT_ZOOM_LEVEL));
        float tilt = preferences.getFloat(KEY_TILT, 0);
        float bearing = preferences.getFloat(KEY_BEARING, 0);
        MapPosition mapPosition = new MapPosition();
        mapPosition.setPosition(latitudeE6 / 1E6, longitudeE6 / 1E6);
        mapPosition.setTilt(tilt);
        mapPosition.setScale(scale);
        mapPosition.setBearing(bearing);
        storeMapPosition(mapPosition);
        map.setMapPosition(mapPosition);
        map.updateMap(true);
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
