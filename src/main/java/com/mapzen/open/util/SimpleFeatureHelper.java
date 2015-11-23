package com.mapzen.open.util;

import com.mapzen.pelias.SimpleFeature;

import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.MarkerItem;

public final class SimpleFeatureHelper {
    private SimpleFeatureHelper() {
    }

    public static final GeoPoint getGeoPoint(SimpleFeature simpleFeature) {
        return new GeoPoint(simpleFeature.getLat(), simpleFeature.getLon());
    }

    public static final MarkerItem getMarker(SimpleFeature simpleFeature) {
        return new MarkerItem(simpleFeature, simpleFeature.getProperty(SimpleFeature.TEXT),
                "Current Location", getGeoPoint(simpleFeature));
    }

    public static final String getFullLocationString(SimpleFeature simpleFeature) {
        return " " + simpleFeature.getProperty(SimpleFeature.TEXT)
                + " [" + simpleFeature.getLat() + ", "
                + simpleFeature.getLon() + "]";
    }
}
