package com.mapzen.fragment;

import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.mapzen.MapzenApplication;
import com.mapzen.MapzenTestRunner;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Feature;
import com.mapzen.shadows.ShadowVolley;
import com.mapzen.util.MapzenProgressDialog;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.FragmentTestUtil;

import java.util.Iterator;
import java.util.List;

import static com.mapzen.util.TestHelper.initBaseActivity;
import static com.mapzen.util.TestHelper.initMapFragment;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;

@RunWith(MapzenTestRunner.class)
public class PagerResultsFragmentTest {
    private PagerResultsFragment fragment;
    private MapzenApplication app;
    private BaseActivity act;

    @Before
    public void setUp() throws Exception {
        ShadowVolley.clearMockRequestQueue();
        act = initBaseActivity();
        initMapFragment(act);
        fragment = PagerResultsFragment.newInstance(act);
        FragmentTestUtil.startFragment(fragment);
        app = (MapzenApplication) application;
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(fragment).isNotNull();
    }

    @Test
    public void executeSearchOnMap_shouldCancelOutstandingAutoCompleteRequests() throws Exception {
        app.enqueueApiRequest(Feature.suggest("Empire", null, null));
        app.enqueueApiRequest(Feature.suggest("Empire State", null, null));
        fragment.executeSearchOnMap(new SearchView(app), "Empire State Building");
        List<Request> requestSet = ShadowVolley.getMockRequestQueue().getRequests();
        assertThat(requestSet).hasSize(3);

        Iterator<Request> iterator = requestSet.iterator();
        assertRequest(iterator.next());
        assertRequest(iterator.next());
        assertRequest(iterator.next());
    }

    @Test
    public void executeSearchOnMap_shouldDismissProgressDialogOnError() throws Exception {
        MapzenProgressDialog dialog = act.getProgressDialog();
        fragment.executeSearchOnMap(new SearchView(app), "Empire State Building");
        assertThat(dialog).isShowing();
        List<Request> requestSet = ShadowVolley.getMockRequestQueue().getRequests();
        Request<JSONObject> request = requestSet.iterator().next();
        request.deliverError(null);
        assertThat(dialog).isNotShowing();
    }

    @Test
    public void executeSearchOnMap_shouldToastAnError() {
        MapzenProgressDialog dialog = act.getProgressDialog();
        fragment.executeSearchOnMap(new SearchView(app), "Empire State Building");
        assertThat(dialog).isShowing();
        List<Request> requestSet = ShadowVolley.getMockRequestQueue().getRequests();
        Request<JSONObject> request = requestSet.iterator().next();
        request.deliverError(null);
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(app.getString(R.string.generic_server_error));
        assertThat(ShadowToast.getLatestToast()).hasDuration(Toast.LENGTH_LONG);
    }

    private void assertRequest(Request request) {
        if (request.getUrl().contains("Building")) {
            assertThat(request.isCanceled()).isFalse();
        } else {
            assertThat(request.isCanceled()).isTrue();
        }
    }
}
