package com.mapzen.open.util;

import com.mapzen.open.MapzenApplication;
import com.mapzen.open.support.MapzenTestRunner;
import com.mapzen.open.support.TestBaseActivity;
import com.mapzen.open.support.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import javax.inject.Inject;

import static org.fest.assertions.api.ANDROID.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class LoggerTest {

    private TestBaseActivity activity;
    @Inject SQLiteDatabase db;

    @Before
    public void setup() throws Exception {
        activity = TestHelper.initBaseActivity();
        ((MapzenApplication) activity.getApplication()).inject(this);
    }

    @Test
    public void logToDatabase_shouldWriteToDatabase() throws Exception {
        TestHelper.enableDebugMode(activity);
        Logger.logToDatabase(activity, db, "tag", "message");
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOG_ENTRIES,
                new String[]{ DatabaseHelper.COLUMN_TAG, DatabaseHelper.COLUMN_MSG},
                "tag = ? AND msg = ?", new String[] {"tag", "message"}, null, null, null);
        assertThat(cursor).hasCount(1);
    }

    @Test
    public void logToDatabase_shouldNotWriteToDatabase() throws Exception {
        Logger.logToDatabase(activity, db, "tag", "message");
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOG_ENTRIES,
                new String[]{ DatabaseHelper.COLUMN_TAG, DatabaseHelper.COLUMN_MSG},
                null, null, null, null, null);
        assertThat(cursor).hasCount(0);
    }
}
