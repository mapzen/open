package com.mapzen;

import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestBaseActivity;
import com.mapzen.support.TestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscim.core.MapPosition;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import static com.mapzen.MapController.DEBUG_LOCATION;
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
        controller.setActivity(activity);
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
    public void getLocation_shouldOverWriteLocation() throws Exception {
        enableFixedLocation();
        Location notExpected = new Location("not expected");
        getMapController().setLocation(notExpected);
        assertThat(getMapController().getLocation()).isNotSameAs(notExpected);
    }

    @Test
    public void getLocation_shouldBeReturnDefaultDebugLocation() throws Exception {
        enableFixedLocation();
        Location actual = getMapController().getLocation();
        String defaultLatLng = activity.getString(R.string.settings_fixed_location_default_value);
        String[] defaultValues = defaultLatLng.split(", ");
        Location expected = new Location(DEBUG_LOCATION);
        expected.setLatitude(Double.valueOf(defaultValues[0]));
        expected.setLongitude(Double.valueOf(defaultValues[1]));
        assertThat(actual.getLatitude()).isEqualTo(expected.getLatitude());
        assertThat(actual.getLongitude()).isEqualTo(expected.getLongitude());
    }

    @Test
    public void getLocation_shouldBeReturnPresetDebugLocation() throws Exception {
        enableFixedLocation();
        String expectedLat = "40.6638";
        String expectedLng = "-73.9843";
        setFixedLocation(expectedLat + ", " + expectedLng);
        Location actual = getMapController().getLocation();
        Location expected = new Location(DEBUG_LOCATION);
        expected.setLatitude(Double.valueOf(expectedLat));
        expected.setLongitude(Double.valueOf(expectedLng));
        assertThat(actual.getLatitude()).isEqualTo(expected.getLatitude());
        assertThat(actual.getLongitude()).isEqualTo(expected.getLongitude());
    }

    @Test
    public void getLocation_shouldReturnNotBlowUpWhenPresetIsMalformed() throws Exception {
        enableFixedLocation();
        setFixedLocation("malformed which makes no sense");
        Location expected = new Location("expected");
        getMapController().setLocation(expected);
        Location actual = getMapController().getLocation();
        assertThat(ShadowToast.getTextOfLatestToast())
                .isEqualTo(activity.getString(R.string.toast_fixed_location_is_malformed));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getLocation_shouldReturnNotBlowUpWhenPresetIsMalformedWithComma() throws Exception {
        enableFixedLocation();
        setFixedLocation("malformed, which makes no sense");
        Location expected = new Location("expected");
        getMapController().setLocation(expected);
        Location actual = getMapController().getLocation();
        assertThat(ShadowToast.getTextOfLatestToast())
                .isEqualTo(activity.getString(R.string.toast_fixed_location_is_malformed));
        assertThat(actual).isEqualTo(expected);
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
    public void getZoomScale_shouldGetStoredZoomScale() throws Exception {
        getMapController().setZoomLevel(5);
        assertThat(getMapController().getZoomScale()).isEqualTo(Math.pow(2, 5));
    }

    private void enableFixedLocation() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putBoolean(
                activity.getString(R.string.settings_key_enable_fixed_location), true);
        prefEditor.commit();
    }

    private void setFixedLocation(String fixedLocation) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString(
                activity.getString(R.string.settings_fixed_location_key), fixedLocation);
        prefEditor.commit();
    }

}
