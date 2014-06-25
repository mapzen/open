package com.mapzen.util;

import com.mapzen.MapzenApplication;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestBaseActivity;
import com.mapzen.support.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class LoggerTest {

    private TestBaseActivity activity;
    private SQLiteDatabase db;

    @Before
    public void setup() throws Exception {
        activity = TestHelper.initBaseActivity();
        db = ((MapzenApplication) Robolectric.application).getDb();
    }

    @Test
    public void logToDatabase_shouldWriteToDatabase() throws Exception {
        TestHelper.enableDebugMode(activity);
        Logger.logToDatabase(activity, "tag", "message");
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOG_ENTRIES,
                new String[]{ DatabaseHelper.COLUMN_TAG, DatabaseHelper.COLUMN_MSG},
                "tag = ? AND msg = ?", new String[] {"tag", "message"}, null, null, null);
        assertThat(cursor).hasCount(1);
    }

    @Test
    public void logToDatabase_shouldNotWriteToDatabase() throws Exception {
        Logger.logToDatabase(activity, "tag", "message");
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOG_ENTRIES,
                new String[]{ DatabaseHelper.COLUMN_TAG, DatabaseHelper.COLUMN_MSG},
                null, null, null, null, null);
        assertThat(cursor).hasCount(0);
    }
}

