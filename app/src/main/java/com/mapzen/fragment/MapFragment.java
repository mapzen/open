package com.mapzen.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.mapzen.R;
import com.mapzen.Tiles;
import org.osmdroid.ResourceProxy;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

public class MapFragment extends Fragment {
    /*
    public static final String TILES_STREET_MAP = "randyme.gajlngfe";
    public static final String TILES_SATELITE_MAP = "randyme.gb92439b";

    private MapView mapView;
    private MapController mapController;
    private MyLocationOverlay myLocationOverlay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_map,
                container, false);
        setupMap((MapView) view.findViewById(R.id.map), TILES_STREET_MAP);
        Button btn = (Button) view.findViewById(R.id.sat);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView = (MapView) view.findViewById(R.id.map);
                Integer tag = (Integer)mapView.getTag();
                Tiles tiles = null;
                if(tag == null || tag == 0) {
                    tiles = Tiles.getTiles(TILES_SATELITE_MAP);
                    mapView.setTag(1);
                } else {
                    tiles = Tiles.getTiles(TILES_STREET_MAP);
                    mapView.setTag(0);
                }

                mapView.setTileSource(tiles);
            }
        });
        setupLocateMeButton(view);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disableLocation();
    }

    private void disableLocation() {
        if(myLocationOverlay != null) {
            myLocationOverlay.disableFollowLocation();
            myLocationOverlay.disableMyLocation();
        }
    }

    private void enableLocation() {
        if (myLocationOverlay != null) {
            myLocationOverlay.enableMyLocation();
            myLocationOverlay.enableFollowLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        disableLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        enableLocation();
    }

    private void setupLocateMeButton(View view) {
        Button locateMe = (Button)view.findViewById(R.id.locate_me);
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

    private void setupMap(MapView v, String tileSet) {
        mapView = v;

        Tiles tiles = Tiles.getTiles(tileSet);
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
        myLocationOverlay = new MyLocationOverlay(getActivity(), mapView, new ResourceProxy() {
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
        enableLocation();
        mapView.getOverlays().add(myLocationOverlay);
        disableHardwareAcceleration();
    }

    private void disableHardwareAcceleration() {
        mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
    */
}
