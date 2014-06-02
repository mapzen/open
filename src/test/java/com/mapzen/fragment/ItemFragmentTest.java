package com.mapzen.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.LocationManager;
import com.mapzen.MapController;
import com.mapzen.R;
import com.mapzen.support.TestBaseActivity;
import com.mapzen.osrm.Router;
import com.mapzen.osrm.Route;
import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowLocationManager;

import android.location.Location;
import android.text.TextUtils;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
import static com.mapzen.osrm.Router.getRouter;
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
    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<Router.Callback> callback;
    private ItemFragment itemFragment;
    private TestBaseActivity act;

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
        Router router = Mockito.spy(getRouter());
        RouteFragment.setRouter(router);
        itemFragment.startButton.performClick();
        Mockito.verify(router).setCallback(callback.capture());
        callback.getValue().failure(500);
        assertThat(act.getSupportFragmentManager()).doesNotHaveFragmentWithTag(RouteFragment.TAG);
    }

    @Test
    public void shouldStartRouteFragment() throws Exception {
        Router router = Mockito.spy(getRouter());
        RouteFragment.setRouter(router);
        itemFragment.startButton.performClick();
        Mockito.verify(router).setCallback(callback.capture());
        callback.getValue().success(new Route(getFixture("around_the_block")));
        assertThat(act.getSupportFragmentManager()).hasFragmentWithTag(RouteFragment.TAG);
    }

    @Test
    public void shouldDisplayGPSPromptOnRoute() throws Exception {
        Router router = Mockito.spy(getRouter());
        RouteFragment.setRouter(router);
        ShadowLocationManager manager = shadowOf(act.getLocationClient().getLocationManager());
        manager.setProviderEnabled(LocationManager.GPS_PROVIDER, false);
        itemFragment.startButton.performClick();
        assertThat(act.getSupportFragmentManager()).hasFragmentWithTag("gps_dialog");
    }

    @Test
    public void shouldNotDisplayGPSPromptOnRoute() throws Exception {
        Router router = Mockito.spy(getRouter());
        RouteFragment.setRouter(router);
        ShadowLocationManager manager = shadowOf(act.getLocationClient().getLocationManager());
        manager.setProviderEnabled(LocationManager.GPS_PROVIDER, true);
        itemFragment.startButton.performClick();
        assertThat(act.getSupportFragmentManager()).doesNotHaveFragmentWithTag("gps_dialog");
    }

    @Test
     public void shouldDismissGPSPromptOnNegativeButton() throws Exception {
        Router router = Mockito.spy(getRouter());
        RouteFragment.setRouter(router);
        ShadowLocationManager manager = shadowOf(act.getLocationClient().getLocationManager());
        manager.setProviderEnabled(LocationManager.GPS_PROVIDER, false);
        itemFragment.startButton.performClick();
        AlertDialog gpsPrompt = ShadowAlertDialog.getLatestAlertDialog();
        assertThat(act.getSupportFragmentManager()).hasFragmentWithTag("gps_dialog");
        gpsPrompt.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
        assertThat(act.getSupportFragmentManager()).doesNotHaveFragmentWithTag("gps_dialog");
    }

    @Test
    public void shouldOpenGPSSettingsOnPositiveButtonClick() throws Exception {
        Router router = Mockito.spy(getRouter());
        RouteFragment.setRouter(router);
        ShadowLocationManager manager = shadowOf(act.getLocationClient().getLocationManager());
        manager.setProviderEnabled(LocationManager.GPS_PROVIDER, false);
        itemFragment.startButton.performClick();
        AlertDialog gpsPrompt = ShadowAlertDialog.getLatestAlertDialog();
        gpsPrompt.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        Intent intent = shadowOf(act).peekNextStartedActivityForResult().intent;
        assertThat(intent).isEqualTo(new Intent(ACTION_LOCATION_SOURCE_SETTINGS));
    }

    @Test
    public void shouldDisplayGPSPromptTextCorrectly() throws Exception {
        Router router = Mockito.spy(getRouter());
        RouteFragment.setRouter(router);
        ShadowLocationManager manager = shadowOf(act.getLocationClient().getLocationManager());
        manager.setProviderEnabled(LocationManager.GPS_PROVIDER, false);
        itemFragment.startButton.performClick();
        AlertDialog gpsPrompt = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowGPSPrompt = shadowOf(gpsPrompt);
        assertThat(shadowGPSPrompt.getTitle()).isEqualTo(act.getString(R.string.gps_dialog_title));
        assertThat(shadowGPSPrompt.getMessage()).isEqualTo(
                act.getString(R.string.gps_dialog_message));
    }
}
