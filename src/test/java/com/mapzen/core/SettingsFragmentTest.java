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

import android.content.SharedPreferences;
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

    @Test
    public void shouldHaveZoomCategory() throws Exception {
        PreferenceCategory category = findCategoryByIndex(1);
        assertThat(category).hasTitle(R.string.settings_zoom_title);
        assertThat(category).hasPreferenceCount(7);
    }

    @Test
    public void shouldHaveWalkingZoom() throws Exception {
        Preference preference = findPreferenceById(R.string.settings_zoom_walking_key);
        assertThat(preference).hasTitle(R.string.settings_zoom_walking_title);
        assertThat(shadowOf(preference).getDefaultValue()).isEqualTo("21");
    }

    @Test
    public void shouldHaveBikingZoom() throws Exception {
        Preference preference = findPreferenceById(R.string.settings_zoom_biking_key);
        assertThat(preference).hasTitle(R.string.settings_zoom_biking_title);
        assertThat(shadowOf(preference).getDefaultValue()).isEqualTo("20");
    }

    @Test
    public void shouldHaveDrivingZoom0to15Mph() throws Exception {
        Preference preference = findPreferenceById(R.string.settings_zoom_driving_0to15_key);
        assertThat(preference).hasTitle(R.string.settings_zoom_driving_0_to_15_title);
        assertThat(shadowOf(preference).getDefaultValue()).isEqualTo("19");
    }

    @Test
    public void shouldHaveDrivingZoom15to25Mph() throws Exception {
        Preference preference = findPreferenceById(R.string.settings_zoom_driving_15to25_key);
        assertThat(preference).hasTitle(R.string.settings_zoom_driving_15_to_25_title);
        assertThat(shadowOf(preference).getDefaultValue()).isEqualTo("18");
    }

    @Test
    public void shouldHaveDrivingZoom25to35Mph() throws Exception {
        Preference preference = findPreferenceById(R.string.settings_zoom_driving_25to35_key);
        assertThat(preference).hasTitle(R.string.settings_zoom_driving_25_to_35_title);
        assertThat(shadowOf(preference).getDefaultValue()).isEqualTo("17");
    }

    @Test
    public void shouldHaveDrivingZoom35to50Mph() throws Exception {
        Preference preference = findPreferenceById(R.string.settings_zoom_driving_35to50_key);
        assertThat(preference).hasTitle(R.string.settings_zoom_driving_35_to_50_title);
        assertThat(shadowOf(preference).getDefaultValue()).isEqualTo("16");
    }

    @Test
    public void shouldHaveDrivingZoomOver50Mph() throws Exception {
        Preference preference = findPreferenceById(R.string.settings_zoom_driving_over50_key);
        assertThat(preference).hasTitle(R.string.settings_zoom_driving_over_50_title);
        assertThat(shadowOf(preference).getDefaultValue()).isEqualTo("15");
    }

    @Test
    public void shouldDisplayDefaultValueAsSummary() throws Exception {
        assertThat(findPreferenceById(R.string.settings_zoom_walking_key)).hasSummary("21");
        assertThat(findPreferenceById(R.string.settings_zoom_biking_key)).hasSummary("20");
        assertThat(findPreferenceById(R.string.settings_zoom_driving_0to15_key)).hasSummary("19");
        assertThat(findPreferenceById(R.string.settings_zoom_driving_15to25_key)).hasSummary("18");
        assertThat(findPreferenceById(R.string.settings_zoom_driving_25to35_key)).hasSummary("17");
        assertThat(findPreferenceById(R.string.settings_zoom_driving_35to50_key)).hasSummary("16");
        assertThat(findPreferenceById(R.string.settings_zoom_driving_over50_key)).hasSummary("15");
    }

    @Test
    public void shouldDisplayUpdatedValueAsSummary() throws Exception {
        SharedPreferences prefs = fragment.getPreferenceManager().getSharedPreferences();
        SharedPreferences.Editor editPrefs = prefs.edit();

        editPrefs.putInt(fragment.getString(R.string.settings_zoom_walking_key), 1);
        editPrefs.putInt(fragment.getString(R.string.settings_zoom_biking_key), 2);
        editPrefs.putInt(fragment.getString(R.string.settings_zoom_driving_0to15_key), 3);
        editPrefs.putInt(fragment.getString(R.string.settings_zoom_driving_15to25_key), 4);
        editPrefs.putInt(fragment.getString(R.string.settings_zoom_driving_25to35_key), 5);
        editPrefs.putInt(fragment.getString(R.string.settings_zoom_driving_35to50_key), 6);
        editPrefs.putInt(fragment.getString(R.string.settings_zoom_driving_over50_key), 7);

        editPrefs.commit();
        fragment.onStart();

        assertThat(findPreferenceById(R.string.settings_zoom_walking_key)).hasSummary("1");
        assertThat(findPreferenceById(R.string.settings_zoom_biking_key)).hasSummary("2");
        assertThat(findPreferenceById(R.string.settings_zoom_driving_0to15_key)).hasSummary("3");
        assertThat(findPreferenceById(R.string.settings_zoom_driving_15to25_key)).hasSummary("4");
        assertThat(findPreferenceById(R.string.settings_zoom_driving_25to35_key)).hasSummary("5");
        assertThat(findPreferenceById(R.string.settings_zoom_driving_35to50_key)).hasSummary("6");
        assertThat(findPreferenceById(R.string.settings_zoom_driving_over50_key)).hasSummary("7");
    }

    private PreferenceCategory findCategoryByIndex(int index) {
        return (PreferenceCategory) fragment.getPreferenceScreen().getPreference(index);
    }

    private Preference findPreferenceById(int resId) {
        return fragment.getPreferenceScreen().findPreference(application.getString(resId));
    }
}
