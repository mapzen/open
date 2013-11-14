package com.mapzen.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.mapzen.R;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

import static com.mapzen.MapzenApplication.DEFAULT_COORDINATES;
import static com.mapzen.MapzenApplication.getLocation;

public class MapActivity extends Activity {
    private MapView mapView;
    StarOverlay stars = null;
    private SlidingMenu slidingMenu ;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        setupMap();
        setupSlidingMenu();
        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar ab = getActionBar();
        ab.setTitle(R.string.application_name);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setupSlidingMenu() {
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.slidingmenu_shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setMenu(R.layout.slidingmenu);
    }

    @Override
    public void onBackPressed() {
        if ( slidingMenu.isMenuShowing()) {
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

    private void setupMap() {
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        Location location = getLocation(this);
        double lat, lng;
        if(location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
        } else {
            lat = DEFAULT_COORDINATES[0];
            lng = DEFAULT_COORDINATES[1];
        }
        GeoPoint p = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
        addStar(p);

        MapController mc = mapView.getController();
        mc.setCenter(p);
        mc.setZoom(6);
    }

    private void addStar(GeoPoint point) {
        Drawable marker=getResources().getDrawable(android.R.drawable.star_big_on);
        int markerWidth = marker.getIntrinsicWidth();
        int markerHeight = marker.getIntrinsicHeight();
        marker.setBounds(0, markerHeight, markerWidth, 0);

        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

        stars = new StarOverlay(marker, resourceProxy);
        mapView.getOverlays().add(stars);
        stars.addItem(point, "myPoint1", "myPoint1");
    }

    private class StarOverlay extends ItemizedOverlay<OverlayItem> {
        private ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

        public StarOverlay(Drawable pDefaultMarker,
                           ResourceProxy pResourceProxy) {
            super(pDefaultMarker, pResourceProxy);
        }

        public void addItem(GeoPoint p, String title, String snippet){
            OverlayItem newItem = new OverlayItem(title, snippet, p);
            items.add(newItem);
            populate();
        }

        @Override
        public boolean onSnapToItem(int arg0, int arg1, Point arg2, IMapView arg3) {
            return false;
        }

        @Override
        protected OverlayItem createItem(int arg0) {
            return items.get(arg0);
        }

        @Override
        public int size() {
            return items.size();
        }
    }
}