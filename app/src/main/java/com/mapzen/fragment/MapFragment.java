package com.mapzen.fragment;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mapzen.PoiLayer;
import com.mapzen.R;
import com.mapzen.entity.Feature;

import org.oscim.android.canvas.AndroidBitmap;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.backend.canvas.Color;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.layers.PathLayer;
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

public class MapFragment extends BaseFragment {
    public static final int DEFAULT_ZOOMLEVEL = 17;
    public static final int ROUTE_LINE_WIDTH = 15;
    public static final int DURATION = 800;
    private VectorTileLayer baseLayer;
    private Button myPosition;
    private ItemizedIconLayer<MarkerItem> meMarkerLayer;
    private PoiLayer<MarkerItem> poiMarkersLayer;
    private ItemizedIconLayer<MarkerItem> highlightLayer;
    private PathLayer pathLayer;
    private ArrayList<MarkerItem> meMarkers = new ArrayList<MarkerItem>(1);
    private Location userLocation;
    // TODO find ways to track state without two variables
    private boolean followMe = true;
    private boolean initialRelocateHappened = false;
    private boolean bootingUp = true;

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        setupMap(view);
        setupMyLocationBtn(view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_map,
                container, false);
        return view;
    }

    public void centerOn(Feature feature) {
        highlightLayer.removeAllItems();
        highlightLayer.addItem(feature.getMarker());
        GeoPoint geoPoint = feature.getGeoPoint();
        map.getAnimator().animateTo(DURATION, geoPoint, Math.pow(2, DEFAULT_ZOOMLEVEL), false);
    }

    private TileSource getTileBase() {
        TileSource tileSource = new OSciMap4TileSource();
        tileSource.setOption(
                getString(R.string.tiles_source_url_key), getString(R.string.tiles_source_url));
        return tileSource;
    }

    private PoiLayer<MarkerItem> buildPoiMarkersLayer() {
        return new PoiLayer<MarkerItem>(getActivity(), map, getDefaultMarkerSymbol());
    }

    private ItemizedIconLayer<MarkerItem> buildHighlightLayer() {
        return new ItemizedIconLayer<MarkerItem>(
                map, new ArrayList<MarkerItem>(), getHighlightMarkerSymbol(), null);
    }

    private PathLayer buildPathLayer() {
        return new PathLayer(map, Color.MAGENTA, ROUTE_LINE_WIDTH);
    }

    private ItemizedIconLayer<MarkerItem> buildMyPositionLayer() {
        return new ItemizedIconLayer<MarkerItem>(map, meMarkers, getDefaultMarkerSymbol(), null);
    }

    private void setupMap(View view) {
        baseLayer = map.setBaseMap(getTileBase());
        map.getLayers().add(new BuildingLayer(map, baseLayer.getTileLayer()));
        map.getLayers().add(new LabelLayer(map, baseLayer.getTileLayer()));

        poiMarkersLayer = buildPoiMarkersLayer();
        map.getLayers().add(poiMarkersLayer);

        highlightLayer = buildHighlightLayer();
        map.getLayers().add(highlightLayer);

        pathLayer = buildPathLayer();
        map.getLayers().add(pathLayer);

        meMarkerLayer = buildMyPositionLayer();
        map.getLayers().add(meMarkerLayer);

        map.setTheme(InternalRenderTheme.OSMARENDER);
        map.bind(new Map.UpdateListener() {
            @Override
            public void onMapUpdate(MapPosition mapPosition, boolean positionChanged, boolean clear) {
                if (positionChanged) {
                    followMe = false;
                }
                app.storeMapPosition(mapPosition);
            }
        });
        setupMyLocationBtn(view);
        if (bootingUp) {
            bootingUp = false;
            map.setMapPosition(app.getLocationPosition());
        }
    }

    public Map getMap() {
        return map;
    }

    public void clearMarkers() {
        if (poiMarkersLayer != null) {
            poiMarkersLayer.removeAllItems();
        }

        if (highlightLayer != null) {
            highlightLayer.removeAllItems();
        }
    }

    public ItemizedIconLayer getPoiLayer() {
        return poiMarkersLayer;
    }

    public PathLayer getPathLayer() {
        return pathLayer;
    }

    private void setupMyLocationBtn(View view) {
        myPosition = (Button) view.findViewById(R.id.btn_my_position);
        myPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followMe = true;
                findMe();
            }
        });
    }

    public void setUserLocation(Location location) {
        if (location != null) {
            userLocation = location;
            findMe();
        }
    }

    private GeoPoint getUserLocationPoint() {
        if (userLocation != null) {
            return new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude());
        }
        return null;
    }

    private MarkerItem getUserLocationMarker() {
        if (userLocation == null) {
            return null;
        }
        MarkerItem markerItem = new MarkerItem("ME", "Current Location", getUserLocationPoint());
        MarkerSymbol symbol = new MarkerSymbol(getMyLocationSymbol(), MarkerItem.HotspotPlace.BOTTOM_CENTER);
        markerItem.setMarker(symbol);
        return markerItem;
    }

    private MapPosition getUserLocationPosition() {
        GeoPoint point = getUserLocationPoint();
        if (point != null) {
            return new MapPosition(point.getLatitude(), point.getLongitude(),
                    Math.pow(2, app.getStoredZoomLevel()));
        }
        return null;
    }

    private void findMe() {
        MarkerItem marker = getUserLocationMarker();
        if (marker == null) {
            Toast.makeText(act, "Don't have a location fix", Toast.LENGTH_LONG).show();
        } else {
            meMarkerLayer.removeAllItems();
            meMarkerLayer.addItem(getUserLocationMarker());
            if (followMe || !initialRelocateHappened) {
                // TODO find ways to accomplish this without two flags ;(
                initialRelocateHappened = true;
                map.setMapPosition(getUserLocationPosition());
            }
        }
        updateMap();
    }

    private Bitmap getMyLocationSymbol() {
        InputStream in = getResources().openRawResource(R.drawable.ic_locate_me);
        AndroidBitmap bitmap = new AndroidBitmap(in);
        return bitmap;
    }

    private Bitmap getPinDefault() {
        InputStream in = getResources().openRawResource(R.drawable.ic_pin);
        AndroidBitmap bitmap = new AndroidBitmap(in);
        return bitmap;
    }

    private Bitmap getHighlightPin() {
        InputStream in = getResources().openRawResource(R.drawable.ic_pin_active);
        AndroidBitmap bitmap = new AndroidBitmap(in);
        return bitmap;
    }

    public MarkerSymbol getHighlightMarkerSymbol() {
        return new MarkerSymbol(getHighlightPin(), MarkerItem.HotspotPlace.BOTTOM_CENTER);
    }

    public MarkerSymbol getDefaultMarkerSymbol() {
        return new MarkerSymbol(getPinDefault(), MarkerItem.HotspotPlace.BOTTOM_CENTER);
    }

    public void updateMap() {
        if (map != null) {
            map.updateMap(true);
        }
    }
}
