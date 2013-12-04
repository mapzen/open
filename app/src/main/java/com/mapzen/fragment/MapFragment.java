package com.mapzen.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;

import org.oscim.android.canvas.AndroidBitmap;
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
    private VectorTileLayer baseLayer;
    private BaseActivity activity;
    private Map map;
    private Button myPosition;
    private ItemizedIconLayer<MarkerItem> meMarkerLayer;
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
        return view;
    }

    private void setupMap(View view) {
        map = activity.getMap();
        TileSource tileSource = new OSciMap4TileSource();
        tileSource.setOption(getString(R.string.tiles_source_url_key), getString(R.string.tiles_source_url));
        baseLayer = map.setBaseMap(tileSource);
        map.getLayers().add(new BuildingLayer(map, baseLayer.getTileLayer()));
        map.getLayers().add(new LabelLayer(map, baseLayer.getTileLayer()));
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

    private void setupMyLocationBtn(View view) {
        myPosition = (Button) view.findViewById(R.id.btn_my_position);
        myPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMyLocation();
                map.getAnimator().animateTo(1300, app.getLocationPoint(), Math.pow(2, 15), false);
            }
        });
    }

    private void addMyLocation() {
        MarkerItem markerItem = new MarkerItem("ME", "Current Location", app.getLocationPoint());
        meMarkerLayer.removeAllItems();
        meMarkerLayer.addItem(markerItem);
        map.updateMap(true);
    }

    private void setupMeMarkerLayer() {
        InputStream in = getResources().openRawResource(R.drawable.pin);
        AndroidBitmap bitmap = new AndroidBitmap(in);
        meMarkerLayer = new ItemizedIconLayer<MarkerItem>(map, meMarkers, new MarkerSymbol(bitmap, 0.0f, 0.0f), null);
        map.getLayers().add(meMarkerLayer);
        addMyLocation();
    }
}
