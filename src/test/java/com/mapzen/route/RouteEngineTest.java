package com.mapzen.route;

import com.mapzen.helpers.ZoomController;
import com.mapzen.osrm.Route;
import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.location.Location;

import static com.mapzen.support.TestHelper.MOCK_ACE_HOTEL;
import static com.mapzen.support.TestHelper.getTestLocation;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class RouteEngineTest {
    private RouteEngine routeEngine;
    private Route route;
    private TestRouteListener listener;
    private ZoomController zoomController;

    @Before
    public void setUp() throws Exception {
        route = new Route(MOCK_ACE_HOTEL);
        listener = new TestRouteListener();
        zoomController = new ZoomController();

        routeEngine = new RouteEngine();
        routeEngine.setRoute(route);
        routeEngine.setListener(listener);
        routeEngine.setZoomController(zoomController);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(routeEngine).isNotNull();
    }

    @Test
    public void shouldRecalculateWhenLost() throws Exception {
        Location location = getTestLocation(0, 0);
        routeEngine.onLocationChanged(location);
        assertThat(listener.recalculating).isTrue();
    }

    @Test
    public void shouldSnapLocation() throws Exception {
        Location location = getTestLocation(40.7444114, -73.9904202);
        routeEngine.onLocationChanged(location);
        assertThat(listener.originalLocation).isEqualsToByComparingFields(location);
        assertThat(listener.snapLocation).isEqualsToByComparingFields(route.snapToRoute(location));
    }

    @Test
    public void shouldNotifyNewInstruction() throws Exception {
        route.addSeenInstruction(route.getRouteInstructions().get(0));
        routeEngine.onLocationChanged(route.getRouteInstructions().get(1).getLocation());
        assertThat(listener.enterIndex).isEqualTo(1);
    }

    @Test
    public void shouldNotifyExitRadius() throws Exception {
        routeEngine.onLocationChanged(route.getRouteInstructions().get(0).getLocation());
        routeEngine.onLocationChanged(route.getRouteInstructions().get(1).getLocation());
        assertThat(listener.exitIndex).isEqualTo(0);
    }

    @Test
    public void shouldNotifyUpdateDistance() throws Exception {
        Location location = route.getRouteInstructions().get(1).getLocation();
        route.addSeenInstruction(route.getRouteInstructions().get(0));
        routeEngine.onLocationChanged(location);
        assertThat(listener.closestDistance).isEqualTo(0);
    }

    private static class TestRouteListener implements RouteEngine.RouteListener {
        private Location originalLocation;
        private Location snapLocation;

        private boolean recalculating = false;
        private int enterIndex = -1;
        private int exitIndex = -1;
        private int closestDistance = -1;

        @Override
        public void onRecalculate(Location location) {
            recalculating = true;
        }

        @Override
        public void onSnapLocation(Location originalLocation, Location snapLocation) {
            this.originalLocation = originalLocation;
            this.snapLocation = snapLocation;
        }

        @Override
        public void onEnterInstructionRadius(int index) {
            enterIndex = index;
        }

        @Override
        public void onExitInstructionRadius(int index) {
            exitIndex = index;
        }

        @Override
        public void onUpdateDistance(int closestDistance) {
            this.closestDistance = closestDistance;
        }
    }
}
