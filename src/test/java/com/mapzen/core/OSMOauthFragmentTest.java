package com.mapzen.core;

import com.mapzen.activity.BaseActivity;
import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.net.Uri;

import static com.mapzen.support.TestHelper.initBaseActivity;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class OSMOauthFragmentTest {
    private OSMOauthFragment fragment;
    private BaseActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = initBaseActivity();
        fragment = OSMOauthFragment.newInstance(activity);
    }

    @Test
    public void shouldSetVerifier() throws Exception {
        String expectedVerifier = "testverifier";
        Uri uri = new Uri.Builder()
                .appendQueryParameter("oauth_verifier", expectedVerifier).build();
        fragment.setVerifier(uri);
        assertThat(activity.getVerifier().getValue()).isEqualTo(expectedVerifier);
    }

}