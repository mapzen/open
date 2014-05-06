package com.mapzen.shadows;

import com.bugsense.trace.BugSenseHandler;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import android.content.Context;

@Implements(BugSenseHandler.class)
public class ShadowBugSenseHandler {
    private ShadowBugSenseHandler() {
        // Do nothing.
    }

    @Implementation
    public static void initAndStartSession(Context context, String s) {
        // Do nothing.
    }

    @Implementation
    public static void addCrashExtraData(String s, String s1) {
        // Do nothing.
    }
}
