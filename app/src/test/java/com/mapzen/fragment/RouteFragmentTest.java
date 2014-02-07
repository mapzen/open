package com.mapzen.fragment;

import android.content.Intent;
import android.location.Location;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mapzen.widget.DistanceView;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Feature;
import com.mapzen.geo.DistanceFormatter;
import com.mapzen.osrm.Instruction;
import com.mapzen.shadows.ShadowVolley;
import com.mapzen.support.MapzenTestRunner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.FragmentTestUtil;

import java.util.ArrayList;

import static com.mapzen.activity.BaseActivity.COM_MAPZEN_UPDATES_LOCATION;
import static com.mapzen.entity.Feature.NAME;
import static com.mapzen.support.TestHelper.MOCK_ROUTE_JSON;
import static com.mapzen.support.TestHelper.getTestFeature;
import static com.mapzen.support.TestHelper.initBaseActivity;
import static com.mapzen.support.TestHelper.initMapFragment;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MapzenTestRunner.class)
public class RouteFragmentTest {

    private BaseActivity act;
    private RouteFragment fragment;
    private ShadowApplication app;

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        ShadowVolley.clearMockRequestQueue();
        act = initBaseActivity();
        initTestFragment();
        app = Robolectric.getShadowApplication();
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
        attachFragment();
        View view = fragment.getView();
        assertThat(view).isNotNull();
    }

    @Test
    public void shouldHaveRoutesViewPager() throws Exception {
        attachFragment();
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        assertThat(view.findViewById(R.id.routes)).isNotNull();
    }

    @Test
    public void shouldHaveViewStepsButton() throws Exception {
        attachFragment();
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        assertThat(view.findViewById(R.id.view_steps)).isNotNull();
        assertThat((Button) view.findViewById(R.id.view_steps)).hasText("View steps");
    }

    @Test
    public void shouldShowDirectionListFragment() throws Exception {
        attachFragment();
        View view = fragment.getView();
        view.findViewById(R.id.view_steps).performClick();
        assertThat(act.getSupportFragmentManager()).hasFragmentWithTag(DirectionListFragment.TAG);
    }

    @Test
    public void onLocationChange_shouldAdvance() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(1, 1));
        instructions.add(getTestInstruction(2, 2));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);

        assertThat(fragment.getCurrentItem()).isEqualTo(0);
        fragment.onLocationChanged(getTestLocation(1, 1));
        assertThat(fragment.getCurrentItem()).isEqualTo(1);
    }

    @Test
    public void onLocationChange_shouldNotAdvance() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);

        assertThat(fragment.getCurrentItem()).isEqualTo(0);
        fragment.onLocationChanged(getTestLocation(1, 0));
        assertThat(fragment.getCurrentItem()).isEqualTo(0);
    }

    @Test
    public void onLocationChange_shouldAdvanceToNextRelevantTurn() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(6, 6));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);

        assertThat(fragment.getCurrentItem()).isEqualTo(0);
        fragment.onLocationChanged(getTestLocation(6, 6));
        assertThat(fragment.getCurrentItem()).isEqualTo(6);
    }

    @Test
    public void shouldHaveNextArrow() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        assertThat(getInstructionView(0).findViewById(R.id.route_next)).isVisible();
    }

    @Test
    public void shouldNotHaveNextArrow() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        assertThat(getInstructionView(0).findViewById(R.id.route_next)).isNotVisible();
    }

    @Test
    public void shouldHavePrevArrow() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        assertThat(getInstructionView(1).findViewById(R.id.route_previous)).isVisible();
    }

    @Test
    public void shouldNotHavePrevArrow() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        FragmentTestUtil.startFragment(fragment);
        assertThat(getInstructionView(0).findViewById(R.id.route_previous)).isNotVisible();
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
    }

    @Test
    public void onCreateView_shouldHaveTotalDistance() throws Exception {
        attachFragment();
        act.showProgressDialog();
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        DistanceView textView = (DistanceView) view.findViewById(R.id.destination_distance);
        int distance = fragment.getRoute().getTotalDistance();
        String expectedFormattedDistance = DistanceFormatter.format(distance, true);
        assertThat(textView.getText()).isEqualTo(expectedFormattedDistance);
    }

    @Test
    public void shouldDecreaseDistanceOnAdvanceViaClick() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction firstInstruction = getTestInstruction(0, 0);
        firstInstruction.setDistance(5);
        instructions.add(firstInstruction);
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        attachFragment();
        int expectedDistance = fragment.getRoute().getTotalDistance()
                - firstInstruction.getDistance();
        String expectedFormattedDistance = DistanceFormatter.format(expectedDistance, true);
        fragment.setInstructions(instructions);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        DistanceView textView = (DistanceView) view.findViewById(R.id.destination_distance);
        getInstructionView(0).findViewById(R.id.route_next).performClick();
        assertThat(textView.getText()).isEqualTo(expectedFormattedDistance);
    }

    @Test
    public void shouldDecreaseDistanceOnAdvanceViaSwipe() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        Instruction firstInstruction = getTestInstruction(0, 0);
        firstInstruction.setDistance(5);
        instructions.add(firstInstruction);
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        attachFragment();
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
    public void shouldIncreaseDistanceOnRecressViaClick() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        attachFragment();
        fragment.setInstructions(instructions);
        int expectedDistance = fragment.getRoute().getTotalDistance();
        String expectedFormattedDistance = DistanceFormatter.format(expectedDistance, true);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        DistanceView textView = (DistanceView) view.findViewById(R.id.destination_distance);
        fragment.next();
        getInstructionView(1).findViewById(R.id.route_previous).performClick();
        assertThat(textView.getText()).isEqualTo(expectedFormattedDistance);
    }

    @Test
    public void shouldIncreaseDistanceOnRecressViaSwipe() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        fragment.setInstructions(instructions);
        attachFragment();
        fragment.setInstructions(instructions);
        int expectedDistance = fragment.getRoute().getTotalDistance();
        String expectedFormattedDistance = DistanceFormatter.format(expectedDistance, true);
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        DistanceView textView = (DistanceView) view.findViewById(R.id.destination_distance);
        fragment.next();
        fragment.onPageSelected(0);
        assertThat(textView.getText()).isEqualTo(expectedFormattedDistance);
    }

    private View getInstructionView(int position) {
        ViewPager pager = (ViewPager) fragment.getView().findViewById(R.id.routes);
        ViewGroup group = new ViewGroup(act) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {

            }
        };
        return (View) pager.getAdapter().instantiateItem(group, position);
    }

    private void initTestFragment() {
        fragment = new RouteFragment();
        fragment.setFeature(getTestFeature());
        fragment.setAct(act);
        fragment.setMapFragment(initMapFragment(act));
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        fragment.setInstructions(instructions);
    }

    private void attachFragment() throws JSONException {
        FragmentTestUtil.startFragment(fragment);
        fragment.onRouteSuccess(new JSONObject(MOCK_ROUTE_JSON));
        fragment.setInstructions(new ArrayList<Instruction>());
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
}
