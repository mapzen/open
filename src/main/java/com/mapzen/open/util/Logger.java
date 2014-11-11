package com.mapzen.open.util;

import com.mapzen.open.activity.BaseActivity;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.UUID;

import javax.inject.Inject;

import static com.mapzen.open.util.DatabaseHelper.COLUMN_MSG;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_TABLE_ID;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_TAG;

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

    public static void e(String msg, Throwable throwable) {
        if (enabled) {
            Log.e(TAG, msg, throwable);
        }
    }

    public static void logToDatabase(BaseActivity activity, SQLiteDatabase db,
            String tag, String msg) {
        Log.v(TAG, tag + ": " + msg);
        if (activity.isInDebugMode()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TAG, tag);
            values.put(COLUMN_MSG, msg);
            values.put(COLUMN_TABLE_ID, UUID.randomUUID().toString());
            db.insert(DatabaseHelper.TABLE_LOG_ENTRIES, null, values);
        }
    }
}
