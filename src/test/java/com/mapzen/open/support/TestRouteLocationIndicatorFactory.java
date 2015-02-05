package com.mapzen.open.support;

import com.mapzen.open.route.RouteLocationIndicatorFactory;
import com.mapzen.open.util.RouteLocationIndicator;

import org.mockito.Mockito;
import org.oscim.map.Map;

public class TestRouteLocationIndicatorFactory extends RouteLocationIndicatorFactory {
    private RouteLocationIndicator routeLocationIndicator;

    @Override
    public void setMap(Map map) {
        routeLocationIndicator = Mockito.mock(RouteLocationIndicator.class);
    }

    @Override
    public RouteLocationIndicator getRouteLocationIndicator() {
        return routeLocationIndicator;
    }
}
