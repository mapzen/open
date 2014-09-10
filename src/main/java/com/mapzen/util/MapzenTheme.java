package com.mapzen.util;

import com.google.common.io.Files;

import org.oscim.backend.AssetAdapter;
import org.oscim.theme.ThemeFile;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public enum MapzenTheme implements ThemeFile {

    MAPZEN("styles/mapzen.xml");

    private final String path;
    private Context context;

    private MapzenTheme(String path) {
        this.path = path;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public InputStream getRenderThemeAsStream() {
        String pathToFile = context.getExternalFilesDir(null).getAbsolutePath() + "/assets/" + path;
        File f = new File(pathToFile);
        InputStream value = null;
        if (f.exists()) {
            try {
                value = new ByteArrayInputStream(Files.toByteArray(f));
            } catch (IOException e) {
                Logger.e("Cannot read styles from: " + pathToFile);
                value = AssetAdapter.g.openFileAsStream(path);
            }
        } else {
            value = AssetAdapter.g.openFileAsStream(path);
        }
        return value;
    }
}
