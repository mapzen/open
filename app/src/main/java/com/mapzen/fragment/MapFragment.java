package com.mapzen.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mapzen.MapzenApplication;
import com.mapzen.PoiLayer;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;

import org.oscim.android.MapView;
import org.oscim.android.canvas.AndroidBitmap;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.layers.marker.ItemizedIconLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.vector.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.theme.InternalRenderTheme;
import org.oscim.tiling.source.TileSource;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

import java.io.InputStream;
import java.util.ArrayList;

public class MapFragment extends Fragment {
    public static final int ANIMATION_DURATION = 1300;
    public static final int DEFAULT_ZOOMLEVEL = 15;
    private VectorTileLayer baseLayer;
    private BaseActivity activity;
    private Map map;
    private Button myPosition;
    private ItemizedIconLayer<MarkerItem> meMarkerLayer;
    private PoiLayer<MarkerItem> poiMarkersLayer;
    private ArrayList<MarkerItem> meMarkers = new ArrayList<MarkerItem>(1);
    private MapzenApplication app;

    @Override
    public void onPause() {
        super.onPause();
        app.stopLocationUpdates();
    }

    @Override
    public void onStop() {
        super.onStop();
        app.stopLocationUpdates();
    }

    @Override
    public void onStart() {
        super.onStart();
        app.setupLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        app.setupLocationUpdates();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_map,
                container, false);
        activity = (BaseActivity) getActivity();
        app = MapzenApplication.getApp(getActivity());
        setupMap(view);
        setupMyLocationBtn(view);
        setupZoomBtns(view);
        return view;
    }

    private void setupZoomBtns(View view) {
        Button zoomInBtn = (Button) view.findViewById(R.id.btn_zoom_in);
        zoomInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomIn();
            }
        });
        Button zoomOutBtn = (Button) view.findViewById(R.id.btn_zoom_out);
        zoomOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomOut();
            }
        });
    }

    private void zoom(int direction) {
        MapPosition pos = map.getMapPostion();
        pos.setZoomLevel(pos.zoomLevel + direction);
        map.setMapPosition(pos);
        map.updateMap(true);
    }

    private void zoomOut() {
        zoom(-1);
    }

    private void zoomIn() {
        zoom(1);
    }

    public void centerOn(GeoPoint geoPoint) {
        MapPosition mapPosition = new MapPosition(geoPoint.getLatitude(),
                geoPoint.getLongitude(), Math.pow(2, app.getStoredZoomLevel()));
        map.setMapPosition(mapPosition);
    }

    private void setupMap(View view) {
        map = activity.getMap();
        MapView mapView = (MapView) view.findViewById(R.id.map);
        TileSource tileSource = new OSciMap4TileSource();
        tileSource.setOption(getString(R.string.tiles_source_url_key), getString(R.string.tiles_source_url));
        baseLayer = map.setBaseMap(tileSource);
        map.getLayers().add(new BuildingLayer(map, baseLayer.getTileLayer()));
        map.getLayers().add(new LabelLayer(map, baseLayer.getTileLayer()));
        mapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("foo", "testing clicking click");
            }
        });

        poiMarkersLayer = new PoiLayer<MarkerItem>(
                map, new ArrayList<MarkerItem>(), getDefaultMarkerSymbol(),
                new ItemizedIconLayer.OnItemGestureListener<MarkerItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, MarkerItem item) {
                Log.v("foo", "testing");
                return true;
            }

            @Override
            public boolean onItemLongPress(int index, MarkerItem item) {
                Log.v("foo", "testing");
                return true;
            }
        });
        map.getLayers().add(poiMarkersLayer);
        map.setTheme(InternalRenderTheme.DEFAULT);
        map.bind(new Map.UpdateListener() {
            @Override
            public void onMapUpdate(MapPosition mapPosition, boolean positionChanged, boolean clear) {
                app.storeMapPosition(mapPosition);
            }
        });
        setupMyLocationBtn(view);
        setupMeMarkerLayer();
        map.setMapPosition(app.getLocationPosition());
    }

    public ItemizedIconLayer getPoiLayer() {
        return poiMarkersLayer;
    }

    private void setupMyLocationBtn(View view) {
        myPosition = (Button) view.findViewById(R.id.btn_my_position);
        myPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMyLocation();
                map.getAnimator().animateTo(ANIMATION_DURATION,
                        app.getLocationPoint(), Math.pow(2, DEFAULT_ZOOMLEVEL), false);
            }
        });
    }

    private void addMyLocation() {
        MarkerItem markerItem = new MarkerItem("ME", "Current Location", app.getLocationPoint());
        meMarkerLayer.removeAllItems();
        meMarkerLayer.addItem(markerItem);
        map.updateMap(true);
    }

    private Bitmap getPinDefault() {
        InputStream in = getResources().openRawResource(R.drawable.ic_pin);
        AndroidBitmap bitmap = new AndroidBitmap(in);
        return bitmap;
    }

    public MarkerSymbol getDefaultMarkerSymbol() {
        return new MarkerSymbol(getPinDefault(), MarkerItem.HotspotPlace.BOTTOM_CENTER);
    }

    private void setupMeMarkerLayer() {
        meMarkerLayer = new ItemizedIconLayer<MarkerItem>(map, meMarkers, getDefaultMarkerSymbol(), null);
        map.getLayers().add(meMarkerLayer);
        addMyLocation();
    }

    public void updateMap() {
        map.updateMap(true);
    }
}
