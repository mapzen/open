package com.mapzen.util;

import android.net.Uri;
import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.map.Map;

public abstract class ApiHelper {
    public static final String HTTP_SCHEMA = "http";
    public static final String JSON = "json";
    public static final String TRUE = "true";

    public static final String ROUTE_URL = "osrm.test.mapzen.com";
    public static final String ROUTE_FOOT_PATH = "foot/viaroute";
    public static final String ROUTE_CAR_PATH = "car/viaroute";
    public static final String ROUTE_ZOOMLEVEL = "z";
    public static final String ROUTE_OUTPUT_KEY = "output";
    public static final String ROUTE_LOCATION_KEY = "loc";
    public static final String ROUTE_INSTRUCTIONS_KEY = "instructions";

    public static String getViewBox(Map map) {
        BoundingBox boundingBox = map.viewport().getBBox();
        return String.valueOf(boundingBox.getMinLongitude()) + ","
                        + String.valueOf(boundingBox.getMaxLatitude()) + ","
                        + String.valueOf(boundingBox.getMaxLongitude()) + ","
                        + String.valueOf(boundingBox.getMinLatitude());
    }

    public static String getRouteUrlForFoot(double zoomLevel, GeoPoint from, GeoPoint destination) {
        return getRouteUrl(ROUTE_FOOT_PATH, zoomLevel, from, destination);
    }

    public static String getRouteUrlForCar(double zoomLevel, GeoPoint from, GeoPoint destination) {
        return getRouteUrl(ROUTE_CAR_PATH, zoomLevel, from, destination);
    }

    public static String getRouteUrl(String type, double zoomLevel, GeoPoint from,
            GeoPoint destination) {
        int zoom = (int) zoomLevel;
        Uri.Builder url = new Uri.Builder();
        url.scheme(HTTP_SCHEMA).authority(ROUTE_URL).path(type);
        url.appendQueryParameter(ROUTE_ZOOMLEVEL, String.valueOf(zoom));
        url.appendQueryParameter(ROUTE_OUTPUT_KEY, ApiHelper.JSON);
        url.appendQueryParameter(ROUTE_LOCATION_KEY,
                String.valueOf(from.getLatitude()) + "," + String.valueOf(from.getLongitude()));
        url.appendQueryParameter(ROUTE_LOCATION_KEY, String.valueOf(
                destination.getLatitude()) + "," + String.valueOf(destination.getLongitude()));
        url.appendQueryParameter(ROUTE_INSTRUCTIONS_KEY, TRUE);
        return url.toString();
    }
}
