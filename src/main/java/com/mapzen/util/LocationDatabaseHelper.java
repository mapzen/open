package com.mapzen.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;

import org.apache.http.entity.ContentProducer;

import java.util.Locale;

public class LocationDatabaseHelper extends SQLiteOpenHelper {
    public static final String COLUMN_PROVIDER = "provider";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_CORRECTED_LAT = "corrected_lat";
    public static final String COLUMN_INSTRUCTION_LAT = "instruction_lat";
    public static final String COLUMN_LNG = "lng";
    public static final String COLUMN_CORRECTED_LNG = "corrected_lng";
    public static final String COLUMN_INSTRUCTION_LNG = "instruction_lng";
    public static final String COLUMN_INSTRUCTION_BEARING = "instruction_bearing";
    public static final String COLUMN_ALT = "alt";
    public static final String COLUMN_ACC = "acc";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_DUMP = "dump";
    public static final String DB_NAME = "locations.db";
    public static final String TABLE_LOCATIONS = "locations";
    public static final String TABLE_ROUTES = "routes";
    public static final String COLUMN_RAW = "raw";
    public static final String COLUMN_ROUTE_ID = "route_id";

    private final String createLocationsSql = "create table " + TABLE_LOCATIONS + " ("
            + "_id integer primary key autoincrement,"
            + COLUMN_PROVIDER + " text not null,"
            + COLUMN_LAT + " text not null,"
            + COLUMN_CORRECTED_LAT + " text,"
            + COLUMN_INSTRUCTION_LAT + " text,"
            + COLUMN_LNG + " text not null,"
            + COLUMN_CORRECTED_LNG + " text,"
            + COLUMN_INSTRUCTION_LNG + " text,"
            + COLUMN_INSTRUCTION_BEARING + " numberic,"
            + COLUMN_ALT + " text not null,"
            + COLUMN_ACC + " integer not null,"
            + COLUMN_TIME + " numeric not null,"
            + COLUMN_ROUTE_ID + " integer not null,"
            + COLUMN_DUMP + " text not null)";

    private final String createRoutesSql = "create table " + TABLE_ROUTES + " ("
            + "_id integer primary key autoincrement,"
            + COLUMN_RAW + " text not null)";

    public LocationDatabaseHelper(Context context) {
        super(context, context.getExternalFilesDir(null).getAbsolutePath() + "/" + DB_NAME,
                null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createLocationsSql);
        db.execSQL(createRoutesSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table " + TABLE_LOCATIONS);
        db.execSQL("drop table " + TABLE_ROUTES);
        db.execSQL(createLocationsSql);
        db.execSQL(createRoutesSql);
    }

    public static ContentValues valuesForLocationCorrection(Location location,
            Location correctedLocation,
            Instruction instruction, long routeId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROVIDER, location.getProvider());
        values.put(COLUMN_LAT, location.getLatitude());
        values.put(COLUMN_LNG, location.getLongitude());
        values.put(COLUMN_DUMP, location.toString());
        values.put(COLUMN_ALT, location.getAltitude());
        values.put(COLUMN_ACC, location.getAccuracy());
        values.put(COLUMN_TIME, System.currentTimeMillis());
        values.put(COLUMN_CORRECTED_LAT, correctedLocation.getLatitude());
        values.put(COLUMN_CORRECTED_LNG, correctedLocation.getLongitude());
        values.put(COLUMN_INSTRUCTION_LAT, instruction.getPoint()[0]);
        values.put(COLUMN_INSTRUCTION_LNG, instruction.getPoint()[1]);
        values.put(COLUMN_INSTRUCTION_BEARING, instruction.getBearing());
        values.put(COLUMN_ROUTE_ID, routeId);
        return values;
    }
}
