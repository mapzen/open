package com.mapzen.shadows;

import android.content.Context;

import com.crashlytics.android.Crashlytics;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Custom shadow implementation for {@link com.crashlytics.android.Crashlytics}.
 */
@SuppressWarnings("unused")
@Implements(Crashlytics.class)
public class ShadowCrashlytics {
    private static Context context = null;
    private static String userIdentifier = null;

    @Implementation
    public static void start(Context context) {
        ShadowCrashlytics.context = context;
    }

    @Implementation
    public static void setUserIdentifier(String identifier) {
        userIdentifier = identifier;
    }

    public static Context getContext() {
        return context;
    }

    public static String getUserIdentifier() {
        return userIdentifier;
    }
}
