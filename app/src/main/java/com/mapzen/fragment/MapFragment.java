package com.mapzen.fragment;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mapzen.R;
import com.mapzen.entity.Feature;
import com.mapzen.search.OnPoiClickListener;

import org.oscim.android.canvas.AndroidBitmap;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.backend.canvas.Color;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.layers.PathLayer;
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

import static org.oscim.layers.marker.ItemizedLayer.OnItemGestureListener;

public class MapFragment extends BaseFragment {
    public static final int DEFAULT_ZOOMLEVEL = 17;
    public static final int ROUTE_LINE_WIDTH = 15;
    public static final int DURATION = 800;
    private VectorTileLayer baseLayer;
    private Button myPosition;
    private ItemizedLayer<MarkerItem> meMarkerLayer;
    private ItemizedLayer<MarkerItem> poiMarkersLayer;
    private MarkerSymbol highlightMarker;
    private PathLayer pathLayer;
    private ArrayList<MarkerItem> meMarkers = new ArrayList<MarkerItem>(1);
    private Location userLocation;
    // TODO find ways to track state without two variables
    private boolean followMe = true;
    private boolean initialRelocateHappened = false;
    private boolean bootingUp = true;
    private OnPoiClickListener onPoiClickListener;

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        setupMap(view);
        setupMyLocationBtn(view);
    }

    @Override
    public void onPause() {
        super.onPause();
        meMarkerLayer.removeAllItems();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_map,
                container, false);
        return view;
    }

    public void centerOn(Feature feature) {
        centerOn(feature, Math.pow(2, DEFAULT_ZOOMLEVEL));
    }

    public void centerOn(Feature feature, double zoom) {
        MarkerItem focused = poiMarkersLayer.getFocus();
        if (focused != null)
            focused.setMarker(null);

        focused = poiMarkersLayer.getByUid(feature);

        if (focused != null){
            focused.setMarker(highlightMarker);
            poiMarkersLayer.setFocus(focused);
        }
        GeoPoint geoPoint = feature.getGeoPoint();
        map.animator().animateTo(DURATION, geoPoint, zoom, false);
    }

    private TileSource getTileBase() {
        return new OSciMap4TileSource(getString(R.string.tiles_source_url));
    }

    private ItemizedLayer<MarkerItem> buildPoiMarkersLayer() {
        return new ItemizedLayer<MarkerItem>(map, new ArrayList<MarkerItem>(),
                getDefaultMarkerSymbol(), new OnItemGestureListener<MarkerItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, MarkerItem item) {
                Toast.makeText(act, item.getTitle(), Toast.LENGTH_SHORT).show();
                if (onPoiClickListener != null) {
                    onPoiClickListener.onPoiClick(index, item);
                }
                return true;
            }

            @Override
            public boolean onItemLongPress(int index, MarkerItem item) {
                return true;
            }
        });
    }

    private PathLayer buildPathLayer() {
        return new PathLayer(map, Color.MAGENTA, ROUTE_LINE_WIDTH);
    }

    private ItemizedLayer<MarkerItem> buildMyPositionLayer() {
        return new ItemizedLayer<MarkerItem>(map, meMarkers, getDefaultMarkerSymbol(), null);
    }

    private void setupMap(View view) {
        baseLayer = map.setBaseMap(getTileBase());
        map.layers().add(new BuildingLayer(map, baseLayer));
        map.layers().add(new LabelLayer(map, baseLayer));

        highlightMarker = getHighlightMarkerSymbol();
        poiMarkersLayer = buildPoiMarkersLayer();
        map.layers().add(poiMarkersLayer);

        pathLayer = buildPathLayer();
        map.layers().add(pathLayer);

        meMarkerLayer = buildMyPositionLayer();
        map.layers().add(meMarkerLayer);

        map.setTheme(InternalRenderTheme.OSMARENDER);
        map.bind(new Map.UpdateListener() {
            @Override
            public void onMapUpdate(MapPosition mapPosition, boolean positionChanged,
                    boolean clear) {
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
    }

    public ItemizedLayer<MarkerItem> getPoiLayer() {
        return poiMarkersLayer;
    }

    public ItemizedLayer<MarkerItem> getMeMarkerLayer() {
        return meMarkerLayer;
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
        MarkerSymbol symbol = new MarkerSymbol(getMyLocationSymbol(),
                MarkerItem.HotspotPlace.BOTTOM_CENTER);
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

    public void setOnPoiClickListener(OnPoiClickListener onPoiClickListener) {
        this.onPoiClickListener = onPoiClickListener;
    }

    public OnPoiClickListener getOnPoiClickListener() {
        return onPoiClickListener;
    }
}
