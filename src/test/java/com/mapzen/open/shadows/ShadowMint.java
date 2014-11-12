package com.mapzen.open.shadows;

import com.splunk.mint.Mint;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import android.content.Context;

@Implements(Mint.class)
public final class ShadowMint {
    private static Exception lastHandledException;

    private ShadowMint() {
        // Do nothing.
    }

    @Implementation
    public static void initAndStartSession(Context context, String s) {
        // Do nothing.
    }

    @Implementation
    public static void addExtraData(String s, String s1) {
        // Do nothing.
    }

    @Implementation
    public static void logException(Exception e) {
        lastHandledException = e;
    }

    public static Exception getLastHandledException() {
        return lastHandledException;
    }
}
