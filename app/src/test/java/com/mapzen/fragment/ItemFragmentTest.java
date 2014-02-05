package com.mapzen.fragment;

import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.shadows.ShadowVolley;
import com.mapzen.support.MapzenTestRunner;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.FragmentTestUtil;

import java.util.List;

import static com.mapzen.support.TestHelper.MOCK_ROUTE_JSON;
import static com.mapzen.support.TestHelper.getTestFeature;
import static com.mapzen.support.TestHelper.initBaseActivity;
import static com.mapzen.support.TestHelper.initMapFragment;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MapzenTestRunner.class)
public class ItemFragmentTest {
    private BaseActivity act;
    private ItemFragment itemFragment;

    @Before
    public void setUp() throws Exception {
        ShadowVolley.clearMockRequestQueue();
        act = initBaseActivity();
        initItemFragment();
        FragmentTestUtil.startFragment(itemFragment);
    }

    private void initItemFragment() {
        itemFragment = new ItemFragment();
        itemFragment.setFeature(getTestFeature());
        itemFragment.setMapFragment(initMapFragment(act));
        itemFragment.setAct(act);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(itemFragment).isNotNull();
    }

    @Test
    public void shouldNotStartRouteFragment() throws Exception {
        ImageButton go = (ImageButton) itemFragment.getView().findViewById(R.id.btn_route_go);
        go.performClick();
        assertThat(itemFragment.getRouteFragment()).isNotAdded();
    }

    @Test
    public void shouldShowDialog() throws Exception {
        ImageButton go = (ImageButton) itemFragment.getView().findViewById(R.id.btn_route_go);
        go.performClick();
        assertThat(act.getProgressDialogFragment()).isAdded();
    }

    @Test
    public void shouldDismissProgressDialogOnError() throws Exception {
        ImageButton go = (ImageButton) itemFragment.getView().findViewById(R.id.btn_route_go);
        go.performClick();
        assertThat(act.getProgressDialogFragment()).isAdded();
        List<Request> requestSet = ShadowVolley.getMockRequestQueue().getRequests();
        Request<JSONObject> request = requestSet.iterator().next();
        request.deliverError(null);
        assertThat(act.getProgressDialogFragment()).isNotAdded();
    }

    @Test
    public void shouldToastOnError() throws Exception {
        ImageButton go = (ImageButton) itemFragment.getView().findViewById(R.id.btn_route_go);
        go.performClick();
        List<Request> requestSet = ShadowVolley.getMockRequestQueue().getRequests();
        Request<JSONObject> request = requestSet.iterator().next();
        request.deliverError(null);
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(act.getString(R.string.generic_server_error));
        assertThat(ShadowToast.getLatestToast()).hasDuration(Toast.LENGTH_LONG);
    }


    @Test
    public void shouldStartRouteFragment() throws Exception {
        ImageButton go = (ImageButton) itemFragment.getView().findViewById(R.id.btn_route_go);
        go.performClick();
        ShadowVolley.MockRequestQueue queue = ShadowVolley.getMockRequestQueue();
        JsonObjectRequest request = (JsonObjectRequest) queue.getRequests().get(0);
        queue.deliverResponse(request, new JSONObject(MOCK_ROUTE_JSON));
        assertThat(itemFragment.getRouteFragment()).isAdded();
    }
}
