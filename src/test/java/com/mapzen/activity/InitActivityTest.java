package com.mapzen.activity;

import android.content.Intent;
import com.mapzen.R;
import com.mapzen.support.MapzenTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.scribe.model.Token;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import android.net.Uri;

import static com.mapzen.support.TestHelper.initInitActivity;
import static org.robolectric.Robolectric.shadowOf;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class InitActivityTest {
    private InitActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = initInitActivity();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(activity).isNotNull();
    }

    @Test
    public void shouldNotShowActionbar() throws Exception {
        assertThat(activity.getActionBar()).isNotShowing();
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
        assertThat(urlOpened).contains("http://www.openstreetmap.org/oauth/authorize?oauth_token=");
    }

    @Test
    public void shouldStartBaseActivityOnTokenReturn() {
        Uri.Builder oauthTokenBuilder = new Uri.Builder();
        oauthTokenBuilder.appendQueryParameter("oauth_token", "Bogus token");
        oauthTokenBuilder.appendQueryParameter("oauth_verifier", "Bogus verifier");
        Uri oauthToken = oauthTokenBuilder.build();
        Intent intent = new Intent();
        intent.setData(oauthToken);
        activity.onNewIntent(intent);
        String componentOpened = shadowOf(activity).getNextStartedActivity()
                .getComponent().toString();
        assertThat(componentOpened).isEqualTo(
                "ComponentInfo{com.mapzen/com.mapzen.activity.BaseActivity}");
    }
}
