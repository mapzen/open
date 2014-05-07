package com.mapzen.util;

import com.mapzen.activity.BaseActivity;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestBaseActivity;

import com.google.common.io.Files;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

import static com.mapzen.support.TestHelper.initBaseActivity;
import static com.mapzen.support.TestHelper.populateDatabase;
import static com.mapzen.util.DatabaseHelper.truncateDatabase;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class DebugDataSubmitterTest {
    DebugDataSubmitter submitter;
    MockWebServer server;
    BaseActivity activity;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.play();
        activity = initBaseActivity();
        populateDatabase(activity);
        submitter = new DebugDataSubmitter(activity);
        submitter.setFile(new File(activity.getDb().getPath()));
        submitter.setEndpoint(server.getUrl("/fake").toString());
    }

    @After
    public void tearDown() throws Exception {
        truncateDatabase(activity);
        server.shutdown();
    }

    @Test
    public void setEndpoint_shouldSubmitToChosenEndpoint() throws Exception {
        server.enqueue(new MockResponse());
        String expected = "/fake-endpoint";
        submitter.setEndpoint(server.getUrl(expected).toString());
        submitter.run();
        RecordedRequest request = server.takeRequest();
        assertThat(request.getPath()).isEqualTo(expected);
    }

    @Test
    public void shouldPostToEndpoint() throws Exception {
        server.enqueue(new MockResponse());
        submitter.run();
        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    public void setFile_shouldSubmitChosenFile() throws Exception {
        server.enqueue(new MockResponse());
        File file = new File(activity.getDb().getPath());
        byte[] expectedBody = Files.toByteArray(file);
        submitter.run();
        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody()).isEqualTo(expectedBody);
    }

    @Test
    public void run_shouldTruncateRoutesTable() throws Exception {
        server.enqueue(new MockResponse());
        submitter.run();
        SQLiteDatabase db = ((TestBaseActivity) activity).getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ROUTES,
                new String[] { DatabaseHelper.COLUMN_TABLE_ID },
                null, null, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void run_shouldTruncateRouteGeometryTable() throws Exception {
        server.enqueue(new MockResponse());
        submitter.run();
        SQLiteDatabase db = ((TestBaseActivity) activity).getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ROUTE_GEOMETRY,
                new String[] { DatabaseHelper.COLUMN_TABLE_ID },
                null, null, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void run_shouldTruncateLocations() throws Exception {
        server.enqueue(new MockResponse());
        submitter.run();
        SQLiteDatabase db = ((TestBaseActivity) activity).getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] { DatabaseHelper.COLUMN_TABLE_ID },
                null, null, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void run_shouldTruncateLogEntries() throws Exception {
        server.enqueue(new MockResponse());
        submitter.run();
        SQLiteDatabase db = ((TestBaseActivity) activity).getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOG_ENTRIES,
                new String[] { DatabaseHelper.COLUMN_TABLE_ID },
                null, null, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void run_shouldNotTruncateRoutesTable() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500));
        submitter.run();
        SQLiteDatabase db = ((TestBaseActivity) activity).getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ROUTES,
                new String[] { DatabaseHelper.COLUMN_TABLE_ID },
                null, null, null, null, null);
        assertThat(cursor.getCount()).isGreaterThan(0);
    }

    @Test
    public void run_shouldNotTruncateRouteGeometryTable() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500));
        submitter.run();
        SQLiteDatabase db = ((TestBaseActivity) activity).getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ROUTE_GEOMETRY,
                new String[] { DatabaseHelper.COLUMN_TABLE_ID },
                null, null, null, null, null);
        assertThat(cursor.getCount()).isGreaterThan(0);
    }

    @Test
    public void run_shouldNotTruncateLocations() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500));
        submitter.run();
        SQLiteDatabase db = ((TestBaseActivity) activity).getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] { DatabaseHelper.COLUMN_TABLE_ID },
                null, null, null, null, null);
        assertThat(cursor.getCount()).isGreaterThan(0);
    }

    @Test
    public void run_shouldNotTruncateLogEntries() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500));
        submitter.run();
        SQLiteDatabase db = ((TestBaseActivity) activity).getReadableDb();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOG_ENTRIES,
                new String[] { DatabaseHelper.COLUMN_TABLE_ID },
                null, null, null, null, null);
        assertThat(cursor.getCount()).isGreaterThan(0);
    }
}
