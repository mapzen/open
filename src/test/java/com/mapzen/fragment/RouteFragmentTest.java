package com.mapzen.fragment;

import com.mapzen.R;
import com.mapzen.entity.Feature;
import com.mapzen.geo.DistanceFormatter;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;
import com.mapzen.shadows.ShadowTextToSpeech;
import com.mapzen.shadows.ShadowVolley;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestBaseActivity;
import com.mapzen.util.DatabaseHelper;
import com.mapzen.util.GearAgentService;
import com.mapzen.util.GearServiceSocket;
import com.mapzen.widget.DistanceView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.oscim.core.GeoPoint;
import org.oscim.map.MapAnimator;
import org.oscim.map.TestMap;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowPopupMenu;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;

import static com.mapzen.activity.BaseActivity.COM_MAPZEN_UPDATES_LOCATION;
import static com.mapzen.entity.Feature.NAME;
import static com.mapzen.geo.DistanceFormatter.METERS_IN_ONE_FOOT;
import static com.mapzen.geo.DistanceFormatter.METERS_IN_ONE_MILE;
import static com.mapzen.support.TestHelper.MOCK_ROUTE_JSON;
import static com.mapzen.support.TestHelper.enableDebugMode;
import static com.mapzen.support.TestHelper.getTestFeature;
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
        MapAnimator animator = Mockito.mock(MapAnimator.class);
        ((TestMap) fragment.mapFragment.getMap()).setAnimator(animator);
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        Route route = fragment.getRoute();
        ArrayList<double[]> geometry = route.getGeometry();
        double[] loc = route.snapToRoute(geometry.get(2));
        Location testLocation = getTestLocation(loc[0], loc[1]);
        fragment.onLocationChanged(testLocation);
        GeoPoint expected = new GeoPoint(testLocation.getLatitude(), testLocation.getLongitude());
        Mockito.verify(animator).animateTo(expected);
    }

    @Test
    public void onLocationChange_shouldStoreOriginalLocationRecordInDatabase() throws Exception {
        enableDebugMode(act);
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        Location testLocation = getTestLocation(20.0, 30.0);
        fragment.onLocationChanged(testLocation);
        SQLiteDatabase db = act.getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[]{ DatabaseHelper.COLUMN_LAT, DatabaseHelper.COLUMN_LNG},
                null, null, null, null, null);
        assertThat(cursor).hasCount(1);
        cursor.moveToNext();
        assertThat(cursor.getString(0)).isEqualTo("20.0");
        assertThat(cursor.getString(1)).isEqualTo("30.0");
    }

    @Test
    public void onLocationChange_shouldStoreCorrectedLocationRecordInDatabase() throws Exception {
        enableDebugMode(act);
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        Location testLocation = getTestLocation(20.0, 30.0);
        fragment.onLocationChanged(testLocation);
        SQLiteDatabase db = act.getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[]{
                        DatabaseHelper.COLUMN_CORRECTED_LAT,
                        DatabaseHelper.COLUMN_CORRECTED_LNG},
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
        instructions.add(getTestInstruction(99.0, 89.0));
        instructions.add(getTestInstruction(0, 0));

        FragmentTestUtil.startFragment(fragment);
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        fragment.setInstructions(instructions);

        Location testLocation = getTestLocation(20.0, 30.0);
        fragment.onLocationChanged(testLocation);
        SQLiteDatabase db = act.getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[]{
                        DatabaseHelper.COLUMN_INSTRUCTION_LAT,
                        DatabaseHelper.COLUMN_INSTRUCTION_LNG},
                null, null, null, null, null);
        assertThat(cursor).hasCount(1);
        cursor.moveToNext();
        assertThat(cursor.getString(0)).isEqualTo("99.0");
        assertThat(cursor.getString(1)).isEqualTo("89.0");
    }

    @Test
    public void onRouteSuccess_shouldStoreRawJson() throws Exception {
        enableDebugMode(act);
        fragment.onResume();
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        fragment.onPause();
        SQLiteDatabase db = act.getReadableDb();
        Cursor cursor = db.query(TABLE_ROUTES,
                new String[] { COLUMN_RAW },
                null, null, null, null, null);
        assertThat(cursor).hasCount(1);
    }

    @Test
    public void onRouteSuccess_shouldNoteStoreRawJson() throws Exception {
        fragment.onResume();
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        fragment.onPause();
        SQLiteDatabase db = act.getReadableDb();
        Cursor cursor = db.query(TABLE_ROUTES,
                new String[]{ COLUMN_RAW},
                null, null, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void drawRoute_shouldStoreCoordinates() throws Exception {
        enableDebugMode(act);
        fragment.onResume();
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
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
        fragment.onResume();
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        fragment.onPause();
        SQLiteDatabase db = act.getReadableDb();
        Cursor cursor = db.query(TABLE_ROUTE_GEOMETRY,
                new String[]{COLUMN_ROUTE_ID},
                COLUMN_ROUTE_ID + " = ?",
                new String[] {String.valueOf(fragment.getRouteId())}, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void onLocationChange_shouldStoreInstructionBearingRecordInDatabase() throws Exception {
        enableDebugMode(act);
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(99.0, 89.0));
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        Location testLocation = getTestLocation(20.0, 30.0);
        fragment.onLocationChanged(testLocation);
        SQLiteDatabase db = act.getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[]{ DatabaseHelper.COLUMN_INSTRUCTION_BEARING},
                null, null, null, null, null);
        assertThat(cursor).hasCount(1);
        cursor.moveToNext();
        assertThat(cursor.getInt(0)).isEqualTo(instructions.get(0).getBearing());
    }

    @Test
    public void onLocationChange_shouldNotStoreDatabaseRecord() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(99.0, 89.0));
        instructions.add(getTestInstruction(0, 0));

        FragmentTestUtil.startFragment(fragment);
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
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
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        Location testLocation = getTestLocation(20.0, 30.0);
        fragment.onLocationChanged(testLocation);
        SQLiteDatabase db = act.getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[]{ COLUMN_ROUTE_ID},
                COLUMN_ROUTE_ID + " = ?",
                new String[] {String.valueOf(fragment.getRouteId())}, null, null, null);
        assertThat(cursor).hasCount(1);
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
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        act.showProgressDialog();
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        DistanceView textView = (DistanceView) view.findViewById(R.id.destination_distance);
        int distance = fragment.getRoute().getTotalDistance();
        String expectedFormattedDistance = DistanceFormatter.format(distance, true);
        assertThat(textView.getText()).isEqualTo(expectedFormattedDistance);
    }

    @Test
    public void onCreateView_shouldHaveOverflowMenu() throws Exception {
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        ImageButton overFlowMenu = (ImageButton) view.findViewById(R.id.overflow_menu);
        assertThat(overFlowMenu).isVisible();
    }

    @Test
    public void menuOnClick_shouldShowMenuOptions() throws Exception {
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        ImageButton overFlowMenu = (ImageButton) view.findViewById(R.id.overflow_menu);
        overFlowMenu.performClick();
        ShadowPopupMenu popupMenu = shadowOf(ShadowPopupMenu.getLatestPopupMenu());
        assertThat(popupMenu.isShowing()).isTrue();
    }

    @Test
    public void shouldShowDirectionListFragment() throws Exception {
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
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
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        int expectedDistance = fragment.getRoute().getTotalDistance()
                - firstInstruction.getDistance();
        String expectedFormattedDistance = DistanceFormatter.format(expectedDistance, true);
        fragment.setInstructions(instructions);
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
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        fragment.setInstructions(instructions);
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
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        Location bogusLocation = getTestLocation(23.0, 63.0);
        act.getLocationListener().onLocationChanged(bogusLocation);
        GeoPoint point = act.getMapFragment().getMeMarker().geoPoint;
        assertThat(Math.round(point.getLatitude())).isNotEqualTo(Math.round(23.0));
        assertThat(Math.round(point.getLongitude())).isNotEqualTo(Math.round(63.0));
    }

    @Test
    public void onPause_shouldActivateActivitiesMapUpdates() throws Exception {
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
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
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        assertThat(act.getDb().inTransaction()).isTrue();
    }

    @Test
    public void onPause_shouldEndDbTransaction() throws Exception {
        enableDebugMode(act);
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        fragment.onPause();
        assertThat(act.getDb().inTransaction()).isFalse();
    }

    @Test
    public void onResume_shouldNotStartDbTransaction() throws Exception {
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        assertThat(act.getDb().inTransaction()).isFalse();
    }

    @Test
    public void onLocationChange_shouldAdvance() throws Exception {
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        fragment.onResume();
        Route route = fragment.getRoute();
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        assertThat(fragment.getItemIndex()).isEqualTo(0);
        double[] point = instructions.get(2).getPoint();
        fragment.onLocationChanged(getTestLocation(point[0], point[1]));
        assertThat(fragment.getItemIndex()).isEqualTo(2);
    }

    @Test
    public void onLocationChange_shouldNotAdvance() throws Exception {
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        assertThat(fragment.getItemIndex()).isEqualTo(0);
        fragment.onLocationChanged(getTestLocation(1, 0));
        assertThat(fragment.getItemIndex()).isEqualTo(0);
    }

    @Test
    public void onResume_shouldAddProximityAlertsForEveryInstruction() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment.getProximityAlerts().size()).isEqualTo(testInstructions.size());
    }

    @Test
    public void onLocationChange_shouldFlipToPostInstructionLanguage() throws Exception {
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
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
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
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
        fragment.setMapPerspectiveForInstruction(instruction);
        TestMap map = (TestMap) act.getMapFragment().getMap();
        assertThat(map.viewport().getRotation()).isEqualTo(instruction.getRotationBearing());
    }

    @Test
    public void setMapPerspectiveForInstruction_shouldSetMapPosition() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction instruction = getTestInstruction(40.0, 100.0);
        instructions.add(instruction);
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        fragment.setMapPerspectiveForInstruction(instruction);
        TestMap map = (TestMap) act.getMap();
        assertThat(Math.round(map.getMapPosition().getLatitude())).isEqualTo(40);
        assertThat(Math.round(map.getMapPosition().getLongitude())).isEqualTo(100);
    }

    @Test
    public void getWalkingAdvanceRadius_shouldHaveDefaultValue() {
        assertThat(fragment.getWalkingAdvanceRadius())
            .isEqualTo(RouteFragment.WALKING_ADVANCE_DEFAULT_RADIUS);
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
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
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
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
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
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
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
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
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

        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        fragment.setInstructions(instructions);
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
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        ShadowTextToSpeech shadowTextToSpeech = shadowOf_(fragment.speakerbox.getTextToSpeech());
        shadowTextToSpeech.getOnInitListener().onInit(TextToSpeech.SUCCESS);
        assertThat(shadowTextToSpeech.getLastSpokenText()).isNull();
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
        testInstructions = new ArrayList<Instruction>();
        testInstructions.add(getTestInstruction(0, 0));
        testInstructions.add(getTestInstruction(1, 1));
        testInstructions.add(getTestInstruction(2, 2));
        fragment.setInstructions(testInstructions);
    }

    private Instruction getTestInstruction(double lat, double lng) throws Exception {
        String raw = "        [\n" +
                "            \"10\",\n" + // turn instruction
                "            \"19th Street\",\n" + // way
                "            160,\n" + // length in meters
                "            0,\n" + // position?
                "            0,\n" + // time in seconds
                "            \"160m\",\n" + // length with unit
                "            \"SE\",\n" + //earth direction
                "            128\n" + // azimuth
                "        ]\n";
        Instruction instruction = new Instruction(new JSONArray(raw));
        double[] point = {lat, lng};
        instruction.setPoint(point);
        return instruction;
    }

    private Location getTestLocation(double lat, double lng) {
        Location testLocation = new Location("testing");
        testLocation.setLatitude(lat);
        testLocation.setLongitude(lng);
        return testLocation;
    }

    private void setWalkingRadius(int expected) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString(act.getString(R.string.settings_key_walking_advance_radius),
                Integer.toString(expected));
        prefEditor.commit();
    }
}
