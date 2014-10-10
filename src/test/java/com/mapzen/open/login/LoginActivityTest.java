package com.mapzen.open.login;

import android.content.Intent;

import com.mapzen.open.MapController;
import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.TestMapzenApplication;
import com.mapzen.android.lost.LocationClient;
import com.mapzen.open.activity.BaseActivity;
import com.mapzen.open.support.MapzenTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;
import org.scribe.model.Token;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import android.net.Uri;

import javax.inject.Inject;

import static com.mapzen.open.support.TestHelper.initLoginActivity;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.getShadowApplication;
import static org.robolectric.Robolectric.shadowOf;
import static org.mockito.Mockito.mock;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class LoginActivityTest {
    private LoginActivity activity;
    @Inject LocationClient locationClient;
    @Inject MapController mapController;

    @Before
    public void setUp() throws Exception {
        ((TestMapzenApplication) Robolectric.application).inject(this);
        activity = initLoginActivity();
        mapController.setActivity(new BaseActivity());
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(activity).isNotNull();
    }

    @Test
    public void shouldNotHaveActionBar() throws Exception {
        assertThat(activity.getActionBar()).isNull();
    }

    @Test
    public void shouldOpenLoginPage() {
        Token testToken = new Token("Bogus_key", "Bogus_verfier");
        activity.openLoginPage(testToken);
        String urlOpened = shadowOf(activity).getNextStartedActivity().getDataString();
        assertThat(urlOpened)
                .contains("http://www.openstreetmap.org/oauth/authorize?oauth_token=");
    }

    @Test
    public void shouldNotStoreLoginPageInHistory() {
        Token testToken = new Token("Bogus_key", "Bogus_verfier");
        activity.openLoginPage(testToken);
        assertThat(shadowOf(activity).getNextStartedActivity().getFlags()
                & Intent.FLAG_ACTIVITY_NO_HISTORY).isEqualTo(Intent.FLAG_ACTIVITY_NO_HISTORY);
    }

    @Test
    public void shouldStartBaseActivityOnTokenReturn() {
        Uri.Builder oauthTokenBuilder = new Uri.Builder();
        oauthTokenBuilder.appendQueryParameter(LoginActivity.OSM_VERIFIER_KEY, "Bogus verifier");
        Uri oauthToken = oauthTokenBuilder.build();
        Intent intent = new Intent();
        intent.setData(oauthToken);
        activity.onNewIntent(intent);
        String componentOpened = shadowOf(activity).getNextStartedActivity()
                .getComponent().toString();
        assertThat(componentOpened)
                .isEqualTo("ComponentInfo{com.mapzen.open/com.mapzen.open.activity.BaseActivity}");
    }

    @Test
    public void shouldForwardGeoIntentDataToBaseActivityAfterLogin() throws Exception {
        String data = "http://maps.example.com/";
        Intent geoIntent = new Intent();
        geoIntent.setData(Uri.parse(data));
        LoginActivity activity = buildActivity(LoginActivity.class)
                .withIntent(geoIntent)
                .create()
                .start()
                .resume()
                .visible()
                .get();

        Uri.Builder oauthTokenBuilder = new Uri.Builder();
        oauthTokenBuilder.appendQueryParameter(LoginActivity.OSM_VERIFIER_KEY, "Bogus verifier");
        Uri oauthToken = oauthTokenBuilder.build();
        Intent oauthIntent = new Intent();
        oauthIntent.setData(oauthToken);
        activity.onNewIntent(oauthIntent);
        assertThat(getShadowApplication().getNextStartedActivity()).hasData(data);
    }

    @Test
    public void shouldForceLoginOnTripleLogoTap() {
        for (int i = 0; i < 3; i++) {
            activity.onClickLogo();
        }
        String activityStarted = shadowOf(activity).getNextStartedActivity()
                .getComponent().toString();
        assertThat(activityStarted)
                .isEqualTo("ComponentInfo{com.mapzen.open/com.mapzen.open.activity.BaseActivity}");
        assertThat(((MapzenApplication) activity.getApplication()).wasForceLoggedIn()).isTrue();
    }

    @Test
    public void shouldNotForceLoginOnDoubleLogoTap() {
        for (int i = 0; i < 2; i++) {
            activity.onClickLogo();
        }
        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertThat(intent).isNull();
        assertThat(((MapzenApplication) activity.getApplication()).wasForceLoggedIn()).isFalse();
    }

    @Test
    public void shouldShowMapOnLoginError() {
        activity.unableToLogInAction();
        String activityStarted = shadowOf(activity).getNextStartedActivity()
                .getComponent().toString();
        assertThat(activityStarted)
                .isEqualTo("ComponentInfo{com.mapzen.open/com.mapzen.open.activity.BaseActivity}");
    }

    @Test
    public void shouldShowToastOnLoginError() {
        activity.unableToLogInAction();
        boolean showedToast = ShadowToast.showedToast(activity.getString(R.string.login_error));
        assertThat(showedToast).isTrue();
    }

    @Test
    public void shouldNotDisplayLocationError() {
        mapController.setActivity(new BaseActivity());
        LocationClient mock = mock(LocationClient.class);
        Mockito.when(mock.getLastLocation()).thenReturn(null);
        assertThat(ShadowToast.getTextOfLatestToast()).isNull();
    }

    @Test
    public void onResume_shouldConnectLocationClient() {
        assertThat(locationClient.isConnected()).isTrue();
    }

    @Test
    public void onPause_shouldDisConnectLocationClient() {
        activity.onPause();
        assertThat(locationClient.isConnected()).isFalse();
    }

    @Test
    public void shouldFadeOutSplashScreen() throws Exception {
        Robolectric.shadowOf(activity.splash.getAnimation()).invokeEnd();
        assertThat(activity.splash).isNotVisible();
    }

    @Test
    public void shouldFadeInViewPager() throws Exception {
        Robolectric.shadowOf(activity.viewPager.getAnimation()).invokeEnd();
        assertThat(activity.viewPager).isVisible();
    }

    @Test
    public void loginFlowShouldHaveViewPagerWithCountThree() throws Exception {
        assertThat(activity.viewPager.getAdapter()).hasCount(3);
    }
}
