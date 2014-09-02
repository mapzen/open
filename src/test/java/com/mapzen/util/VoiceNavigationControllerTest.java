package com.mapzen.util;

import com.mapzen.R;
import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowTextToSpeech;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;

import static com.mapzen.helpers.DistanceFormatter.METERS_IN_ONE_FOOT;
import static com.mapzen.helpers.DistanceFormatter.METERS_IN_ONE_MILE;
import static com.mapzen.support.TestHelper.getTestInstruction;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf_;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class VoiceNavigationControllerTest {
    private VoiceNavigationController controller;
    private ShadowTextToSpeech shadowTextToSpeech;

    @Before
    public void setUp() throws Exception {
        controller = new VoiceNavigationController(new Activity());
        shadowTextToSpeech = shadowOf_(controller.speakerbox.getTextToSpeech());
        shadowTextToSpeech.getOnInitListener().onInit(TextToSpeech.SUCCESS);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(controller).isNotNull();
    }

    @Test
    public void shouldPlay() throws Exception {
        controller.playInstruction(getTestInstruction());
        assertThat(shadowTextToSpeech.getLastSpokenText()).isNotNull();
    }

    @Test
    public void shouldMute() throws Exception {
        PreferenceManager.getDefaultSharedPreferences(application).edit()
                .putBoolean(application.getString(R.string.settings_voice_navigation_key), false)
                .commit();
        controller = new VoiceNavigationController(new Activity());
        controller.playInstruction(getTestInstruction());
        assertThat(shadowTextToSpeech.getLastSpokenText()).isNull();
    }

    @Test
    public void shouldReplaceMiWithMiles() throws Exception {
        int distanceInMeters = (int) Math.round(2 * METERS_IN_ONE_MILE);
        controller.playFlippedInstruction(getTestInstruction(distanceInMeters));
        assertThat(shadowTextToSpeech.getLastSpokenText())
                .isEqualTo("Continue on 19th Street for 2 miles");
    }

    @Test
    public void shouldReplace1MilesWith1Mile() throws Exception {
        int distanceInMeters = (int) Math.round(METERS_IN_ONE_MILE);
        controller.playFlippedInstruction(getTestInstruction(distanceInMeters));
        assertThat(shadowTextToSpeech.getLastSpokenText())
                .isEqualTo("Continue on 19th Street for 1 mile");
    }

    @Test
    public void shouldReplaceFtWithFeet() throws Exception {
        int distanceInMeters = (int) Math.ceil(100 * METERS_IN_ONE_FOOT);
        controller.playFlippedInstruction(getTestInstruction(distanceInMeters));
        assertThat(shadowTextToSpeech.getLastSpokenText())
                .isEqualTo("Continue on 19th Street for 100 feet");
    }

    @Test
    public void shouldReplaceUnitedStatesWithUS() throws Exception {
        controller.playInstruction(getTestInstruction("US 101"));
        assertThat(shadowTextToSpeech.getLastSpokenText()).contains("U.S.");
        assertThat(shadowTextToSpeech.getLastSpokenText()).doesNotContain("US");
    }

    @Test
    public void shouldIgnorePunctuation() throws Exception {
        controller.playInstruction(getTestInstruction(";"));
        assertThat(shadowTextToSpeech.getLastSpokenText()).doesNotContain(";");

        controller.playInstruction(getTestInstruction(":"));
        assertThat(shadowTextToSpeech.getLastSpokenText()).doesNotContain(":");

        controller.playInstruction(getTestInstruction(","));
        assertThat(shadowTextToSpeech.getLastSpokenText()).doesNotContain(",");
    }

    @Test
    public void shouldSetQueueModeAdd() throws Exception {
        controller.playInstruction(getTestInstruction());
        assertThat(shadowTextToSpeech.getQueueMode()).isEqualTo(TextToSpeech.QUEUE_ADD);
    }
}
