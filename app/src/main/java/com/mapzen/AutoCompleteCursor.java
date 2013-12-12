package com.mapzen;

import android.database.MatrixCursor;

public class AutoCompleteCursor extends MatrixCursor {
    public AutoCompleteCursor(String[] columnNames) {
        super(columnNames);
    }

    public class RowBuilder {
        private int identifier;
        private String json;
    }
}
