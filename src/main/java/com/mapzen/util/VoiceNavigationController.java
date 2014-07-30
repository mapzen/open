package com.mapzen.util;

import com.mapzen.R;
import com.mapzen.osrm.Instruction;
import com.mapzen.speakerbox.Speakerbox;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;

/**
 * Manages voice navigation including spoken instructions, mute/unmute, and text substitution.
 */
public class VoiceNavigationController {
    final Activity activity;
    final Speakerbox speakerbox;

    public VoiceNavigationController(Activity activity) {
        this.activity = activity;
        this.speakerbox = new Speakerbox(activity);
        addRemixPatterns();

        if (!isVoiceNavigationEnabled()) {
            speakerbox.mute();
        }
    }

    private void addRemixPatterns() {
        speakerbox.remix(" mi", " miles");
        speakerbox.remix(" 1 miles", " 1 mile");
        speakerbox.remix(" ft", " feet");
        speakerbox.remix("US ", "U.S. ");
    }

    private boolean isVoiceNavigationEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(activity)
                .getBoolean(activity.getString(R.string.settings_voice_navigation_key), true);
    }

    public void playInstruction(Instruction instruction) {
        speakerbox.play(instruction.getFullInstruction());
    }

    public void playFlippedInstruction(Instruction instruction) {
        speakerbox.play(instruction.getFullInstructionAfterAction());
    }

    public void recalculating() {
        speakerbox.play(activity.getString(R.string.recalculating));
    }

    public void mute() {
        speakerbox.mute();
    }

    public void unmute() {
        speakerbox.unmute();
    }

    public boolean isMuted() {
        return speakerbox.isMuted();
    }

    public TextToSpeech getTextToSpeech() {
        return speakerbox.getTextToSpeech();
    }
}
