package com.mapzen.route;

import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.TestMapzenApplication;
import com.mapzen.activity.BaseActivity;
import com.mapzen.android.lost.LocationClient;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.fragment.MapFragment;
import com.mapzen.helpers.DistanceFormatter;
import com.mapzen.helpers.ZoomController;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;
import com.mapzen.osrm.Router;
import com.mapzen.shadows.ShadowBugSenseHandler;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestBaseActivity;
import com.mapzen.support.TestHelper;
import com.mapzen.util.DatabaseHelper;
import com.mapzen.util.MapzenNotificationCreator;
import com.mapzen.util.RouteLocationIndicator;
import com.mapzen.widget.DistanceView;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.oscim.core.GeoPoint;
import org.oscim.layers.PathLayer;
import org.oscim.map.TestMap;
import org.oscim.map.TestViewport;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.shadows.ShadowNotification;
import org.robolectric.shadows.ShadowNotificationManager;
import org.robolectric.shadows.ShadowTextToSpeech;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.shadows.ShadowView;
import org.robolectric.tester.android.view.TestMenu;
import org.robolectric.util.FragmentTestUtil;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
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
import java.util.List;

import javax.inject.Inject;

import static com.mapzen.MapController.KEY_STORED_MAPPOSITION;
import static com.mapzen.MapController.getMapController;
import static com.mapzen.activity.BaseActivity.COM_MAPZEN_UPDATES_LOCATION;
import static com.mapzen.entity.SimpleFeature.NAME;
import static com.mapzen.helpers.DistanceFormatter.METERS_IN_ONE_FOOT;
import static com.mapzen.helpers.DistanceFormatter.METERS_IN_ONE_MILE;
import static com.mapzen.support.TestHelper.MOCK_AROUND_THE_BLOCK;
import static com.mapzen.support.TestHelper.MOCK_NY_TO_VT;
import static com.mapzen.support.TestHelper.MOCK_ROUTE_JSON;
import static com.mapzen.support.TestHelper.enableDebugMode;
import static com.mapzen.support.TestHelper.getTestInstruction;
import static com.mapzen.support.TestHelper.getTestLocation;
import static com.mapzen.support.TestHelper.getTestSimpleFeature;
import static com.mapzen.support.TestHelper.initBaseActivityWithMenu;
import static com.mapzen.support.TestHelper.initMapFragment;
import static com.mapzen.util.DatabaseHelper.COLUMN_ROUTE_ID;
import static com.mapzen.util.DatabaseHelper.TABLE_ROUTE_GEOMETRY;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.getShadowApplication;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.Robolectric.shadowOf_;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class RouteFragmentTest {
    @Inject
    LocationClient locationClient;
    TestBaseActivity act;
    RouteFragment fragment;
    ShadowApplication app;
    TestMenu menu;
    ArrayList<Instruction> testInstructions;
    Router router = spy(Router.getRouter());
    SQLiteDatabase db;

    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<Router.Callback> callback;

    @Inject
    PathLayer path;

    @Before
    public void setUp() throws Exception {
        ((TestMapzenApplication) Robolectric.application).inject(this);
        SharedPreferences.Editor editor = application.getSharedPreferences(KEY_STORED_MAPPOSITION,
                Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
        MockitoAnnotations.initMocks(this);
        RouteFragment.setRouter(router);
        menu = new TestMenu();
        act = initBaseActivityWithMenu(menu);
        initTestFragment();
        app = Robolectric.getShadowApplication();
        db = ((MapzenApplication) Robolectric.application).getDb();
        setVoiceNavigationEnabled(true);
    }

    @After
    public void tearDown() throws Exception {
        db.close();
        act.onPause();
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
    public void shouldHideActionBar() throws Exception {
        FragmentTestUtil.startFragment(fragment);
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
        FragmentTestUtil.startFragment(fragment);
        View view = fragment.getView();
        assertThat(view).isNotNull();
    }

    @Test
    public void shouldHaveRoutesViewPager() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment.pager).isNotNull();
    }

    @Test
    public void locateButtonShouldNotBeVisible() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        assertThat(act.findViewById(R.id.locate_button)).isNotVisible();
    }

    @Test
    public void onDetach_locateButtonShouldBeVisible() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        fragment.onDetach();
        assertThat(act.findViewById(R.id.locate_button)).isVisible();
    }

    @Test
    public void onLocationChange_shouldStoreOriginalLocationRecordInDatabase() throws Exception {
        initTestFragment();
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Location sample1 = fragment.getRoute().getGeometry().get(0);
        Location sample2 = fragment.getRoute().getGeometry().get(1);
        Location expected = fragment.getRoute().getGeometry().get(2);
        instructions.add(getTestInstruction(sample1.getLatitude(), sample1.getLongitude()));
        instructions.add(getTestInstruction(sample2.getLatitude(), sample2.getLongitude()));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        fragment.onLocationChanged(expected);
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] { DatabaseHelper.COLUMN_LAT, DatabaseHelper.COLUMN_LNG },
                null, null, null, null, null);
        assertThat(cursor).hasCount(1);
        cursor.moveToNext();
        assertThat(cursor.getString(0)).isEqualTo(String.valueOf(expected.getLatitude()));
        assertThat(cursor.getString(1)).isEqualTo(String.valueOf(expected.getLongitude()));
    }

    @Test
    public void onLocationChange_shouldStoreCorrectedLocationRecordInDatabase() throws Exception {
        initTestFragment();
        FragmentTestUtil.startFragment(fragment);
        Location testLocation = fragment.getRoute().getGeometry().get(2);
        fragment.onLocationChanged(testLocation);
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] {
                        DatabaseHelper.COLUMN_CORRECTED_LAT,
                        DatabaseHelper.COLUMN_CORRECTED_LNG
                },
                null, null, null, null, null);
        assertThat(cursor).hasCount(1);
        cursor.moveToNext();
        assertThat(cursor.getString(0)).isNotNull();
        assertThat(cursor.getString(1)).isNotNull();
    }

    @Test
    public void onMapSwipe_ShouldDisplayResumeButton() throws Exception {
        initTestFragment();
        FragmentTestUtil.startFragment(fragment);
        simulateUserDrag();
        assertThat(fragment.getView().findViewById(R.id.resume_button)).isVisible();
    }

    @Test
    public void onMapTwoFingerScroll_ShouldNotDisplayResumeButton() throws Exception {
        initTestFragment();
        FragmentTestUtil.startFragment(fragment);
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

        FragmentTestUtil.startFragment(fragment);
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
        FragmentTestUtil.startFragment(fragment);
        fragment.createRouteTo(getTestLocation(100.0, 100.0));
        verify(router).setCallback(callback.capture());
        callback.getValue().success(new Route(MOCK_ROUTE_JSON));
        fragment.onPause();
        Cursor cursor = db.query(TABLE_ROUTE_GEOMETRY,
                new String[] { COLUMN_ROUTE_ID },
                COLUMN_ROUTE_ID + " = ?",
                new String[] { String.valueOf(fragment.getRouteId()) }, null, null, null);
        assertThat(cursor).hasCount(fragment.getRoute().getGeometry().size());
    }

    @Test
    public void drawRoute_shouldNotStoreCoordinates() throws Exception {
        FragmentTestUtil.startFragment(fragment);
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
        FragmentTestUtil.startFragment(fragment);
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
        FragmentTestUtil.startFragment(fragment);
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
    public void onLocationChange_shouldNotStoreDatabaseRecord() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(99.0, 89.0));
        instructions.add(getTestInstruction(0, 0));

        FragmentTestUtil.startFragment(fragment);
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
        FragmentTestUtil.startFragment(fragment);
        Location testLocation = fragment.getRoute().getGeometry().get(2);
        fragment.onLocationChanged(testLocation);
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] { COLUMN_ROUTE_ID },
                COLUMN_ROUTE_ID + " = ?",
                new String[] { String.valueOf(fragment.getRouteId()) }, null, null, null);
        assertThat(cursor).hasCount(1);
    }

    @Test
    public void onLocationChange_shouldReRouteWhenLost() throws Exception {
        initTestFragment();
        FragmentTestUtil.startFragment(fragment);

        Route oldRoute = fragment.getRoute();
        Location testLocation = getTestLocation(111.0, 111.0);
        fragment.onLocationChanged(testLocation);
        verify(router).setCallback(callback.capture());
        callback.getValue().success(new Route(MOCK_NY_TO_VT));
        assertThat(fragment.getRoute()).isNotSameAs(oldRoute);
    }

    @Test
    public void onCreate_shouldHideLocationMarker() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment.getMapFragment().getMap().layers().
                contains(fragment.getMapFragment().getLocationMarkerLayer())).isFalse();
    }

    @Test
    public void onDetach_shouldShowLocationMarker() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        fragment.onDetach();
        assertThat(fragment.getMapFragment().getMap().layers().
                contains(fragment.getMapFragment().getLocationMarkerLayer())).isTrue();
    }

    @Test
    public void onBack_shouldActAsResumeButton() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        simulateUserPagerTouch();
        ImageButton resume = (ImageButton) fragment.getView().findViewById(R.id.resume_button);
        assertThat(resume).isVisible();
        fragment.onBackAction();
        assertThat(resume).isNotVisible();
    }

    @Test
    public void onCreate_shouldShowRouteLocationIndicator() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment.getMapFragment().getMap().layers().
                contains(fragment.getRouteLocationIndicator())).isTrue();
    }

    @Test
    public void onDetach_shouldHideRouteLocationIndicator() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        fragment.onDetach();
        assertThat(fragment.getMapFragment().getMap().layers().
                contains(fragment.getRouteLocationIndicator())).isFalse();
    }

    @Test
    public void onDetach_shouldRefreshRoutePreview() throws Exception {
        BaseActivity baseActivityMock = spy(act);
        fragment.setAct(baseActivityMock);
        FragmentTestUtil.startFragment(fragment);
        fragment.onDetach();
        verify(baseActivityMock).updateView();
    }

    @Test
    public void shouldHaveRouteLocationIndicator() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment.getRouteLocationIndicator()).isNotNull();
    }

    @Test
    public void onCreate_shouldSetRouteLocationIndicatorToStartingCoordinates() throws Exception {
        RouteLocationIndicator mockLocationIndicator = mock(RouteLocationIndicator.class);
        fragment.setRouteLocationIndicator(mockLocationIndicator);
        FragmentTestUtil.startFragment(fragment);
        verify(mockLocationIndicator).setPosition(
                fragment.getRoute().getStartCoordinates().getLatitude(),
                fragment.getRoute().getStartCoordinates().getLongitude());
    }

    @Test
    public void onCreate_shouldSetRouteLocationIndicatorToStartingBearing() throws Exception {
        RouteLocationIndicator mockLocationIndicator = mock(RouteLocationIndicator.class);
        fragment.setRouteLocationIndicator(mockLocationIndicator);
        FragmentTestUtil.startFragment(fragment);
        verify(mockLocationIndicator).setRotation(
                (float) fragment.getRoute().getCurrentRotationBearing());
    }

    @Test
    public void shouldRegisterReceiver() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        assertThat(app.hasReceiverForIntent(new Intent(COM_MAPZEN_UPDATES_LOCATION))).isTrue();
    }

    @Test
    public void shouldUnRegisterReceiver() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        int numberOfReceivers =
                app.getReceiversForIntent(new Intent(COM_MAPZEN_UPDATES_LOCATION)).size();
        fragment.onPause();
        assertThat(app.getReceiversForIntent(
                new Intent(COM_MAPZEN_UPDATES_LOCATION))).hasSize(numberOfReceivers - 1);
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
        FragmentTestUtil.startFragment(fragment);
        SimpleFeature simpleFeature = getTestSimpleFeature();
        TextView view = (TextView) fragment.getView().findViewById(R.id.destination_name);
        assertThat(view.getText()).isEqualTo(act
                .getString(R.string.routing_to_text) + simpleFeature.getProperty(NAME));
        assertThat(view).hasEllipsize(TextUtils.TruncateAt.END);
        assertThat(view).hasMaxLines(1);
    }

    @Test
    public void onCreateView_shouldHaveTotalDistance() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        act.showProgressDialog();
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        DistanceView textView = (DistanceView) view.findViewById(R.id.destination_distance);
        int distance = fragment.getRoute().getTotalDistance();
        String expectedFormattedDistance = DistanceFormatter.format(distance, true);
        assertThat(textView.getText()).isEqualTo(expectedFormattedDistance);
    }

    @Test
    public void onCreateView_shouldNotShowResumeButton() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        ImageButton resume = (ImageButton) view.findViewById(R.id.resume_button);
        assertThat(resume).isNotVisible();
    }

    @Test
    public void onTouch_shouldDisplayResumeButton() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        ImageButton resume = (ImageButton) view.findViewById(R.id.resume_button);
        simulateUserPagerTouch();
        assertThat(resume).isVisible();
    }

    @Test
    public void onTouch_shouldStoreCurrentItemWhenPagerWasFirstTouched() throws Exception {
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        route.addSeenInstruction(instructions.get(0));
        route.addSeenInstruction(instructions.get(1));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        fragment.onLocationChanged(instructions.get(2).getLocation());
        simulateUserPagerTouch();
        fragment.pager.setCurrentItem(0);
        simulateUserPagerTouch();
        ImageButton resume = (ImageButton) fragment.getView().findViewById(R.id.resume_button);
        resume.performClick();
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(2);
    }

    @Test
    public void onClickResume_shouldHideResumeButton() throws Exception {
        FragmentTestUtil.startFragment(fragment);
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
        route.addSeenInstruction(instructions.get(0));
        route.addSeenInstruction(instructions.get(1));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        fragment.onLocationChanged(instructions.get(2).getLocation());
        simulateUserPagerTouch();
        fragment.pager.setCurrentItem(0);
        ImageButton resume = (ImageButton) fragment.getView().findViewById(R.id.resume_button);
        resume.performClick();
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(2);
    }

    @Test
    public void expandedPane_shouldShowDirectionListFragment() {
        FragmentTestUtil.startFragment(fragment);
        simulatePaneOpenSlide();
        assertThat(fragment.getChildFragmentManager()).
                hasFragmentWithTag(DirectionListFragment.TAG);
    }

    @Test
    public void collapsedPane_shouldNotShowDirectionListFragment() {
        FragmentTestUtil.startFragment(fragment);
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
        route.addSeenInstruction(instructions.get(0));
        route.addSeenInstruction(instructions.get(1));
        FragmentTestUtil.startFragment(fragment);
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
        FragmentTestUtil.startFragment(fragment);
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
        FragmentTestUtil.startFragment(fragment);
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
        FragmentTestUtil.startFragment(fragment);
        assertThat(((MapzenApplication) application).shouldMoveMapToLocation()).isFalse();
    }

    @Test
    public void onResume_shouldDisableActionbar() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        assertThat(act.getActionBar()).isNotShowing();
    }

    @Test
    public void onPause_shouldActivateActivitiesMapUpdates() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        fragment.onPause();
        FragmentTestUtil.startFragment(fragment);
        assertThat(((MapzenApplication) application).shouldMoveMapToLocation()).isTrue();
    }

    @Test
    public void onPause_shouldEndDbTransaction() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        fragment.onPause();
        assertThat(act.getDb().inTransaction()).isFalse();
    }

    @Test
    public void onLocationChange_shouldAdvance() throws Exception {
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.setInstructions(instructions);
        route.addSeenInstruction(instructions.get(0));
        route.addSeenInstruction(instructions.get(1));
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(0);
        fragment.onLocationChanged(instructions.get(2).getLocation());
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(2);
    }

    @Test
    public void onLocationChange_shouldNotAdvanceWhenUserHasPaged() throws Exception {
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        simulateUserPagerTouch();
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(0);
        fragment.onLocationChanged(instructions.get(2).getLocation());
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(0);
    }

    @Test
    public void onLocationChange_shouldAdvanceWhenUserHasResumed() throws Exception {
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        route.addSeenInstruction(instructions.get(0));
        route.addSeenInstruction(instructions.get(1));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        simulateUserPagerTouch();
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(0);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        view.findViewById(R.id.resume_button).performClick();
        fragment.onLocationChanged(instructions.get(2).getLocation());
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(2);
    }

    @Test
    public void onLocationChange_shouldNotAdvance() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(0);
        fragment.onLocationChanged(getTestLocation(1, 0));
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(0);
    }

    @Test
    public void onLocationChange_shouldFlipToPostInstructionLanguage() throws Exception {
        fragment.setRoute(new Route(MOCK_ROUTE_JSON));
        FragmentTestUtil.startFragment(fragment);
        fragment.onResume();
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.onLocationChanged(instructions.get(0).getLocation());
        fragment.onLocationChanged(instructions.get(1).getLocation());
        fragment.onLocationChanged(instructions.get(1).getLocation());
        fragment.onLocationChanged(instructions.get(2).getLocation());
        assertThat(fragment.getFlippedInstructions().contains(instructions.get(0))).isTrue();
        assertThat(fragment.getFlippedInstructions().contains(instructions.get(1))).isTrue();
        assertThat(fragment.getFlippedInstructions().contains(instructions.get(2))).isFalse();
    }

    @Test
    public void onLocationChange_shouldNotCrashIfClosestInstructionsAreExhausted()
            throws Exception {
        fragment.setRoute(new Route(MOCK_ROUTE_JSON));
        FragmentTestUtil.startFragment(fragment);
        fragment.onResume();
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        for (Instruction instruction : instructions) {
            route.addSeenInstruction(instruction);
        }
        fragment.onLocationChanged(instructions.get(0).getLocation());
        fragment.onLocationChanged(instructions.get(1).getLocation());
        fragment.onLocationChanged(instructions.get(2).getLocation());
    }

    @Test
    public void onLocationChange_shouldUpdateDistanceAppendedToInstruction() throws Exception {
        loadMockAroundTheBlock();
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.onLocationChanged(instructions.get(0).getLocation());
        Location midPoint = getTestLocation(40.660278, -73.988611);
        fragment.onLocationChanged(midPoint);

        View view = fragment.pager.findViewWithTag("Instruction_0");
        String expectedInstructionDistance = DistanceFormatter.format(instructions.get(0)
                .getRemainingDistance(midPoint));
        TextView instructionText = (TextView) view.findViewById(R.id.full_instruction_after_action);
        assertThat(instructionText).containsText(expectedInstructionDistance);
    }

    @Test
    public void onLocationChanged_shouldUpdateDistanceBelowTurnIcon() throws Exception {
        loadMockAroundTheBlock();
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.onLocationChanged(instructions.get(0).getLocation());
        Location midPoint = getTestLocation(40.660278, -73.988611);
        fragment.onLocationChanged(midPoint);

        View view = fragment.pager.findViewWithTag("Instruction_0");
        String expectedInstructionDistance = DistanceFormatter.format(instructions.get(0)
                .getRemainingDistance(midPoint));
        TextView distanceText = (TextView) view.findViewById(R.id.distance_instruction);
        assertThat(distanceText).hasText(expectedInstructionDistance);
    }

    @Test
    public void onLocationChanged_shouldUpdateDistanceToDestination() throws Exception {
        loadMockAroundTheBlock();
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.onLocationChanged(instructions.get(0).getLocation());
        Location midPoint = getTestLocation(40.660278, -73.988611);
        fragment.onLocationChanged(midPoint);

        int expectedDistanceToDestination = route.getTotalDistance()
                - instructions.get(0).getDistance()
                + instructions.get(0).getRemainingDistance(midPoint);
        assertThat(fragment.distanceToDestination.getText())
                .isEqualTo(DistanceFormatter.format(expectedDistanceToDestination));
    }

    @Test
    public void onLocationChange_shouldNotFlipToPostInstructionLanguage() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        fragment.onResume();
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        assertThat(fragment.getFlippedInstructions().contains(instructions.get(0))).isFalse();
        assertThat(fragment.getFlippedInstructions().contains(instructions.get(1))).isFalse();
        assertThat(fragment.getFlippedInstructions().contains(instructions.get(2))).isFalse();
    }

    @Test
    public void setMapPerspectiveForInstruction_shouldAlignBearing() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(0, 0);
        instructions.add(instruction);
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        getMapController().setMapPerspectiveForInstruction(instruction);
        TestMap map = (TestMap) act.getMapFragment().getMap();
        assertThat(act.getMap().getMapPosition().getBearing()).isEqualTo(
                instruction.getRotationBearing());
    }

    @Test
    public void getAdvanceRadius_shouldHaveDefaultValue() {
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment.getAdvanceRadius()).isEqualTo(ZoomController.DEFAULT_TURN_RADIUS);
    }

    @Test
    public void getAdvanceRadius_shouldBeConfigurable() {
        setAdvanceRadiusPreference(R.string.settings_turn_driving_0to15_key, 100);
        setAdvanceRadiusPreference(R.string.settings_turn_driving_15to25_key, 200);
        setAdvanceRadiusPreference(R.string.settings_turn_driving_25to35_key, 300);
        setAdvanceRadiusPreference(R.string.settings_turn_driving_35to50_key, 400);
        setAdvanceRadiusPreference(R.string.settings_turn_driving_over50_key, 500);
        setNumberOfLocationForAverageSpeed(1);

        FragmentTestUtil.startFragment(fragment);
        Location location = fragment.getRoute().getRouteInstructions().get(0).getLocation();

        assertAdvanceRadius(100, 10, location);
        assertAdvanceRadius(200, 20, location);
        assertAdvanceRadius(300, 30, location);
        assertAdvanceRadius(400, 40, location);
        assertAdvanceRadius(500, 50, location);
    }

    @Test
    public void onLocationChanged_finalInstructionShouldNotAdvance() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        fragment.pager.setCurrentItem(1);
        fragment.onLocationChanged(getTestLocation(0, 0));
        assertThat(fragment.pager).hasCurrentItem(1);
    }

    @Test
    public void onCreateView_shouldSpeakFirstInstruction() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(3, 3);
        instructions.add(instruction);

        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        ShadowTextToSpeech shadowTextToSpeech = shadowOf_(fragment.speakerbox.getTextToSpeech());
        shadowTextToSpeech.getOnInitListener().onInit(TextToSpeech.SUCCESS);
        assertThat(shadowTextToSpeech.getLastSpokenText())
                .isEqualTo("Head on 19th Street for 520 feet");
    }

    @Test
    public void onPageSelected_shouldSpeakInstruction() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();

        Instruction firstInstruction = getTestInstruction(0, 0);
        firstInstruction.setDistance(100);
        instructions.add(firstInstruction);

        Instruction secondInstruction = getTestInstruction(0, 0);
        secondInstruction.setDistance(200);
        instructions.add(secondInstruction);

        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        fragment.onPageSelected(1);
        ShadowTextToSpeech shadowTextToSpeech = shadowOf_(fragment.speakerbox.getTextToSpeech());
        shadowTextToSpeech.getOnInitListener().onInit(TextToSpeech.SUCCESS);
        assertThat(shadowTextToSpeech.getLastSpokenText())
                .isEqualTo("Head on 19th Street for 0.1 miles");
    }

    @Test
    public void onPageSelected_shouldTurnMap() throws Exception {
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.setInstructions(instructions);
        route.addSeenInstruction(instructions.get(0));
        route.addSeenInstruction(instructions.get(1));
        FragmentTestUtil.startFragment(fragment);
        simulateUserPagerTouch();
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(0);
        fragment.onPageSelected(2);
        Instruction i = instructions.get(2);
        TestViewport viewport = (TestViewport) act.getMap().viewport();
        assertThat(act.getMap().getMapPosition().getBearing()).isEqualTo(i.getRotationBearing());
    }

    @Test
    public void onPageSelected_shouldNotTurnMap() throws Exception {
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.setInstructions(instructions);
        route.addSeenInstruction(instructions.get(0));
        route.addSeenInstruction(instructions.get(1));
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(0);
        fragment.onPageSelected(2);
        Instruction i = instructions.get(2);
        TestViewport viewport = (TestViewport) act.getMap().viewport();
        assertThat(viewport.getRotation()).isNotEqualTo(i.getRotationBearing());
    }

    @Test
    public void onPageScrolled_shouldNotSpeakInstruction() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        fragment.onPageScrolled(1, (float) 0.1, 1);
        ShadowTextToSpeech shadowTextToSpeech = shadowOf_(fragment.speakerbox.getTextToSpeech());
        shadowTextToSpeech.getOnInitListener().onInit(TextToSpeech.SUCCESS);
        assertThat(shadowTextToSpeech.getLastSpokenText())
                .isEqualTo("Head on 19th Street for 520 feet");
    }

    @Test
    public void shouldAnnounceRecalculationOnLost() throws Exception {
        initTestFragment();
        FragmentTestUtil.startFragment(fragment);

        Location testLocation = getTestLocation(111.0, 111.0);
        fragment.onLocationChanged(testLocation);
        ShadowTextToSpeech shadowTextToSpeech = shadowOf_(fragment.speakerbox.getTextToSpeech());
        shadowTextToSpeech.getOnInitListener().onInit(TextToSpeech.SUCCESS);
        assertThat(shadowTextToSpeech.getLastSpokenText())
                .isEqualTo("Recalculating");
    }

    @Test
    public void textToSpeechRules_shouldIgnoreContinueOn() throws Exception {
        fragment.setRoute(new Route(MOCK_NY_TO_VT));
        FragmentTestUtil.startFragment(fragment);
        Route route = fragment.getRoute();

        ShadowTextToSpeech shadowTextToSpeech = shadowOf_(fragment.speakerbox.getTextToSpeech());
        shadowTextToSpeech.getOnInitListener().onInit(TextToSpeech.SUCCESS);
        fragment.onPageSelected(13);
        assertThat(route.getRouteInstructions().get(13).getFullInstruction())
                .contains("Continue on  for");
        assertThat(shadowTextToSpeech.getLastSpokenText()).doesNotContain("Continue on  for");
    }

    @Test
    public void textToSpeechRules_shouldReplaceMiWithMiles() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(0, 0);
        instruction.setDistance((int) Math.round(2 * METERS_IN_ONE_MILE));
        instructions.add(instruction);

        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        ShadowTextToSpeech shadowTextToSpeech = shadowOf_(fragment.speakerbox.getTextToSpeech());
        shadowTextToSpeech.getOnInitListener().onInit(TextToSpeech.SUCCESS);
        assertThat(shadowTextToSpeech.getLastSpokenText())
                .isEqualTo("Head on 19th Street for 2 miles");
    }

    @Test
    public void textToSpeech_shouldReplace1MilesWith1Mile() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(0, 0);
        instruction.setDistance((int) Math.round(METERS_IN_ONE_MILE));
        instructions.add(instruction);

        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        ShadowTextToSpeech shadowTextToSpeech = shadowOf_(fragment.speakerbox.getTextToSpeech());
        shadowTextToSpeech.getOnInitListener().onInit(TextToSpeech.SUCCESS);
        assertThat(shadowTextToSpeech.getLastSpokenText())
                .isEqualTo("Head on 19th Street for 1 mile");
    }

    @Test
    public void textToSpeechRules_shouldReplaceFtWithFeet() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(0, 0);
        instruction.setDistance((int) Math.ceil(100 * METERS_IN_ONE_FOOT));
        instructions.add(instruction);

        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        ShadowTextToSpeech shadowTextToSpeech = shadowOf_(fragment.speakerbox.getTextToSpeech());
        shadowTextToSpeech.getOnInitListener().onInit(TextToSpeech.SUCCESS);
        assertThat(shadowTextToSpeech.getLastSpokenText())
                .isEqualTo("Head on 19th Street for 100 feet");
    }

    @Test
    public void shouldMuteVoiceNavigation() throws Exception {
        setVoiceNavigationEnabled(false);
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        ShadowTextToSpeech shadowTextToSpeech = shadowOf_(fragment.speakerbox.getTextToSpeech());
        shadowTextToSpeech.getOnInitListener().onInit(TextToSpeech.SUCCESS);
        assertThat(shadowTextToSpeech.getLastSpokenText()).isNull();
    }

    @Test
    public void voiceNavigation_shouldBeEnabledByDefault() throws Exception {
        PreferenceManager.getDefaultSharedPreferences(act).edit().clear().commit();
        initTestFragment();
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment.speakerbox.isMuted()).isFalse();
    }

    @Test
    public void createRouteTo_shouldDisplayProgressDialog() throws Exception {
        Location testLocation = getTestLocation(100.0, 100.0);
        FragmentTestUtil.startFragment(fragment);
        fragment.createRouteTo(testLocation);
        assertThat(act.getProgressDialogFragment()).isAdded();
    }

    @Test
    public void createRouteTo_shouldDismissProgressDialogOnError() throws Exception {
        Location testLocation = getTestLocation(100.0, 100.0);
        FragmentTestUtil.startFragment(fragment);
        fragment.createRouteTo(testLocation);
        verify(router).setCallback(callback.capture());
        callback.getValue().failure(500);
        assertThat(act.getProgressDialogFragment()).isNotAdded();
    }

    @Test
    public void createRouteTo_shouldDismissProgressDialogOnSuccess() throws Exception {
        Location testLocation = getTestLocation(100.0, 100.0);
        FragmentTestUtil.startFragment(fragment);
        fragment.createRouteTo(testLocation);
        verify(router).setCallback(callback.capture());
        callback.getValue().failure(207);
        assertThat(act.getProgressDialogFragment()).isNotAdded();
    }

    @Test
    public void createRouteTo_shouldToastIfNoRouteFound() throws Exception {
        Location testLocation = getTestLocation(100.0, 100.0);
        FragmentTestUtil.startFragment(fragment);
        fragment.createRouteTo(testLocation);
        verify(router).setCallback(callback.capture());
        callback.getValue().failure(207);
        assertThat(ShadowToast.getTextOfLatestToast())
                .isEqualTo(act.getString(R.string.no_route_found));
    }

    @Test
    public void createRouteTo_shouldAddFragment() throws Exception {
        Location testLocation = getTestLocation(100.0, 100.0);
        FragmentTestUtil.startFragment(fragment);
        fragment.createRouteTo(testLocation);
        verify(router).setCallback(callback.capture());
        callback.getValue().success(new Route(MOCK_NY_TO_VT));
        assertThat(fragment).isAdded();
    }

    @Test
    public void createRouteTo_shouldResetPager() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment).isAdded();
        int previousCount = fragment.pager.getAdapter().getCount();
        assertThat(previousCount).isEqualTo(3);
        Location testLocation = getTestLocation(100.0, 100.0);
        fragment.createRouteTo(testLocation);
        verify(router).setCallback(callback.capture());
        Route newRoute = new Route(MOCK_NY_TO_VT);
        callback.getValue().success(newRoute);
        assertThat(fragment.pager.getAdapter().getCount())
                .isEqualTo(newRoute.getRouteInstructions().size());
    }

    @Test
    public void createRouteTo_shouldRedrawPath() throws Exception {
        MapFragment mapFragmentMock = mock(MapFragment.class, Mockito.CALLS_REAL_METHODS);
        mapFragmentMock.setAct(act);
        fragment.setMapFragment(mapFragmentMock);
        Location testLocation = getTestLocation(100.0, 100.0);
        FragmentTestUtil.startFragment(fragment);
        fragment.createRouteTo(testLocation);
        verify(router).setCallback(callback.capture());
        callback.getValue().success(new Route(MOCK_NY_TO_VT));
        verify(path, Mockito.times(2)).clearPath();
        for (Location location : fragment.getRoute().getGeometry()) {
            verify(path).addPoint(
                    new GeoPoint(location.getLatitude(), location.getLongitude()));
        }
    }

    @Test
    public void createRouteTo_shouldRedoUrl() throws Exception {
        MapFragment mapFragmentMock = mock(MapFragment.class, Mockito.CALLS_REAL_METHODS);
        mapFragmentMock.setAct(act);
        fragment.setMapFragment(mapFragmentMock);
        FragmentTestUtil.startFragment(fragment);
        fragment.createRouteTo(getTestLocation(100.0, 200.0));
        fragment.createRouteTo(getTestLocation(200.0, 300.0));
        verify(router, atLeastOnce()).setCallback(callback.capture());
        callback.getValue().success(new Route(MOCK_NY_TO_VT));
        assertThat(router.getRouteUrl().toString()).contains("200.0,300.0");
        assertThat(router.getRouteUrl().toString()).doesNotContain("100.0,200.0");
    }

    @Test
    public void createRouteTo_shouldRequestNewRoute() throws Exception {
        Location testLocation = getTestLocation(100.0, 100.0);
        FragmentTestUtil.startFragment(fragment);
        fragment.createRouteTo(testLocation);
        verify(router).fetch();
    }

    @Test
    public void onLocationChange_shouldDoNothingWhileRerouting() throws Exception {
        Location testLocation = getTestLocation(40.658563, -73.986853);
        RouteFragment spyFragment = spy(fragment);
        spyFragment.setRoute(new Route(MOCK_AROUND_THE_BLOCK));
        FragmentTestUtil.startFragment(spyFragment);
        spyFragment.onLocationChanged(testLocation);
        spyFragment.onLocationChanged(testLocation);
        verify(spyFragment, Mockito.times(1)).createRouteTo(testLocation);
    }

    @Test
    public void onLocationChange_shouldBeReEnabledOnceReRoutingIsCompleted() throws Exception {
        Location testLocation = getTestLocation(40.658563, -73.986853);
        RouteFragment spyFragment = spy(fragment);
        spyFragment.setRoute(new Route(MOCK_AROUND_THE_BLOCK));
        FragmentTestUtil.startFragment(spyFragment);
        spyFragment.onLocationChanged(testLocation);
        verify(router).setCallback(callback.capture());
        callback.getValue().success(new Route(new JSONObject(MOCK_AROUND_THE_BLOCK)));
        spyFragment.onLocationChanged(testLocation);
        verify(spyFragment, Mockito.times(2)).createRouteTo(testLocation);
    }

    @Test
    public void onLocationChange_shouldBeReEnabledOnceReRoutingHasError() throws Exception {
        Location testLocation = getTestLocation(40.658563, -73.986853);
        RouteFragment spyFragment = spy(fragment);
        spyFragment.setRoute(new Route(MOCK_AROUND_THE_BLOCK));
        FragmentTestUtil.startFragment(spyFragment);
        spyFragment.onLocationChanged(testLocation);
        verify(router).setCallback(callback.capture());
        callback.getValue().failure(500);
        spyFragment.onLocationChanged(testLocation);
        verify(spyFragment, Mockito.times(2)).createRouteTo(testLocation);
    }

    @Test
    public void getGPXDescription_shouldIncludeReturnBeginningAndEnd() throws Exception {
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
    public void storeRouteInDatabase_shouldSendExceptionToBugSense() throws Exception {
        act.getDb().close();
        fragment.storeRouteInDatabase(new JSONObject());
        assertThat(ShadowBugSenseHandler.getLastHandledException())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void addCoordinatesToDatabase_shouldSendExceptionToBugSense() throws Exception {
        initTestFragment();
        FragmentTestUtil.startFragment(fragment);
        fragment.createRouteTo(getTestLocation(100.0, 100.0));
        verify(router).setCallback(callback.capture());
        act.getDb().close();
        callback.getValue().success(new Route(MOCK_ROUTE_JSON));
        assertThat(ShadowBugSenseHandler.getLastHandledException())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldInitDynamicZoomUsingDefaultValues() throws Exception {
        Resources res = act.getResources();
        FragmentTestUtil.startFragment(fragment);
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
        FragmentTestUtil.startFragment(fragment);
        Location location = fragment.getRoute().getRouteInstructions().get(0).getLocation();

        assertZoomLevel(14, 10, location);
        assertZoomLevel(13, 20, location);
        assertZoomLevel(12, 30, location);
        assertZoomLevel(11, 40, location);
        assertZoomLevel(10, 50, location);
    }

    @Test
    public void debugViewShouldBeHidden() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment.getView().findViewById(R.id.debugging)).isNotVisible();
    }

    @Test
    public void debugViewShouldBeVisible() throws Exception {
        enableDebugMode(Robolectric.application);
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment.getView().findViewById(R.id.debugging)).isVisible();
    }

    @Test
    public void shouldGenerateNotificationOnFirstInstruction() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(3, 3);
        instructions.add(instruction);
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);

        ShadowNotification sNotification = getRoutingNotification();
        assertThat(sNotification.getContentTitle()).isEqualTo("Test SimpleFeature");
        assertThat(sNotification.getContentText()).isEqualTo("Head on 19th Street for 520 ft");
        assertThat(sNotification.getActions().get(0).title).isEqualTo("Exit Navigation");
    }

    @Test
    public void shouldGenerateNotificationOnPageSelected() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(3, 3);
        instructions.add(instruction);
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        fragment.onPageSelected(0);

        ShadowNotification sNotification = getRoutingNotification();
        assertThat(sNotification.getContentTitle()).isEqualTo("Test SimpleFeature");
        assertThat(sNotification.getContentText()).isEqualTo("Head on 19th Street for 520 ft");
        assertThat(sNotification.getActions().get(0).title).isEqualTo("Exit Navigation");
    }

    @Test
    public void shouldKillNotificationOnExitNavigation() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(3, 3);
        instructions.add(instruction);
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        fragment.onPageSelected(0);

        ShadowNotification sNotification = getRoutingNotification();
        sNotification.getActions().get(0).actionIntent.send();

        ShadowApplication application = shadowOf(act.getApplication());
        Intent broadcastIntent = application.getBroadcastIntents().get(1);
        String broadcastClassName = broadcastIntent.getComponent().getClassName();
        boolean shouldExit = broadcastIntent.getExtras()
                .getBoolean(MapzenNotificationCreator.EXIT_NAVIGATION);
        assertThat(shouldExit).isTrue();
        assertThat(broadcastClassName).isEqualTo("com.mapzen.util.NotificationBroadcastReciever");
    }

    @Test
    public void getAverageSpeed_shouldDefaultToZero() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment.getAverageSpeed()).isEqualTo(0);
    }

    @Test
    public void getAverageSpeed_shouldReturnAverageOfLastNLocationUpdates() throws Exception {
        initTestFragment();
        FragmentTestUtil.startFragment(fragment);
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

    @Test
    public void shouldUseGpxTraceWhenMockModeEnabled() throws Exception {
        loadTestGpxTrace();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        prefs.edit().putBoolean(act.getString(R.string.settings_mock_gpx_key), true).commit();
        initTestFragment();
        FragmentTestUtil.startFragment(fragment);
        Thread.sleep(100);
        Robolectric.runUiThreadTasks();
        List<Intent> intents = getShadowApplication().getBroadcastIntents();
        Location location = intents.get(1).getExtras().getParcelable("location");
        assertThat(location).hasLatitude(0.0);
        assertThat(location).hasLongitude(0.1);
    }

    @Test
    public void onDetach_shouldDisableMockMode() throws Exception {
        loadTestGpxTrace();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        prefs.edit().putBoolean(act.getString(R.string.settings_mock_gpx_key), true).commit();
        initTestFragment();
        FragmentTestUtil.startFragment(fragment);
        fragment.onDetach();
        ShadowLocationManager shadowLocationManager = Robolectric.shadowOf((LocationManager)
                application.getSystemService(Context.LOCATION_SERVICE));
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
    }

    private void loadMockAroundTheBlock() {
        setAdvanceRadiusPreference(R.string.settings_turn_driving_0to15_key, 0);
        fragment.createRouteTo(getTestLocation(100.0, 100.0));
        verify(router).setCallback(callback.capture());
        callback.getValue().success(new Route(MOCK_AROUND_THE_BLOCK));
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
        fragment.onLocationChanged(location);
        assertThat(getMapController().getZoomLevel()).isEqualTo(expected);
    }

    private void assertAdvanceRadius(int expected, float milesPerHour, Location location) {
        location.setSpeed(ZoomController.milesPerHourToMetersPerSecond(milesPerHour));
        fragment.onLocationChanged(location);
        assertThat(fragment.getAdvanceRadius()).isEqualTo(expected);
    }

    private void setVoiceNavigationEnabled(boolean enabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putBoolean(act.getString(R.string.settings_voice_navigation_key), enabled);
        prefEditor.commit();
    }

    private void initTestFragment() throws Exception {
        // TODO make this call newInstance for consistency
        fragment = new RouteFragment();
        fragment.setSimpleFeature(getTestSimpleFeature());
        fragment.setAct(act);
        fragment.inject();
        fragment.setMapFragment(initMapFragment(act));
        fragment.setRoute(new Route(MOCK_ROUTE_JSON));
        fragment.setRouteLocationIndicator(new RouteLocationIndicator(act.getMap()));
        testInstructions = new ArrayList<Instruction>();
        testInstructions.add(getTestInstruction(0, 0));
        testInstructions.add(getTestInstruction(1, 1));
        testInstructions.add(getTestInstruction(2, 2));
        fragment.setInstructions(testInstructions);
    }

    private ShadowNotification getRoutingNotification() {
        NotificationManager manager = (NotificationManager) act.getSystemService(
                act.getApplicationContext().NOTIFICATION_SERVICE);
        ShadowNotificationManager sManager = shadowOf(manager);
        return shadowOf(sManager.getAllNotifications().get(0));
    }

    private void setAdvanceRadiusPreference(int key, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(act.getString(key), value);
        prefEditor.commit();
    }

    private void setNumberOfLocationForAverageSpeed(int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(
                act.getString(R.string.settings_number_of_locations_for_average_speed_key), value);
        prefEditor.commit();
    }

    private void simulateUserPagerTouch() {
        MotionEvent motionEvent =
                MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 12f, 34f, 0);
        View.OnTouchListener listener = shadowOf(fragment.pager).getOnTouchListener();
        listener.onTouch(null, motionEvent);
    }

    private void simulateUserDrag() {
        MotionEvent e =
                MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_MOVE, 120f, 100f, 0);
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
}
