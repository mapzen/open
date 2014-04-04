package com.mapzen.fragment;

import com.mapzen.MapController;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.support.MapzenTestRunner;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import android.location.Location;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.List;

import static com.mapzen.shadows.ShadowVolley.MockRequestQueue;
import static com.mapzen.shadows.ShadowVolley.clearMockRequestQueue;
import static com.mapzen.shadows.ShadowVolley.getMockRequestQueue;
import static com.mapzen.support.TestHelper.MOCK_ROUTE_JSON;
import static com.mapzen.support.TestHelper.getTestSimpleFeature;
import static com.mapzen.support.TestHelper.initBaseActivity;
import static com.mapzen.support.TestHelper.initMapFragment;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class ItemFragmentTest {
    private ItemFragment itemFragment;
    private BaseActivity act;

    @Before
    public void setUp() throws Exception {
        clearMockRequestQueue();
        act = initBaseActivity();
        initItemFragment();
        startFragment(itemFragment);
        MapController.getMapController().setLocation(new Location(""));
    }

    private void initItemFragment() {
        itemFragment = new ItemFragment();
        itemFragment.setSimpleFeature(getTestSimpleFeature());
        itemFragment.setMapFragment(initMapFragment(act));
        itemFragment.setAct(act);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(itemFragment).isNotNull();
    }

    @Test
    public void shouldHaveTitle() throws Exception {
        assertThat(itemFragment.title).hasText("Test SimpleFeature");
        assertThat(itemFragment.title).hasEllipsize(TextUtils.TruncateAt.END);
        assertThat(itemFragment.title).hasMaxLines(1);
    }

    @Test
    public void shouldHaveAddress() throws Exception {
        assertThat(itemFragment.address).hasText("New York, NY");
        assertThat(itemFragment.title).hasEllipsize(TextUtils.TruncateAt.END);
        assertThat(itemFragment.title).hasMaxLines(1);
    }

    @Test
    public void shouldHaveStartButton() throws Exception {
        assertThat(itemFragment.startButton).hasText("Start");
        assertThat(shadowOf(itemFragment.startButton.getCompoundDrawables()[1])
                .getCreatedFromResId()).isEqualTo(R.drawable.ic_lets_go);
    }

    @Test
    public void shouldNotStartRouteFragment() throws Exception {
        itemFragment.startButton.performClick();
        assertThat(act.getSupportFragmentManager()).doesNotHaveFragmentWithTag(RouteFragment.TAG);
    }

    @Test
    public void shouldShowDialog() throws Exception {
        itemFragment.startButton.performClick();
        assertThat(act.getProgressDialogFragment()).isAdded();
    }

    @Test
    public void shouldDismissProgressDialogOnError() throws Exception {
        itemFragment.startButton.performClick();
        assertThat(act.getProgressDialogFragment()).isAdded();
        List<Request> requestSet = getMockRequestQueue().getRequests();
        Request<JSONObject> request = requestSet.iterator().next();
        request.deliverError(null);
        assertThat(act.getProgressDialogFragment()).isNotAdded();
    }

    @Test
    public void shouldToastOnError() throws Exception {
        itemFragment.startButton.performClick();
        List<Request> requestSet = getMockRequestQueue().getRequests();
        Request<JSONObject> request = requestSet.iterator().next();
        request.deliverError(null);
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(
                act.getString(R.string.generic_server_error));
        assertThat(ShadowToast.getLatestToast()).hasDuration(Toast.LENGTH_LONG);
    }

    @Test
    public void shouldStartRouteFragment() throws Exception {
        itemFragment.startButton.performClick();
        MockRequestQueue queue = getMockRequestQueue();
        JsonObjectRequest request = (JsonObjectRequest) queue.getRequests().get(0);
        queue.deliverResponse(request, new JSONObject(MOCK_ROUTE_JSON));
        assertThat(act.getSupportFragmentManager()).hasFragmentWithTag(RouteFragment.TAG);
    }
}
