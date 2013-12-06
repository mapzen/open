package com.mapzen;

import android.database.MatrixCursor;

public class AutoCompleteCursor extends MatrixCursor {
    public AutoCompleteCursor(String[] columnNames) {
        super(columnNames);
    }

    public RowBuilder getRowBuilder() {
        return new RowBuilder(this);
    }

    public class RowBuilder {
        private int identifier;
        private String text;
        private double lat;
        private double lon;
        private AutoCompleteCursor cursor;

        public RowBuilder(AutoCompleteCursor cursor) {
            this.cursor = cursor;
        }

        public RowBuilder setId(int i) {
            identifier = i;
            return this;
        }

        public RowBuilder setText(String text) {
            this.text = text;
            return this;
        }

        public RowBuilder setLat(double lat) {
            this.lat = lat;
            return this;
        }

        public RowBuilder setLon(double lon) {
            this.lon = lon;
            return this;
        }

        public void buildRow() {
            cursor.addRow(
                    new String[] {
                            String.valueOf(identifier),
                            text, String.valueOf(lat),
                            String.valueOf(lon) });
        }
    }
}
