package com.mapzen.util;

import android.util.Log;

public final class Logger {
    private static boolean enabled = true;
    private static final String tag = "Mapzen: ";

    private Logger() {
    }

    public static void d(String msg) {
        if (enabled) {
            Log.v(tag, msg);
        }
    }
}
