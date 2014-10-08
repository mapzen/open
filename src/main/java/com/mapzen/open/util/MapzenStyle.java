package com.mapzen.open.util;

import com.google.common.io.Files;

import org.oscim.backend.AssetAdapter;
import org.oscim.theme.ThemeFile;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MapzenStyle {
    public enum Theme implements ThemeFile {

        MAPZEN("styles/mapzen.xml");

        private final String path;

        private Theme(String path) {
            this.path = path;
        }

        @Override
        public InputStream getRenderThemeAsStream() {
            return AssetAdapter.g.openFileAsStream(path);
        }

    }

    public static class MapzenAssetAdapter extends AssetAdapter {
        private Context context;

        public MapzenAssetAdapter(Context context) {
            super();
            this.context = context;
        }

        @Override
        public InputStream openFileAsStream(String fileName) {
            InputStream value = null;
            String pathToFile = context.getExternalFilesDir(null).getAbsolutePath()
                    + "/assets/" + fileName;
            File f = new File(pathToFile);
            try {
                if (f.exists()) {
                    value = new ByteArrayInputStream(Files.toByteArray(f));
                } else {
                    value = context.getAssets().open(fileName);
                }
            } catch (IOException e) {
                Logger.e("opening file failed: " + e.toString());
                return null;
            }
            return value;
        }
    }
}
