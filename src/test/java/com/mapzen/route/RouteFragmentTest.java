package com.mapzen.route;

import com.mapzen.MapController;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.fragment.DirectionListFragment;
import com.mapzen.fragment.MapFragment;
import com.mapzen.helpers.DistanceFormatter;
import com.mapzen.helpers.ZoomController;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;
import com.mapzen.osrm.Router;
import com.mapzen.shadows.ShadowBugSenseHandler;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestBaseActivity;
import com.mapzen.util.DatabaseHelper;
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
import org.oscim.map.Animator;
import org.oscim.map.TestMap;
import org.oscim.map.TestViewport;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowPopupMenu;
import org.robolectric.shadows.ShadowTextToSpeech;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.tester.android.view.TestMenu;
import org.robolectric.tester.android.view.TestMenuItem;
import org.robolectric.util.FragmentTestUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;

import static com.mapzen.MapController.getMapController;
import static com.mapzen.activity.BaseActivity.COM_MAPZEN_UPDATES_LOCATION;
import static com.mapzen.entity.SimpleFeature.NAME;
import static com.mapzen.helpers.DistanceFormatter.METERS_IN_ONE_FOOT;
import static com.mapzen.helpers.DistanceFormatter.METERS_IN_ONE_MILE;
import static com.mapzen.support.TestHelper.MOCK_AROUND_THE_BLOCK;
import static com.mapzen.support.TestHelper.MOCK_NY_TO_VT;
import static com.mapzen.support.TestHelper.MOCK_ROUTE_JSON;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.Robolectric.shadowOf_;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class RouteFragmentTest {
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

    @Before
    public void setUp() throws Exception {
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
    public void onLocationChange_shouldCenterMapOnLocation() throws Exception {
        Animator animator = mock(Animator.class);
        ((TestMap) fragment.getMapFragment().getMap()).setAnimator(animator);
        FragmentTestUtil.startFragment(fragment);
        ArrayList<Location> geometry = fragment.getRoute().getGeometry();
        Location testLocation = fragment.getRoute().snapToRoute(geometry.get(2));
        fragment.onLocationChanged(testLocation);
        GeoPoint expected = new GeoPoint(testLocation.getLatitude(), testLocation.getLongitude());
        verify(animator).animateTo(expected);
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
        fragment.onPause();
        assertThat(app.hasReceiverForIntent(new Intent(COM_MAPZEN_UPDATES_LOCATION))).isFalse();
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
        assertThat(view.getText()).isEqualTo(simpleFeature.getProperty(NAME));
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
    public void onCreateView_shouldHaveOverflowMenu() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        ImageButton overFlowMenu = (ImageButton) view.findViewById(R.id.overflow_menu);
        assertThat(overFlowMenu).isVisible();
    }

    @Test
    public void onCreateView_shouldNotShowResumeButton() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        Button resume = (Button) view.findViewById(R.id.resume_button);
        assertThat(resume).isNotVisible();
    }

    @Test
    public void onTouch_shouldDisplayResumeButton() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        Button resume = (Button) view.findViewById(R.id.resume_button);
        simulateUserPagerTouch();
        assertThat(resume).isVisible();
    }

    @Test
    public void onTouch_shouldStoreCurrentItemWhenPagerWasFirstTouched() throws Exception {
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        fragment.onLocationChanged(instructions.get(2).getLocation());
        simulateUserPagerTouch();
        fragment.pager.setCurrentItem(0);
        simulateUserPagerTouch();
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        Button resume = (Button) view.findViewById(R.id.resume_button);
        resume.performClick();
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(2);
    }

    @Test
    public void onClickResume_shouldHideResumeButton() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        Button resume = (Button) view.findViewById(R.id.resume_button);
        simulateUserPagerTouch();
        resume.performClick();
        assertThat(resume).isNotVisible();
    }

    @Test
    public void onClickResume_shouldStartAtPagerLocation() throws Exception {
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        fragment.onLocationChanged(instructions.get(2).getLocation());
        simulateUserPagerTouch();
        fragment.pager.setCurrentItem(0);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        Button resume = (Button) view.findViewById(R.id.resume_button);
        resume.performClick();
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(2);
    }

    @Test
    public void menuOnClick_shouldShowMenuOptions() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        ImageButton overFlowMenu = (ImageButton) view.findViewById(R.id.overflow_menu);
        overFlowMenu.performClick();
        ShadowPopupMenu popupMenu = shadowOf(ShadowPopupMenu.getLatestPopupMenu());
        assertThat(popupMenu.isShowing()).isTrue();
    }

    @Test
    public void shouldShowDirectionListFragment() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        ImageButton overFlowMenu = (ImageButton) view.findViewById(R.id.overflow_menu);
        overFlowMenu.performClick();
        ShadowPopupMenu popupMenu = shadowOf(ShadowPopupMenu.getLatestPopupMenu());
        PopupMenu.OnMenuItemClickListener listener = popupMenu.getOnMenuItemClickListener();
        TestMenuItem item = new TestMenuItem();
        item.setItemId(R.id.route_menu_steps);
        listener.onMenuItemClick(item);
        assertThat(act.getSupportFragmentManager()).hasFragmentWithTag(DirectionListFragment.TAG);
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
        act.getLocationListener().onLocationChanged(getTestLocation(11.0, 11.0));
        FragmentTestUtil.startFragment(fragment);
        Location bogusLocation = getTestLocation(23.0, 63.0);
        act.getLocationListener().onLocationChanged(bogusLocation);
        GeoPoint point = act.getMapFragment().getMeMarker().geoPoint;
        assertThat(Math.round(point.getLatitude())).isNotEqualTo(Math.round(23.0));
        assertThat(Math.round(point.getLongitude())).isNotEqualTo(Math.round(63.0));
    }

    @Test
    public void onResume_shouldDisableActionbar() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        act.showActionBar();
        assertThat(act.getActionBar()).isNotShowing();
    }

    @Test
    public void onDetach_shouldEnableActionbar() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        fragment.onDetach();
        assertThat(act.getActionBar()).isShowing();
    }

    @Test
    public void onPause_shouldActivateActivitiesMapUpdates() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        fragment.onPause();
        Location expectedLocation = getTestLocation(23.0, 63.0);
        act.getLocationListener().onLocationChanged(expectedLocation);
        GeoPoint point = act.getMapFragment().getMeMarker().geoPoint;
        assertThat(Math.round(point.getLatitude())).isEqualTo(Math.round(23.0));
        assertThat(Math.round(point.getLongitude())).isEqualTo(Math.round(63.0));
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
        fragment.onLocationChanged(instructions.get(2).getLocation());
        assertThat(fragment.getFlippedInstructions().contains(instructions.get(0))).isTrue();
        assertThat(fragment.getFlippedInstructions().contains(instructions.get(1))).isTrue();
        assertThat(fragment.getFlippedInstructions().contains(instructions.get(2))).isFalse();
    }

    @Test
    public void onLocationChange_shouldUpdateDistanceIfAlreadyFlipped() throws Exception {
        setAdvanceRadiusPreference(R.string.settings_turn_driving_0to15_key, 0);
        fragment.createRouteTo(getTestLocation(100.0, 100.0));
        verify(router).setCallback(callback.capture());
        callback.getValue().success(new Route(MOCK_AROUND_THE_BLOCK));
        FragmentTestUtil.startFragment(fragment);
        fragment.onResume();
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();

        // Flip first instruction
        fragment.onLocationChanged(instructions.get(0).getLocation());

        // Midpoint between first and second instruction (pre-calculated)
        Location midPoint = getTestLocation(40.660278, -73.988611);
        fragment.onLocationChanged(midPoint);

        View view = fragment.pager.findViewWithTag("Instruction_0");
        TextView textView = (TextView) view.findViewById(R.id.full_instruction_after_action);
        assertThat(textView).containsText(DistanceFormatter.format(instructions.get(0)
                .getRemainingDistance(midPoint)));
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
        assertThat(((TestViewport) map.viewport()).getRotation()).isEqualTo(
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
        PathLayer pathLayerMock = mock(PathLayer.class);
        when(mapFragmentMock.getPathLayer()).thenReturn(pathLayerMock);
        fragment.setMapFragment(mapFragmentMock);
        Location testLocation = getTestLocation(100.0, 100.0);
        FragmentTestUtil.startFragment(fragment);
        fragment.createRouteTo(testLocation);
        verify(router).setCallback(callback.capture());
        callback.getValue().success(new Route(MOCK_NY_TO_VT));
        verify(pathLayerMock, Mockito.times(2)).clearPath();
        for (Location location : fragment.getRoute().getGeometry()) {
            verify(pathLayerMock).addPoint(
                    new GeoPoint(location.getLatitude(), location.getLongitude()));
        }
    }

    @Test
    public void createRouteTo_shouldRedoUrl() throws Exception {
        MapFragment mapFragmentMock = mock(MapFragment.class, Mockito.CALLS_REAL_METHODS);
        mapFragmentMock.setAct(act);
        PathLayer pathLayerMock = mock(PathLayer.class);
        when(mapFragmentMock.getPathLayer()).thenReturn(pathLayerMock);
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
    public void onLocationChange_shouldNotReRouteWhenSnapToIsNull() throws Exception {
        Location testLocation = getTestLocation(40.662046, -73.987089);
        RouteFragment spyFragment = spy(fragment);
        spyFragment.setRoute(new Route(MOCK_AROUND_THE_BLOCK));
        FragmentTestUtil.startFragment(spyFragment);
        spyFragment.onLocationChanged(testLocation);
        verify(spyFragment, never()).createRouteTo(testLocation);
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
    public void toString_shouldIncludeReturnBeginningAndEnd() throws Exception {
        testInstructions = new ArrayList<Instruction>();
        testInstructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(testInstructions);
        String actual = fragment.toString();
        assertThat(actual).contains(getTestInstruction(0, 0).toString());
        assertThat(actual).contains(getTestSimpleFeature().toString());
    }

    @Test
    public void toString_shouldDisplay() throws Exception {
        String expected = "Route without instructions";
        fragment.setInstructions(new ArrayList<Instruction>());
        String actual = fragment.toString();
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
        FragmentTestUtil.startFragment(fragment);
        Location location = fragment.getRoute().getRouteInstructions().get(0).getLocation();

        assertZoomLevel(14, 10, location);
        assertZoomLevel(13, 20, location);
        assertZoomLevel(12, 30, location);
        assertZoomLevel(11, 40, location);
        assertZoomLevel(10, 50, location);
    }

    private void assertZoomLevel(int expected, float milesPerHour, Location location) {
        location.setSpeed(ZoomController.milesPerHourToMetersPerSecond(milesPerHour));
        fragment.onLocationChanged(location);
        assertThat(MapController.getMapController().getZoomLevel()).isEqualTo(expected);
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
        fragment = new RouteFragment();
        fragment.setSimpleFeature(getTestSimpleFeature());
        fragment.setAct(act);
        fragment.setMapFragment(initMapFragment(act));
        fragment.setRoute(new Route(MOCK_ROUTE_JSON));
        fragment.setRouteLocationIndicator(new RouteLocationIndicator(act.getMap()));
        testInstructions = new ArrayList<Instruction>();
        testInstructions.add(getTestInstruction(0, 0));
        testInstructions.add(getTestInstruction(1, 1));
        testInstructions.add(getTestInstruction(2, 2));
        fragment.setInstructions(testInstructions);
    }

    private void setAdvanceRadiusPreference(int key, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(act.getString(key), value);
        prefEditor.commit();
    }

    private void simulateUserPagerTouch() {
        MotionEvent motionEvent =
                MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 12f, 34f, 0);
        View.OnTouchListener listener = shadowOf(fragment.pager).getOnTouchListener();
        listener.onTouch(null, motionEvent);
    }
}
