package com.mapzen.util;

import org.oscim.core.GeoPoint;
import org.oscim.layers.PathLayer;
import org.oscim.map.Map;

import java.util.ArrayList;

public class RouteLayer extends PathLayer {
    public RouteLayer(Map map, int lineColor, float lineWidth) {
        super(map, lineColor, lineWidth);
    }

    public void clear() {
        setPoints(new ArrayList<GeoPoint>());
        updateMap();
    }

    public void updateMap() {
        mMap.updateMap(true);
    }
}
