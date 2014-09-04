package com.mapzen.route;

import com.mapzen.MapController;
import com.mapzen.TestMapzenApplication;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.layers.Layer;
import org.oscim.layers.PathLayer;
import org.oscim.map.TestMap;
import org.oscim.map.ViewController;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.mapzen.MapController.locationToGeoPoint;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.stub;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class DrawPathTaskTest {
    DrawPathTask task;
    TestMapzenApplication application;
    @Inject MapController mapController;
    ViewController viewController;
    BoundingBox box;
    Location outsideBefore1, outsideBefore2,
            inside1, inside2, outSideAfter1, outSideAfter2;

    @Before
    public void setup() throws Exception {
        application = (TestMapzenApplication) Robolectric.application;
        application.inject(this);
        TestHelper.initBaseActivity();
        TestMap testMap = (TestMap) mapController.getMap();
        viewController = Mockito.mock(ViewController.class);
        testMap.setViewport(viewController);
        task = new DrawPathTask(application);
        box = Mockito.mock(BoundingBox.class);
        stub(viewController.getBBox()).toReturn(box);
        outsideBefore1 = new Location("f");
        outsideBefore1.setLatitude(1);
        outsideBefore2 = new Location("f");
        outsideBefore2.setLatitude(2);
        inside1 = new Location("f");
        inside1.setLatitude(3);
        inside2 = new Location("f");
        inside2.setLatitude(4);
        outSideAfter1 = new Location("f");
        outSideAfter1.setLatitude(5);
        outSideAfter2 = new Location("f");
        outSideAfter2.setLatitude(6);
    }

    @Test
    public void shouldNotDrawAnyPointsIfCancelled() throws Exception {
        ArrayList<Location> locations = new ArrayList<Location>();
        stub(box.contains(locationToGeoPoint(outsideBefore1))).toReturn(false);
        stub(box.contains(locationToGeoPoint(outsideBefore2))).toReturn(false);
        stub(box.contains(locationToGeoPoint(inside1))).toReturn(true);
        stub(box.contains(locationToGeoPoint(inside2))).toReturn(true);
        locations.add(outsideBefore1);
        locations.add(outsideBefore2);
        locations.add(inside1);
        locations.add(inside2);
        task.cancel(true);
        task.execute(locations);
        Robolectric.runUiThreadTasksIncludingDelayedTasks();
        assertThat(getPathLayer()).isNull();
    }

    @Test
    public void shouldDrawFirstPointBeforeEnteringBBox() throws Exception {
        ArrayList<Location> locations = new ArrayList<Location>();
        stub(box.contains(locationToGeoPoint(outsideBefore1))).toReturn(false);
        stub(box.contains(locationToGeoPoint(outsideBefore2))).toReturn(false);
        stub(box.contains(locationToGeoPoint(inside1))).toReturn(true);
        stub(box.contains(locationToGeoPoint(inside2))).toReturn(true);
        locations.add(outsideBefore1);
        locations.add(outsideBefore2);
        locations.add(inside1);
        locations.add(inside2);

        task.execute(locations);
        Robolectric.runUiThreadTasksIncludingDelayedTasks();
        assertThat(getPathPoints()).doesNotContain(locationToGeoPoint(outsideBefore1));
        assertThat(getPathPoints()).contains(locationToGeoPoint(outsideBefore2));
        assertThat(getPathPoints()).contains(locationToGeoPoint(inside1));
        assertThat(getPathPoints()).contains(locationToGeoPoint(inside2));
    }

    @Test
    public void shouldDrawFirstPointAfterExitingBBox() throws Exception {
        ArrayList<Location> locations = new ArrayList<Location>();
        stub(box.contains(locationToGeoPoint(inside1))).toReturn(true);
        stub(box.contains(locationToGeoPoint(inside2))).toReturn(true);
        stub(box.contains(locationToGeoPoint(outSideAfter1))).toReturn(false);
        stub(box.contains(locationToGeoPoint(outSideAfter2))).toReturn(false);
        locations.add(inside1);
        locations.add(inside2);
        locations.add(outSideAfter1);
        locations.add(outSideAfter2);
        task.execute(locations);
        Robolectric.runUiThreadTasksIncludingDelayedTasks();
        assertThat(getPathPoints()).contains(locationToGeoPoint(inside1));
        assertThat(getPathPoints()).contains(locationToGeoPoint(inside2));
        assertThat(getPathPoints()).contains(locationToGeoPoint(outSideAfter1));
        assertThat(getPathPoints()).doesNotContain(locationToGeoPoint(outSideAfter2));
    }

    private List<GeoPoint> getPathPoints() {
        PathLayer pathLayer = getPathLayer();
        return pathLayer.getPoints();
    }

    private PathLayer getPathLayer() {
        PathLayer pathLayer = null;
        for (Layer layer: mapController.getMap().layers()) {
            if (layer.getClass().equals(PathLayer.class)) {
                pathLayer = (PathLayer) layer;
            }
        }
        return pathLayer;
    }
}
