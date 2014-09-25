package com.mapzen.activity;

import com.mapzen.MapzenApplication;
import com.mapzen.TestMapzenApplication;
import com.mapzen.support.MapzenTestRunner;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.fest.assertions.api.Assertions;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.scribe.model.Token;

import android.content.Intent;
import android.net.Uri;

import javax.inject.Inject;

import static com.mapzen.support.TestHelper.initBaseActivity;
import static com.mapzen.support.TestHelper.initInitialActivity;
import static com.mapzen.util.MixpanelHelper.Event.INITIAL_LAUNCH;
import static com.mapzen.util.MixpanelHelper.Payload.LOGGED_IN_KEY;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.getShadowApplication;
import static org.robolectric.Robolectric.shadowOf;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class InitialActivityTest {
    private BaseActivity baseActivity;
    @Inject MixpanelAPI mixpanelAPI;

    @Before
    public void setUp() throws Exception {
        ((TestMapzenApplication) Robolectric.application).inject(this);
        baseActivity = initBaseActivity();
    }

    @Test
    public void onCreate_shouldTrackAsLoggedOut() throws Exception {
        initInitialActivity();
        JSONObject expectedPayload = new JSONObject();
        expectedPayload.put(LOGGED_IN_KEY, String.valueOf(false));
        Mockito.verify(mixpanelAPI).track(eq(INITIAL_LAUNCH), refEq(expectedPayload));
    }

    @Test
    public void onCreate_shouldTrackAsLoggedIn() throws Exception {
        ((MapzenApplication) application).setAccessToken(new Token("hokus", "bogus"));
        initInitialActivity();
        JSONObject expectedPayload = new JSONObject();
        expectedPayload.put(LOGGED_IN_KEY, String.valueOf(true));
        Mockito.verify(mixpanelAPI).track(eq(INITIAL_LAUNCH), refEq(expectedPayload));
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        InitialActivity activity = initInitialActivity();
        assertThat(activity).isNotNull();
    }

    @Test
    public void shouldNotHaveActionBar() throws Exception {
        InitialActivity activity = initInitialActivity();
        assertThat(activity.getActionBar()).isNull();
    }

    @Test
    public void onPreviouslyNotLoggedIn_ShouldOpenLoginActivity() throws Exception {
        InitialActivity activity = initInitialActivity();
        String activityStarted = shadowOf(activity).getNextStartedActivity()
                .getComponent().toString();
        Assertions.assertThat(activityStarted)
                .isEqualTo("ComponentInfo{com.mapzen/com.mapzen.activity.LoginActivity}");
    }

    @Test
    public void onPreviouslyLoggedIn_ShouldOpenBaseActivity() {
        simulateLogin();
        InitialActivity activity = initInitialActivity();
        String activityStarted = shadowOf(activity).getNextStartedActivity()
                .getComponent().toString();
        Assertions.assertThat(activityStarted)
                .isEqualTo("ComponentInfo{com.mapzen/com.mapzen.activity.BaseActivity}");
    }

    @Test
    public void shouldForwardIntentDataToLoginActivity() throws Exception {
        String data = "http://maps.example.com/";
        Intent intent = new Intent();
        intent.setData(Uri.parse(data));
        buildActivity(InitialActivity.class).withIntent(intent).create();
        assertThat(getShadowApplication().getNextStartedActivity()).hasData(data);
    }

    @Test
    public void shouldForwardIntentDataToBaseActivity() throws Exception {
        simulateLogin();
        String data = "http://maps.example.com/";
        Intent intent = new Intent();
        intent.setData(Uri.parse(data));
        buildActivity(InitialActivity.class).withIntent(intent).create();
        assertThat(getShadowApplication().getNextStartedActivity()).hasData(data);
    }

    private void simulateLogin() {
        baseActivity.setAccessToken(new Token("token", "fun"));
    }
}
