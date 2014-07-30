package com.mapzen.util;

import com.mapzen.R;
import com.mapzen.osrm.Instruction;
import com.mapzen.support.MapzenTestRunner;

import org.json.JSONArray;
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
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class VoiceNavigationControllerTest {
    private VoiceNavigationController controller;
    private ShadowTextToSpeech shadowTextToSpeech;

    @Before
    public void setUp() throws Exception {
        controller = new VoiceNavigationController(new Activity());
        shadowTextToSpeech = shadowOf(controller.speakerbox.getTextToSpeech());
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
        Instruction instruction = getTestInstruction();
        instruction.setDistance((int) Math.round(2 * METERS_IN_ONE_MILE));
        controller.playInstruction(instruction);
        assertThat(shadowTextToSpeech.getLastSpokenText())
                .isEqualTo("Head on 19th Street for 2 miles");
    }

    @Test
    public void shouldReplace1MilesWith1Mile() throws Exception {
        Instruction instruction = getTestInstruction();
        instruction.setDistance((int) Math.round(METERS_IN_ONE_MILE));
        controller.playInstruction(instruction);
        assertThat(shadowTextToSpeech.getLastSpokenText())
                .isEqualTo("Head on 19th Street for 1 mile");
    }

    @Test
    public void shouldReplaceFtWithFeet() throws Exception {
        Instruction instruction = getTestInstruction();
        instruction.setDistance((int) Math.ceil(100 * METERS_IN_ONE_FOOT));
        controller.playInstruction(instruction);
        assertThat(shadowTextToSpeech.getLastSpokenText())
                .isEqualTo("Head on 19th Street for 100 feet");
    }

    @Test
    public void shouldReplaceUnitedStatesWithUS() throws Exception {
        Instruction instruction = getTestInstructionWithName("US 101");
        controller.playInstruction(instruction);
        assertThat(shadowTextToSpeech.getLastSpokenText()).contains("U.S.");
        assertThat(shadowTextToSpeech.getLastSpokenText()).doesNotContain("US");
    }

    public static Instruction getTestInstruction() throws Exception {
        return getTestInstructionWithName("19th Street");
    }

    public static Instruction getTestInstructionWithName(String name) {
        final JSONArray jsonArray = new JSONArray();
        jsonArray.put(10).put(name).put(100).put(0).put(0).put("160m").put("SE").put(128);
        return new Instruction(jsonArray);
    }
}
