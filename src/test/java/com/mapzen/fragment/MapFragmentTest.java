package com.mapzen.fragment;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.search.OnPoiClickListener;
import com.mapzen.support.FakeMotionEvent;
import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscim.core.GeoPoint;
import org.oscim.event.Gesture;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.tile.TileLayer;
import org.oscim.map.Map;
import org.oscim.tiling.TileSource;
import org.oscim.tiling.source.HttpEngine;
import org.oscim.tiling.source.OkHttpEngine;
import org.robolectric.annotation.Config;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static com.mapzen.support.TestHelper.getTestSimpleFeature;
import static com.mapzen.support.TestHelper.initBaseActivity;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class MapFragmentTest {
    private MapFragment mapFragment;
    private TestPoiClickListener listener;
    private BaseActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = initBaseActivity();
        listener = new TestPoiClickListener();
        mapFragment = new MapFragment();
        mapFragment.setAct(activity);
        mapFragment.setOnPoiClickListener(listener);
        startFragment(mapFragment);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(mapFragment).isNotNull();
    }

    @Test
    public void shouldHavePoiLayer() throws Exception {
        assertThat(mapFragment.getPoiLayer()).isNotNull();
    }

    @Test
    public void shouldHaveMeMarkerLayer() throws Exception {
        assertThat(mapFragment.getLocationMarkerLayer()).isNotNull();
    }

    @Test
    public void onItemSingleTapUp_shouldNotifyListener() throws Exception {
        ItemizedLayer<MarkerItem> poiLayer = mapFragment.getPoiLayer();
        poiLayer.addItem(new MarkerItem("Title", "Description", new GeoPoint(0, 0)));
        poiLayer.onGesture(Gesture.TAP, new FakeMotionEvent(0, 0));
        assertThat(listener.getIndex()).isEqualTo(0);
        assertThat(listener.getItem().getTitle()).isEqualTo("Title");
    }

    class TestPoiClickListener implements OnPoiClickListener {
        private int index = -1;
        private MarkerItem item;

        @Override
        public void onPoiClick(int index, MarkerItem item) {
            this.index = index;
            this.item = item;
        }

        public int getIndex() {
            return index;
        }

        public MarkerItem getItem() {
            return item;
        }
    }

    @Test
    public void onPause_shouldEmptyMeMarkers() throws Exception {
        ItemizedLayer<MarkerItem> meMarkerLayer = mapFragment.getLocationMarkerLayer();
        meMarkerLayer.addItem(new MarkerItem("Title", "Description", new GeoPoint(0, 0)));
        mapFragment.onPause();
        assertThat(meMarkerLayer.size()).isEqualTo(0);
    }

    @Test
    public void onPause_shouldEmptyPoiMarkers() throws Exception {
        mapFragment.addPoi(getTestSimpleFeature());
        mapFragment.addPoi(getTestSimpleFeature());
        ItemizedLayer<MarkerItem> poiMarkerLayer = mapFragment.getPoiLayer();
        mapFragment.onPause();
        assertThat(poiMarkerLayer.size()).isEqualTo(0);
    }

    @Test
    public void onResume_shouldRepopulatePoiMarkers() throws Exception {
        mapFragment.addPoi(getTestSimpleFeature());
        mapFragment.addPoi(getTestSimpleFeature());
        ItemizedLayer<MarkerItem> poiMarkerLayer = mapFragment.getPoiLayer();
        mapFragment.onPause();
        mapFragment.onResume();
        assertThat(poiMarkerLayer.size()).isEqualTo(2);
    }

    @Test
    public void clearMarkers_shouldEmptyMapPois() throws Exception {
        mapFragment.addPoi(getTestSimpleFeature());
        mapFragment.addPoi(getTestSimpleFeature());
        ItemizedLayer<MarkerItem> poiMarkerLayer = mapFragment.getPoiLayer();
        mapFragment.clearMarkers();
        assertThat(poiMarkerLayer.size()).isZero();
    }

    @Test
    public void clearMarkers_shouldEmptyStoredPois() throws Exception {
        mapFragment.addPoi(getTestSimpleFeature());
        mapFragment.addPoi(getTestSimpleFeature());
        mapFragment.clearMarkers();
        mapFragment.onPause();
        mapFragment.onResume();
        ItemizedLayer<MarkerItem> poiMarkerLayer = mapFragment.getPoiLayer();
        assertThat(poiMarkerLayer.size()).isZero();
    }

    @Test
    public void shouldPointToDefaultTileService() throws Exception {
        assertThat(mapFragment.getTileBaseSource()).isEqualTo(
                mapFragment.getActivity().getResources().getString(
                        R.string.settings_default_mapsource));
    }

    @Test
    public void shouldPointToConfiguredTileService() throws Exception {
        String expected = "http://test.com";
        setTileSourceConfiguration(expected);
        assertThat(mapFragment.getTileBaseSource()).isEqualTo(expected);
    }

    @Test
    public void shouldUseOkHttp() throws Exception {
        Map map = mapFragment.getMap();
        TileLayer baseLayer = field("mBaseLayer").ofType(TileLayer.class).in(map).get();
        TileSource tileSource = field("mTileSource").ofType(TileSource.class).in(baseLayer).get();
        HttpEngine.Factory factory = field("mHttpFactory").ofType(HttpEngine.Factory.class)
                .in(tileSource).get();

        assertThat(factory).isInstanceOf(OkHttpEngine.OkHttpFactory.class);
    }

    @Test
    public void shouldSetupLocationMarker() throws Exception {
        assertThat(mapFragment.getMap().layers().
                contains(mapFragment.getLocationMarkerLayer())).isTrue();
    }

    @Test
    public void shouldHideLocationMarker() throws Exception {
        mapFragment.hideLocationMarker();
        assertThat(mapFragment.getMap().layers().
                contains(mapFragment.getLocationMarkerLayer())).isFalse();
    }

    @Test
    public void shouldShowLocationMarker() throws Exception {
        mapFragment.getMap().layers().remove(mapFragment.getLocationMarkerLayer());
        mapFragment.showLocationMarker();
        assertThat(mapFragment.getMap().layers().
                contains(mapFragment.getLocationMarkerLayer())).isTrue();
    }

    private void setTileSourceConfiguration(String source) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                activity);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString(activity.getString(R.string.settings_key_mapsource), source);
        prefEditor.commit();
    }
}