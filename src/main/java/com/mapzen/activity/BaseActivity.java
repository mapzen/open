package com.mapzen.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.Tiles;
import com.mapzen.entity.Place;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

import static com.mapzen.MapzenApplication.LOG_TAG;
import static com.mapzen.MapzenApplication.setZoomLevel;

public class BaseActivity extends Activity {
    private MapView mapView;
    private MapController mapController;
    private SlidingMenu slidingMenu;
    private StarOverlay stars;
    private MyLocationOverlay myLocationOverlay;
    private GeoNamesAdapter geoNamesAdapter;

    private static final String[] COLUMNS = {
        BaseColumns._ID,
        SearchManager.SUGGEST_COLUMN_TEXT_1,
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        setupMap();
        setupSlidingMenu();
        setupActionBar();
        setupLocateMeButton();
    }

    private void setupLocateMeButton() {
        Button locateMe = (Button)findViewById(R.id.locate_me);
        locateMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeoPoint currentLocation = myLocationOverlay.getMyLocation();
                if (currentLocation != null) {
                    mapController.animateTo(currentLocation);
                }
            }
        });
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

    @Override
    public void startActivity(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            BoundingBoxE6 boundingBox = mapView.getBoundingBox();
            double[] box = {
                ((double) boundingBox.getLonWestE6()) / 1E6,
                ((double) boundingBox.getLatNorthE6()) / 1E6,
                ((double) boundingBox.getLonEastE6()) / 1E6,
                ((double) boundingBox.getLatSouthE6()) / 1E6,
            };
            intent.putExtra("box", box);
        }
        super.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                final MatrixCursor cursor = new MatrixCursor(COLUMNS);
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://api-pelias-test.mapzen.com/suggest/" + newText;
                Log.v(LOG_TAG, url);
                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                    Log.v(LOG_TAG, jsonArray.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj;
                        try {
                            obj = (JSONObject) jsonArray.get(i);
                            cursor.addRow(new String[] {String.valueOf(i), obj.getString("text")});
                        } catch (Exception e) {

                        }
                    }
                    geoNamesAdapter.swapCursor(cursor);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                    }
                });
                queue.add(jsonArrayRequest);

                return false;
            }
        });
        if (geoNamesAdapter == null) {
            MatrixCursor cursor = new MatrixCursor(COLUMNS);
            geoNamesAdapter = new GeoNamesAdapter(getActionBar().getThemedContext(), cursor);
        }
        searchView.setSuggestionsAdapter(geoNamesAdapter);
        return true;
    }

    private int getZoomLevel() {
        return MapzenApplication.getZoomLevel();
    }

    private void setupMap() {
        Intent intent = getIntent();
        final Bundle bundle = intent.getExtras();
        mapView = (MapView) findViewById(R.id.map);
        Tiles tiles = new Tiles();
        mapView.setTileSource(tiles);
        mapController = mapView.getController();
        mapController.setZoom(getZoomLevel());
        mapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                setZoomLevel(event.getZoomLevel());
                return false;
            }
        });
        mapView.setMultiTouchControls(true);
        myLocationOverlay = new MyLocationOverlay(this, mapView, new ResourceProxy() {
            @Override
            public String getString(string pResId) {
                return null;
            }

            @Override
            public String getString(string pResId, Object... formatArgs) {
                return null;
            }

            @Override
            public Bitmap getBitmap(bitmap pResId) {
                return BitmapFactory.decodeResource(getResources(), R.drawable.my_location);
            }

            @Override
            public Drawable getDrawable(bitmap pResId) {
                return null;
            }

            @Override
            public float getDisplayMetricsDensity() {
                return 0;
            }
        });
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        GeoPoint location = myLocationOverlay.getMyLocation();
        mapView.getOverlays().add(myLocationOverlay);
        disableHardwareAcceleration();

        if(bundle != null) {
            Place place = bundle.getParcelable("place");
            if(place != null) {
                addStar(place.getPoint());
                mapController.setCenter(place.getPoint());
            } else {
                ArrayList<Place> places = bundle.getParcelableArrayList("places");
                for(Place p: places) {
                    addStar(p.getPoint());
                }
            }

        } else if(location != null) {
            mapController.setCenter(location);
        }
    }

    private void disableHardwareAcceleration() {
        mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private void addStar(GeoPoint point) {
        Drawable marker=getResources().getDrawable(R.drawable.poi);
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
        public boolean onSnapToItem(int x, int y, Point snapPoint, IMapView mapView) {
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

    private class GeoNamesAdapter extends CursorAdapter {
        public GeoNamesAdapter(Context context, Cursor c) {
            super(context, c, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView tv = (TextView) view;
            final int textIndex = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
            tv.setText(cursor.getString(textIndex));
        }
    }
}