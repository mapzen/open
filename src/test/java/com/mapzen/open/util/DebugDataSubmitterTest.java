package com.mapzen.open.util;

import com.mapzen.open.MapzenApplication;
import com.mapzen.open.activity.BaseActivity;
import com.mapzen.open.support.MapzenTestRunner;

import com.google.common.io.Files;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

import javax.inject.Inject;

import static com.mapzen.open.support.TestHelper.initBaseActivity;
import static com.mapzen.open.support.TestHelper.populateDatabase;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MapzenTestRunner.class)
public class DebugDataSubmitterTest {
    DebugDataSubmitter submitter;
    MockWebServer server;
    BaseActivity activity;

    @Inject SQLiteDatabase db;

    @Before
    public void setUp() throws Exception {
        activity = initBaseActivity();
        ((MapzenApplication) activity.getApplication()).inject(this);
        server = new MockWebServer();
        server.play();
        populateDatabase(db);
        submitter = new DebugDataSubmitter(activity);
        submitter.setFile(new File(db.getPath()));
        submitter.setEndpoint(server.getUrl("/fake").toString());
    }

    @After
    public void tearDown() throws Exception {
        submitter.truncateDatabase();
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
        File file = new File(db.getPath());
        byte[] expectedBody = Files.toByteArray(file);
        submitter.run();
        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody()).isEqualTo(expectedBody);
    }

    @Test
    public void run_shouldTruncateRoutesTable() throws Exception {
        server.enqueue(new MockResponse());
        submitter.run();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ROUTES,
                new String[] { DatabaseHelper.COLUMN_TABLE_ID },
                null, null, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void run_shouldTruncateRouteGeometryTable() throws Exception {
        server.enqueue(new MockResponse());
        submitter.run();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ROUTE_GEOMETRY,
                new String[] { DatabaseHelper.COLUMN_TABLE_ID },
                null, null, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void run_shouldTruncateLocations() throws Exception {
        server.enqueue(new MockResponse());
        submitter.run();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] { DatabaseHelper.COLUMN_TABLE_ID },
                null, null, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void run_shouldTruncateLogEntries() throws Exception {
        server.enqueue(new MockResponse());
        submitter.run();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOG_ENTRIES,
                new String[] { DatabaseHelper.COLUMN_TABLE_ID },
                null, null, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void run_shouldNotTruncateRoutesTable() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500));
        submitter.run();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ROUTES,
                new String[] { DatabaseHelper.COLUMN_TABLE_ID },
                null, null, null, null, null);
        assertThat(cursor.getCount()).isGreaterThan(0);
    }

    @Test
    public void run_shouldNotTruncateRouteGeometryTable() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500));
        submitter.run();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ROUTE_GEOMETRY,
                new String[] { DatabaseHelper.COLUMN_TABLE_ID },
                null, null, null, null, null);
        assertThat(cursor.getCount()).isGreaterThan(0);
    }

    @Test
    public void run_shouldNotTruncateLocations() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500));
        submitter.run();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCATIONS,
                new String[] { DatabaseHelper.COLUMN_TABLE_ID },
                null, null, null, null, null);
        assertThat(cursor.getCount()).isGreaterThan(0);
    }

    @Test
    public void run_shouldNotTruncateLogEntries() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500));
        submitter.run();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOG_ENTRIES,
                new String[] { DatabaseHelper.COLUMN_TABLE_ID },
                null, null, null, null, null);
        assertThat(cursor.getCount()).isGreaterThan(0);
    }
}
