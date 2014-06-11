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

import static com.mapzen.util.DatabaseHelper.COLUMN_RAW;
import static com.mapzen.util.DatabaseHelper.COLUMN_READY_FOR_UPLOAD;
import static com.mapzen.util.DatabaseHelper.COLUMN_TABLE_ID;
import static com.mapzen.util.DatabaseHelper.COLUMN_UPLOADED;
import static com.mapzen.util.DatabaseHelper.TABLE_ROUTES;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.mockito.Matchers.anyString;
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

//    @Test
//    public void onStartCommand_shouldNotAttemptToGenerateGPX() throws Exception {
//        DataUploadService spy = spy(service);
//        spy.onStartCommand(null, 0, 0);
//        verify(spy, never()).generateGpxXmlFor(anyString());
//    }
//
//    @Test
//    public void onStartCommand_shouldNotAttemptToGenerateGPXWhenUploaded() throws Exception {
//        DataUploadService spy = spy(service);
//        makeRouteUploaded("does-not-matter");
//        spy.onStartCommand(null, 0, 0);
//        verify(spy, never()).generateGpxXmlFor("does-not-matter");
//    }
//
//    @Test
//    public void onStartCommand_shouldAttemptToGenerateGPXforReadyRoute() throws Exception {
//        String expectedRouteId = "route-1";
//        makeRouteReady(expectedRouteId);
//        DataUploadService spy = spy(service);
//        spy.onStartCommand(null, 0, 0);
//        verify(spy).generateGpxXmlFor(expectedRouteId);
//    }

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

    private void makeRouteReady(String routeId) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(COLUMN_TABLE_ID, routeId);
        insertValues.put(COLUMN_RAW, "does not matter");
        insertValues.put(COLUMN_READY_FOR_UPLOAD, 1);
        app.getDb().insert(TABLE_ROUTES, null, insertValues);
    }

    private void makeRouteUploaded(String routeId) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(COLUMN_TABLE_ID, routeId);
        insertValues.put(COLUMN_RAW, "does not matter");
        insertValues.put(COLUMN_UPLOADED, 1);
        app.getDb().insert(TABLE_ROUTES, null, insertValues);
    }
}
