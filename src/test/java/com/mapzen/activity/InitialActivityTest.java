package com.mapzen.activity;

import com.mapzen.support.MapzenTestRunner;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.scribe.model.Token;

import static com.mapzen.support.TestHelper.initBaseActivity;
import static com.mapzen.support.TestHelper.initInitialActivity;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class InitialActivityTest {
    private BaseActivity baseActivity;

    @Before
    public void setUp() throws Exception {
        baseActivity = initBaseActivity();
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
        Token token = new Token("token", "fun");
        baseActivity.setAccessToken(token);
        InitialActivity activity = initInitialActivity();
        String activityStarted = shadowOf(activity).getNextStartedActivity()
                .getComponent().toString();
        Assertions.assertThat(activityStarted)
                .isEqualTo("ComponentInfo{com.mapzen/com.mapzen.activity.BaseActivity}");
    }
}
