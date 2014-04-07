package com.mapzen;

import android.app.Application;

import static android.provider.BaseColumns._ID;

public class MapzenApplication extends Application {
    public static final String PELIAS_BLOB = "text";
    private final String[] columns = {
            _ID, PELIAS_BLOB
    };
    public static final String LOG_TAG = "Mapzen: ";
    private String currentSearchTerm = "";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public String[] getColumns() {
        return columns;
    }

    public String getCurrentSearchTerm() {
        return currentSearchTerm;
    }

    public void setCurrentSearchTerm(String currentSearchTerm) {
        this.currentSearchTerm = currentSearchTerm;
    }
}
