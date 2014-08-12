package com.mapzen.activity;

import android.content.Intent;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.TestMapzenApplication;
import com.mapzen.android.lost.LocationClient;
import com.mapzen.support.MapzenTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;
import org.scribe.model.Token;

import static com.mapzen.activity.LoginActivity.OSM_VERIFIER_KEY;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import android.net.Uri;

import javax.inject.Inject;

import static com.mapzen.support.TestHelper.initLoginActivity;
import static org.robolectric.Robolectric.shadowOf;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class LoginActivityTest {
    private LoginActivity activity;
    @Inject LocationClient locationClient;

    @Before
    public void setUp() throws Exception {
        ((TestMapzenApplication) Robolectric.application).inject(this);
        activity = initLoginActivity();
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
    public void shouldOpenSignUpPage() {
        activity.findViewById(R.id.sign_up_button).callOnClick();
        String urlOpened = shadowOf(activity).getNextStartedActivity().getDataString();
        assertThat(urlOpened).isEqualTo("https://www.openstreetmap.org/user/new");
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
        oauthTokenBuilder.appendQueryParameter(OSM_VERIFIER_KEY, "Bogus verifier");
        Uri oauthToken = oauthTokenBuilder.build();
        Intent intent = new Intent();
        intent.setData(oauthToken);
        activity.onNewIntent(intent);
        String componentOpened = shadowOf(activity).getNextStartedActivity()
                .getComponent().toString();
        assertThat(componentOpened)
                .isEqualTo("ComponentInfo{com.mapzen/com.mapzen.activity.BaseActivity}");
    }

    @Test
    public void shouldForceLoginOnTripleLogoTap() {
        for (int i = 0; i < 3; i++) {
            activity.onClickLogo();
        }
        String activityStarted = shadowOf(activity).getNextStartedActivity()
                .getComponent().toString();
        assertThat(activityStarted)
                .isEqualTo("ComponentInfo{com.mapzen/com.mapzen.activity.BaseActivity}");
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
                .isEqualTo("ComponentInfo{com.mapzen/com.mapzen.activity.BaseActivity}");
    }

    @Test
    public void shouldShowToastOnLoginError() {
        activity.unableToLogInAction();
        boolean showedToast = ShadowToast.showedToast(activity.getString(R.string.login_error));
        assertThat(showedToast).isTrue();
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
}
