package com.mapzen.util;

import com.mapzen.activity.BaseActivity;

import android.content.ContentValues;
import android.util.Log;

import static com.mapzen.util.LocationDatabaseHelper.COLUMN_MSG;
import static com.mapzen.util.LocationDatabaseHelper.COLUMN_TAG;

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

    public static void logToDatabase(BaseActivity activity, String tag, String msg) {
        if (activity.isInDebugMode()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TAG, tag);
            values.put(COLUMN_MSG, msg);
            activity.getDb().insert(LocationDatabaseHelper.TABLE_LOG_ENTRIES, null, values);
        }
    }
}
