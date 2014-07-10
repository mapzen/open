package com.mapzen.fragment;

import com.mapzen.R;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.search.OnPoiClickListener;
import com.mapzen.util.MapzenTheme;
import com.mapzen.util.PoiLayer;

import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.event.Event;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.renderer.MapRenderer;
import org.oscim.theme.IRenderTheme;
import org.oscim.theme.ThemeLoader;
import org.oscim.tiling.source.OkHttpEngine;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import static com.mapzen.core.MapzenLocation.COM_MAPZEN_FIND_ME;
import static org.oscim.layers.marker.ItemizedLayer.OnItemGestureListener;

public class MapFragment extends BaseFragment {
    public static final int DURATION = 800;
    private VectorTileLayer baseLayer;
    private ItemizedLayer<MarkerItem> locationMarkerLayer;
    private PoiItemizedLayer poiMarkersLayer;
    private MarkerSymbol highlightMarker;
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
        getMapController().saveLocation();
        locationMarkerLayer.removeAllItems();
        poiMarkersLayer.removeAllItems();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(COM_MAPZEN_FIND_ME);
        FindMeReceiver findMeReceiver = new FindMeReceiver();
        app.registerReceiver(findMeReceiver, filter);

        getMapController().restoreFromSavedLocation();
        poiMarkersLayer.repopulate();
    }

    private class FindMeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            findMe();
        }
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
        getMap().animator().animateTo(DURATION, geoPoint, zoom, false);
    }

    public MarkerItem getMeMarker() {
        if (locationMarkerLayer.size() == 1) {
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
        return new PoiItemizedLayer(getMap(), new ArrayList<MarkerItem>(),
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

    public void setTheme(MapzenTheme theme) {
        IRenderTheme t = ThemeLoader.load(theme);
        baseLayer.setRenderTheme(t);
        MapRenderer.setBackgroundColor(t.getMapBackground());
        getMap().clearMap();
    }

    public void showLocationMarker() {
        if (!getMap().layers().contains(getLocationMarkerLayer())) {
            getMap().layers().add(getLocationMarkerLayer());
        }
    }

    public void hideLocationMarker() {
        getMap().layers().remove(getLocationMarkerLayer());
    }

    private void setupMap() {
        final OSciMap4TileSource tileSource = new OSciMap4TileSource(getTileBaseSource());
        tileSource.setHttpEngine(new OkHttpEngine.OkHttpFactory());
        baseLayer = getMap().setBaseMap(tileSource);

        getMap().layers().add(new BuildingLayer(getMap(), baseLayer));
        getMap().layers().add(new PoiLayer(getMap(), baseLayer, act));

        highlightMarker = getHighlightMarkerSymbol();

        getMap().layers().add(new LabelLayer(getMap(), baseLayer));

        poiMarkersLayer = buildPoiMarkersLayer();
        getMap().layers().add(poiMarkersLayer);

        locationMarkerLayer = new ItemizedLayer<MarkerItem>(getMap(),
                meMarkers, getDefaultMarkerSymbol(), null);
        getMap().layers().add(locationMarkerLayer);

        MapzenTheme theme = MapzenTheme.MAPZEN;
        theme.setContext(act);
        setTheme(theme);
        getMap().events.bind(new Map.UpdateListener() {
            @Override
            public void onMapEvent(Event e, MapPosition mapPosition) {
                if (e == Map.POSITION_EVENT) {
                    followMe = false;
                }

                getMapController().storeMapPosition(mapPosition);
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

    public ItemizedLayer<MarkerItem> getLocationMarkerLayer() {
        return locationMarkerLayer;
    }

    private void setupMyLocationBtn() {
        View view = getView();
        Button myPosition = (Button) view.findViewById(R.id.btn_my_position);
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
        MapPosition mapPosition = new MapPosition(point.getLatitude(), point.getLongitude(),
                getMapController().getZoomScale());
        mapPosition.setBearing(getMapController().getMapPosition().getBearing());
        mapPosition.setTilt(getMapController().getMapPosition().getTilt());
        return mapPosition;
    }

    public void findMe() {
        if (locationMarkerLayer != null) {
            locationMarkerLayer.removeAllItems();
            locationMarkerLayer.addItem(getUserLocationMarker());
        }

        if (followMe || !initialRelocateHappened) {
            // TODO find ways to accomplish this without two flags ;(
            initialRelocateHappened = true;
            getMap().setMapPosition(getUserLocationPosition());
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
        if (getMap() != null) {
            getMap().updateMap(true);
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
