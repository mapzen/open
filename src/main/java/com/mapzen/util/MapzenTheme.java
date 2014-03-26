package com.mapzen.util;

import org.oscim.backend.AssetAdapter;
import org.oscim.theme.ThemeFile;

import java.io.InputStream;

public enum MapzenTheme implements ThemeFile {

    MAPZEN("styles/mapzen.xml"),
    VTM("styles/vtm.xml"),
    TRONRENDER("styles/tronrender.xml"),
    MAPNIK("styles/carto.xml"),
    OSMARENDER("styles/osmarender.xml");

    private final String path;

    private MapzenTheme(String path) {
        this.path = path;
    }

    @Override
    public InputStream getRenderThemeAsStream() {
        return AssetAdapter.g.openFileAsStream(path);
    }
}
