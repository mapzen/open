package com.mapzen.core;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.FragmentTestUtil;

import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class SettingsFragmentTest {
    private SettingsFragment fragment;
    private BaseActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = TestHelper.initBaseActivity();
        fragment = SettingsFragment.newInstance(activity);
        FragmentTestUtil.startFragment(fragment);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(fragment).isNotNull();
    }

    @Test
    public void onStart_shouldHideActionbar() throws Exception {
        activity.showActionBar();
        fragment.onStart();
        assertThat(activity.getActionBar()).isNotShowing();
    }

    @Test
    public void onStop_shouldShowActionbar() throws Exception {
        activity.hideActionBar();
        fragment.onStop();
        assertThat(activity.getActionBar()).isShowing();
    }

    @Test
    public void shouldHaveDebugCategory() throws Exception {
        PreferenceCategory category = findCategoryByIndex(0);
        assertThat(category).hasTitle(R.string.settings_debug_title);
        assertThat(category).hasPreferenceCount(6);
    }

    @Test
    public void shouldHaveDebugPreference() throws Exception {
        Preference preference = findPreferenceById(R.string.settings_key_debug);
        assertThat(preference).hasSummary(R.string.settings_debug_mode_summary);
        assertThat(preference).hasTitle(R.string.settings_debug_mode);
    }

    @Test
    public void shouldHaveEnableFixedLocationPreference() throws Exception {
        Preference preference = findPreferenceById(R.string.settings_key_enable_fixed_location);
        assertThat(preference).hasSummary(R.string.settings_enable_fixed_location_summary);
        assertThat(preference).hasTitle(R.string.settings_enable_fixed_location_title);
    }

    @Test
    public void shouldHaveFixedLocationPreference() throws Exception {
        Preference preference = findPreferenceById(R.string.settings_fixed_location_key);
        assertThat(preference).hasSummary(R.string.settings_fixed_location_summary);
        assertThat(preference).hasTitle(R.string.settings_fixed_location_title);
        assertThat((EditTextPreference) preference)
                .hasDialogTitle(R.string.settings_fixed_location_dialog_title);

        assertThat(shadowOf(preference).getDefaultValue()).isEqualTo("40.7443, -73.9903");
    }

    @Test
    public void shouldHaveVoiceNavigationPreference() throws Exception {
        Preference preference = findPreferenceById(R.string.settings_voice_navigation_key);
        assertThat(preference).hasSummary(R.string.settings_voice_navigation_summary);
        assertThat(preference).hasTitle(R.string.settings_voice_navigation_title);

        assertThat(shadowOf(preference).getDefaultValue()).isEqualTo("true");
    }

    @Test
    public void shouldHaveAdvanceRadiusPreference() throws Exception {
        Preference preference = findPreferenceById(R.string.settings_key_walking_advance_radius);
        assertThat(preference).hasSummary(R.string.settings_advance_radius_summary);
        assertThat(preference).hasTitle(R.string.settings_advance_radius);
        assertThat((EditTextPreference) preference)
                .hasDialogTitle(R.string.settings_advance_radius_dialog_title);

        assertThat(shadowOf(preference).getDefaultValue()).isEqualTo("30");
    }

    @Test
    public void shouldHaveMapSourcePreference() throws Exception {
        Preference preference = findPreferenceById(R.string.settings_key_mapsource);
        assertThat(preference).hasSummary(R.string.settings_mapsource_summary);
        assertThat(preference).hasTitle(R.string.settings_mapsource);
        assertThat((EditTextPreference) preference)
                .hasDialogTitle(R.string.settings_mapsource_dialog_title);

        assertThat(shadowOf(preference).getDefaultValue())
                .isEqualTo("http://vector.test.mapzen.com/vector/lite");
    }

    private PreferenceCategory findCategoryByIndex(int index) {
        return (PreferenceCategory) fragment.getPreferenceScreen().getPreference(index);
    }

    private Preference findPreferenceById(int resId) {
        return fragment.getPreferenceScreen().findPreference(application.getString(resId));
    }
}
