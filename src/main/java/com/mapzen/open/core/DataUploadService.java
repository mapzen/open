package com.mapzen.open.core;

import com.mapzen.open.MapzenApplication;
import com.mapzen.open.util.DatabaseHelper;
import com.mapzen.open.util.Logger;

import org.apache.http.Header;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static com.mapzen.open.util.DatabaseHelper.COLUMN_ALT;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_GROUP_ID;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_LAT;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_LNG;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_MSG;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_READY_FOR_UPLOAD;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_ROUTE_ID;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_SPEED;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_TABLE_ID;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_TIME;
import static com.mapzen.open.util.DatabaseHelper.COLUMN_UPLOADED;
import static com.mapzen.open.util.DatabaseHelper.TABLE_GROUPS;
import static com.mapzen.open.util.DatabaseHelper.TABLE_LOCATIONS;
import static com.mapzen.open.util.DatabaseHelper.TABLE_ROUTES;
import static com.mapzen.open.util.DatabaseHelper.TABLE_ROUTE_GROUP;
import static javax.xml.transform.OutputKeys.ENCODING;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.METHOD;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static javax.xml.transform.OutputKeys.VERSION;
import static org.apache.http.protocol.HTTP.UTF_8;

public class DataUploadService extends Service {
    private static final int MIN_RANGE_IN_METERS = 50;

    private static final String RANGE_QUERY_EXT = " from "
            + TABLE_ROUTE_GROUP + " inner join " + TABLE_LOCATIONS + " on "
            + TABLE_LOCATIONS + "." + COLUMN_ROUTE_ID + " = " + TABLE_ROUTE_GROUP + "."
            + COLUMN_ROUTE_ID + " where " + COLUMN_GROUP_ID + " = ?";

    private static final String MIN_LAT_QUERY = "select min(" + COLUMN_LAT + ")" + RANGE_QUERY_EXT;
    private static final String MAX_LAT_QUERY = "select max(" + COLUMN_LAT + ")" + RANGE_QUERY_EXT;
    private static final String MIN_LNG_QUERY = "select min(" + COLUMN_LNG + ")" + RANGE_QUERY_EXT;
    private static final String MAX_LNG_QUERY = "select max(" + COLUMN_LNG + ")" + RANGE_QUERY_EXT;

    private MapzenApplication app;

    @Inject OAuthRequestFactory requestFactory;
    @Inject SQLiteDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        app = (MapzenApplication) getApplication();
        Logger.d("DataUploadService: oncreate");
        app.inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("DataUploadService: onStartCommand");
        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                String permissionResponse = getPermissionResponse();
                if (!hasWritePermission(permissionResponse)) {
                    stopSelf();
                }
                Cursor cursor = null;
                try {
                    if (db == null) {
                        return null;
                    }
                    cursor = db.query(TABLE_GROUPS,
                            new String[] { COLUMN_TABLE_ID, COLUMN_MSG },
                            COLUMN_UPLOADED + " is null AND " + COLUMN_READY_FOR_UPLOAD + " == ?",
                            new String[] { "1" }, null, null, null);
                    while (cursor.moveToNext()) {
                        int groupIdIndex = cursor.getColumnIndex(COLUMN_TABLE_ID);
                        int routeDescription = cursor.getColumnIndex(COLUMN_MSG);
                        generateGpxXmlFor(cursor.getString(groupIdIndex),
                                cursor.getString(routeDescription));
                    }
                } catch (SQLiteDatabaseLockedException exception) {
                    Logger.d("DataUpload: database is locked lets try again later");
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                return null;
            }
        }).execute();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public DataUploadService() {
        Logger.d("DataUploadService: constructor");
    }

    public void generateGpxXmlFor(String groupId, String description) {
        if (app.getAccessToken() == null) {
            Logger.d("DataUploadService: user not logged into OSM");
            return;
        }
        Logger.d("DataUpload: generating for " + groupId);
        ByteArrayOutputStream output = null;
        try {
            DOMSource domSource = getDocument(groupId);
            if (domSource == null) {
                Logger.d("There are not enough tracking points");
                setGroupAsUploaded(groupId);
                return;
            }
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            setOutputFormat(transformer);
            output = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(output);
            transformer.transform(domSource, result);
        } catch (TransformerException e) {
            Logger.e("Transforming failed: " + e.getMessage());
        }
        Logger.d("DataUpload gonna write " + description);
        submitCompressedFile(output, groupId, description);
    }

    public void submitCompressedFile(ByteArrayOutputStream output,
                                     String routeId, String description) {
        Logger.d("DataUpload gonna submit");
        try {
            String gpxString = output.toString();
            byte[] compressedGPX = compressGPX(gpxString);
            submitTrace(description, routeId, compressedGPX);

        } catch (IOException e) {
            Logger.e("IOException occurred " + e.getMessage());
        }
    }

    public DOMSource getDocument(String groupId) {
        DOMSource domSource = null;
        try {
            DateTimeFormatter isoDateParser = ISODateTimeFormat.dateTimeNoMillis();
            String selectStatement = String.format(Locale.US,
                    "SELECT %s, %s, %s, %s, %s ", COLUMN_LAT, COLUMN_LNG, COLUMN_ALT, COLUMN_TIME,
                    COLUMN_SPEED);
            String fullQuery = selectStatement
                    + "from " + TABLE_ROUTE_GROUP
                    + " inner join " + TABLE_LOCATIONS + " on "
                    + TABLE_ROUTE_GROUP + "." + COLUMN_ROUTE_ID
                    + " = "
                    + TABLE_LOCATIONS + "." + COLUMN_ROUTE_ID
                    + " WHERE " + COLUMN_GROUP_ID + " = ? "
                    + "ORDER BY " + TABLE_LOCATIONS + "." + COLUMN_TIME
                    + " ASC";
            Logger.d("full query: " + fullQuery);
            Cursor cursor = db.rawQuery(fullQuery, new String[] { groupId });
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory
                    .newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            Element rootElement = getRootElement(document);
            document.appendChild(rootElement);
            Element trkElement = document.createElement("trk");
            rootElement.appendChild(trkElement);
            Element nameElement = document.createElement("name");
            nameElement.setTextContent("Mapzen Route");
            trkElement.appendChild(nameElement);
            Element trksegElement = document.createElement("trkseg");
            while (cursor.moveToNext()) {
                addTrackPoint(isoDateParser, cursor, document, trksegElement);
            }
            trkElement.appendChild(trksegElement);
            Element documentElement =  document.getDocumentElement();
            if (calculateMaxRange(groupId) < MIN_RANGE_IN_METERS) {
                return null;
            }
            domSource = new DOMSource(documentElement);
        } catch (ParserConfigurationException e) {
            Logger.e("Building xml failed: " + e.getMessage());
        }
        return domSource;
    }

    /**
     * Calculates distance between the two theoretical farthest points in the route.
     *
     * @return theoretical max range in meters.
     */
    private float calculateMaxRange(String groupId) {
        final Cursor minLatCursor = db.rawQuery(MIN_LAT_QUERY, new String[] { groupId });
        final Cursor maxLatCursor = db.rawQuery(MAX_LAT_QUERY, new String[] { groupId });
        final Cursor minLngCursor = db.rawQuery(MIN_LNG_QUERY, new String[] { groupId });
        final Cursor maxLngCursor = db.rawQuery(MAX_LNG_QUERY, new String[] { groupId });

        minLatCursor.moveToFirst();
        minLngCursor.moveToFirst();
        maxLatCursor.moveToFirst();
        maxLngCursor.moveToFirst();

        final double minLat = minLatCursor.getDouble(0);
        final double maxLat = maxLatCursor.getDouble(0);
        final double minLng = minLngCursor.getDouble(0);
        final double maxLng = maxLngCursor.getDouble(0);

        final Location min = new Location("temp");
        min.setLatitude(minLat);
        min.setLongitude(minLng);

        final Location max = new Location("temp");
        max.setLatitude(maxLat);
        max.setLongitude(maxLng);

        return min.distanceTo(max);
    }

    private void setOutputFormat(Transformer transformer) {
        Properties outFormat = new Properties();
        outFormat.setProperty(INDENT, "yes");
        outFormat.setProperty(METHOD, "xml");
        outFormat.setProperty(OMIT_XML_DECLARATION, "no");
        outFormat.setProperty(VERSION, "1.0");
        outFormat.setProperty(ENCODING, UTF_8);
        transformer.setOutputProperties(outFormat);
    }

    private void addTrackPoint(DateTimeFormatter isoDateParser, Cursor cursor, Document document,
                               Element trksegElement) {
        int latIndex = cursor.getColumnIndex(COLUMN_LAT);
        int lonIndex = cursor.getColumnIndex(COLUMN_LNG);
        int altIndex = cursor.getColumnIndex(COLUMN_ALT);
        int timeIndex = cursor.getColumnIndex(COLUMN_TIME);
        int speedIndex = cursor.getColumnIndex(COLUMN_SPEED);
        Element trkptElement = document.createElement("trkpt");
        Element elevationElement = document.createElement("ele");
        Element timeElement = document.createElement("time");
        Element speedElement = document.createElement("speed");
        trkptElement.setAttribute("lat", cursor.getString(latIndex));
        trkptElement.setAttribute("lon", cursor.getString(lonIndex));
        elevationElement.setTextContent(cursor.getString(altIndex));

        DateTime date = new DateTime(cursor.getLong(timeIndex));
        timeElement.setTextContent(date.toString(isoDateParser));

        speedElement.setTextContent(String.valueOf(cursor.getDouble(speedIndex)));

        trkptElement.appendChild(elevationElement);
        trkptElement.appendChild(timeElement);
        trkptElement.appendChild(speedElement);
        trksegElement.appendChild(trkptElement);
    }

    private Element getRootElement(Document document) {
        Element rootElement = document.createElement("gpx");
        rootElement.setAttribute("version", "1.0");
        rootElement.setAttribute("creator", "mapzen - start where you are http://mapzen.com");
        rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        rootElement.setAttribute("xmlns:schemaLocation",
                "http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd");
        rootElement.setAttribute("xmlns", "http://www.topografix.com/GPX/1/0");
        return rootElement;
    }

    public void submitTrace(String description, String routeId, byte[] compressedGPX) {
        Logger.d("DataUpload submitting trace");

        MultipartEntity reqEntity = new MultipartEntity();
        try {
            reqEntity.addPart("description", new StringBody(description));
            reqEntity.addPart("visibility", new StringBody("public"));
            reqEntity.addPart("public", new StringBody("1"));
            reqEntity.addPart("file", new ByteArrayBody(compressedGPX, routeId + ".gpx.gz"));
        } catch (UnsupportedEncodingException e) {
            Logger.e(e.getMessage());
        }
        ByteArrayOutputStream bos =
                new ByteArrayOutputStream((int) reqEntity.getContentLength());
        try {
            reqEntity.writeTo(bos);
        } catch (IOException e) {
            Logger.e("IOException: " + e.getMessage());
        }

        OAuthRequest request = requestFactory.getOAuthRequest();
        request.addPayload(bos.toByteArray());
        Header contentType = reqEntity.getContentType();
        request.addHeader(contentType.getName(), contentType.getValue());

        app.getOsmOauthService().signRequest(app.getAccessToken(), request);
        Response response = request.send();

        Logger.d("DataUpload Response:" + response.getBody());
        if (response.isSuccessful()) {
            setGroupAsUploaded(routeId);
            Logger.d("DataUpload: done uploading: " + routeId);
        }
    }

    public boolean hasWritePermission(String response) {
        if (response == null) {
            return false;
        }
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource xmlSource = new InputSource(new StringReader(response));
            Document doc = builder.parse(xmlSource);
            NodeList permissionList = doc.getElementsByTagName("permission");
            for (int i = 0; i < permissionList.getLength(); i++) {
                String nodeContent = permissionList.item(i).getAttributes().item(0)
                        .getTextContent();
                if (nodeContent.equals("allow_write_gpx")) {
                    return true;
                }
            }
        } catch (Exception e) {
            Logger.d(e.toString());
        }
        return false;
    }

    public String getPermissionResponse() {
        try {
            OAuthRequest request = requestFactory.getPermissionsRequest();
            app.getOsmOauthService().signRequest(app.getAccessToken(), request);
            Response response = request.send();
            return response.getBody();
        } catch (Exception e) {
            Logger.d("Unable to get permissions");
        }
        return null;
    }

    private void setGroupAsUploaded(String groupId) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_UPLOADED, 1);
        db.update(TABLE_GROUPS, cv, COLUMN_TABLE_ID + " = ?",
                new String[] { groupId });
        Cursor cursor = db.query(TABLE_ROUTE_GROUP, new String[] { COLUMN_ROUTE_ID },
                COLUMN_GROUP_ID + " = ?",
                new String[] { groupId }, null, null, null);
        while (cursor.moveToNext()) {
            int routeIdIndex = cursor.getColumnIndex(COLUMN_ROUTE_ID);
            String routeId = cursor.getString(routeIdIndex);
            db.delete(TABLE_ROUTES, COLUMN_TABLE_ID + " = ?",
                    new String[] { routeId });
            db.delete(TABLE_LOCATIONS, COLUMN_ROUTE_ID + " = ?",
                    new String[] { routeId });
        }
        db.delete(TABLE_GROUPS, COLUMN_TABLE_ID + " = ?",
                new String[] { groupId });
    }

    private byte[] compressGPX(String gpxString) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(gpxString.length());
        GZIPOutputStream gos = new GZIPOutputStream(os);
        gos.write(gpxString.getBytes());
        gos.close();
        byte[] compressedGPX = os.toByteArray();
        os.close();
        return compressedGPX;
    }
}
