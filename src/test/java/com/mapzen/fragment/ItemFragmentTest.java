package com.mapzen.fragment;

import com.mapzen.MapController;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.osrm.Callback;
import com.mapzen.osrm.Direction;
import com.mapzen.osrm.Route;
import com.mapzen.support.MapzenTestRunner;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.configuration.MockAnnotationProcessor;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import android.location.Location;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.List;

import static com.mapzen.support.TestHelper.MOCK_ROUTE_JSON;
import static com.mapzen.support.TestHelper.getFixture;
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
    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<Callback> callback;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
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
        Direction.Router router = Mockito.spy(Direction.getRouter());
        RouteFragment.router = router;
        itemFragment.startButton.performClick();
        Mockito.verify(router).setCallback(callback.capture());
        callback.getValue().failure(500);
        assertThat(act.getSupportFragmentManager()).doesNotHaveFragmentWithTag(RouteFragment.TAG);
    }

    @Test
    public void shouldStartRouteFragment() throws Exception {
        Direction.Router router = Mockito.spy(Direction.getRouter());
        RouteFragment.router = router;
        itemFragment.startButton.performClick();
        Mockito.verify(router).setCallback(callback.capture());
        callback.getValue().success(new Route(getFixture("around_the_block")));
        assertThat(act.getSupportFragmentManager()).hasFragmentWithTag(RouteFragment.TAG);
    }
}
