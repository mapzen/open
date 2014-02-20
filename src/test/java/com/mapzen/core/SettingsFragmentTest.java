package com.mapzen.core;

import com.mapzen.activity.BaseActivity;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.FragmentTestUtil;

import static org.fest.assertions.api.ANDROID.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class SettingsFragmentTest {
    private SettingsFragment settingsFragment;
    private BaseActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = TestHelper.initBaseActivity();
        settingsFragment = SettingsFragment.newInstance(activity);
    }

    @Test
    public void onStartShouldHideActionbar() throws Exception {
        FragmentTestUtil.startFragment(settingsFragment);
        assertThat(activity.getActionBar()).isNotShowing();
    }

    @Test
    public void onStopShouldShowActionbar() throws Exception {
        FragmentTestUtil.startFragment(settingsFragment);
        settingsFragment.onStop();
        assertThat(activity.getActionBar()).isShowing();
    }
}
