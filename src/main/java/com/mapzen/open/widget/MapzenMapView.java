package com.mapzen.open.widget;

import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.fragment.MapFragment;
import com.mapzen.open.util.MapzenStyle;

import org.oscim.android.MapView;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.renderer.MapRenderer;
import org.oscim.theme.IRenderTheme;
import org.oscim.theme.ThemeLoader;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.AttributeSet;

import javax.inject.Inject;

public class MapzenMapView extends MapView {
    @Inject SharedPreferences prefs;
    @Inject Resources res;

    private VectorTileLayer baseLayer;
    private MapFragment.PoiItemizedLayer poiMarkersLayer;

    public MapzenMapView(Context context) {
        super(context);
    }

    public MapzenMapView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public String getTileBaseSource() {
        return prefs.getString(res.getString(R.string.settings_key_mapsource),
                res.getString(R.string.settings_default_mapsource));
    }

    public void setTheme(MapzenStyle.Theme theme) {
        IRenderTheme t = ThemeLoader.load(theme);
        baseLayer.setRenderTheme(t);
        MapRenderer.setBackgroundColor(t.getMapBackground());
        map().clearMap();
    }

    private void setupMap() {
        final OSciMap4TileSource tileSource = new OSciMap4TileSource(getTileBaseSource());
        baseLayer = map().setBaseMap(tileSource);
        map().layers().add(new BuildingLayer(map(), baseLayer));
        map().layers().add(new LabelLayer(map(), baseLayer));
        setTheme(MapzenStyle.Theme.MAPZEN);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        ((MapzenApplication) getContext().getApplicationContext()).inject(this);
        setupMap();
    }
}
