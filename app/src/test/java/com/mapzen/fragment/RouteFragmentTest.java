package com.mapzen.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.android.volley.toolbox.JsonObjectRequest;
import com.mapzen.MapzenTestRunner;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.shadows.ShadowVolley;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscim.core.GeoPoint;
import org.robolectric.Robolectric;

import static com.mapzen.util.TestHelper.initMapFragment;
import static org.fest.assertions.api.ANDROID.assertThat;

@RunWith(MapzenTestRunner.class)
public class RouteFragmentTest {
    public static final String MOCK_ROUTE_JSON = "{\n" +
            "    \"alternative_geometries\": [],\n" +
            "    \"alternative_indices\": [],\n" +
            "    \"alternative_instructions\": [],\n" +
            "    \"alternative_names\": [\n" +
            "        [\n" +
            "            \"\",\n" +
            "            \"\"\n" +
            "        ]\n" +
            "    ],\n" +
            "    \"alternative_summaries\": [],\n" +
            "    \"hint_data\": {\n" +
            "        \"checksum\": 0,\n" +
            "        \"locations\": [\n" +
            "            \"yhZmAQqkAAAdAAAA____f13_oOjJA8Y_5G5sAiwDl_s\",\n" +
            "            \"WEtmAdIPAADHAAAAjAAAAEceNbcnvuI_NaZsAkw1l_s\"\n" +
            "        ]\n" +
            "    },\n" +
            // sample geometry taken from:
            // https://developers.google.com/maps/documentation/utilities/polylinealgorithm?csw=1
            // Points: (38.5, -120.2), (40.7, -120.95), (43.252, -126.453)
            "    \"route_geometry\": \"_p~iF~ps|U_ulLnnqC_mqNvxq`@\",\n" +
            "    \"route_instructions\": [\n" +
            "        [\n" +
            "            \"10\",\n" + // turn instruction
            "            \"19th Street\",\n" + // way
            "            160,\n" + // length in meters
            "            0,\n" + // position?
            "            0,\n" + // time in seconds
            "            \"160m\",\n" + // length with unit
            "            \"SE\",\n" + //earth direction
            "            128\n" + // azimuth
            "        ],\n" +
            "        [\n" +
            "            \"7\",\n" +
            "            \"7th Avenue\",\n" +
            "            1937,\n" +
            "            1,\n" +
            "            0,\n" +
            "            \"1937m\",\n" +
            "            \"NE\",\n" +
            "            38\n" +
            "        ],\n" +
            "        [\n" +
            "            \"7\",\n" +
            "            \"Union Street\",\n" +
            "            97,\n" +
            "            29,\n" +
            "            0,\n" +
            "            \"97m\",\n" +
            "            \"NW\",\n" +
            "            297\n" +
            "        ],\n" +
            "        [\n" +
            "            \"15\",\n" +
            "            \"\",\n" +
            "            0,\n" +
            "            30,\n" +
            "            0,\n" +
            "            \"\",\n" +
            "            \"N\",\n" +
            "            0.0\n" +
            "        ]\n" +
            "    ],\n" +
            "    \"route_name\": [\n" +
            "        \"19th Street\",\n" +
            "        \"7th Avenue\"\n" +
            "    ],\n" +
            "    \"route_summary\": {\n" +
            "        \"end_point\": \"Union Street\",\n" +
            "        \"start_point\": \"19th Street\",\n" +
            "        \"total_distance\": 2195,\n" +
            "        \"total_time\": 211\n" +
            "    },\n" +
            "    \"status\": 0,\n" +
            "    \"status_message\": \"Found route between points\",\n" +
            "    \"via_indices\": [\n" +
            "        0,\n" +
            "        30\n" +
            "    ],\n" +
            "    \"via_points\": [\n" +
            "        [\n" +
            "            40.660708,\n" +
            "            -73.989332\n" +
            "        ],\n" +
            "        [\n" +
            "            40.674869,\n" +
            "            -73.9765\n" +
            "        ]\n" +
            "    ]\n" +
            "}\n";

    private BaseActivity act;
    private RouteFragment fragment;

    @Before
    public void setUp() throws Exception {
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
}
