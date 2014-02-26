package com.mapzen.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;

@SuppressWarnings("unused")
@Implements(TextToSpeech.class)
public class ShadowTextToSpeech {
    private Context context;
    private TextToSpeech.OnInitListener listener;
    private String lastSpokenText;
    private boolean shutdown = false;

    public void __constructor__(Context context, TextToSpeech.OnInitListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public Context getContext() {
        return context;
    }

    public TextToSpeech.OnInitListener getOnInitListener() {
        return listener;
    }

    @Implementation
    public int speak(final String text, final int queueMode, final HashMap<String, String> params) {
        lastSpokenText = text;
        return TextToSpeech.SUCCESS;
    }

    public String getLastSpokenText() {
        return lastSpokenText;
    }

    public void clearLastSpokenText() {
        lastSpokenText = null;
    }

    @Implementation
    public void shutdown() {
        shutdown = true;
    }

    public boolean isShutdown() {
        return shutdown;
    }
}
