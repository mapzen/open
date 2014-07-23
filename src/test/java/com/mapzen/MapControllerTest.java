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
import org.oscim.core.BoundingBox;
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

import static android.content.Context.MODE_PRIVATE;
import static com.mapzen.MapController.DEBUG_LOCATION;
import static com.mapzen.MapController.KEY_BEARING;
import static com.mapzen.MapController.KEY_LATITUDE;
import static com.mapzen.MapController.KEY_LONGITUDE;
import static com.mapzen.MapController.KEY_MAP_SCALE;
import static com.mapzen.MapController.KEY_STORED_MAPPOSITION;
import static com.mapzen.MapController.KEY_TILT;
import static com.mapzen.MapController.geoPointToPair;
import static com.mapzen.MapController.getMapController;
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
    public void setLocation_shouldReturnMapController() throws Exception {
        Location expected = new Location("expected");
        assertThat(getMapController().setLocation(expected)).isEqualTo(getMapController());
    }

    @Test
    public void centerOn_shouldCallAnimateTo() throws Exception {
        Animator animator = Mockito.mock(Animator.class);
        TestMap map = (TestMap) getMapController().getMap();
        map.setAnimator(animator);
        Location location = new Location("expected");
        location.setLatitude(21.0);
        location.setLongitude(45.0);
        getMapController().centerOn(location);
        Mockito.verify(animator).animateTo(new GeoPoint(21.0, 45.0));
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
        assertThat(position.getLatitude()).isEqualTo(expectedLat, Offset.offset(0.0001));
        assertThat(position.getLongitude()).isEqualTo(expectedLng, Offset.offset(0.0001));
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
        getMapController().storeMapPosition(expected);
        assertThat(getMapController().getMapPosition()).isEqualTo(expected);
    }

    @Test
    public void getZoomScale_shouldGetStoredZoomScale() throws Exception {
        getMapController().setZoomLevel(5);
        assertThat(getMapController().getZoomScale()).isEqualTo(Math.pow(2, 5));
    }

    @Test
    public void setMapPerspectiveForInstruction_shouldSetMapPosition() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(40.0, 100.0);
        instructions.add(instruction);
        getMapController().setMapPerspectiveForInstruction(instruction);
        Map map = getMapController().getMap();
        BoundingBox box = map.viewport().getBBox();
        double latitudeOffset =  ((box.getMaxLatitude() - box.getMinLatitude()) / 4);
        int approximateOffset = ((int) ((map.getMapPosition().getLatitude() - 40) * 1000000));
        assertThat(approximateOffset).isEqualTo((int) (1000000 * latitudeOffset));
        assertThat(Math.round(map.getMapPosition().getLatitude())).isEqualTo(40);
        assertThat(Math.round(map.getMapPosition().getLongitude())).isEqualTo(100);
    }

    @Test
    public void setRoatation_shouldSetRotation() throws Exception {
        getMapController().setRotation(55f);
        assertThat(((TestViewport) getMapController().getMap().viewport()).getRotation())
                .isEqualTo(55f);
    }

    @Test
    public void setPosition_shouldSetRotation() throws Exception {
        getMapController().setPosition(getTestLocation(33.3, 44.4));
        MapPosition pos = getMapController().getMap().getMapPosition();
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
        getMapController().setZoomLevel(10);
        assertThat(getMapController().getMap().getMapPosition().getZoomLevel()).isEqualTo(10);
    }

    @Test
    public void saveLocation_shouldStoreCoordinates() {
        getMapController().setPosition(getTestLocation(22.0, 44.0));
        getMapController().saveLocation();
        assertThat(getSavedMapPrefs().getInt(KEY_LATITUDE, 0)).isEqualTo((int) (22.0 * 1e6));
        assertThat(getSavedMapPrefs().getInt(KEY_LONGITUDE, 0)).isEqualTo((int) (44.0 * 1e6));
    }

    @Test
    public void saveLocation_shouldStoreScale() {
        getMapController().setZoomLevel(8);
        getMapController().saveLocation();
        assertThat(getSavedMapPrefs().getFloat(KEY_MAP_SCALE, 0)).isEqualTo((float) Math.pow(2, 8));
    }

    @Test
    public void saveLocation_shouldStoreTilt() {
        MapPosition pos = getMapController().getMap().getMapPosition();
        pos.setTilt(2f);
        getMapController().getMap().setMapPosition(pos);
        getMapController().saveLocation();
        assertThat(getSavedMapPrefs().getFloat(KEY_TILT, 0)).isEqualTo(2f);
    }

    @Test
    public void saveLocation_shouldStoreBearing() {
        MapPosition pos = getMapController().getMap().getMapPosition();
        pos.setBearing(2f);
        getMapController().getMap().setMapPosition(pos);
        getMapController().saveLocation();
        assertThat(getSavedMapPrefs().getFloat(KEY_BEARING, 0)).isEqualTo(2f);
    }

    @Test
    public void restoreFromSavedLocation_shouldRestoreCoorinates() {
        populateSavedMapPosition();
        SharedPreferences.Editor editor = getSavedMapPrefs().edit();
        editor.putInt(KEY_LATITUDE, (int) (40.0 * 1e6));
        editor.putInt(KEY_LONGITUDE, (int) (20.0 * 1e6));
        editor.commit();
        getMapController().restoreFromSavedLocation();
        assertThat(Math.round(getMapController().getMap().getMapPosition().getLatitude()))
                .isEqualTo(40L);
        assertThat(Math.round(getMapController().getMap().getMapPosition().getLongitude()))
                .isEqualTo(20L);
    }

    @Test
    public void restoreFromSavedLocation_shouldRestoreScale() {
        populateSavedMapPosition();
        SharedPreferences.Editor editor = getSavedMapPrefs().edit();
        editor.putFloat(KEY_MAP_SCALE, (float) Math.pow(2, 8));
        editor.commit();
        getMapController().restoreFromSavedLocation();
        assertThat(getMapController().getMap().getMapPosition().getZoomLevel()).isEqualTo(8);
    }

    @Test
    public void restoreFromSavedLocation_shouldRestoreTilt() {
        populateSavedMapPosition();
        SharedPreferences.Editor editor = getSavedMapPrefs().edit();
        editor.putFloat(KEY_TILT, 2.3f);
        editor.commit();
        getMapController().restoreFromSavedLocation();
        assertThat(getMapController().getMap().getMapPosition().getTilt()).isEqualTo(2.3f);
    }

    @Test
    public void restoreFromSavedLocation_shouldRestoreBearing() {
        populateSavedMapPosition();
        SharedPreferences.Editor editor = getSavedMapPrefs().edit();
        editor.putFloat(KEY_BEARING, 4.3f);
        editor.commit();
        getMapController().restoreFromSavedLocation();
        assertThat(getMapController().getMap().getMapPosition().getBearing()).isEqualTo(4.3f);
    }

    @Test
    public void restoreFromSavedLocation_shouldActivateMapLocationUpdates() {
        getMapController().restoreFromSavedLocation();
        MapzenApplication app = ((MapzenApplication) Robolectric.application);
        assertThat(app.shouldMoveMapToLocation()).isTrue();
    }

    @Test
    public void restoreFromSavedLocation_shouldNotActivateMapLocationUpdates() {
        populateSavedMapPosition();
        getMapController().restoreFromSavedLocation();
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
