package com.mapzen.activity;

import android.content.Intent;
import android.net.Uri;
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

import javax.inject.Inject;

import static com.mapzen.support.TestHelper.initLoginActivity;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class InitialActivityTest {
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


}
