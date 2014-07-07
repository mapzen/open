package com.mapzen.shadows;

import com.bugsense.trace.BugSenseHandler;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import android.content.Context;

@Implements(BugSenseHandler.class)
public final class ShadowBugSenseHandler {
    private static Exception lastHandledException;

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

    @Implementation
    public static void sendException(Exception e) {
        lastHandledException = e;
    }

    public static Exception getLastHandledException() {
        return lastHandledException;
    }
}
