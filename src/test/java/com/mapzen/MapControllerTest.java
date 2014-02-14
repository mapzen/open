package com.mapzen;

import android.location.Location;

import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestBaseActivity;
import com.mapzen.support.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscim.core.MapPosition;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class MapControllerTest {
    private MapController controller;
    private TestBaseActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = TestHelper.initBaseActivity();
        controller = MapController.getInstance(activity);
    }

    @Test
    public void getInstance_shouldNotBeNull() throws Exception {
        assertThat(controller).isNotNull();
    }

    @Test
    public void getInstance_shouldBeSingleton() throws Exception {
        assertThat(controller).isSameAs(MapController.getInstance(activity));
    }

    @Test
    public void getMap_shouldNotBeNull() throws Exception {
        assertThat(controller.getMap()).isNotNull();
    }

    @Test
    public void getApp_shouldNotBeNull() throws Exception {
        assertThat(controller.getApp()).isNotNull();
    }

    @Test
    public void getLocation_shouldNotBeNull() throws Exception {
        controller.setLocation(new Location(""));
        assertThat(controller.getLocation()).isNotNull();
    }

    @Test
    public void setLocation_shouldUpdateLocation() throws Exception {
        Location expected = new Location("expected");
        controller.setLocation(expected);
        assertThat(controller.getLocation()).isSameAs(expected);
    }

    @Test
    public void getMapPosition_shouldNotBeNull() throws Exception {
        Location expected = new Location("expected");
        controller.setLocation(expected);
        assertThat(controller.getMapPosition()).isNotNull();
    }

    @Test
    public void getMapPosition_shouldHaveCorrectLocationCoordinates() throws Exception {
        int expectedLat = 12;
        int expectedLng = 24;
        Location expected = new Location("expected");
        expected.setLatitude(expectedLat);
        expected.setLongitude(expectedLng);
        controller.setLocation(expected);
        MapPosition position = controller.getMapPosition();
        assertThat(Math.round(position.getLatitude())).isEqualTo(expectedLat);
        assertThat(Math.round(position.getLongitude())).isEqualTo(expectedLng);
    }

    @Test
    public void getMapPosition_shouldHaveCorrectZoomLevel() throws Exception {
        int expected = 5;
        controller.setZoomLevel(expected);
        MapPosition mapPosition = controller.getMapPosition();
        assertThat(mapPosition.zoomLevel).isEqualTo(5);
    }

    @Test
    public void setZoomLevel_shouldStoreZoom() throws Exception {
        int expected = 5;
        controller.setZoomLevel(expected);
        assertThat(controller.getZoomLevel()).isEqualTo(expected);
    }

    @Test
    public void getZoomLevel_shouldGetStoredZoom() throws Exception {
        controller.setZoomLevel(5);
        assertThat(controller.getZoomLevel()).isNotNull();
    }

    @Test
    public void setMapPosition_shouldStoreMapPosition() throws Exception {
        MapPosition expected = new MapPosition(34.0, 34.0, 3.0);
        controller.setMapPosition(expected);
        assertThat(controller.getMapPosition()).isEqualTo(expected);
    }
}
