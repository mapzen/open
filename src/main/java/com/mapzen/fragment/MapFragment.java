package com.mapzen.fragment;

import com.mapzen.R;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.search.OnPoiClickListener;
import com.mapzen.util.MapzenTheme;
import com.mapzen.util.PoiLayer;

import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.backend.canvas.Color;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.event.Event;
import org.oscim.layers.PathLayer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.vector.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.renderer.MapRenderer;
import org.oscim.theme.IRenderTheme;
import org.oscim.theme.ThemeLoader;
import org.oscim.tiling.source.OkHttpEngine;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import static com.mapzen.MapController.DEFAULT_ZOOMLEVEL;
import static com.mapzen.MapController.getMapController;
import static org.oscim.layers.marker.ItemizedLayer.OnItemGestureListener;

public class MapFragment extends BaseFragment {
    public static final int ROUTE_LINE_WIDTH = 10;
    public static final int DURATION = 800;
    private VectorTileLayer baseLayer;
    private Button myPosition;
    private ItemizedLayer<MarkerItem> meMarkerLayer;
    private PoiItemizedLayer poiMarkersLayer;
    private MarkerSymbol highlightMarker;
    private PathLayer pathLayer;
    private ArrayList<MarkerItem> meMarkers = new ArrayList<MarkerItem>(1);
    // TODO find ways to track state without two variables
    private boolean followMe = true;
    private boolean initialRelocateHappened = false;
    private OnPoiClickListener onPoiClickListener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupMap();
    }

    @Override
    public void onStart() {
        super.onStart();
        setupMyLocationBtn();
    }

    @Override
    public void onPause() {
        super.onPause();
        meMarkerLayer.removeAllItems();
        poiMarkersLayer.removeAllItems();
    }

    @Override
    public void onResume() {
        super.onResume();
        poiMarkersLayer.repopulate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_map,
                container, false);
        return view;
    }

    public void centerOn(SimpleFeature simpleFeature) {
        centerOn(simpleFeature, Math.pow(2, DEFAULT_ZOOMLEVEL));
    }

    public void centerOn(SimpleFeature simpleFeature, double zoom) {
        MarkerItem focused = poiMarkersLayer.getFocus();
        if (focused != null) {
            focused.setMarker(null);
        }

        focused = poiMarkersLayer.getByUid(simpleFeature);

        if (focused != null) {
            focused.setMarker(highlightMarker);
            poiMarkersLayer.setFocus(focused);
        }
        GeoPoint geoPoint = simpleFeature.getGeoPoint();
        map.animator().animateTo(DURATION, geoPoint, zoom, false);
    }

    public MarkerItem getMeMarker() {
        if (meMarkerLayer.size() == 1) {
            return meMarkers.get(0);
        }
        return null;
    }

    public void addPoi(SimpleFeature simpleFeature) {
        MarkerItem markerItem = simpleFeature.getMarker();
        poiMarkersLayer.addItem(markerItem);
    }

    public String getTileBaseSource() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        return prefs.getString(getString(R.string.settings_key_mapsource),
                getString(R.string.settings_default_mapsource));
    }

    private PoiItemizedLayer buildPoiMarkersLayer() {
        return new PoiItemizedLayer(map, new ArrayList<MarkerItem>(),
                getDefaultMarkerSymbol(), new OnItemGestureListener<MarkerItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, MarkerItem item) {
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

    public void setMap(Map map) {
        this.map = map;
    }

    public void setTheme(MapzenTheme theme) {
        IRenderTheme t = ThemeLoader.load(theme);
        baseLayer.setRenderTheme(t);
        MapRenderer.setBackgroundColor(t.getMapBackground());
        map.clearMap();
    }

    private void setupMap() {
        final OSciMap4TileSource tileSource = new OSciMap4TileSource(getTileBaseSource());
        tileSource.setHttpEngine(new OkHttpEngine.OkHttpFactory());
        baseLayer = map.setBaseMap(tileSource);

        map.layers().add(new BuildingLayer(map, baseLayer));
        map.layers().add(new LabelLayer(map, baseLayer));
        map.layers().add(new PoiLayer(map, baseLayer, act));

        highlightMarker = getHighlightMarkerSymbol();

        poiMarkersLayer = buildPoiMarkersLayer();
        map.layers().add(poiMarkersLayer);

        pathLayer = buildPathLayer();
        map.layers().add(pathLayer);

        meMarkerLayer = buildMyPositionLayer();
        map.layers().add(meMarkerLayer);

        MapzenTheme theme = MapzenTheme.MAPZEN;
        theme.setContext(act);
        setTheme(theme);
        map.events.bind(new Map.UpdateListener() {
            @Override
            public void onMapEvent(Event e, MapPosition mapPosition) {
                if (e == Map.POSITION_EVENT) {
                    followMe = false;
                }
                getMapController().setMapPosition(mapPosition);
            }
        });
    }

    public Map getMap() {
        return act.getMap();
    }

    public void clearMarkers() {
        if (poiMarkersLayer != null) {
            poiMarkersLayer.clearAll();
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

    private void setupMyLocationBtn() {
        View view = getView();
        myPosition = (Button) view.findViewById(R.id.btn_my_position);
        myPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followMe = true;
                findMe();
            }
        });
    }

    public GeoPoint getUserLocationPoint() {
        Location userLocation = getMapController().getLocation();
        return new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude());
    }

    private MarkerItem getUserLocationMarker() {
        MarkerItem markerItem = new MarkerItem("ME", "Current Location", getUserLocationPoint());
        MarkerSymbol symbol = AndroidGraphics.makeMarker(
                getResources().getDrawable(R.drawable.ic_locate_me),
                MarkerItem.HotspotPlace.CENTER);
        markerItem.setMarker(symbol);
        return markerItem;
    }

    private MapPosition getUserLocationPosition() {
        GeoPoint point = getUserLocationPoint();
        return new MapPosition(point.getLatitude(), point.getLongitude(),
                getMapController().getZoomScale());
    }

    public void findMe() {
        if (meMarkerLayer != null) {
            meMarkerLayer.removeAllItems();
            meMarkerLayer.addItem(getUserLocationMarker());
        }

        if (followMe || !initialRelocateHappened) {
            // TODO find ways to accomplish this without two flags ;(
            initialRelocateHappened = true;
            map.setMapPosition(getUserLocationPosition());
        }

        updateMap();
    }

    public MarkerSymbol getHighlightMarkerSymbol() {
        return AndroidGraphics.makeMarker(getResources().getDrawable(R.drawable.ic_pin_active),
                MarkerItem.HotspotPlace.BOTTOM_CENTER);
    }

    public MarkerSymbol getDefaultMarkerSymbol() {
        return AndroidGraphics.makeMarker(getResources().getDrawable(R.drawable.ic_pin),
                MarkerItem.HotspotPlace.BOTTOM_CENTER);
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

    private static class PoiItemizedLayer extends ItemizedLayer<MarkerItem> {
        private ArrayList<MarkerItem> poiMarkers = new ArrayList<MarkerItem>();

        public PoiItemizedLayer(Map map, List<MarkerItem> list, MarkerSymbol defaultMarker,
                OnItemGestureListener<MarkerItem> onItemGestureListener) {
            super(map, list, defaultMarker, onItemGestureListener);
        }

        public void repopulate() {
            addItems(poiMarkers);
        }

        public void clearAll() {
            poiMarkers.clear();
            removeAllItems();
        }

        @Override
        public boolean addItem(MarkerItem item) {
            poiMarkers.add(item);
            return super.addItem(item);
        }
    }
}
