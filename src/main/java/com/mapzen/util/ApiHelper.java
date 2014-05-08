package com.mapzen.util;

import org.oscim.core.BoundingBox;
import org.oscim.map.Map;

public abstract class ApiHelper {
    public static final String TRUE = "true";

    public static String getViewBox(Map map) {
        BoundingBox boundingBox = map.viewport().getBBox();
        return String.valueOf(boundingBox.getMinLongitude()) + ","
                        + String.valueOf(boundingBox.getMaxLatitude()) + ","
                        + String.valueOf(boundingBox.getMaxLongitude()) + ","
                        + String.valueOf(boundingBox.getMinLatitude());
    }
}
