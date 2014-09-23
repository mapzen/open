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
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static com.mapzen.support.TestHelper.getTestInstruction;
import static com.mapzen.support.TestHelper.getTestLocation;
import static com.mapzen.util.DatabaseHelper.COLUMN_GROUP_ID;
import static com.mapzen.util.DatabaseHelper.COLUMN_MSG;
import static com.mapzen.util.DatabaseHelper.COLUMN_RAW;
import static com.mapzen.util.DatabaseHelper.COLUMN_READY_FOR_UPLOAD;
import static com.mapzen.util.DatabaseHelper.COLUMN_ROUTE_ID;
import static com.mapzen.util.DatabaseHelper.COLUMN_TABLE_ID;
import static com.mapzen.util.DatabaseHelper.COLUMN_UPLOADED;
import static com.mapzen.util.DatabaseHelper.TABLE_GROUPS;
import static com.mapzen.util.DatabaseHelper.TABLE_LOCATIONS;
import static com.mapzen.util.DatabaseHelper.TABLE_ROUTES;
import static com.mapzen.util.DatabaseHelper.TABLE_ROUTE_GROUP;
import static com.mapzen.util.DatabaseHelper.valuesForLocationCorrection;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
        String expectedGroupId = "route-1";
        makeGroupReady(expectedGroupId);
        DataUploadService spy = spy(service);
        spy.onStartCommand(null, 0, 0);
        verify(spy).generateGpxXmlFor(expectedGroupId, "does not matter");
    }

    @Test
    public void onStartCommand_shouldNotMarkUploaded() throws Exception {
        String expectedGroupId = "route-1";
        makeGroupReady(expectedGroupId);
        service.onStartCommand(null, 0, 0);
        Cursor cursor = app.getDb().query(TABLE_GROUPS, new String[] { COLUMN_UPLOADED },
                COLUMN_TABLE_ID + " = ? AND " + COLUMN_UPLOADED + " = 1",
                new String[] { expectedGroupId }, null, null, null);
        assertThat(cursor).hasCount(0);
    }

    @Test
    public void onStartCommand_shouldMarkUploaded() throws Exception {
        Token token = new Token("stuff", "fun");
        OAuthService mockService = mock(OAuthService.class);
        ((MapzenApplication) Robolectric.application).setOsmOauthService(mockService);
        String expectedGroupId = "route-1";
        makeGroupReady(expectedGroupId);
        app.setAccessToken(token);
        service.onStartCommand(null, 0, 0);
        Cursor cursor = app.getDb().query(TABLE_GROUPS, new String[] { COLUMN_UPLOADED },
                COLUMN_TABLE_ID + " = ? AND " + COLUMN_UPLOADED + " = 1",
                new String[] { expectedGroupId }, null, null, null);
        assertThat(cursor).hasCount(1);
    }

    @Test
    public void onStartCommand_shouldCreateButNotUploadXML() throws Exception {
        String expectedGroupId = "route-1";
        makeGroupReady(expectedGroupId);
        DataUploadService spy = spy(service);
        spy.onStartCommand(null, 0, 0);
        verify(spy).generateGpxXmlFor(expectedGroupId, "does not matter");
    }

    @Test
    public void shouldGenerateGPX_shouldSubmit() throws Exception {
        Token token = new Token("stuff", "fun");
        app.setAccessToken(token);

        String expectedGroupId = "test_route";
        String expectedRouteDescription = "does not matter";
        fillLocationsTable(expectedGroupId, 10);
        DataUploadService spy = spy(service);
        spy.onStartCommand(null, 0, 0);
        verify(spy).generateGpxXmlFor(expectedGroupId, expectedRouteDescription);
        verify(spy).getDocument(expectedGroupId);
        verify(spy).submitCompressedFile(any(ByteArrayOutputStream.class),
                eq(expectedGroupId),
                eq(expectedRouteDescription));
    }

    @Test
    public void shouldGenerateGPX_shouldNotSubmit() throws Exception {
        Token token = new Token("stuff", "fun");
        app.setAccessToken(token);

        String expectedGroupId = "test_route";
        String expectedRouteDescription = "does not matter";
        fillLocationsTable(expectedGroupId, 4);
        DataUploadService spy = spy(service);
        spy.onStartCommand(null, 0, 0);
        verify(spy).generateGpxXmlFor(expectedGroupId, expectedRouteDescription);
        verify(spy).getDocument(expectedGroupId);
        verify(spy, never()).submitCompressedFile(any(ByteArrayOutputStream.class),
                eq(expectedGroupId),
                eq(expectedRouteDescription));
    }

    @Test
    public void shouldHaveLocationsFromAllRoutesInGroup() throws Exception {
        String groupId = "test-group-id";
        String routeId = "test-route-id";
        fillLocationsTable(groupId, routeId, 10);
        String anotherRoute = "second-route-id";
        fillLocationsTable(groupId, anotherRoute, 10);

        DOMSource domSource = service.getDocument(groupId);
        Document document = domSource.getNode().getOwnerDocument();
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();

        XPathExpression expr = xp.compile("//trk/trkseg/trkpt");
        Object result = expr.evaluate(document, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        assertThat(nodes.getLength()).isEqualTo(20);
    }

    @Test
    public void shouldHaveSpeedElement() throws Exception {
        String expectedSpeed = "40.0";
        String groupId = "test-group-id";
        String routeId = "test-route-id";
        fillLocationsTable(groupId, routeId, 10);
        Location loc = getTestLocation(100, 200);
        loc.setBearing(4.0f);
        loc.setSpeed(Float.valueOf(expectedSpeed));
        loc.setAccuracy(12f);
        loc.setAltitude(12f);
        loc.setTime(System.currentTimeMillis());
        ContentValues insertValues = valuesForLocationCorrection(loc, loc, getTestInstruction(0, 0),
            routeId);
        app.getDb().insert(TABLE_LOCATIONS, null, insertValues);

        String actual = getTextForXpath(groupId,
                "//trk/trkseg/trkpt[position()=last()]/speed/text()");

        assertThat(actual).isEqualTo(expectedSpeed);
    }

    @Test
    public void shouldNotHaveOauthPermissions_shouldNotCreateXML() {
        DataUploadService spy = spy(service);
        spy.onStartCommand(null, 0, 0);
        verify(spy).hasWritePermission(service.getPermissionResponse());
        verify(spy, never()).generateGpxXmlFor(anyString(),
                anyString());
    }

    @Test
    public void shouldHaveOauthPermission() throws IOException, ParserConfigurationException,
            TransformerException {
        String fakePermission = generatePermissionXml();
        assertThat(service.hasWritePermission(fakePermission)).isTrue();
    }

    private void makeGroupReady(String groupId) throws Exception {
        ContentValues insertValues = new ContentValues();
        insertValues.put(COLUMN_TABLE_ID, groupId);
        insertValues.put(COLUMN_MSG, "does not matter");
        insertValues.put(COLUMN_READY_FOR_UPLOAD, 1);
        long result = app.getDb().insert(TABLE_GROUPS, null, insertValues);
        if (result < 0) {
            throw new Exception("database insert failed");
        }
    }

    private void fillLocationsTable(String groupId, double numPoints) throws Exception {
        fillLocationsTable(groupId, "test-route-id", numPoints);
    }

    private void fillLocationsTable(String groupId, String routeId, double numPoints)
            throws Exception {
        makeGroupReady(groupId);

        ContentValues routeValues = new ContentValues();
        routeValues.put(COLUMN_TABLE_ID, routeId);
        routeValues.put(COLUMN_RAW, "does not matter");
        long routeResults = app.getDb().insert(TABLE_ROUTES, null, routeValues);

        ContentValues routeGroupValues = new ContentValues();
        routeGroupValues.put(COLUMN_ROUTE_ID, routeId);
        routeGroupValues.put(COLUMN_GROUP_ID, groupId);
        long routeGroupResults = app.getDb().insert(TABLE_ROUTE_GROUP, null, routeGroupValues);

        if (routeResults < 0 || routeGroupResults < 0) {
            throw new Exception("database insertion failed");
        }

        ContentValues cv;
        for (int i = 0; i < numPoints; i++) {
            cv = valuesForLocationCorrection(getTestLocation(i, i),
                    getTestLocation(i, i), getTestInstruction(i, i), routeId);
            app.getDb().insert(TABLE_LOCATIONS, null, cv);
        }
    }

    private void makeRouteUploaded(String routeId) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(COLUMN_TABLE_ID, routeId);
        insertValues.put(COLUMN_RAW, "does not matter");
        insertValues.put(COLUMN_UPLOADED, 1);
        app.getDb().insert(TABLE_ROUTES, null, insertValues);
    }

    public String generatePermissionXml() throws ParserConfigurationException,
            TransformerException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.newDocument();
        Element rootElement = document.createElement("Permissions");
        document.appendChild(rootElement);
        Element permission = document.createElement("permission");
        rootElement.appendChild(permission);
        Attr nameAttribute = document.createAttribute("name");
        nameAttribute.setValue("allow_write_gpx");
        permission.setAttributeNode(nameAttribute);

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new StringWriter());
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(source, result);
        return result.getWriter().toString();
    }

    private String getTextForXpath(String fakeRouteId, String xpath)
            throws XPathExpressionException {
        DOMSource domSource = service.getDocument(fakeRouteId);
        Document document = domSource.getNode().getOwnerDocument();
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
        return xp.evaluate(xpath,
                document.getDocumentElement());
    }
}
