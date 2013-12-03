package com.mapzen.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;

import org.oscim.android.MapView;
import org.oscim.android.canvas.AndroidBitmap;
import org.oscim.core.MapPosition;
import org.oscim.layers.marker.ItemizedIconLayer;
import org.oscim.layers.marker.ItemizedLayer;
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

import static com.mapzen.MapzenApplication.getLocationPoint;
import static com.mapzen.MapzenApplication.getLocationPosition;
import static com.mapzen.MapzenApplication.storeMapPosition;

public class MapFragment extends Fragment {
    private MapView mapView;
    private VectorTileLayer baseLayer;
    private BaseActivity activity;
    private Map map;
    private Button myPosition;
    private LinearLayout container;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_map,
                container, false);
        activity = (BaseActivity) getActivity();
        setupMap(view);
        setupMyLocationBtn(view);
        return view;
    }

    private void setupMap(View view) {
        map = activity.getMap();
        mapView = (MapView) view.findViewById(R.id.map);
        TileSource tileSource = new OSciMap4TileSource();
        tileSource.setOption(getString(R.string.tiles_source_url_key), getString(R.string.tiles_source_url));
        baseLayer = map.setBaseMap(tileSource);
        map.getLayers().add(new BuildingLayer(map, baseLayer.getTileLayer()));
        map.getLayers().add(new LabelLayer(map, baseLayer.getTileLayer()));
        map.setTheme(InternalRenderTheme.DEFAULT);
        map.bind(new Map.UpdateListener() {
            @Override
            public void onMapUpdate(MapPosition mapPosition, boolean positionChanged, boolean clear) {
                storeMapPosition(mapPosition);
            }
        });
        setupMyLocationBtn(view);
        addMyPositionMarker();
        map.setMapPosition(getLocationPosition(getActivity()));
    }

    private void setupMyLocationBtn(View view) {
        myPosition = (Button) view.findViewById(R.id.btn_my_position);
        myPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMyPositionMarker();
                map.getAnimator().animateTo(1300, getLocationPoint(getActivity()), Math.pow(2, 15), false);
            }
        });
    }

    private void addMyPositionMarker() {
        InputStream in = getResources().openRawResource(R.drawable.pin);
        AndroidBitmap bitmap = new AndroidBitmap(in);
        MarkerItem markerItem = new MarkerItem("ME", "Current Location", getLocationPoint(getActivity()));
        ArrayList<MarkerItem> markers = new ArrayList<MarkerItem>();
        markers.add(markerItem);
        ItemizedLayer<MarkerItem> itemItemizedLayer = new ItemizedIconLayer<MarkerItem>(map, markers, new MarkerSymbol(bitmap, 0.0f, 0.0f), null);
        map.getLayers().add(itemItemizedLayer);
        map.updateMap(true);
    }
}
