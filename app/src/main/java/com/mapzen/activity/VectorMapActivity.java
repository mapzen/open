package com.mapzen.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import org.oscim.android.MapActivity;
import org.oscim.android.MapView;
import org.oscim.layers.tile.vector.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.theme.InternalRenderTheme;
import org.oscim.tiling.source.TileSource;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

public class VectorMapActivity extends MapActivity {
    private SlidingMenu slidingMenu;
    MapView mMapView;
    VectorTileLayer mBaseLayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vector);
        setupActionbar();
        setupSlidingMenu();
        setupMap();
    }

    @Override
    public void onBackPressed() {
        if ( slidingMenu.isMenuShowing() ) {
            slidingMenu.toggle();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            this.slidingMenu.toggle();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.slidingMenu.toggle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupSlidingMenu() {
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        slidingMenu.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.slidingmenu_shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        slidingMenu.setMenu(R.layout.slidingmenu);
    }

    private void setupActionbar() {
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setupMap() {
        mMapView = (MapView) findViewById(R.id.map);
        TileSource tileSource = new OSciMap4TileSource();
        tileSource.setOption(getString(R.string.tiles_source_url_key), getString(R.string.tiles_source_url));
        mBaseLayer = mMap.setBaseMap(tileSource);
        mMap.getLayers().add(new BuildingLayer(mMap, mBaseLayer.getTileLayer()));
        mMap.getLayers().add(new LabelLayer(mMap, mBaseLayer.getTileLayer()));
        mMap.setTheme(InternalRenderTheme.DEFAULT);
        mMap.setMapPosition(MapzenApplication.getLocationPosition(this));
    }
}
