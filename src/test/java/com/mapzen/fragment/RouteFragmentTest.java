package com.mapzen.fragment;

import com.mapzen.R;
import com.mapzen.entity.Feature;
import com.mapzen.helpers.DistanceFormatter;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;
import com.mapzen.routing.RoutingListener;
import com.mapzen.shadows.ShadowTextToSpeech;
import com.mapzen.shadows.ShadowVolley;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestBaseActivity;
import com.mapzen.util.DatabaseHelper;
import com.mapzen.util.GearAgentService;
import com.mapzen.util.GearServiceSocket;
import com.mapzen.widget.DistanceView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.oscim.core.GeoPoint;
import org.oscim.layers.PathLayer;
import org.oscim.map.Animator;
import org.oscim.map.TestMap;
import org.oscim.map.TestViewport;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowPopupMenu;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.tester.android.view.TestMenu;
import org.robolectric.tester.android.view.TestMenuItem;
import org.robolectric.util.FragmentTestUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.mapzen.MapController.getMapController;
import static com.mapzen.activity.BaseActivity.COM_MAPZEN_UPDATES_LOCATION;
import static com.mapzen.entity.Feature.NAME;
import static com.mapzen.helpers.DistanceFormatter.METERS_IN_ONE_FOOT;
import static com.mapzen.helpers.DistanceFormatter.METERS_IN_ONE_MILE;
import static com.mapzen.shadows.ShadowVolley.getMockRequestQueue;
import static com.mapzen.support.TestHelper.MOCK_AROUND_THE_BLOCK;
import static com.mapzen.support.TestHelper.MOCK_NO_ROUTE_JSON;
import static com.mapzen.support.TestHelper.MOCK_NY_TO_VT;
import static com.mapzen.support.TestHelper.MOCK_ROUTE_JSON;
import static com.mapzen.support.TestHelper.enableDebugMode;
import static com.mapzen.support.TestHelper.getTestFeature;
import static com.mapzen.support.TestHelper.getTestInstruction;
import static com.mapzen.support.TestHelper.getTestLocation;
import static com.mapzen.support.TestHelper.initBaseActivityWithMenu;
import static com.mapzen.support.TestHelper.initMapFragment;
import static com.mapzen.util.DatabaseHelper.COLUMN_RAW;
import static com.mapzen.util.DatabaseHelper.COLUMN_ROUTE_ID;
import static com.mapzen.util.DatabaseHelper.TABLE_ROUTES;
import static com.mapzen.util.DatabaseHelper.TABLE_ROUTE_GEOMETRY;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
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

    @Before
    public void setUp() throws Exception {
        ShadowVolley.clearMockRequestQueue();
        menu = new TestMenu();
        act = initBaseActivityWithMenu(menu);
        initTestFragment();
        app = Robolectric.getShadowApplication();
        setVoiceNavigationEnabled(true);
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
        Animator animator = Mockito.mock(Animator.class);
        ((TestMap) fragment.mapFragment.getMap()).setAnimator(animator);
        FragmentTestUtil.startFragment(fragment);
        ArrayList<double[]> geometry = fragment.getRoute().getGeometry();
        double[] loc = fragment.getRoute().snapToRoute(geometry.get(2));
        Location testLocation = getTestLocation(loc[0], loc[1]);
        fragment.onLocationChanged(testLocation);
        GeoPoint expected = new GeoPoint(testLocation.getLatitude(), testLocation.getLongitude());
        Mockito.verify(animator).animateTo(expected);
    }

    @Test
    public void onLocationChange_shouldStoreOriginalLocationRecordInDatabase() throws Exception {
        enableDebugMode(act);
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        double[] sample1 = fragment.getRoute().getGeometry().get(0);
        double[] sample2 = fragment.getRoute().getGeometry().get(1);
        double[] expected = fragment.getRoute().getGeometry().get(2);
        instructions.add(getTestInstruction(sample1[0], sample1[1]));
        instructions.add(getTestInstruction(sample2[0], sample2[1]));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        Location testLocation = getTestLocation(expected[0], expected[1]);
        fragment.onLocationChanged(testLocation);
        SQLiteDatabase db = act.getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] { DatabaseHelper.COLUMN_LAT, DatabaseHelper.COLUMN_LNG },
                null, null, null, null, null);
        assertThat(cursor).hasCount(1);
        cursor.moveToNext();
        assertThat(cursor.getString(0)).isEqualTo(String.valueOf(expected[0]));
        assertThat(cursor.getString(1)).isEqualTo(String.valueOf(expected[1]));
    }

    @Test
    public void onLocationChange_shouldStoreCorrectedLocationRecordInDatabase() throws Exception {
        enableDebugMode(act);
        FragmentTestUtil.startFragment(fragment);
        double[] sample = fragment.getRoute().getGeometry().get(2);
        Location testLocation = getTestLocation(sample[0], sample[1]);
        fragment.onLocationChanged(testLocation);
        SQLiteDatabase db = act.getReadableDb();
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
        enableDebugMode(act);
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        double[] expected = fragment.getRoute().getGeometry().get(0);
        double[] sample1 = fragment.getRoute().getGeometry().get(1);
        double[] sample2 = fragment.getRoute().getGeometry().get(2);
        instructions.add(getTestInstruction(expected[0], expected[1]));
        instructions.add(getTestInstruction(sample1[0], sample1[1]));

        FragmentTestUtil.startFragment(fragment);
        fragment.setInstructions(instructions);

        Location testLocation = getTestLocation(sample2[0], sample2[1]);
        fragment.onLocationChanged(testLocation);
        SQLiteDatabase db = act.getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] {
                        DatabaseHelper.COLUMN_INSTRUCTION_LAT,
                        DatabaseHelper.COLUMN_INSTRUCTION_LNG
                },
                null, null, null, null, null);
        assertThat(cursor).hasCount(1);
        cursor.moveToNext();
        assertThat(cursor.getString(0)).isEqualTo(String.valueOf(expected[0]));
        assertThat(cursor.getString(1)).isEqualTo(String.valueOf(expected[1]));
    }

    @Test
    public void onRouteSuccess_shouldStoreRawJson() throws Exception {
        enableDebugMode(act);
        fragment.setRoute(new JSONObject(MOCK_ROUTE_JSON));
        FragmentTestUtil.startFragment(fragment);
        fragment.onPause();
        SQLiteDatabase db = act.getReadableDb();
        Cursor cursor = db.query(TABLE_ROUTES,
                new String[] { COLUMN_RAW },
                null, null, null, null, null);
        assertThat(cursor).hasCount(1);
    }

    @Test
    public void onRouteSuccess_shouldNoteStoreRawJson() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        fragment.onPause();
        SQLiteDatabase db = act.getReadableDb();
        Cursor cursor = db.query(TABLE_ROUTES,
                new String[] { COLUMN_RAW },
                null, null, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void drawRoute_shouldStoreCoordinates() throws Exception {
        enableDebugMode(act);
        FragmentTestUtil.startFragment(fragment);
        fragment.onPause();
        SQLiteDatabase db = act.getReadableDb();
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
        SQLiteDatabase db = act.getReadableDb();
        Cursor cursor = db.query(TABLE_ROUTE_GEOMETRY,
                new String[] { COLUMN_ROUTE_ID },
                COLUMN_ROUTE_ID + " = ?",
                new String[] { String.valueOf(fragment.getRouteId()) }, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void onLocationChange_shouldStoreInstructionBearingRecordInDatabase() throws Exception {
        enableDebugMode(act);
        FragmentTestUtil.startFragment(fragment);
        double[] sample = fragment.getRoute().getGeometry().get(2);
        Location testLocation = getTestLocation(sample[0], sample[1]);
        fragment.onLocationChanged(testLocation);
        SQLiteDatabase db = act.getReadableDb();
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
        SQLiteDatabase db = act.getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] { DatabaseHelper.COLUMN_INSTRUCTION_BEARING },
                null, null, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void onLocationChange_shouldStoreAssociatedRoute() throws Exception {
        enableDebugMode(act);
        FragmentTestUtil.startFragment(fragment);
        double[] sample = fragment.getRoute().getGeometry().get(2);
        Location testLocation = getTestLocation(sample[0], sample[1]);
        fragment.onLocationChanged(testLocation);
        SQLiteDatabase db = act.getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] { COLUMN_ROUTE_ID },
                COLUMN_ROUTE_ID + " = ?",
                new String[] { String.valueOf(fragment.getRouteId()) }, null, null, null);
        assertThat(cursor).hasCount(1);
    }

    @Test
    public void onLocationChange_shouldReRouteWhenLost() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        Location testLocation = getTestLocation(111.0, 111.0);
        fragment.onLocationChanged(testLocation);
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
        Feature feature = new Feature();
        fragment.setFeature(feature);
        assertThat(fragment.getDestinationPoint()).isEqualTo(feature.getGeoPoint());
    }

    @Test
    public void setDestination_shouldSetFeature() throws Exception {
        Feature feature = getTestFeature();
        fragment.setFeature(feature);
        assertThat(fragment.getFeature()).isEqualTo(feature);
    }

    @Test
    public void onCreateView_shouldShowNameOfDestination() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        Feature feature = getTestFeature();
        TextView view = (TextView) fragment.getView().findViewById(R.id.destination_name);
        assertThat(view.getText()).isEqualTo(feature.getProperty(NAME));
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
        double[] point = instructions.get(2).getPoint();
        fragment.onLocationChanged(getTestLocation(point[0], point[1]));
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
    public void onResume_shouldStartDbTransaction() throws Exception {
        enableDebugMode(act);
        FragmentTestUtil.startFragment(fragment);
        assertThat(act.getDb().inTransaction()).isTrue();
    }

    @Test
    public void onPause_shouldEndDbTransaction() throws Exception {
        enableDebugMode(act);
        FragmentTestUtil.startFragment(fragment);
        fragment.onPause();
        assertThat(act.getDb().inTransaction()).isFalse();
    }

    @Test
    public void onResume_shouldNotStartDbTransaction() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        assertThat(act.getDb().inTransaction()).isFalse();
    }

    @Test
    public void onLocationChange_shouldAdvance() throws Exception {
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(0);
        double[] point = instructions.get(2).getPoint();
        fragment.onLocationChanged(getTestLocation(point[0], point[1]));
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
        double[] point = instructions.get(2).getPoint();
        fragment.onLocationChanged(getTestLocation(point[0], point[1]));
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
        double[] point = instructions.get(2).getPoint();
        fragment.onLocationChanged(getTestLocation(point[0], point[1]));
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
    public void onResume_shouldAddProximityAlertsForEveryInstruction() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment.getProximityAlerts().size()).isEqualTo(testInstructions.size());
    }

    @Test
    public void onLocationChange_shouldFlipToPostInstructionLanguage() throws Exception {
        fragment.setRoute(new JSONObject(MOCK_ROUTE_JSON));
        FragmentTestUtil.startFragment(fragment);
        fragment.onResume();
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        double[] point0 = instructions.get(0).getPoint();
        fragment.onLocationChanged(getTestLocation(point0[0], point0[1]));
        double[] point1 = instructions.get(1).getPoint();
        fragment.onLocationChanged(getTestLocation(point1[0], point1[1]));
        double[] point2 = instructions.get(2).getPoint();
        fragment.onLocationChanged(getTestLocation(point2[0], point2[1]));
        assertThat(fragment.getFlippedInstructions().contains(instructions.get(0))).isTrue();
        assertThat(fragment.getFlippedInstructions().contains(instructions.get(1))).isTrue();
        assertThat(fragment.getFlippedInstructions().contains(instructions.get(2))).isFalse();
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
    public void getWalkingAdvanceRadius_shouldHaveDefaultValue() {
        assertThat(fragment.getWalkingAdvanceRadius())
                .isEqualTo(act.getResources().getInteger(R.integer.route_advance_radius));
    }

    @Test
    public void getWalkingAdvanceRadius_shouldBeConfigurable() {
        int expected = 102;
        setWalkingRadius(expected);
        assertThat(fragment.getWalkingAdvanceRadius()).isEqualTo(expected);
    }

    @Test
    public void firstInstruction_shouldHaveDarkGrayBackground() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        View view = getInstructionView(0);
        ColorDrawable background = (ColorDrawable) view.getBackground();
        assertThat(background.getColor()).isEqualTo(0xff333333);
    }

    @Test
    public void lastInstruction_shouldHaveGreenBackground() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        View view = getInstructionView(1);
        ColorDrawable background = (ColorDrawable) view.getBackground();
        assertThat(background.getColor()).isEqualTo(0xff68a547);
    }

    @Test
    public void shouldBoldName() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        TextView textView = (TextView) getInstructionView(0).findViewById(R.id.full_instruction);
        SpannedString spannedString = (SpannedString) textView.getText();
        assertThat(spannedString.getSpans(0, spannedString.length(), StyleSpan.class)).isNotNull();
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
    public void onCreateView_shouldSendFirstInstructionToGear() throws Exception {
        GearServiceSocket mockSocket = Mockito.mock(GearServiceSocket.class);
        GearAgentService.setConnection(mockSocket);
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(3, 3);
        instructions.add(instruction);

        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);

        Mockito.verify(mockSocket).send(Matchers.eq(GearAgentService.CHANNEL_ID),
                Matchers.eq(instruction.getGearJson().toString().getBytes()));
    }

    @Test
    public void onCreateView_shouldNotSendFirstInstructionToGear() throws Exception {
        GearServiceSocket mockSocket = Mockito.mock(GearServiceSocket.class);
        GearAgentService.setConnection(null);
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(3, 3);
        instructions.add(instruction);

        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);

        Mockito.verifyZeroInteractions(mockSocket);
    }

    @Test
    public void onPageSelected_shouldSendInstructionToGear() throws Exception {
        GearServiceSocket mockSocket = Mockito.mock(GearServiceSocket.class);
        InOrder inOrder = Mockito.inOrder(mockSocket);
        GearAgentService.setConnection(mockSocket);
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

        inOrder.verify(mockSocket).send(Matchers.eq(GearAgentService.CHANNEL_ID),
                Matchers.eq(instructions.get(0).getGearJson().toString().getBytes()));
        inOrder.verify(mockSocket).send(Matchers.eq(GearAgentService.CHANNEL_ID),
                Matchers.eq(instructions.get(1).getGearJson().toString().getBytes()));
    }

    @Test
    public void onPageSelected_shouldNotSendInstructionToGear() throws Exception {
        GearServiceSocket mockSocket = Mockito.mock(GearServiceSocket.class);
        GearAgentService.setConnection(null);
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
        Mockito.verifyZeroInteractions(mockSocket);
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
    public void onLost_shouldDisplayProgressDialog() throws Exception {
        Location testLocation = getTestLocation(100.0, 100.0);
        FragmentTestUtil.startFragment(fragment);
        fragment.onLost(testLocation);
        assertThat(act.getProgressDialogFragment()).isAdded();
    }

    @Test
    public void onLost_shouldDismissProgressDialogOnError() throws Exception {
        Location testLocation = getTestLocation(100.0, 100.0);
        FragmentTestUtil.startFragment(fragment);
        fragment.onLost(testLocation);
        List<Request> requestSet = getMockRequestQueue().getRequests();
        Request<JSONObject> request = requestSet.iterator().next();
        request.deliverError(null);
        assertThat(act.getProgressDialogFragment()).isNotAdded();
    }

    @Test
    public void onLost_shouldDismissProgressDialogOnSuccess() throws Exception {
        Location testLocation = getTestLocation(100.0, 100.0);
        FragmentTestUtil.startFragment(fragment);
        fragment.onLost(testLocation);
        List<Request> requestSet = getMockRequestQueue().getRequests();
        JsonObjectRequest request = (JsonObjectRequest) requestSet.iterator().next();
        getMockRequestQueue().deliverResponse(request, new JSONObject(MOCK_NO_ROUTE_JSON));
        assertThat(act.getProgressDialogFragment()).isNotAdded();
    }

    @Test
    public void onLost_shouldToastIfNoRouteFound() throws Exception {
        Location testLocation = getTestLocation(100.0, 100.0);
        FragmentTestUtil.startFragment(fragment);
        fragment.onLost(testLocation);
        ShadowVolley.MockRequestQueue queue = getMockRequestQueue();
        JsonObjectRequest request = (JsonObjectRequest) queue.getRequests().get(0);
        queue.deliverResponse(request, new JSONObject(MOCK_NO_ROUTE_JSON));
        assertThat(ShadowToast.getTextOfLatestToast())
                .isEqualTo(act.getString(R.string.no_route_found));
    }

    @Test
    public void onLost_shouldResetPager() throws Exception {
        Location testLocation = getTestLocation(100.0, 100.0);
        FragmentTestUtil.startFragment(fragment);
        fragment.onLost(testLocation);
        int previousCount = fragment.pager.getAdapter().getCount();
        ShadowVolley.MockRequestQueue queue = getMockRequestQueue();
        JsonObjectRequest request = (JsonObjectRequest) queue.getRequests().get(0);
        queue.deliverResponse(request, new JSONObject(MOCK_NY_TO_VT));
        assertThat(fragment.pager.getAdapter().getCount()).isNotEqualTo(previousCount);
        assertThat(fragment.pager.getCurrentItem()).isEqualTo(0);
    }

    @Test
    public void onLost_shouldRedrawPath() throws Exception {
        MapFragment mapFragmentMock = Mockito.mock(MapFragment.class, Mockito.CALLS_REAL_METHODS);
        PathLayer pathLayerMock = Mockito.mock(PathLayer.class);
        Mockito.when(mapFragmentMock.getPathLayer()).thenReturn(pathLayerMock);
        fragment.setMapFragment(mapFragmentMock);
        Location testLocation = getTestLocation(100.0, 100.0);
        FragmentTestUtil.startFragment(fragment);
        fragment.onLost(testLocation);
        ShadowVolley.MockRequestQueue queue = getMockRequestQueue();
        JsonObjectRequest request = (JsonObjectRequest) queue.getRequests().get(0);
        queue.deliverResponse(request, new JSONObject(MOCK_NY_TO_VT));
        Mockito.verify(pathLayerMock, Mockito.times(2)).clearPath();
        for (double[] pair : fragment.getRoute().getGeometry()) {
            Mockito.verify(pathLayerMock).addPoint(new GeoPoint(pair[0], pair[1]));
        }
    }

    @Test
    public void onLost_shouldRequestNewRoute() throws Exception {
        Location testLocation = getTestLocation(100.0, 100.0);
        FragmentTestUtil.startFragment(fragment);
        fragment.onLost(testLocation);
        assertThat(getMockRequestQueue().getRequests()).hasSize(1);
    }

    @Test
    public void onLocationChange_shouldBeLostWhenSnapToIsNull() throws Exception {
        Location testLocation = getTestLocation(40.662046, -73.987089);
        RoutingListener listenerMock = Mockito.mock(RoutingListener.class);
        fragment.setRoutingListener(listenerMock);
        fragment.setRoute(new JSONObject(MOCK_AROUND_THE_BLOCK));
        FragmentTestUtil.startFragment(fragment);
        fragment.onLocationChanged(testLocation);
        Mockito.verify(listenerMock).onLost(testLocation);
    }

    @Test
    public void onLocationChange_shouldDoNothingWhileRerouting() throws Exception {
        Location testLocation = getTestLocation(40.662046, -73.987089);
        fragment.setRoute(new JSONObject(MOCK_AROUND_THE_BLOCK));
        FragmentTestUtil.startFragment(fragment);
        fragment.onLocationChanged(testLocation);
        fragment.onLocationChanged(testLocation);
        assertThat(getMockRequestQueue().getRequests()).hasSize(1);
    }

    @Test
    public void onLocationChange_shouldBeReEnabledOnceReRoutingIsCompleted() throws Exception {
        Location testLocation = getTestLocation(40.662046, -73.987089);
        fragment.setRoute(new JSONObject(MOCK_AROUND_THE_BLOCK));
        FragmentTestUtil.startFragment(fragment);
        fragment.onLocationChanged(testLocation);
        ShadowVolley.MockRequestQueue queue = getMockRequestQueue();
        JsonObjectRequest request = (JsonObjectRequest) queue.getRequests().get(0);
        queue.deliverResponse(request, new JSONObject(MOCK_AROUND_THE_BLOCK));
        ShadowVolley.clearMockRequestQueue();
        fragment.onLocationChanged(testLocation);
        assertThat(getMockRequestQueue().getRequests()).hasSize(1);
    }

    @Test
    public void onLocationChange_shouldBeReEnabledOnceReRoutingHasError() throws Exception {
        Location testLocation = getTestLocation(40.662046, -73.987089);
        fragment.setRoute(new JSONObject(MOCK_AROUND_THE_BLOCK));
        FragmentTestUtil.startFragment(fragment);
        fragment.onLocationChanged(testLocation);
        List<Request> requestSet = getMockRequestQueue().getRequests();
        Request<JSONObject> request = requestSet.iterator().next();
        request.deliverError(null);
        ShadowVolley.clearMockRequestQueue();
        fragment.onLocationChanged(testLocation);
        assertThat(getMockRequestQueue().getRequests()).hasSize(1);
    }

    private void setVoiceNavigationEnabled(boolean enabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putBoolean(act.getString(R.string.settings_voice_navigation_key), enabled);
        prefEditor.commit();
    }

    private View getInstructionView(int position) {
        ViewGroup group = new ViewGroup(act) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
            }
        };
        return (View) fragment.pager.getAdapter().instantiateItem(group, position);
    }

    private void initTestFragment() throws Exception {
        fragment = new RouteFragment();
        fragment.setFeature(getTestFeature());
        fragment.setAct(act);
        fragment.setMapFragment(initMapFragment(act));
        fragment.setRoute(new JSONObject(MOCK_ROUTE_JSON));
        fragment.setRoutingListener(fragment);
        testInstructions = new ArrayList<Instruction>();
        testInstructions.add(getTestInstruction(0, 0));
        testInstructions.add(getTestInstruction(1, 1));
        testInstructions.add(getTestInstruction(2, 2));
        fragment.setInstructions(testInstructions);
    }

    private void setWalkingRadius(int expected) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(act.getString(R.string.settings_key_walking_advance_radius), expected);
        prefEditor.commit();
    }

    private void simulateUserPagerTouch() {
        MotionEvent motionEvent =
                MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 12f, 34f, 0);
        View.OnTouchListener listener = shadowOf(fragment.pager).getOnTouchListener();
        listener.onTouch(null, motionEvent);
    }
}
