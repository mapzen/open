package com.mapzen.core;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.annotation.Config;
import org.robolectric.util.FragmentTestUtil;

import android.preference.ListPreference;

import static org.fest.assertions.api.ANDROID.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class SettingsFragmentTest {
    private SettingsFragment mockSettingsFragment;
    private BaseActivity activity;

    @Before public void setUp() throws Exception {
        activity = TestHelper.initBaseActivity();
        mockSettingsFragment = Mockito.mock(SettingsFragment.class, Mockito.CALLS_REAL_METHODS);
        mockSettingsFragment.setActivity(activity);
        ListPreference pref = Mockito.mock(ListPreference.class);
        Mockito.when(mockSettingsFragment.findPreference(
                activity.getString(R.string.settings_key_mapstyle))).
                thenReturn(pref);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                activity.showActionBar();
                return null;
            }
        }).when(mockSettingsFragment).onStop();
    }

    @Test public void onStart_shouldHideActionbar() throws Exception {
        FragmentTestUtil.startFragment(mockSettingsFragment);
        assertThat(activity.getActionBar()).isNotShowing();
    }

    @Test public void onStop_shouldShowActionbar() throws Exception {
        FragmentTestUtil.startFragment(mockSettingsFragment);
        mockSettingsFragment.onStop();
        assertThat(activity.getActionBar()).isShowing();
    }

    @Test public void onCreate_shouldPopulateMapStyles() throws Exception {
        // TODO find a way to test this
    }

    @Test public void onSharedPreferenceChanged_shouldUpdateMapStyle() throws Exception {
        // TODO find a way to test this
    }
}
