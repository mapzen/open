package com.mapzen.route;

import com.mapzen.TestMapzenApplication;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.osrm.Route;
import com.mapzen.osrm.Router;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestBaseActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.util.FragmentTestUtil;

import javax.inject.Inject;

import static com.mapzen.support.TestHelper.getFixture;
import static com.mapzen.support.TestHelper.getTestSimpleFeature;
import static com.mapzen.support.TestHelper.initBaseActivity;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class RoutePreviewFragmentTest {
    TestBaseActivity activity;
    RoutePreviewFragment fragment;
    SimpleFeature destination;
    @Inject Router router;
    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<Router.Callback> callback;

    @Before
    public void setup() throws Exception {
        ((TestMapzenApplication) Robolectric.application).inject(this);
        MockitoAnnotations.initMocks(this);
        activity = initBaseActivity();
        fragment = RoutePreviewFragment.newInstance(activity);
        destination = getTestSimpleFeature();
    }

    @Test
    public void shouldHaveTag() throws Exception {
        assertThat(RoutePreviewFragment.TAG).isEqualTo(RoutePreviewFragment.class.getSimpleName());
    }

    @Test
    public void newInstance_shouldReturnInstanceOfRoutePreviewFragment() throws Exception {
        assertThat(RoutePreviewFragment.newInstance(activity)).
                isInstanceOf(RoutePreviewFragment.class);
    }

    @Test
    public void setRouteTo_shouldClearMap() throws Exception {
        fragment.getMapFragment().addPoi(getTestSimpleFeature());
        fragment.createRouteTo(getTestSimpleFeature());
        assertThat(fragment.getMapFragment().getPoiLayer().size()).isEqualTo(0);
    }

    @Test
    public void setRouteTo_shouldShowLoadingDialog() throws Exception {
        fragment.createRouteTo(getTestSimpleFeature());
        assertThat(activity.getProgressDialogFragment()).isAdded();
    }

    @Test
    public void setRouteTo_successShouldDismissLoadingDialogUpon() throws Exception {
        fragment.createRouteTo(getTestSimpleFeature());
        Mockito.verify(router).setCallback(callback.capture());
        callback.getValue().success(new Route(getFixture("around_the_block")));
        assertThat(activity.getProgressDialogFragment()).isNotAdded();
    }

    @Test
    public void setRouteTo_failureShouldDismissLoadingDialogUpon() throws Exception {
        fragment.createRouteTo(getTestSimpleFeature());
        Mockito.verify(router).setCallback(callback.capture());
        callback.getValue().failure(500);
        assertThat(activity.getProgressDialogFragment()).isNotAdded();
    }

    @Test
    public void onStart_shouldHideActionbar() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        assertThat(activity.getActionBar()).isNotShowing();
    }

    @Test
    public void onDetach_shouldShowActionbar() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        fragment.onDetach();
        assertThat(activity.getActionBar()).isShowing();
    }
}
