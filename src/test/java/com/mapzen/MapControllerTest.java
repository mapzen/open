package com.mapzen;

import com.mapzen.osrm.Instruction;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestBaseActivity;
import com.mapzen.support.TestHelper;

import org.fest.assertions.data.Offset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.map.Animator;
import org.oscim.map.Map;
import org.oscim.map.TestMap;
import org.oscim.map.TestViewport;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import java.util.ArrayList;

import javax.inject.Inject;

import static android.content.Context.MODE_PRIVATE;
import static com.mapzen.MapController.DEBUG_LOCATION;
import static com.mapzen.MapController.KEY_BEARING;
import static com.mapzen.MapController.KEY_LATITUDE;
import static com.mapzen.MapController.KEY_LONGITUDE;
import static com.mapzen.MapController.KEY_MAP_SCALE;
import static com.mapzen.MapController.KEY_STORED_MAPPOSITION;
import static com.mapzen.MapController.KEY_TILT;
import static com.mapzen.MapController.geoPointToPair;
import static com.mapzen.MapController.locationToGeoPoint;
import static com.mapzen.MapController.locationToPair;
import static com.mapzen.support.TestHelper.getTestInstruction;
import static com.mapzen.support.TestHelper.getTestLocation;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class MapControllerTest {
    private TestBaseActivity activity;
    @Inject MapController controller;

    @Before
    public void setUp() throws Exception {
        ((TestMapzenApplication) Robolectric.application).inject(this);
        activity = TestHelper.initBaseActivity();
        controller.setActivity(activity);
    }

    @Test
    public void getInstance_shouldNotBeNull() throws Exception {
        assertThat(controller).isNotNull();
    }

    @Test
    public void getInstance_shouldBeSingleton() throws Exception {
        assertThat(controller).isSameAs(controller);
    }

    @Test
    public void getMap_shouldNotBeNull() throws Exception {
        assertThat(controller.getMap()).isNotNull();
    }

    @Test
    public void getLocation_shouldNotBeNull() throws Exception {
        controller.setLocation(new Location(""));
        assertThat(controller.getLocation()).isNotNull();
    }

    @Test
    public void getLocation_shouldOverWriteLocation() throws Exception {
        enableFixedLocation();
        Location notExpected = new Location("not expected");
        controller.setLocation(notExpected);
        assertThat(controller.getLocation()).isNotSameAs(notExpected);
    }

    @Test
    public void getLocation_shouldBeReturnDefaultDebugLocation() throws Exception {
        enableFixedLocation();
        Location actual = controller.getLocation();
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
        Location actual = controller.getLocation();
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
        controller.setLocation(expected);
        Location actual = controller.getLocation();
        assertThat(ShadowToast.getTextOfLatestToast())
                .isEqualTo(activity.getString(R.string.toast_fixed_location_is_malformed));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getLocation_shouldReturnNotBlowUpWhenPresetIsMalformedWithComma() throws Exception {
        enableFixedLocation();
        setFixedLocation("malformed, which makes no sense");
        Location expected = new Location("expected");
        controller.setLocation(expected);
        Location actual = controller.getLocation();
        assertThat(ShadowToast.getTextOfLatestToast())
                .isEqualTo(activity.getString(R.string.toast_fixed_location_is_malformed));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void setLocation_shouldUpdateLocation() throws Exception {
        Location expected = new Location("expected");
        controller.setLocation(expected);
        assertThat(controller.getLocation()).isSameAs(expected);
    }

    @Test
    public void setLocation_shouldReturnMapController() throws Exception {
        Location expected = new Location("expected");
        assertThat(controller.setLocation(expected)).isEqualTo(controller);
    }

    @Test
    public void centerOn_shouldCallAnimateTo() throws Exception {
        Animator animator = Mockito.mock(Animator.class);
        TestMap map = (TestMap) controller.getMap();
        map.setAnimator(animator);
        Location location = new Location("expected");
        location.setLatitude(21.0);
        location.setLongitude(45.0);
        controller.centerOn(location);
        Mockito.verify(animator).animateTo(new GeoPoint(21.0, 45.0));
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
        assertThat(position.getLatitude()).isEqualTo(expectedLat, Offset.offset(0.0001));
        assertThat(position.getLongitude()).isEqualTo(expectedLng, Offset.offset(0.0001));
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
        controller.storeMapPosition(expected);
        assertThat(controller.getMapPosition()).isEqualTo(expected);
    }

    @Test
    public void getZoomScale_shouldGetStoredZoomScale() throws Exception {
        controller.setZoomLevel(5);
        assertThat(controller.getZoomScale()).isEqualTo(Math.pow(2, 5));
    }

    @Test
    public void setMapPerspectiveForInstruction_shouldSetMapPosition() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(40.0, 100.0);
        Map map = controller.getMap();
        map.setMapPosition(new MapPosition(instruction.getLocation().getLatitude(),
                instruction.getLocation().getLongitude(),
                controller.ROUTE_ZOOM_LEVEL));
        double originalX =  map.getMapPosition().getX();
                instructions.add(instruction);

        controller.setMapPerspectiveForInstruction(instruction);
        double newX = map.getMapPosition().getX();
        assertThat(originalX - newX).isGreaterThan(0);
        assertThat(originalX - newX).isLessThan(0.00001);
        assertThat(Math.round(map.getMapPosition().getLatitude())).isEqualTo(40);
        assertThat(Math.round(map.getMapPosition().getLongitude())).isEqualTo(100);
    }

    @Test
    public void setRoatation_shouldSetRotation() throws Exception {
        controller.setRotation(55f);
        assertThat(((TestViewport) controller.getMap().viewport()).getRotation())
                .isEqualTo(55f);
    }

    @Test
    public void setPosition_shouldSetRotation() throws Exception {
        controller.setPosition(getTestLocation(33.3, 44.4));
        MapPosition pos = controller.getMap().getMapPosition();
        assertThat(Math.round(pos.getLatitude())).isEqualTo(Math.round(33.3));
        assertThat(Math.round(pos.getLongitude())).isEqualTo(Math.round(44.4));
    }

    @Test
    public void locationToPair_shouldReturnDoublePair() throws Exception {
        double expectedLat = 20.0;
        double expectedLon = 40.0;
        Location testLoc = getTestLocation(expectedLat, expectedLon);
        assertThat(locationToPair(testLoc)[0]).isEqualTo(expectedLat);
        assertThat(locationToPair(testLoc)[1]).isEqualTo(expectedLon);
    }

    @Test
    public void geoPointToPair_shouldReturnDoublePair() throws Exception {
        double expectedLat = 20.0;
        double expectedLon = 40.0;
        GeoPoint testGeoPoint = new GeoPoint(expectedLat, expectedLon);
        assertThat(geoPointToPair(testGeoPoint)[0]).isEqualTo(expectedLat);
        assertThat(geoPointToPair(testGeoPoint)[1]).isEqualTo(expectedLon);
    }

    @Test
    public void locationToGeoPoint_shouldReturnGeoPoint() throws Exception {
        double expectedLat = 20.0;
        double expectedLon = 40.0;
        Location testLoc = getTestLocation(expectedLat, expectedLon);
        assertThat(locationToGeoPoint(testLoc).getLatitude()).isEqualTo(expectedLat);
        assertThat(locationToGeoPoint(testLoc).getLongitude()).isEqualTo(expectedLon);
    }

    @Test
    public void setZoomLevel_shouldUpdateMap() throws Exception {
        controller.setZoomLevel(10);
        assertThat(controller.getMap().getMapPosition().getZoomLevel()).isEqualTo(10);
    }

    @Test
    public void saveLocation_shouldStoreCoordinates() {
        controller.setPosition(getTestLocation(22.0, 44.0));
        controller.saveLocation();
        assertThat(getSavedMapPrefs().getInt(KEY_LATITUDE, 0)).isEqualTo((int) (22.0 * 1e6));
        assertThat(getSavedMapPrefs().getInt(KEY_LONGITUDE, 0)).isEqualTo((int) (44.0 * 1e6));
    }

    @Test
    public void saveLocation_shouldStoreScale() {
        controller.setZoomLevel(8);
        controller.saveLocation();
        assertThat(getSavedMapPrefs().getFloat(KEY_MAP_SCALE, 0)).isEqualTo((float) Math.pow(2, 8));
    }

    @Test
    public void saveLocation_shouldStoreTilt() {
        MapPosition pos = controller.getMap().getMapPosition();
        pos.setTilt(2f);
        controller.getMap().setMapPosition(pos);
        controller.saveLocation();
        assertThat(getSavedMapPrefs().getFloat(KEY_TILT, 0)).isEqualTo(2f);
    }

    @Test
    public void saveLocation_shouldStoreBearing() {
        MapPosition pos = controller.getMap().getMapPosition();
        pos.setBearing(2f);
        controller.getMap().setMapPosition(pos);
        controller.saveLocation();
        assertThat(getSavedMapPrefs().getFloat(KEY_BEARING, 0)).isEqualTo(2f);
    }

    @Test
    public void restoreFromSavedLocation_shouldRestoreCoorinates() {
        populateSavedMapPosition();
        SharedPreferences.Editor editor = getSavedMapPrefs().edit();
        editor.putInt(KEY_LATITUDE, (int) (40.0 * 1e6));
        editor.putInt(KEY_LONGITUDE, (int) (20.0 * 1e6));
        editor.commit();
        controller.restoreFromSavedLocation();
        assertThat(Math.round(controller.getMap().getMapPosition().getLatitude()))
                .isEqualTo(40L);
        assertThat(Math.round(controller.getMap().getMapPosition().getLongitude()))
                .isEqualTo(20L);
    }

    @Test
    public void restoreFromSavedLocation_shouldRestoreScale() {
        populateSavedMapPosition();
        SharedPreferences.Editor editor = getSavedMapPrefs().edit();
        editor.putFloat(KEY_MAP_SCALE, (float) Math.pow(2, 8));
        editor.commit();
        controller.restoreFromSavedLocation();
        assertThat(controller.getMap().getMapPosition().getZoomLevel()).isEqualTo(8);
    }

    @Test
    public void restoreFromSavedLocation_shouldRestoreTilt() {
        populateSavedMapPosition();
        SharedPreferences.Editor editor = getSavedMapPrefs().edit();
        editor.putFloat(KEY_TILT, 2.3f);
        editor.commit();
        controller.restoreFromSavedLocation();
        assertThat(controller.getMap().getMapPosition().getTilt()).isEqualTo(2.3f);
    }

    @Test
    public void restoreFromSavedLocation_shouldRestoreBearing() {
        populateSavedMapPosition();
        SharedPreferences.Editor editor = getSavedMapPrefs().edit();
        editor.putFloat(KEY_BEARING, 4.3f);
        editor.commit();
        controller.restoreFromSavedLocation();
        assertThat(controller.getMap().getMapPosition().getBearing()).isEqualTo(4.3f);
    }

    @Test
    public void resetZoomAndPointNorth_shouldSetDefaultZoom() {
        controller.setZoomLevel(3);
        assertThat(controller.getZoomLevel()).isEqualTo(3);
        controller.resetZoomAndPointNorth();
        assertThat(controller.getZoomLevel())
            .isEqualTo(MapController.DEFAULT_ZOOM_LEVEL);
    }

    @Test
    public void resetZoomAndPointNorth_shouldSetZeroBearing() {
        controller.getMapPosition().setBearing(1.0f);
        assertThat(controller.getMapPosition().getBearing()).isEqualTo(1.0f);
        controller.resetZoomAndPointNorth();
        assertThat(controller.getMapPosition().getBearing()).isEqualTo(0.0f);
    }

    @Test
    public void restoreFromSavedLocation_shouldActivateMapLocationUpdates() {
        controller.restoreFromSavedLocation();
        MapzenApplication app = ((MapzenApplication) Robolectric.application);
        assertThat(app.shouldMoveMapToLocation()).isTrue();
    }

    @Test
    public void restoreFromSavedLocation_shouldNotActivateMapLocationUpdates() {
        populateSavedMapPosition();
        controller.restoreFromSavedLocation();
        MapzenApplication app = ((MapzenApplication) Robolectric.application);
        assertThat(app.shouldMoveMapToLocation()).isFalse();
    }

    private SharedPreferences getSavedMapPrefs() {
        return activity.getSharedPreferences(KEY_STORED_MAPPOSITION, MODE_PRIVATE);
    }

    private void populateSavedMapPosition() {
        SharedPreferences.Editor editor =
                activity.getSharedPreferences(KEY_STORED_MAPPOSITION, MODE_PRIVATE).edit();
        editor.putInt(KEY_LATITUDE, 0);
        editor.putInt(KEY_LONGITUDE, 0);
        editor.putFloat(KEY_MAP_SCALE, 0);
        editor.putFloat(KEY_BEARING, 0);
        editor.putFloat(KEY_TILT, 0);
        editor.commit();
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
