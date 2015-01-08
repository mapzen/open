package com.mapzen.open.fragment;

import com.mapzen.open.MapController;
import com.mapzen.open.R;
import com.mapzen.open.TestMapzenApplication;
import com.mapzen.open.route.RoutePreviewFragment;
import com.mapzen.open.support.MapzenTestRunner;
import com.mapzen.open.support.TestBaseActivity;
import com.mapzen.osrm.Route;
import com.mapzen.osrm.Router;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.shadows.ShadowToast;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.text.TextUtils;

import javax.inject.Inject;

import static com.mapzen.open.support.TestHelper.getFixture;
import static com.mapzen.open.support.TestHelper.getTestSimpleFeature;
import static com.mapzen.open.support.TestHelper.initBaseActivity;
import static com.mapzen.open.support.TestHelper.initMapFragment;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class ItemFragmentTest {
    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<Router.Callback> callback;
    private ItemFragment itemFragment;
    private TestBaseActivity act;
    @Inject Router router;
    @Inject MapController mapController;

    @Before
    public void setUp() throws Exception {
        ((TestMapzenApplication) Robolectric.application).inject(this);
        MockitoAnnotations.initMocks(this);
        act = initBaseActivity();
        mapController.setActivity(act);
        initItemFragment();
        startFragment(itemFragment);
        mapController.setLocation(new Location(""));
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
        assertThat(itemFragment.address).hasText("Manhattan, NY");
        assertThat(itemFragment.title).hasEllipsize(TextUtils.TruncateAt.END);
        assertThat(itemFragment.title).hasMaxLines(1);
    }

    @Test
    public void shouldHaveStartButton() throws Exception {
        assertThat(itemFragment.startButton).hasText("Start");
        assertThat(shadowOf(itemFragment.startButton.getCompoundDrawables()[1])
                .getCreatedFromResId()).isEqualTo(R.drawable.ic_car_start);
    }

    @Test
    public void start_shouldPopRoutePreviewFragmentWhenFailure() throws Exception {
        itemFragment.startButton.performClick();
        Mockito.verify(router).setCallback(callback.capture());
        callback.getValue().failure(500);
        assertThat(act.getSupportFragmentManager()).
                doesNotHaveFragmentWithTag(RoutePreviewFragment.TAG);
    }

    @Test
    public void start_shouldToastFailure() throws Exception {
        itemFragment.startButton.performClick();
        Mockito.verify(router).setCallback(callback.capture());
        callback.getValue().failure(500);
        assertThat(ShadowToast.getTextOfLatestToast()).
                isEqualTo(act.getString(R.string.generic_server_error));
    }

    @Test
    public void start_shouldStartRoutePreviewFragment() throws Exception {
        itemFragment.startButton.performClick();
        Mockito.verify(router).setCallback(callback.capture());
        callback.getValue().success(new Route(getFixture("around_the_block")));
        assertThat(act.getSupportFragmentManager()).hasFragmentWithTag(RoutePreviewFragment.TAG);
    }

    @Test
    public void shouldDisplayGPSPromptOnRoute() throws Exception {
        ShadowLocationManager manager = shadowOf(getLocationManager());
        manager.setProviderEnabled(LocationManager.GPS_PROVIDER, false);
        itemFragment.startButton.performClick();
        assertThat(act.getSupportFragmentManager()).hasFragmentWithTag("gps_dialog");
    }

    @Test
    public void shouldNotDisplayGPSPromptOnRoute() throws Exception {
        ShadowLocationManager manager = shadowOf(getLocationManager());
        manager.setProviderEnabled(LocationManager.GPS_PROVIDER, true);
        itemFragment.startButton.performClick();
        assertThat(act.getSupportFragmentManager()).doesNotHaveFragmentWithTag("gps_dialog");
    }

    private LocationManager getLocationManager() {
        return (LocationManager) Robolectric.application.getSystemService(Context.LOCATION_SERVICE);
    }
}
