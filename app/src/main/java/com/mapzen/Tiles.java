package com.mapzen;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

public class Tiles extends OnlineTileSourceBase {

    private static String mapBoxBaseUrl = "http://api.tiles.mapbox.com/v3/";
    private String mapBoxKey;

    protected Tiles(String key) {
        super(key, ResourceProxy.string.base, 1, 20, 256, ".png", mapBoxBaseUrl);
    }

    public static Tiles getTiles(String key) {
        Tiles tiles = new Tiles(key);
        tiles.setMapBoxKey(key);
        return tiles;
    }

    protected void setMapBoxKey(String key) {
        mapBoxKey = key;
    }

    @Override
    public String getTileURLString(MapTile mapTile) {
        StringBuffer url = new StringBuffer(getBaseUrl());
        url.append(mapBoxKey);
        url.append("/");
        url.append(mapTile.getZoomLevel());
        url.append("/");
        url.append(mapTile.getX());
        url.append("/");
        url.append(mapTile.getY());
        url.append(".png");
        return url.toString();
    }
}
