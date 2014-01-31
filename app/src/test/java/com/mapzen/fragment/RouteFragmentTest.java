package com.mapzen.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mapzen.MapzenTestRunner;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.shadows.ShadowVolley;
import com.mapzen.util.TestHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscim.core.GeoPoint;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowToast;

import java.util.List;

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
}
