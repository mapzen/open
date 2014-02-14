package com.mapzen.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import com.mapzen.osrm.Instruction;

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

    private final String sql = "create table " + TABLE_LOCATIONS + " ("
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
            + COLUMN_DUMP + " text not null)";

    public LocationDatabaseHelper(Context context) {
        super(context, context.getExternalFilesDir(null).getAbsolutePath() + "/" + DB_NAME,
                null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table locations");
        db.execSQL(sql);
    }

    public static String insertSQLForLocationCorrection(Location location,
            Location correctedLocation, Instruction instruction) {
        String lat = String.valueOf(location.getLatitude());
        String correctedLat = String.valueOf(correctedLocation.getLatitude());
        String instructionLat = String.valueOf(instruction.getPoint()[0]);
        String lng = String.valueOf(location.getLongitude());
        String correctedLng = String.valueOf(correctedLocation.getLongitude());
        String instructionLng = String.valueOf(instruction.getPoint()[1]);
        String full = location.toString();
        String provider = location.getProvider();
        long time = System.currentTimeMillis();
        float acc = location.getAccuracy();
        String alt = String.valueOf(location.getAltitude());

        String insertSql = String.format(Locale.ENGLISH, "insert into %s " +
                "(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) ",
                TABLE_LOCATIONS, COLUMN_PROVIDER, COLUMN_LAT, COLUMN_LNG, COLUMN_DUMP, COLUMN_ALT,
                COLUMN_ACC, COLUMN_TIME, COLUMN_CORRECTED_LAT, COLUMN_CORRECTED_LNG,
                COLUMN_INSTRUCTION_LAT, COLUMN_INSTRUCTION_LNG, COLUMN_INSTRUCTION_BEARING);
        String valuesSql = String.format(Locale.ENGLISH, "values (\"%s\", \"%s\", \"%s\", " +
                "\"%s\", \"%s\", %.2f, %d, \"%s\", \"%s\", \"%s\", \"%s\", %d)",
                provider, lat, lng, full, alt, acc, time, correctedLat, correctedLng,
                instructionLat, instructionLng, instruction.getBearing());
        return insertSql + valuesSql;
    }
}
