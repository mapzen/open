package com.mapzen.core;

import com.mapzen.MapzenApplication;
import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.ByteArrayOutputStream;

import static com.mapzen.support.TestHelper.getTestInstruction;
import static com.mapzen.support.TestHelper.getTestLocation;
import static com.mapzen.util.DatabaseHelper.COLUMN_RAW;
import static com.mapzen.util.DatabaseHelper.COLUMN_READY_FOR_UPLOAD;
import static com.mapzen.util.DatabaseHelper.COLUMN_TABLE_ID;
import static com.mapzen.util.DatabaseHelper.COLUMN_UPLOADED;
import static com.mapzen.util.DatabaseHelper.TABLE_ROUTES;
import static com.mapzen.util.DatabaseHelper.COLUMN_MSG;
import static com.mapzen.util.DatabaseHelper.TABLE_LOCATIONS;
import static com.mapzen.util.DatabaseHelper.valuesForLocationCorrection;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class DataUploadServiceTest {
    DataUploadService service;
    MapzenApplication app;

    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<OAuthRequest> callback;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        service = new DataUploadService();
        service.onCreate();
        app = (MapzenApplication) Robolectric.application;
    }

    @Test
    public void onStartCommand_shouldNotAttemptToGenerateGPX() throws Exception {
        DataUploadService spy = spy(service);
        spy.onStartCommand(null, 0, 0);
        verify(spy, never()).generateGpxXmlFor(anyString(), anyString());
    }

    @Test
    public void onStartCommand_shouldNotAttemptToGenerateGPXWhenUploaded() throws Exception {
        DataUploadService spy = spy(service);
        makeRouteUploaded("does-not-matter");
        spy.onStartCommand(null, 0, 0);
        verify(spy, never()).generateGpxXmlFor("does-not-matter", "description");
    }

    @Test
    public void onStartCommand_shouldAttemptToGenerateGPXforReadyRoute() throws Exception {
        String expectedRouteId = "route-1";
        makeRouteReady(expectedRouteId);
        DataUploadService spy = spy(service);
        spy.onStartCommand(null, 0, 0);
        verify(spy).generateGpxXmlFor(expectedRouteId, "does not matter");
    }

    @Test
    public void onStartCommand_shouldNotMarkUploaded() throws Exception {
        String expectedRouteId = "route-1";
        makeRouteReady(expectedRouteId);
        DataUploadService spy = spy(service);
        spy.onStartCommand(null, 0, 0);
        Cursor cursor = app.getDb().query(TABLE_ROUTES, new String[] {COLUMN_UPLOADED},
                COLUMN_TABLE_ID + " = ? AND " + COLUMN_UPLOADED + " = 1",
                new String[] {expectedRouteId}, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void onStartCommand_shouldMarkUploaded() throws Exception {
        Token token = new Token("stuff", "fun");
        OAuthService mockService = mock(OAuthService.class);
        ((MapzenApplication) Robolectric.application).setOsmOauthService(mockService);
        String expectedRouteId = "route-1";
        makeRouteReady(expectedRouteId);
        app.setAccessToken(token);
        DataUploadService spy = spy(service);
        OAuthRequest mockRequest = mock(OAuthRequest.class);
        doReturn(mockRequest).when(spy).getOAuthRequest();
        Response responseMock = mock(Response.class);
        doReturn(responseMock).when(mockRequest).send();
        doReturn(true).when(responseMock).isSuccessful();
        spy.onStartCommand(null, 0, 0);
        Cursor cursor = app.getDb().query(TABLE_ROUTES, new String[] {COLUMN_UPLOADED},
                COLUMN_TABLE_ID + " = ? AND " + COLUMN_UPLOADED + " = 1",
                new String[] {expectedRouteId}, null, null, null);
        assertThat(cursor).hasCount(1);
    }

    @Test
    public void onStartCommand_shouldCreateButNotUploadXML() throws Exception {
        String expectedRouteId = "route-1";
        makeRouteReady(expectedRouteId);
        DataUploadService spy = spy(service);
        spy.onStartCommand(null, 0, 0);
        verify(spy).generateGpxXmlFor(expectedRouteId, "does not matter");
    }

    @Test
    public void shouldGenerateGPX_shouldSubmit() {
        Token token = new Token("stuff", "fun");
        app.setAccessToken(token);

        String expectedRouteId = "test_route";
        String expectedRouteDescription = "does not matter";
        fillLocationsTable(expectedRouteId, 10);
        DataUploadService spy = spy(service);
        spy.onStartCommand(null, 0, 0);
        verify(spy).generateGpxXmlFor(expectedRouteId, expectedRouteDescription);
        verify(spy).getDocument(expectedRouteId);
        verify(spy).submitCompressedFile(any(ByteArrayOutputStream.class),
                eq(expectedRouteId),
                eq(expectedRouteDescription));
    }

    @Test
    public void shouldGenerateGPX_shouldNotSubmit() {
        Token token = new Token("stuff", "fun");
        app.setAccessToken(token);

        String expectedRouteId = "test_route";
        String expectedRouteDescription = "does not matter";
        fillLocationsTable(expectedRouteId, 4);
        DataUploadService spy = spy(service);
        spy.onStartCommand(null, 0, 0);
        verify(spy).generateGpxXmlFor(expectedRouteId, expectedRouteDescription);
        verify(spy).getDocument(expectedRouteId);
        verify(spy, never()).submitCompressedFile(any(ByteArrayOutputStream.class),
                eq(expectedRouteId),
                eq(expectedRouteDescription));
    }

    private void makeRouteReady(String routeId) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(COLUMN_TABLE_ID, routeId);
        insertValues.put(COLUMN_RAW, "does not matter");
        insertValues.put(COLUMN_MSG, "does not matter");
        insertValues.put(COLUMN_READY_FOR_UPLOAD, 1);
        app.getDb().insert(TABLE_ROUTES, null, insertValues);
    }

    private void fillLocationsTable(String routeId, double numPoints) {
        makeRouteReady(routeId);
        ContentValues cv;
        for (int i = 0; i < numPoints; i++) {
            try {
                cv = valuesForLocationCorrection(getTestLocation(i, i),
                        getTestLocation(i, i), getTestInstruction(i, i), routeId);
                app.getDb().insert(TABLE_LOCATIONS, null, cv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void makeRouteUploaded(String routeId) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(COLUMN_TABLE_ID, routeId);
        insertValues.put(COLUMN_RAW, "does not matter");
        insertValues.put(COLUMN_UPLOADED, 1);
        app.getDb().insert(TABLE_ROUTES, null, insertValues);
    }
}
