package com.mapzen.util;

import android.util.Log;

public final class Logger {
    private static boolean enabled = true;
    private static final String TAG = "Mapzen: ";

    private Logger() {
    }

    public static void d(String msg) {
        if (enabled) {
            Log.v(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (enabled) {
            Log.e(TAG, msg);
        }
    }
}
