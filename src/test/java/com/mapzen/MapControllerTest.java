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

import static com.mapzen.MapController.getMapController;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class MapControllerTest {
    private TestBaseActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = TestHelper.initBaseActivity();
        MapController controller = getMapController();
        controller.setMap(activity.getMap());
    }

    @Test
    public void getInstance_shouldNotBeNull() throws Exception {
        assertThat(getMapController()).isNotNull();
    }

    @Test
    public void getInstance_shouldBeSingleton() throws Exception {
        assertThat(getMapController()).isSameAs(getMapController());
    }

    @Test
    public void getMap_shouldNotBeNull() throws Exception {
        assertThat(getMapController().getMap()).isNotNull();
    }

    @Test
    public void getLocation_shouldNotBeNull() throws Exception {
        getMapController().setLocation(new Location(""));
        assertThat(getMapController().getLocation()).isNotNull();
    }

    @Test
    public void setLocation_shouldUpdateLocation() throws Exception {
        Location expected = new Location("expected");
        getMapController().setLocation(expected);
        assertThat(getMapController().getLocation()).isSameAs(expected);
    }

    @Test
    public void getMapPosition_shouldNotBeNull() throws Exception {
        Location expected = new Location("expected");
        getMapController().setLocation(expected);
        assertThat(getMapController().getMapPosition()).isNotNull();
    }

    @Test
    public void getMapPosition_shouldHaveCorrectLocationCoordinates() throws Exception {
        int expectedLat = 12;
        int expectedLng = 24;
        Location expected = new Location("expected");
        expected.setLatitude(expectedLat);
        expected.setLongitude(expectedLng);
        getMapController().setLocation(expected);
        MapPosition position = getMapController().getMapPosition();
        assertThat(Math.round(position.getLatitude())).isEqualTo(expectedLat);
        assertThat(Math.round(position.getLongitude())).isEqualTo(expectedLng);
    }

    @Test
    public void getMapPosition_shouldHaveCorrectZoomLevel() throws Exception {
        int expected = 5;
        getMapController().setZoomLevel(expected);
        MapPosition mapPosition = getMapController().getMapPosition();
        assertThat(mapPosition.zoomLevel).isEqualTo(5);
    }

    @Test
    public void setZoomLevel_shouldStoreZoom() throws Exception {
        int expected = 5;
        getMapController().setZoomLevel(expected);
        assertThat(getMapController().getZoomLevel()).isEqualTo(expected);
    }

    @Test
    public void getZoomLevel_shouldGetStoredZoom() throws Exception {
        getMapController().setZoomLevel(5);
        assertThat(getMapController().getZoomLevel()).isNotNull();
    }

    @Test
    public void setMapPosition_shouldStoreMapPosition() throws Exception {
        MapPosition expected = new MapPosition(34.0, 34.0, 3.0);
        getMapController().setMapPosition(expected);
        assertThat(getMapController().getMapPosition()).isEqualTo(expected);
    }

    @Test
    public void getZoomScale_shouldGetSToredZoomScale() throws Exception {
        getMapController().setZoomLevel(5);
        assertThat(getMapController().getZoomScale()).isEqualTo(Math.pow(2, 5));
    }
}
