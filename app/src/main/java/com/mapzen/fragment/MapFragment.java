package com.mapzen.fragment;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.mapzen.R;
import com.mapzen.activity.VectorMapActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oscim.android.MapView;
import org.oscim.core.MapPosition;
import org.oscim.layers.tile.vector.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.theme.InternalRenderTheme;
import org.oscim.tiling.source.TileSource;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

import static android.provider.BaseColumns._ID;
import static com.mapzen.MapzenApplication.LOG_TAG;
import static com.mapzen.MapzenApplication.PELIAS_LAT;
import static com.mapzen.MapzenApplication.PELIAS_LON;
import static com.mapzen.MapzenApplication.PELIAS_TEXT;
import static com.mapzen.MapzenApplication.getLocationPosition;
import static com.mapzen.MapzenApplication.storeMapPosition;

public class MapFragment extends Fragment {
    private MapView mMapView;
    private VectorTileLayer mBaseLayer;
    private VectorMapActivity activity;
    private Map mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_map,
                container, false);
        activity = (VectorMapActivity) getActivity();
        setupMap(view);
        return view;
    }

    private void setupMap(View view) {
        mMap = activity.getMap();
        mMapView = (MapView) view.findViewById(R.id.map);
        TileSource tileSource = new OSciMap4TileSource();
        tileSource.setOption(getString(R.string.tiles_source_url_key), getString(R.string.tiles_source_url));
        mBaseLayer = mMap.setBaseMap(tileSource);
        mMap.getLayers().add(new BuildingLayer(mMap, mBaseLayer.getTileLayer()));
        mMap.getLayers().add(new LabelLayer(mMap, mBaseLayer.getTileLayer()));
        mMap.setTheme(InternalRenderTheme.DEFAULT);
        mMap.bind(new Map.UpdateListener() {
            @Override
            public void onMapUpdate(MapPosition mapPosition, boolean positionChanged, boolean clear) {
                Log.v(LOG_TAG, "updating zoomlevel");
                Log.v(LOG_TAG, String.valueOf(mapPosition.getZoomScale()));
                Log.v(LOG_TAG, String.valueOf(mapPosition.zoomLevel));
                storeMapPosition(mapPosition);
            }
        });
        mMap.setMapPosition(getLocationPosition(getActivity()));

    }
}
