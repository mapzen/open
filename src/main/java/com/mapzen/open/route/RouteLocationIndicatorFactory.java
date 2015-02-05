package com.mapzen.open.route;

import com.mapzen.open.util.RouteLocationIndicator;

import org.oscim.map.Map;

public class RouteLocationIndicatorFactory {
    private RouteLocationIndicator routeLocationIndicator;

    public void setMap(Map map) {
        routeLocationIndicator = new RouteLocationIndicator(map);
    }

    public RouteLocationIndicator getRouteLocationIndicator() {
        return routeLocationIndicator;
    }
}
