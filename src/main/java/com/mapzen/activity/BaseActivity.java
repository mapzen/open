package com.mapzen.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.mapquest.android.maps.MapActivity;
import com.mapzen.R;

import com.mapquest.android.maps.GeoPoint;
import com.mapquest.android.maps.MapView;
import com.mapquest.android.maps.MyLocationOverlay;

public class BaseActivity extends MapActivity {
    @Override
    protected boolean isLocationDisplayed() {
        return super.isLocationDisplayed();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onStop() {
        super.onStop();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
    }

    private MapView mapView;
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
        mapView = (MapView) findViewById(R.id.map);
        mapView.setBuiltInZoomControls(true);

        final MyLocationOverlay myLocationOverlay = new MyLocationOverlay(this, mapView);

        myLocationOverlay.enableMyLocation();
        myLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                GeoPoint currentLocation = myLocationOverlay.getMyLocation();
                mapView.getController().animateTo(currentLocation);
                mapView.getController().setZoom(14);
                mapView.getOverlays().add(myLocationOverlay);
                myLocationOverlay.setFollowing(true);
            }
        });

    }


}