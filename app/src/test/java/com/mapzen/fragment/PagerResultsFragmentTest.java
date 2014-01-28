package com.mapzen.fragment;

import android.widget.SearchView;

import com.android.volley.Request;
import com.mapzen.MapzenApplication;
import com.mapzen.MapzenTestRunner;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Feature;
import com.mapzen.shadows.ShadowVolley;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

    @Before
    public void setUp() throws Exception {
        BaseActivity activity = initBaseActivity();
        initMapFragment(activity);
        fragment = PagerResultsFragment.newInstance(activity);
        FragmentTestUtil.startFragment(fragment);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(fragment).isNotNull();
    }

    @Test
    public void executeSearchOnMap_shouldCancelOutstandingAutoCompleteRequests() throws Exception {
        ShadowVolley.clearMockRequestQueue();
        MapzenApplication app = (MapzenApplication) application;
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

    private void assertRequest(Request request) {
        if (request.getUrl().contains("Building")) {
            assertThat(request.isCanceled()).isFalse();
        } else {
            assertThat(request.isCanceled()).isTrue();
        }
    }
}
