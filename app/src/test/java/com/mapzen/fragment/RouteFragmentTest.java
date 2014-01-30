package com.mapzen.fragment;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.LocationListener;
import com.mapzen.MapzenTestRunner;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.osrm.Instruction;
import com.mapzen.shadows.ShadowLocationClient;
import com.mapzen.shadows.ShadowVolley;
import com.mapzen.util.TestHelper;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscim.core.GeoPoint;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowToast;

import java.util.List;
import org.robolectric.util.FragmentTestUtil;

import java.util.ArrayList;

import static com.mapzen.util.TestHelper.initMapFragment;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MapzenTestRunner.class)
public class RouteFragmentTest {
    public static final String MOCK_ROUTE_JSON = TestHelper.getFixture("basic_route");

    private BaseActivity act;
    private RouteFragment fragment;

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        ShadowVolley.clearMockRequestQueue();
        act = Robolectric.buildActivity(BaseActivity.class).create().get();
        fragment = new RouteFragment();
        fragment.setDestination(new GeoPoint(1.0, 2.0));
        fragment.setFrom(new GeoPoint(3.0, 4.0));
        fragment.setAct(act);
        fragment.setMapFragment(initMapFragment(act));
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
        fragment.attachToActivity();
        assertThat(act.getActionBar()).isNotShowing();
    }

    @Test
    public void shouldKeepScreenOn() throws Exception {
        LayoutInflater inflater = act.getLayoutInflater();
        View view = inflater.inflate(R.layout.route_widget, null, false);
        assertThat(view.findViewById(R.id.routes)).isKeepingScreenOn();
    }

    @Test
    public void shouldBeAddedAfterCompletedApiRequest() throws Exception {
        attachFragment();
        assertThat(fragment).isAdded();
    }

    @Test
    public void shouldCreateView() throws Exception {
        attachFragment();
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
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
        View view = fragment.onCreateView(act.getLayoutInflater(), null, null);
        view.findViewById(R.id.view_steps).performClick();
        assertThat(act.getSupportFragmentManager()).hasFragmentWithTag(DirectionListFragment.TAG);
    }

    private void attachFragment() throws JSONException {
        ShadowVolley.clearMockRequestQueue();
        fragment.attachToActivity();
        ShadowVolley.MockRequestQueue queue = ShadowVolley.getMockRequestQueue();
        JsonObjectRequest request = (JsonObjectRequest) queue.getRequests().get(0);
        queue.deliverResponse(request, new JSONObject(MOCK_ROUTE_JSON));
    }

    @Test
    public void attachToActivity_shouldDismissProgressDialogOnError() throws Exception {
        fragment.attachToActivity();
        assertThat(act.getProgressDialogFragment()).isAdded();
        List<Request> requestSet = ShadowVolley.getMockRequestQueue().getRequests();
        Request<JSONObject> request = requestSet.iterator().next();
        request.deliverError(null);
        assertThat(act.getProgressDialogFragment()).isNotAdded();
    }

    @Test
    public void attachToActivity_shouldToastOnError() throws Exception {
        fragment.attachToActivity();
        List<Request> requestSet = ShadowVolley.getMockRequestQueue().getRequests();
        Request<JSONObject> request = requestSet.iterator().next();
        request.deliverError(null);
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(act.getString(R.string.generic_server_error));
        assertThat(ShadowToast.getLatestToast()).hasDuration(Toast.LENGTH_LONG);
    }

    public void onResume_shouldRequestLocationUpdates() throws Exception {
        ShadowLocationClient shadowLocationClient = Robolectric.shadowOf_(act.getLocationClient());
        shadowLocationClient.clearAll();
        fragment.onResume();
        assertThat(shadowLocationClient.hasUpdatesRequests()).isTrue();
    }

    @Test
    public void onPause_shouldRemoveLocationUpdates() throws Exception {
        ShadowLocationClient shadowLocationClient = Robolectric.shadowOf_(act.getLocationClient());
        shadowLocationClient.clearAll();
        fragment.onResume();
        assertThat(shadowLocationClient.hasUpdatesRequests()).isTrue();
        fragment.onPause();
        assertThat(shadowLocationClient.hasUpdatesRequests()).isFalse();
    }

    @Test
    public void onLocationChange_shouldToastShort() throws Exception {
        fragment.setInstructions(new ArrayList<Instruction>());
        FragmentTestUtil.startFragment(fragment);
        ShadowVolley.clearMockRequestQueue();
        ShadowLocationClient shadowLocationClient = Robolectric.shadowOf_(act.getLocationClient());
        shadowLocationClient.clearAll();
        fragment.onResume();
        fragment.attachToActivity();
        ShadowVolley.MockRequestQueue queue = ShadowVolley.getMockRequestQueue();
        JsonObjectRequest request = (JsonObjectRequest) queue.getRequests().get(0);
        JSONObject json = new JSONObject(MOCK_ROUTE_JSON);
        queue.deliverResponse(request, json);
        Location testLocation = new Location("testing");
        testLocation.setLatitude(1.0);
        testLocation.setLongitude(1.0);
        LocationListener listener = shadowLocationClient.getLocationListener();
        listener.onLocationChanged(testLocation);
        assertThat(ShadowToast.getLatestToast()).hasDuration(Toast.LENGTH_SHORT);
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
        double[] point = { lat , lng };
        instruction.setPoint(point);
        return instruction;
    }

    private Location getTestLocation(double lat, double lng) {
        Location testLocation = new Location("testing");
        testLocation.setLatitude(lat);
        testLocation.setLongitude(lng);
        return testLocation;
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
    public void onLocationChange_shouldAdvanceToNextReleventTurn() throws Exception {
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
}
