package com.mapzen.open.route;

import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.internal.FusedLocationProviderApiImpl;
import com.mapzen.helpers.DistanceFormatter;
import com.mapzen.helpers.ZoomController;
import com.mapzen.open.MapController;
import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.TestMapzenApplication;
import com.mapzen.open.entity.SimpleFeature;
import com.mapzen.open.fragment.MapFragment;
import com.mapzen.open.shadows.ShadowMint;
import com.mapzen.open.support.MapzenTestRunner;
import com.mapzen.open.support.TestBaseActivity;
import com.mapzen.open.support.TestHelper;
import com.mapzen.open.support.TestHelper.LocationUpdateSubscriber;
import com.mapzen.open.support.TestHelper.ViewUpdateSubscriber;
import com.mapzen.open.util.DatabaseHelper;
import com.mapzen.open.util.MapzenNotificationCreator;
import com.mapzen.open.util.RouteLocationIndicator;
import com.mapzen.open.widget.DistanceView;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;
import com.mapzen.osrm.Router;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.squareup.otto.Bus;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.map.TestMap;
import org.oscim.map.TestViewport;
import org.oscim.map.ViewController;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.shadows.ShadowNotification;
import org.robolectric.shadows.ShadowNotificationManager;
import org.robolectric.shadows.ShadowTextToSpeech;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.shadows.ShadowView;
import org.robolectric.tester.android.view.TestMenu;
import org.robolectric.util.FragmentTestUtil;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.inject.Inject;

import static com.mapzen.open.entity.SimpleFeature.TEXT;
import static com.mapzen.open.support.TestHelper.MOCK_ACE_HOTEL;
import static com.mapzen.open.support.TestHelper.MOCK_AROUND_THE_BLOCK;
import static com.mapzen.open.support.TestHelper.MOCK_NY_TO_VT;
import static com.mapzen.open.support.TestHelper.MOCK_ROUTE_JSON;
import static com.mapzen.open.support.TestHelper.enableDebugMode;
import static com.mapzen.open.support.TestHelper.getTestInstruction;
import static com.mapzen.open.support.TestHelper.getTestLastInstruction;
import static com.mapzen.open.support.TestHelper.getTestLocation;
import static com.mapzen.open.support.TestHelper.getTestSimpleFeature;
import static com.mapzen.open.support.TestHelper.initBaseActivityWithMenu;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_GROUP_ID;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_MSG;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_READY_FOR_UPLOAD;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_ROUTE_ID;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_TABLE_ID;
import static com.mapzen.open.util.DatabaseHelper.TABLE_GROUPS;
import static com.mapzen.open.util.DatabaseHelper.TABLE_ROUTES;
import static com.mapzen.open.util.DatabaseHelper.TABLE_ROUTE_GEOMETRY;
import static com.mapzen.open.util.DatabaseHelper.TABLE_ROUTE_GROUP;
import static com.mapzen.open.util.MixpanelHelper.Event.ROUTING_START;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.Robolectric.shadowOf_;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class RouteFragmentTest {
    @Inject Router router;
    @Inject MapController mapController;
    @Inject ZoomController zoomController;
    @Inject MixpanelAPI mixpanelAPI;
    @Inject SQLiteDatabase db;
    @Inject Bus bus;
    @Inject RouteLocationIndicatorFactory routeLocationIndicatorFactory;

    private static TestMenu menu = new TestMenu();
    private static TestBaseActivity act = initBaseActivityWithMenu(menu);

    private RouteFragment fragment;
    private ShadowApplication app;
    private ArrayList<Instruction> testInstructions;
    private Location startLocation;

    @Before
    public void setUp() throws Exception {
        ((TestMapzenApplication) Robolectric.application).inject(this);
        initTestFragment();
        app = Robolectric.getShadowApplication();
        GeoPoint start = fragment.getSimpleFeature().getGeoPoint();
        startLocation = getTestLocation(start.getLatitude(), start.getLongitude());
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(fragment).isNotNull();
    }

    @Test
    public void shouldNotBeAdded() throws Exception {
        assertThat(fragment).isNotAdded();
    }

    @Test
    public void shouldRetainInstance() throws Exception {
        assertThat(fragment.getRetainInstance()).isTrue();
    }

    @Test
    public void shouldHideActionBar() throws Exception {
        TestHelper.startFragment(fragment, act);
        assertThat(act.getActionBar()).isNotShowing();
    }

    @Test
    public void shouldKeepScreenOn() throws Exception {
        LayoutInflater inflater = act.getLayoutInflater();
        View view = inflater.inflate(R.layout.route_widget, null, false);
        assertThat(view.findViewById(R.id.routes)).isKeepingScreenOn();
    }

    @Test
    public void shouldCreateView() throws Exception {
        TestHelper.startFragment(fragment, act);
        View view = fragment.getView();
        assertThat(view).isNotNull();
    }

    @Test
    public void shouldHaveRoutesViewPager() throws Exception {
        TestHelper.startFragment(fragment, act);
        assertThat(fragment.pager).isNotNull();
    }

    @Test
    public void locateButtonShouldNotBeVisible() throws Exception {
        TestHelper.startFragment(fragment, act);
        assertThat(act.findViewById(R.id.locate_button)).isNotVisible();
    }

    @Test
    public void onDestroy_locateButtonShouldBeVisible() throws Exception {
        TestHelper.startFragment(fragment, act);
        fragment.onDestroy();
        assertThat(act.findViewById(R.id.locate_button)).isVisible();
    }

    @Test
    public void onSnapLocation_shouldStoreOriginalLocationRecordInDatabase() throws Exception {
        initTestFragment();
        TestHelper.startFragment(fragment, act);
        Location expected = fragment.getRoute().getGeometry().get(2);
        fragment.onSnapLocation(expected, fragment.getRoute().snapToRoute(expected));
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] { DatabaseHelper.COLUMN_LAT, DatabaseHelper.COLUMN_LNG },
                null, null, null, null, null);
        cursor.moveToNext();
        assertThat(cursor.getString(0)).isEqualTo(String.valueOf(expected.getLatitude()));
        assertThat(cursor.getString(1)).isEqualTo(String.valueOf(expected.getLongitude()));
    }

    @Test
    public void onSnapLocation_shouldStoreCorrectedLocationRecordInDatabase() throws Exception {
        initTestFragment();
        TestHelper.startFragment(fragment, act);
        Location testLocation = fragment.getRoute().getGeometry().get(2);
        fragment.onSnapLocation(testLocation, fragment.getRoute().snapToRoute(testLocation));
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] {
                        DatabaseHelper.COLUMN_CORRECTED_LAT,
                        DatabaseHelper.COLUMN_CORRECTED_LNG
                },
                null, null, null, null, null);
        cursor.moveToNext();
        assertThat(cursor.getString(0)).isNotNull();
        assertThat(cursor.getString(1)).isNotNull();
    }

    @Test
    public void onMapSwipe_ShouldDisplayResumeButton() throws Exception {
        initTestFragment();
        TestHelper.startFragment(fragment, act);
        simulateUserDrag();
        assertThat(fragment.getView().findViewById(R.id.resume_button)).isVisible();
    }

    @Test
    public void onMapTwoFingerScroll_ShouldNotDisplayResumeButton() throws Exception {
        initTestFragment();
        TestHelper.startFragment(fragment, act);
        assertThat(fragment.getView().findViewById(R.id.resume_button)).isNotVisible();
        simulateTwoFingerDrag();
        assertThat(fragment.getView().findViewById(R.id.resume_button)).isNotVisible();
    }

    @Test
    public void onLocationChange_shouldStoreInstructionPointsRecordInDatabase() throws Exception {
        initTestFragment();
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Location expected = fragment.getRoute().getGeometry().get(0);
        Location sample1 = fragment.getRoute().getGeometry().get(1);
        Location sample2 = fragment.getRoute().getGeometry().get(2);
        instructions.add(getTestInstruction(expected.getLatitude(), expected.getLongitude()));
        instructions.add(getTestInstruction(sample1.getLatitude(), sample1.getLongitude()));

        TestHelper.startFragment(fragment, act);
        fragment.setInstructions(instructions);

        Location testLocation = getTestLocation(sample2.getLatitude(), sample2.getLongitude());
        fragment.onLocationChanged(testLocation);
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] {
                        DatabaseHelper.COLUMN_INSTRUCTION_LAT,
                        DatabaseHelper.COLUMN_INSTRUCTION_LNG
                },
                null, null, null, null, null);
        assertThat(cursor).hasCount(1);
        cursor.moveToNext();
        assertThat(cursor.getString(0)).isEqualTo(String.valueOf(expected.getLatitude()));
        assertThat(cursor.getString(1)).isEqualTo(String.valueOf(expected.getLongitude()));
    }

    @Test
    public void drawRoute_shouldStoreCoordinates() throws Exception {
        initTestFragment();
        TestHelper.startFragment(fragment, act);
        fragment.createRouteTo(getTestLocation(100.0, 100.0));
        fragment.success(new Route(MOCK_ROUTE_JSON));
        fragment.onPause();
        Cursor cursor = db.query(TABLE_ROUTE_GEOMETRY,
                new String[] { COLUMN_ROUTE_ID },
                COLUMN_ROUTE_ID + " = ?",
                new String[] { String.valueOf(fragment.getRouteId()) }, null, null, null);
        assertThat(cursor).hasCount(fragment.getRoute().getGeometry().size());
    }

    @Test
    public void drawRoute_shouldNotStoreCoordinates() throws Exception {
        TestHelper.startFragment(fragment, act);
        fragment.onPause();
        Cursor cursor = db.query(TABLE_ROUTE_GEOMETRY,
                new String[] { COLUMN_ROUTE_ID },
                COLUMN_ROUTE_ID + " = ?",
                new String[] { String.valueOf(fragment.getRouteId()) }, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void onLocationChange_shouldStoreInstructionBearingRecordInDatabase() throws Exception {
        initTestFragment();
        TestHelper.startFragment(fragment, act);
        Location testLocation = fragment.getRoute().getGeometry().get(2);
        fragment.onLocationChanged(testLocation);
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] { DatabaseHelper.COLUMN_INSTRUCTION_BEARING },
                null, null, null, null, null);
        assertThat(cursor).hasCount(1);
        cursor.moveToNext();
        assertThat(cursor.getInt(0)).isEqualTo(
                fragment.getRoute().getRouteInstructions().get(0).getBearing());
    }

    @Test
    public void onLocationChange_shouldStoreSpeedInDatabase() throws Exception {
        initTestFragment();
        TestHelper.startFragment(fragment, act);
        Location testLocation = fragment.getRoute().getGeometry().get(2);
        float expectedSpeed = 44.0f;
        testLocation.setSpeed(expectedSpeed);
        fragment.onLocationChanged(testLocation);
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] { DatabaseHelper.COLUMN_SPEED },
                null, null, null, null, null);
        assertThat(cursor).hasCount(1);
        cursor.moveToNext();
        assertThat(cursor.getFloat(0)).isEqualTo(expectedSpeed);
    }

    @Test
    public void onLocationChange_shouldStoreBearingInDatabase() throws Exception {
        initTestFragment();
        TestHelper.startFragment(fragment, act);
        Location testLocation = fragment.getRoute().getGeometry().get(2);
        float expectedBearing = 44.0f;
        testLocation.setBearing(expectedBearing);
        fragment.onLocationChanged(testLocation);
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] { DatabaseHelper.COLUMN_BEARING },
                null, null, null, null, null);
        assertThat(cursor).hasCount(1);
        cursor.moveToNext();
        assertThat(cursor.getFloat(0)).isEqualTo(expectedBearing);
    }

    @Test
    public void onLocationChange_shouldNotStoreDatabaseRecord() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(99.0, 89.0));
        instructions.add(getTestInstruction(0, 0));

        TestHelper.startFragment(fragment, act);
        fragment.setInstructions(instructions);

        Location testLocation = getTestLocation(20.0, 30.0);
        fragment.onLocationChanged(testLocation);
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] { DatabaseHelper.COLUMN_INSTRUCTION_BEARING },
                null, null, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void onLocationChange_shouldStoreAssociatedRoute() throws Exception {
        initTestFragment();
        TestHelper.startFragment(fragment, act);
        Location testLocation = fragment.getRoute().getGeometry().get(2);
        fragment.onLocationChanged(testLocation);
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] { COLUMN_ROUTE_ID },
                COLUMN_ROUTE_ID + " = ?",
                new String[] { String.valueOf(fragment.getRouteId()) }, null, null, null);
        assertThat(cursor).hasCount(1);
    }

    @Test
    public void onRecalculate_shouldCreateNewRoute() throws Exception {
        initTestFragment();
        TestHelper.startFragment(fragment, act);

        Route oldRoute = fragment.getRoute();
        fragment.onRecalculate(getTestLocation(111.0, 111.0));
        fragment.success(new Route(MOCK_NY_TO_VT));
        assertThat(fragment.getRoute()).isNotSameAs(oldRoute);
    }

    @Test
    public void onCreate_shouldFireMixpanelEvent() throws Exception {
        initTestFragment();
        TestHelper.startFragment(fragment, act);
        verify(mixpanelAPI).track(eq(ROUTING_START), any(JSONObject.class));
    }

    @Test
    public void onCreate_shouldHideLocationMarker() throws Exception {
        TestHelper.startFragment(fragment, act);
        assertThat(fragment.getMapFragment().getMap().layers().
                contains(fragment.getMapFragment().getLocationMarkerLayer())).isFalse();
    }

    @Test
    public void onDestroy_shouldShowLocationMarker() throws Exception {
        TestHelper.startFragment(fragment, act);
        fragment.onDestroy();
        assertThat(fragment.getMapFragment().getMap().layers().
                contains(fragment.getMapFragment().getLocationMarkerLayer())).isTrue();
    }

    @Test
    public void onBack_shouldActAsResumeButton() throws Exception {
        TestHelper.startFragment(fragment, act);
        simulateUserPagerTouch();
        ImageButton resume = (ImageButton) fragment.getView().findViewById(R.id.resume_button);
        assertThat(resume).isVisible();
        fragment.onBackAction();
        assertThat(resume).isNotVisible();
    }

    @Test
    public void onCreate_shouldShowRouteLocationIndicator() throws Exception {
        TestHelper.startFragment(fragment, act);
        assertThat(fragment.getMapFragment().getMap().layers().
                contains(fragment.getRouteLocationIndicator())).isTrue();
    }

    @Test
    public void onDestroy_shouldHideRouteLocationIndicator() throws Exception {
        TestHelper.startFragment(fragment, act);
        fragment.onDestroy();
        assertThat(fragment.getMapFragment().getMap().layers().
                contains(fragment.getRouteLocationIndicator())).isFalse();
    }

    @Test
    public void onDestroy_shouldRefreshRoutePreview() throws Exception {
        ViewUpdateSubscriber viewUpdateSubscriber = new ViewUpdateSubscriber();
        bus.register(viewUpdateSubscriber);
        act = initBaseActivityWithMenu(menu);
        TestHelper.startFragment(fragment, act);
        fragment.onDestroy();
        assertThat(viewUpdateSubscriber.getEvent()).isNotNull();
    }

    @Test
    public void shouldHaveRouteLocationIndicator() throws Exception {
        TestHelper.startFragment(fragment, act);
        assertThat(fragment.getRouteLocationIndicator()).isNotNull();
    }

    @Test
    public void onResume_shouldSetRouteLocationIndicatorToStartingCoordinates() throws Exception {
        TestHelper.startFragment(fragment, act);
        fragment.onResume();
        RouteLocationIndicator mockLocationIndicator = routeLocationIndicatorFactory
                .getRouteLocationIndicator();
        verify(mockLocationIndicator, atLeastOnce()).setPosition(
                fragment.getRoute().getStartCoordinates().getLatitude(),
                fragment.getRoute().getStartCoordinates().getLongitude());
    }

    @Test
    public void onResume_shouldSetRouteLocationIndicatorToStartingBearing() throws Exception {
        TestHelper.startFragment(fragment, act);
        fragment.onResume();
        RouteLocationIndicator mockLocationIndicator = routeLocationIndicatorFactory
                .getRouteLocationIndicator();
        verify(mockLocationIndicator, atLeastOnce()).setRotation(
                (float) fragment.getRoute().getCurrentRotationBearing());
    }

    @Test
    public void onCreate_shouldCreateGroupInDatabase() throws Exception {
        TestHelper.startFragment(fragment, act);
        Cursor cursor = db.query(TABLE_GROUPS,
                new String[] { COLUMN_TABLE_ID }, null, null, null, null, null);
        assertThat(cursor).hasCount(1);
    }

    @Test
    public void onCreate_shouldCreateGroupInDatabaseWithDescription() throws Exception {
        TestHelper.startFragment(fragment, act);
        String expected = fragment.getGPXDescription();
        Cursor cursor = db.query(TABLE_GROUPS,
                new String[] { COLUMN_TABLE_ID },
                COLUMN_MSG + " = ?",
                new String[] { expected }, null, null, null);
        assertThat(cursor).hasCount(1);
    }

    @Test
    public void onCreate_shouldCreateGroupThatIsNotReadyForUploadInDatabase() throws Exception {
        TestHelper.startFragment(fragment, act);
        Cursor cursor = db.query(TABLE_GROUPS,
                new String[] { COLUMN_TABLE_ID },
                COLUMN_READY_FOR_UPLOAD + " is null", null, null, null, null);
        assertThat(cursor).hasCount(1);
    }

    @Test
    public void onDestroy_shouldMarkGroupAsReadyForUpload() throws Exception {
        TestHelper.startFragment(fragment, act);
        fragment.onDestroy();
        Cursor cursor = db.query(TABLE_GROUPS,
                new String[] { COLUMN_TABLE_ID },
                COLUMN_READY_FOR_UPLOAD + " is not null", null, null, null, null);
        assertThat(cursor).hasCount(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onCreate_shouldRegisterReceiver() throws Exception {
        TestHelper.startFragment(fragment, act);
        bus.register(fragment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onDestroy_shouldUnRegisterReceiver() throws Exception {
        TestHelper.startFragment(fragment, act);
        fragment.onDestroy();
        bus.unregister(fragment);
    }

    @Test
    public void setFeature_shouldGenerateDestinationPoint() throws Exception {
        SimpleFeature simpleFeature = new SimpleFeature();
        fragment.setSimpleFeature(simpleFeature);
        assertThat(fragment.getDestinationPoint()).isEqualTo(simpleFeature.getGeoPoint());
    }

    @Test
    public void setDestination_shouldSetFeature() throws Exception {
        SimpleFeature simpleFeature = getTestSimpleFeature();
        fragment.setSimpleFeature(simpleFeature);
        assertThat(fragment.getSimpleFeature()).isEqualTo(simpleFeature);
    }

    @Test
    public void onCreateView_shouldShowNameOfDestination() throws Exception {
        TestHelper.startFragment(fragment, act);
        SimpleFeature simpleFeature = getTestSimpleFeature();
        TextView view = (TextView) fragment.getView().findViewById(R.id.destination_name);
        assertThat(view.getText()).isEqualTo(act
                .getString(R.string.routing_to_text) + simpleFeature.getProperty(TEXT));
        assertThat(view).hasEllipsize(TextUtils.TruncateAt.END);
        assertThat(view).hasMaxLines(1);
    }

    @Test
    public void onCreateView_shouldHaveTotalDistance() throws Exception {
        TestHelper.startFragment(fragment, act);
        act.showLoadingIndicator();
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        DistanceView textView = (DistanceView) view.findViewById(R.id.destination_distance);
        int distance = fragment.getRoute().getTotalDistance();
        String expectedFormattedDistance = DistanceFormatter.format(distance, true);
        assertThat(textView.getText()).isEqualTo(expectedFormattedDistance);
    }

    @Test
    public void onCreateView_shouldNotShowResumeButton() throws Exception {
        TestHelper.startFragment(fragment, act);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        ImageButton resume = (ImageButton) view.findViewById(R.id.resume_button);
        assertThat(resume).isNotVisible();
    }

    @Test
    public void onTouch_shouldDisplayResumeButton() throws Exception {
        TestHelper.startFragment(fragment, act);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        ImageButton resume = (ImageButton) view.findViewById(R.id.resume_button);
        simulateUserPagerTouch();
        assertThat(resume).isVisible();
    }

    @Test
    public void onTouch_shouldStoreCurrentItemWhenPagerWasFirstTouched() throws Exception {
        loadAceHotelMockRoute();
        fragment.onLocationChanged(fragment.getRoute().getRouteInstructions().get(0).getLocation());
        fragment.onLocationChanged(fragment.getRoute().getRouteInstructions().get(1).getLocation());
        int expected = fragment.pager.getCurrentItem();
        simulateUserPagerTouch();
        fragment.pager.setCurrentItem(0);
        simulateUserPagerTouch();
        fragment.getView().findViewById(R.id.resume_button).performClick();
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(expected);
    }

    @Test
    public void onClickResume_shouldHideResumeButton() throws Exception {
        TestHelper.startFragment(fragment, act);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        ImageButton resume = (ImageButton) view.findViewById(R.id.resume_button);
        simulateUserPagerTouch();
        resume.performClick();
        assertThat(resume).isNotVisible();
    }

    @Test
    public void onClickResume_shouldStartAtPagerLocation() throws Exception {
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        fragment.onLocationChanged(instructions.get(1).getLocation());
        fragment.onLocationChanged(instructions.get(2).getLocation());
        int expected = fragment.pager.getCurrentItem();
        simulateUserPagerTouch();
        fragment.pager.setCurrentItem(0);
        ImageButton resume = (ImageButton) fragment.getView().findViewById(R.id.resume_button);
        resume.performClick();
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(expected);
    }

    @Test
    public void expandedPane_shouldShowDirectionListFragment() {
        TestHelper.startFragment(fragment, act);
        simulatePaneOpenSlide();
        assertThat(fragment.getChildFragmentManager()).
                hasFragmentWithTag(DirectionListFragment.TAG);
    }

    @Test
    public void collapsedPane_shouldNotShowDirectionListFragment() {
        TestHelper.startFragment(fragment, act);
        simulatePaneOpenSlide();
        simulatePaneCloseSlide();
        assertThat(fragment.getChildFragmentManager())
                .doesNotHaveFragmentWithTag(DirectionListFragment.TAG);
    }

    @Test
    public void arrowButtons_ShouldPageThrough() {
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        int firstInstruction = fragment.pager.getCurrentItem();
        ImageButton rightArrow = (ImageButton) getInstructionView(firstInstruction)
                .findViewById(R.id.right_arrow);
        rightArrow.performClick();
        assertThat(fragment.pager.getCurrentItem() - 1).isEqualTo(firstInstruction);
        ImageButton leftArrow = (ImageButton) getInstructionView(firstInstruction)
                .findViewById(R.id.left_arrow);
        leftArrow.performClick();
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(firstInstruction);
    }

    @Test
    public void shouldDecreaseDistanceOnAdvanceViaSwipe() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction firstInstruction = getTestInstruction(0, 0);
        firstInstruction.setDistance(5);
        instructions.add(firstInstruction);
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        int expectedDistance = fragment.getRoute().getTotalDistance()
                - firstInstruction.getDistance();
        String expectedFormattedDistance = DistanceFormatter.format(expectedDistance, true);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        DistanceView textView = (DistanceView) view.findViewById(R.id.destination_distance);
        fragment.onPageSelected(1);
        assertThat(textView.getText()).isEqualTo(expectedFormattedDistance);
    }

    @Test
    public void shouldIncreaseDistanceOnRegressViaSwipe() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        int expectedDistance = fragment.getRoute().getTotalDistance();
        String expectedFormattedDistance = DistanceFormatter.format(expectedDistance, true);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        DistanceView textView = (DistanceView) view.findViewById(R.id.destination_distance);
        int current = fragment.pager.getCurrentItem();
        fragment.pager.setCurrentItem(++current);
        fragment.onPageSelected(0);
        assertThat(textView.getText()).isEqualTo(expectedFormattedDistance);
    }

    @Test
    public void onResume_shouldDeactivateActivitiesMapUpdates() throws Exception {
        TestHelper.startFragment(fragment, act);
        assertThat(((MapzenApplication) application).shouldMoveMapToLocation()).isFalse();
    }

    @Test
    public void onResume_shouldSetDefaultRouteZoom() throws Exception {
        TestHelper.startFragment(fragment, act);
        assertThat(mapController.getZoomLevel()).isEqualTo(zoomController.getZoom());
    }

    @Test
    public void onResume_shouldSetDefaultRouteTilt() throws Exception {
        TestHelper.startFragment(fragment, act);
        TestViewport viewport = (TestViewport) act.getMap().viewport();
        assertThat(viewport.getTilt()).isEqualTo(RouteFragment.DEFAULT_ROUTING_TILT);
    }

    @Test
    public void onResume_shouldDisableActionbar() throws Exception {
        TestHelper.startFragment(fragment, act);
        assertThat(act.getActionBar()).isNotShowing();
    }

    @Test
    public void onDestroy_shouldActivateMoveMapToLocation() throws Exception {
        TestHelper.startFragment(fragment, act);
        fragment.onDestroy();
        assertThat(((MapzenApplication) application).shouldMoveMapToLocation()).isTrue();
    }

    @Test
    public void onPause_shouldEndDbTransaction() throws Exception {
        TestHelper.startFragment(fragment, act);
        fragment.onPause();
        assertThat(db.inTransaction()).isFalse();
    }

    @Test
    public void onLocationChange_shouldAdvance() throws Exception {
        loadAceHotelMockRoute();
        fragment.onLocationChanged(fragment.getRoute().getRouteInstructions().get(0).getLocation());
        fragment.onLocationChanged(fragment.getRoute().getRouteInstructions().get(1).getLocation());
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(2);
    }

    @Test
    public void onLocationChange_shouldNotChangeUsersMap() throws Exception {
        loadAceHotelMockRoute();
        simulateUserPagerTouch();
        double lat = fragment.getMapFragment().getMap().getMapPosition().getLatitude();
        double lng = fragment.getMapFragment().getMap().getMapPosition().getLongitude();
        fragment.getRoute().addSeenInstruction(fragment.getRoute().getRouteInstructions().get(0));
        fragment.onLocationChanged(
                fragment.getRoute().getRouteInstructions().get(1).getLocation());
        fragment.onLocationChanged(
                fragment.getRoute().getRouteInstructions().get(2).getLocation());
        assertThat(lat).isEqualTo(
                fragment.getMapFragment().getMap().getMapPosition().getLatitude());
        assertThat(lng).isEqualTo(
                fragment.getMapFragment().getMap().getMapPosition().getLongitude());
    }

    @Test
    public void onLocationChange_shouldChangeUsersMap() throws Exception {
        loadAceHotelMockRoute();
        simulateUserPagerTouch();
        double lat = fragment.getMapFragment().getMap().getMapPosition().getLatitude();
        double lng = fragment.getMapFragment().getMap().getMapPosition().getLongitude();
        fragment.getRoute().addSeenInstruction(
                fragment.getRoute().getRouteInstructions().get(0));
        fragment.onLocationChanged(
                fragment.getRoute().getRouteInstructions().get(1).getLocation());
        fragment.onClickResume();
        fragment.onLocationChanged(
                fragment.getRoute().getRouteInstructions().get(2).getLocation());
        assertThat(lat).isNotEqualTo(
                fragment.getMapFragment().getMap().getMapPosition().getLatitude());
        assertThat(lng).isNotEqualTo(
                fragment.getMapFragment().getMap().getMapPosition().getLongitude());
    }

    @Test
    public void onInstructionComplete_shouldPreservePagerIndex() throws Exception {
        loadAceHotelMockRoute();
        simulateUserPagerTouch();
        fragment.getRoute().addSeenInstruction(fragment.getRoute().getRouteInstructions().get(0));
        fragment.onInstructionComplete(0);
        fragment.onInstructionComplete(1);
        fragment.onInstructionComplete(2);
        fragment.onClickResume();
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(3);
    }

    @Test
    public void onLocationChange_shouldAlwaysMoveRouteLocationIndicator()
            throws Exception {
        loadAceHotelMockRoute();
        RouteLocationIndicator routeLocationIndicator = Mockito.mock(RouteLocationIndicator.class);
        fragment.setRouteLocationIndicator(routeLocationIndicator);
        simulateUserPagerTouch();
        Location loc = fragment.getRoute().getRouteInstructions().get(1).getLocation();
        fragment.onLocationChanged(loc);
        Mockito.verify(routeLocationIndicator).setPosition(loc.getLatitude(), loc.getLongitude());
    }

    @Test
    public void onLocationChange_shouldAlwaysRotateRouteLocationIndicator()
            throws Exception {
        loadAceHotelMockRoute();
        RouteLocationIndicator routeLocationIndicator = Mockito.mock(RouteLocationIndicator.class);
        fragment.setRouteLocationIndicator(routeLocationIndicator);
        simulateUserPagerTouch();
        Location loc = fragment.getRoute().getRouteInstructions().get(1).getLocation();
        fragment.onLocationChanged(loc);
        Mockito.verify(routeLocationIndicator).setRotation(Mockito.anyFloat());
    }

    @Test
    public void onLocationChange_shouldNotAdvanceWhenUserHasPaged() throws Exception {
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        simulateUserPagerTouch();
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(0);
        fragment.onLocationChanged(instructions.get(2).getLocation());
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(0);
    }

    @Test
    public void onLocationChange_shouldAdvanceWhenUserHasResumed() throws Exception {
        loadAceHotelMockRoute();
        simulateUserPagerTouch();
        fragment.onClickResume();
        fragment.onLocationChanged(fragment.getRoute().getRouteInstructions().get(0).getLocation());
        fragment.onLocationChanged(fragment.getRoute().getRouteInstructions().get(1).getLocation());
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(2);
    }

    @Test
    public void onLocationChange_shouldNotCrashIfClosestInstructionsAreExhausted()
            throws Exception {
        fragment.setRoute(new Route(MOCK_ROUTE_JSON));
        TestHelper.startFragment(fragment, act);
        fragment.onResume();
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.onLocationChanged(instructions.get(0).getLocation());
        fragment.onLocationChanged(instructions.get(1).getLocation());
        fragment.onLocationChanged(instructions.get(2).getLocation());
    }

    @Test
    public void onLocationChanged_shouldNotAdvanceWhenDistanceEqualsTurnRadius() throws Exception {
        loadAceHotelMockRoute();
        fragment.pager.setCurrentItem(1);
        fragment.onLocationChanged(getTestLocation(40.743016, -73.987105)); // 50 meters away
        assertThat(fragment.pager).hasCurrentItem(1);
    }

    @Test
    public void distanceToDestination_shouldEqualRouteDistanceAtStart() throws Exception {
        loadAceHotelMockRoute();
        fragment.onLocationChanged(fragment.getRoute().getRouteInstructions().get(0).getLocation());
        int expected = fragment.getRoute().getTotalDistance();
        assertThat(fragment.distanceToDestination).hasText(DistanceFormatter.format(expected));
    }

    @Test
    public void setMapPerspectiveForInstruction_shouldAlignBearing() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(0, 0);
        instructions.add(instruction);
        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        mapController.setMapPerspectiveForInstruction(instruction);
        assertThat(act.getMap().getMapPosition().getBearing()).isEqualTo(
                instruction.getRotationBearing());
    }

    @Test
    public void getAdvanceRadius_shouldHaveDefaultValue() {
        TestHelper.startFragment(fragment, act);
        assertThat(fragment.getAdvanceRadius()).isEqualTo(ZoomController.DEFAULT_TURN_RADIUS);
    }

    @Test
    public void onLocationChanged_finalInstructionShouldNotAdvance() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        fragment.pager.setCurrentItem(1);
        fragment.onLocationChanged(getTestLocation(0, 0));
        assertThat(fragment.pager).hasCurrentItem(1);
    }

    @Test
    public void onApproachInstruction_shouldSpeakTurnInstruction() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();

        Instruction firstInstruction = getTestInstruction(0, 0);
        firstInstruction.setDistance(100);
        instructions.add(firstInstruction);

        Instruction secondInstruction = getTestInstruction(0, 0);
        secondInstruction.setDistance(200);
        instructions.add(secondInstruction);

        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        fragment.onApproachInstruction(0);
        assertLastSpokenText("Head on 19th Street");
    }

    @Test
    public void onApproachInstruction_shouldNotAnnounceYouHaveArrivedEarly() throws Exception {
        loadAceHotelMockRoute();
        fragment.onApproachInstruction(fragment.getRoute().getRouteInstructions().size() - 1);
        assertLastSpokenText(null);
    }

    @Test
    public void onInstructionComplete_shouldSpeakContinueInstruction() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();

        Instruction firstInstruction = getTestInstruction(0, 0);
        firstInstruction.setDistance(100);
        instructions.add(firstInstruction);

        Instruction secondInstruction = getTestInstruction(0, 0);
        secondInstruction.setDistance(200);
        instructions.add(secondInstruction);

        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        fragment.onInstructionComplete(0);
        assertLastSpokenText("Continue on 19th Street for 320 feet");
    }

    @Test
    public void onInstructionComplete_shouldVerifyNextIndexIsWithinBounds() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        fragment.onInstructionComplete(1);
    }

    @Test
    public void onRouteComplete_shouldAdvancePagerToFinalInstruction() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction());
        instructions.add(getTestLastInstruction());
        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        fragment.onRouteComplete();
        assertThat(fragment.pager).hasCurrentItem(1);
    }

    @Test
    public void onRouteComplete_shouldAnnounceFinalInstruction() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction());
        instructions.add(getTestLastInstruction());
        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        fragment.onRouteComplete();
        assertLastSpokenText(getTestLastInstruction().getSimpleInstruction(fragment.getActivity()));
    }

    @Test
    public void onRouteComplete_shouldSetZeroDistanceToDestination() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction());
        instructions.add(getTestLastInstruction());
        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        fragment.onRouteComplete();
        assertThat(fragment.distanceToDestination.getDistance()).isEqualTo(0);
    }

    @Test
    public void onRouteComplete_shouldHideFooter() throws Exception {
        loadAceHotelMockRoute();
        fragment.onRouteComplete();
        assertThat(fragment.getView().findViewById(R.id.footer_wrapper)).isNotVisible();
    }

    @Test
    public void onPageSelected_shouldTurnMap() throws Exception {
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        simulateUserPagerTouch();
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(0);
        fragment.onPageSelected(2);
        Instruction i = instructions.get(2);
        assertThat(act.getMap().getMapPosition().getBearing()).isEqualTo(i.getRotationBearing());
    }

    @Test
    public void onPageSelected_shouldNotTurnMap() throws Exception {
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(0);
        fragment.onPageSelected(2);
        Instruction i = instructions.get(2);
        TestViewport viewport = (TestViewport) act.getMap().viewport();
        assertThat(viewport.getRotation()).isNotEqualTo(i.getRotationBearing());
    }

    @Test
    public void onRecalculate_shouldAnnounceRecalculation() throws Exception {
        initTestFragment();
        TestHelper.startFragment(fragment, act);
        Location testLocation = getTestLocation(111.0, 111.0);
        fragment.onRecalculate(testLocation);
        assertLastSpokenText(act.getString(R.string.recalculating));
    }

    @Test
    public void onRecalculate_shouldUpdateCurrentInstructionText() throws Exception {
        act = initBaseActivityWithMenu(menu);
        initTestFragment();
        loadAceHotelMockRoute();
        fragment.onRecalculate(getTestLocation(111.0, 111.0));
        View view = fragment.getPagerViewForIndex(0);

        TextView before = (TextView) view.findViewById(R.id.full_instruction);
        assertThat(before).hasText(app.getString(R.string.recalculating));

        TextView after = (TextView) view.findViewById(R.id.full_instruction_after_action);
        assertThat(after).hasText(app.getString(R.string.recalculating));
    }

    @Test
    public void onRecalculate_shouldHideIconDistanceAndArrows() throws Exception {
        loadAceHotelMockRoute();
        fragment.onRecalculate(getTestLocation(111.0, 111.0));
        View view = fragment.getPagerViewForIndex(0);
        assertThat(view.findViewById(R.id.left_arrow)).isNotVisible();
        assertThat(view.findViewById(R.id.turn_container)).isNotVisible();
        assertThat(view.findViewById(R.id.right_arrow)).isNotVisible();
    }

    @Test
    public void onRecalculate_shouldHideRouteFooter() throws Exception {
        loadAceHotelMockRoute();
        fragment.onRecalculate(getTestLocation(111.0, 111.0));
        assertThat(fragment.footerWrapper).isNotVisible();
    }

    @Test
    public void onUpdateDistance_shouldShowDistanceToDestinationAfterReroute() throws Exception {
        loadAceHotelMockRoute();
        fragment.onRecalculate(getTestLocation(111.0, 111.0));
        fragment.onUpdateDistance(0, 0);
        assertThat(fragment.distanceToDestination).isVisible();
    }

    @Test
    public void setRoute_shouldShowRouteFooterAfterReroute() throws Exception {
        loadAceHotelMockRoute();
        fragment.onRecalculate(getTestLocation(111.0, 111.0));
        Route route = new Route(MOCK_ACE_HOTEL);
        fragment.setRoute(route);
        Robolectric.runUiThreadTasks();
        assertThat(fragment.footerWrapper).isVisible();
    }

    @Test
    public void turnAutoPageOff_shouldMuteVoiceNavigation() throws Exception {
        initTestFragment();
        TestHelper.startFragment(fragment, act);
        fragment.turnAutoPageOff();
        assertThat(fragment.voiceNavigationController.isMuted()).isTrue();
    }

    @Test
    public void resumeAutoPaging_shouldUnmuteVoiceNavigation() throws Exception {
        initTestFragment();
        TestHelper.startFragment(fragment, act);
        fragment.turnAutoPageOff();
        fragment.resumeAutoPaging();
        assertThat(fragment.voiceNavigationController.isMuted()).isFalse();
    }

    @Test
    public void resumeAutoPaging_shouldSetPerspectiveForCurrentInstruction() throws Exception {
        loadAceHotelMockRoute();
        fragment.turnAutoPageOff();
        fragment.pager.setCurrentItem(1);
        fragment.resumeAutoPaging();
        assertThat(fragment.pager).hasCurrentItem(0);
    }

    @Test
    public void onPageScrolled_shouldNotResumeAutoPagingIfAlreadyOn() throws Exception {
        ViewController viewport = Mockito.mock(ViewController.class);
        ((TestMap) mapController.getMap()).setViewport(viewport);
        loadAceHotelMockRoute();
        fragment.onPageScrolled(0, 0, 0);
        Mockito.verify(viewport, atLeast(2)).setMapPosition(Mockito.any(MapPosition.class));
    }

    @Test
    public void createRouteTo_shouldDisplayProgressDialog() throws Exception {
        TestHelper.startFragment(fragment, act);
        fragment.createRouteTo(getTestLocation(0, 0));
        assertThat(act.getMapFragment().getView().findViewById(R.id.progress)).isVisible();
    }

    @Test
    public void createRouteTo_shouldDismissProgressDialogOnError() throws Exception {
        TestHelper.startFragment(fragment, act);
        fragment.createRouteTo(getTestLocation(0, 0));
        fragment.failure(500);
        assertThat(act.getMapFragment().getView().findViewById(R.id.map)).isVisible();
        assertThat(act.getMapFragment().getView().findViewById(R.id.progress)).isNotVisible();
    }

    @Test
    public void createRouteTo_shouldDismissProgressDialogOnSuccess() throws Exception {
        TestHelper.startFragment(fragment, act);
        fragment.createRouteTo(getTestLocation(0, 0));
        fragment.failure(207);
        assertThat(act.getMapFragment().getView().findViewById(R.id.map)).isVisible();
        assertThat(act.getMapFragment().getView().findViewById(R.id.progress)).isNotVisible();
    }

    @Test
    public void createRouteTo_shouldToastIfNoRouteFound() throws Exception {
        Location testLocation = getTestLocation(100.0, 100.0);
        TestHelper.startFragment(fragment, act);
        fragment.createRouteTo(testLocation);
        fragment.failure(207);
        assertThat(ShadowToast.getTextOfLatestToast())
                .isEqualTo(act.getString(R.string.no_route_found));
    }

    @Test
    public void createRouteTo_shouldAddFragment() throws Exception {
        TestHelper.startFragment(fragment, act);
        fragment.success(new Route(MOCK_NY_TO_VT));
        assertThat(fragment).isAdded();
    }

    @Test
    public void createRouteTo_shouldResetPager() throws Exception {
        TestHelper.startFragment(fragment, act);
        assertThat(fragment).isAdded();
        int previousCount = fragment.pager.getAdapter().getCount();
        assertThat(previousCount).isEqualTo(3);
        Location testLocation = getTestLocation(100.0, 100.0);
        fragment.createRouteTo(testLocation);
        Route newRoute = new Route(MOCK_NY_TO_VT);
        fragment.success(newRoute);
        assertThat(fragment.pager.getAdapter().getCount())
                .isEqualTo(newRoute.getRouteInstructions().size());
    }

    @Test
    public void createRouteTo_shouldRedoUrl() throws Exception {
        MapFragment mapFragmentMock = mock(MapFragment.class, Mockito.CALLS_REAL_METHODS);
        mapFragmentMock.setAct(act);
        fragment.setMapFragment(mapFragmentMock);
        TestHelper.startFragment(fragment, act);
        fragment.createRouteTo(getTestLocation(100.0, 200.0));
        fragment.createRouteTo(getTestLocation(200.0, 300.0));
        assertThat(router.getRouteUrl().toString()).contains("200.0,300.0");
        assertThat(router.getRouteUrl().toString()).doesNotContain("100.0,200.0");
    }

    @Test
    public void createRouteTo_shouldRequestNewRoute() throws Exception {
        Location testLocation = getTestLocation(100.0, 100.0);
        TestHelper.startFragment(fragment, act);
        fragment.createRouteTo(testLocation);
        verify(router).fetch();
    }

    @Test
    public void onLocationChange_shouldDoNothingWhileRerouting() throws Exception {
        Location testLocation = getTestLocation(40.658563, -73.986853);
        RouteFragment spyFragment = spy(fragment);
        spyFragment.setRoute(new Route(MOCK_AROUND_THE_BLOCK));
        TestHelper.startFragment(spyFragment, act);
        spyFragment.onLocationChanged(testLocation);
        spyFragment.onLocationChanged(testLocation);
        verify(spyFragment, Mockito.times(1)).createRouteTo(testLocation);
    }

    @Test
    public void onLocationChange_shouldBeReEnabledOnceReRoutingIsCompleted() throws Exception {
        Location testLocation = getTestLocation(40.658563, -73.986853);
        RouteFragment spyFragment = spy(fragment);
        spyFragment.setRoute(new Route(MOCK_AROUND_THE_BLOCK));
        TestHelper.startFragment(spyFragment, act);
        spyFragment.onLocationChanged(startLocation);
        spyFragment.onLocationChanged(testLocation);
        spyFragment.success(new Route(new JSONObject(MOCK_AROUND_THE_BLOCK)));
        spyFragment.onLocationChanged(startLocation);
        spyFragment.onLocationChanged(testLocation);
        verify(spyFragment, Mockito.times(2)).createRouteTo(testLocation);
    }

    @Test
    public void onLocationChange_shouldBeReEnabledOnceReRoutingHasError() throws Exception {
        Location testLocation = getTestLocation(40.658563, -73.986853);
        RouteFragment spyFragment = spy(fragment);
        spyFragment.setRoute(new Route(MOCK_AROUND_THE_BLOCK));
        TestHelper.startFragment(spyFragment, act);
        spyFragment.onLocationChanged(startLocation);
        spyFragment.onLocationChanged(testLocation);
        spyFragment.failure(500);
        spyFragment.onLocationChanged(testLocation);
        verify(spyFragment, Mockito.times(2)).createRouteTo(testLocation);
    }

    @Test
    public void getGPXDescription_shouldIncludeReturnBeginningAndEnd() throws Exception {
        TestHelper.startFragment(fragment, act);
        testInstructions = new ArrayList<Instruction>();
        testInstructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(testInstructions);
        String actual = fragment.getGPXDescription();
        assertThat(actual).contains("Route between:  19th Street [0.0, 0.0]");
        assertThat(actual).contains("Test SimpleFeature [1.0, 1.0]");
    }

    @Test
    public void getGPXDescription_shouldDisplay() throws Exception {
        String expected = "Route without instructions";
        fragment.setInstructions(new ArrayList<Instruction>());
        String actual = fragment.getGPXDescription();
        assertThat(actual).contains(expected);
    }

    @Test
    public void storeRouteInDatabase_shouldCreateRoute() throws Exception {
        TestHelper.startFragment(fragment, act);
        fragment.storeRouteInDatabase(new JSONObject());
        Cursor cursor = db.query(TABLE_ROUTES, new String[] { COLUMN_TABLE_ID },
                COLUMN_TABLE_ID + " = ?",
                new String[] { fragment.getRouteId() } , null, null, null);
        assertThat(cursor).hasCount(1);
    }

    @Test
    public void newInstance_shouldCreateGroupId() throws Exception {
        assertThat(fragment.groupId).isNotNull();
    }

    @Test
    public void setRoute_shouldCreateRouteGroupEntry() throws Exception {
        TestHelper.startFragment(fragment, act);
        fragment.setRoute(new Route(MOCK_ROUTE_JSON));
        Cursor cursor = db.query(TABLE_ROUTE_GROUP, null,
                COLUMN_ROUTE_ID + " = ? AND " + COLUMN_GROUP_ID + " = ?",
                new String[] { fragment.getRouteId(), fragment.groupId }, null, null, null);
        assertThat(cursor).hasCount(1);
    }

    @Test
    public void storeRouteInDatabase_shouldSendExceptionToBugSense() throws Exception {
        db.close();
        fragment.storeRouteInDatabase(new JSONObject());
        assertThat(ShadowMint.getLastHandledException())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void addCoordinatesToDatabase_shouldSendExceptionToBugSense() throws Exception {
        initTestFragment();
        TestHelper.startFragment(fragment, act);
        db.close();
        fragment.success(new Route(MOCK_ROUTE_JSON));
        assertThat(ShadowMint.getLastHandledException())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldInitDynamicZoomUsingDefaultValues() throws Exception {
        Resources res = act.getResources();
        TestHelper.startFragment(fragment, act);
        Location location = fragment.getRoute().getRouteInstructions().get(0).getLocation();
        setNumberOfLocationForAverageSpeed(1);

        assertZoomLevel(res.getInteger(R.integer.zoom_driving_0to15), 10, location);
        assertZoomLevel(res.getInteger(R.integer.zoom_driving_15to25), 20, location);
        assertZoomLevel(res.getInteger(R.integer.zoom_driving_25to35), 30, location);
        assertZoomLevel(res.getInteger(R.integer.zoom_driving_35to50), 40, location);
        assertZoomLevel(res.getInteger(R.integer.zoom_driving_over50), 50, location);
    }

    @Test
    public void shouldUpdateDynamicZoomWithNewValues() throws Exception {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        SharedPreferences.Editor editPrefs = prefs.edit();

        editPrefs.putInt(act.getString(R.string.settings_zoom_driving_0to15_key), 14);
        editPrefs.putInt(act.getString(R.string.settings_zoom_driving_15to25_key), 13);
        editPrefs.putInt(act.getString(R.string.settings_zoom_driving_25to35_key), 12);
        editPrefs.putInt(act.getString(R.string.settings_zoom_driving_35to50_key), 11);
        editPrefs.putInt(act.getString(R.string.settings_zoom_driving_over50_key), 10);

        editPrefs.commit();

        setNumberOfLocationForAverageSpeed(1);
        TestHelper.startFragment(fragment, act);
        Location location = fragment.getRoute().getRouteInstructions().get(0).getLocation();

        assertZoomLevel(14, 10, location);
        assertZoomLevel(13, 20, location);
        assertZoomLevel(12, 30, location);
        assertZoomLevel(11, 40, location);
        assertZoomLevel(10, 50, location);
    }

    @Test
    public void debugViewShouldBeHidden() throws Exception {
        TestHelper.startFragment(fragment, act);
        assertThat(fragment.getView().findViewById(R.id.debugging)).isNotVisible();
    }

    @Test
    public void debugViewShouldBeVisible() throws Exception {
        enableDebugMode(Robolectric.application);
        TestHelper.startFragment(fragment, act);
        assertThat(fragment.getView().findViewById(R.id.debugging)).isVisible();
    }

    @Test @SuppressLint("NewApi")
    public void shouldGenerateNotificationOnFirstInstruction() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(3, 3);
        instructions.add(instruction);
        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);

        ShadowNotification sNotification = getRoutingNotification();
        assertThat(sNotification.getContentTitle()).isEqualTo("Test SimpleFeature");
        assertThat(sNotification.getContentText()).isEqualTo("Head on 19th Street for 520 ft");
        assertThat(sNotification.getActions().get(0).title).isEqualTo("Exit Navigation");
    }

    @Test @SuppressLint("NewApi")
    public void shouldGenerateNotificationOnPageSelected() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(3, 3);
        instructions.add(instruction);
        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        fragment.onPageSelected(0);

        ShadowNotification sNotification = getRoutingNotification();
        assertThat(sNotification.getContentTitle()).isEqualTo("Test SimpleFeature");
        assertThat(sNotification.getContentText()).isEqualTo("Head on 19th Street for 520 ft");
        assertThat(sNotification.getActions().get(0).title).isEqualTo("Exit Navigation");
    }

    @Test @SuppressLint("NewApi")
    public void shouldKillNotificationOnExitNavigation() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(3, 3);
        instructions.add(instruction);
        fragment.setInstructions(instructions);
        TestHelper.startFragment(fragment, act);
        fragment.onPageSelected(0);

        ShadowNotification sNotification = getRoutingNotification();
        sNotification.getActions().get(0).actionIntent.send();

        ShadowApplication application = shadowOf(act.getApplication());
        Intent broadcastIntent = application.getBroadcastIntents().get(0);
        String broadcastClassName = broadcastIntent.getComponent().getClassName();
        boolean shouldExit = broadcastIntent.getExtras()
                .getBoolean(MapzenNotificationCreator.EXIT_NAVIGATION);
        assertThat(shouldExit).isTrue();
        assertThat(broadcastClassName)
                .isEqualTo("com.mapzen.open.util.NotificationBroadcastReceiver");
    }

    @Test
    public void getAverageSpeed_shouldDefaultToZero() throws Exception {
        TestHelper.startFragment(fragment, act);
        assertThat(fragment.getAverageSpeed()).isEqualTo(0);
    }

    @Test
    public void getAverageSpeed_shouldReturnAverageOfLastNLocationUpdates() throws Exception {
        initTestFragment();
        TestHelper.startFragment(fragment, act);
        TestHelper.TestLocation.Builder locationBuilder =
                new TestHelper.TestLocation.Builder(fragment.getRoute().getGeometry().get(2));
        float expectedSpeed = 10.0f;
        locationBuilder.setSpeed(200.0f);
        fragment.onLocationChanged(locationBuilder.build());
        locationBuilder.setSpeed(expectedSpeed);
        setNumberOfLocationForAverageSpeed(10);
        for (int i = 0; i < fragment.getNumberOfLocationsForAverageSpeed(); i++) {
            fragment.onLocationChanged(locationBuilder.build());
        }
        assertThat(fragment.getAverageSpeed()).isEqualTo(expectedSpeed);
    }

    @Ignore
    @Test
    public void shouldUseGpxTraceWhenMockModeEnabled() throws Exception {
        loadTestGpxTrace();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        prefs.edit().putBoolean(act.getString(R.string.settings_mock_gpx_key), true).commit();
        fragment = RouteFragment.newInstance(act, getTestSimpleFeature());
        fragment.setRoute(new Route(MOCK_ROUTE_JSON));
        LocationUpdateSubscriber locationUpdateSubscriber = new LocationUpdateSubscriber();
        bus.register(locationUpdateSubscriber);
        act = initBaseActivityWithMenu(menu);
        TestHelper.startFragment(fragment, act);
        Thread.sleep(300);
        Robolectric.runUiThreadTasks();
        assertThat(locationUpdateSubscriber.getEvent().getLocation()).hasLatitude(0.0);
        assertThat(locationUpdateSubscriber.getEvent().getLocation()).hasLongitude(0.1);
    }

    @Test
    public void onDestroy_shouldDisableMockMode() throws Exception {
        loadTestGpxTrace();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        prefs.edit().putBoolean(act.getString(R.string.settings_mock_gpx_key), true).commit();
        initTestFragment();
        TestHelper.startFragment(fragment, act);
        fragment.onDestroy();
        assertThat(((TestFusedLocationProviderApiImpl) LocationServices.FusedLocationApi).mockMode)
                .isFalse();
    }

    @Test
    public void getGpxDescription_shouldReturnErrorMessageForNullInstructions() throws Exception {
        loadAceHotelMockRoute();
        fragment.setInstructions(null);
        assertThat(fragment.getGPXDescription()).isEqualTo("Route without instructions");
    }

    private void loadAceHotelMockRoute() {
        fragment.success(new Route(MOCK_ACE_HOTEL));
        FragmentTestUtil.startFragment(fragment);
        fragment.onResume();
    }

    private void loadTestGpxTrace() throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/lost.gpx"));
        String contents = new String(encoded, "UTF-8");

        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
        File directory = Environment.getExternalStorageDirectory();
        File file = new File(directory, "lost.gpx");
        FileWriter fileWriter = new FileWriter(file, false);
        fileWriter.write(contents);
        fileWriter.close();
    }

    private void assertZoomLevel(int expected, float milesPerHour, Location location) {
        location.setSpeed(ZoomController.milesPerHourToMetersPerSecond(milesPerHour));
        location.setTime(System.currentTimeMillis());
        fragment.onLocationChanged(location);
        assertThat(mapController.getZoomLevel()).isEqualTo(expected);
    }

    private void initTestFragment() throws Exception {
        fragment = RouteFragment.newInstance(act, getTestSimpleFeature());
        fragment.setRoute(new Route(MOCK_ROUTE_JSON));

        testInstructions = new ArrayList<Instruction>();
        testInstructions.add(getTestInstruction(0, 0));
        testInstructions.add(getTestInstruction(1, 1));
        testInstructions.add(getTestInstruction(2, 2));
        fragment.setInstructions(testInstructions);
        setTestLocationClient();
    }

    private ShadowNotification getRoutingNotification() {
        NotificationManager manager = (NotificationManager) act.getSystemService(
                Context.NOTIFICATION_SERVICE);
        ShadowNotificationManager sManager = shadowOf(manager);
        return shadowOf(sManager.getAllNotifications().get(0));
    }

    private void setNumberOfLocationForAverageSpeed(int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(
                act.getString(R.string.settings_number_of_locations_for_average_speed_key), value);
        prefEditor.commit();
    }

    private void simulateUserPagerTouch() {
        MotionEvent motionEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 12f, 34f, 0);
        View.OnTouchListener listener = shadowOf(fragment.pager).getOnTouchListener();
        listener.onTouch(null, motionEvent);
    }

    private void simulateUserDrag() {
        MotionEvent e = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_MOVE, 120f, 100f, 0);
        ShadowView view = shadowOf(act.findViewById(R.id.map));
        fragment.setCurrentXCor(3.0f);
        view.getOnTouchListener().onTouch(null, e);
    }

    private void simulateTwoFingerDrag() {
        MotionEvent e = mock(MotionEvent.class);
        Mockito.when(e.getPointerCount()).thenReturn(2);
        Mockito.when(e.getAction()).thenReturn(MotionEvent.ACTION_MOVE);
        ShadowView view = shadowOf(act.findViewById(R.id.map));
        view.getOnTouchListener().onTouch(null, e);
    }

    private void simulatePaneOpenSlide() {
        fragment.getPanelSlideListener().onPanelSlide(fragment.getSlideLayout(), 0.95f);
    }

    private void simulatePaneCloseSlide() {
        fragment.getPanelSlideListener().onPanelSlide(fragment.getSlideLayout(), 1.0f);
    }

    private View getInstructionView(int position) {
        ViewGroup group = new ViewGroup(act) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
            }
        };
        return (View) fragment.pager.getAdapter().instantiateItem(group, position);
    }

    private void assertLastSpokenText(String expected) {
        TextToSpeech tts = fragment.voiceNavigationController.getTextToSpeech();
        ShadowTextToSpeech shadowTts = shadowOf_(tts);
        shadowTts.getOnInitListener().onInit(TextToSpeech.SUCCESS);
        assertThat(shadowTts.getLastSpokenText()).isEqualTo(expected);
    }

    private void setTestLocationClient() {
        LocationServices.FusedLocationApi = new TestFusedLocationProviderApiImpl(application);
    }

    class TestFusedLocationProviderApiImpl extends FusedLocationProviderApiImpl {
        private boolean mockMode = false;

        public TestFusedLocationProviderApiImpl(Context context) {
            super(context);
        }

        @Override
        public void setMockMode(boolean isMockMode) {
            super.setMockMode(isMockMode);
            mockMode = isMockMode;
        }

        @Override
        public void setMockTrace(File file) {
            // Prevents spawning thread to replay mock locations
        }
    }
}
