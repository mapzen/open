package com.mapzen.open.fragment;

import com.mapzen.open.R;
import com.mapzen.open.TestMapzenApplication;
import com.mapzen.open.activity.BaseActivity;
import com.mapzen.open.core.StyleDownLoader;
import com.mapzen.open.search.OnPoiClickListener;
import com.mapzen.open.support.FakeMotionEvent;
import com.mapzen.open.support.MapzenTestRunner;

import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkResponseCache;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.event.Gesture;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.tile.TileLayer;
import org.oscim.map.Map;
import org.oscim.map.TestMap;
import org.oscim.tiling.TileSource;
import org.oscim.tiling.source.HttpEngine;
import org.oscim.tiling.source.OkHttpEngine;
import org.oscim.tiling.source.UrlTileSource;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.util.FragmentTestUtil;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import static com.mapzen.open.activity.BaseActivity.COM_MAPZEN_UPDATES_LOCATION;
import static com.mapzen.open.core.MapzenLocation.COM_MAPZEN_FIND_ME;
import static com.mapzen.open.support.TestHelper.getTestSimpleFeature;
import static com.mapzen.open.support.TestHelper.initBaseActivity;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class MapFragmentTest {
    private MapFragment mapFragment;
    private TestPoiClickListener listener;
    private BaseActivity activity;
    @Inject StyleDownLoader styleDownLoader;

    @Before
    public void setUp() throws Exception {
        ((TestMapzenApplication) Robolectric.application).inject(this);
        activity = initBaseActivity();
        listener = new TestPoiClickListener();
        mapFragment = activity.getMapFragment();
        mapFragment.setOnPoiClickListener(listener);
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
    public void onResume_shouldLocationUpdatesReceivers() {
        Intent expectedIntent = new Intent(COM_MAPZEN_UPDATES_LOCATION);
        List<BroadcastReceiver> intents =
                Robolectric.getShadowApplication().getReceiversForIntent(expectedIntent);
        assertThat(intents).hasSize(1);
    }

    @Test
    public void onPause_shouldUnregisterLocationUpdatesReceivers() {
        mapFragment.onPause();
        Intent expectedIntent = new Intent(COM_MAPZEN_UPDATES_LOCATION);
        List<BroadcastReceiver> intents =
                Robolectric.getShadowApplication().getReceiversForIntent(expectedIntent);
        assertThat(intents).isEmpty();
    }

    @Test
    public void onResume_shouldRegisterFindMeReceiver() {
        Intent expectedIntent = new Intent(COM_MAPZEN_FIND_ME);
        List<BroadcastReceiver> findMeReceivers =
                Robolectric.getShadowApplication().getReceiversForIntent(expectedIntent);
        assertThat(findMeReceivers).hasSize(1);
    }

    @Test
    public void onPause_shouldUnregisterFindMeReceiver() {
        mapFragment.onPause();
        Intent expectedIntent = new Intent(COM_MAPZEN_FIND_ME);
        List<BroadcastReceiver> findMeReceivers =
                Robolectric.getShadowApplication().getReceiversForIntent(expectedIntent);
        assertThat(findMeReceivers).isEmpty();
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
    public void shouldUse10MegResponseCache() throws Exception {
        Map map = mapFragment.getMap();
        TileLayer baseLayer = field("mBaseLayer").ofType(TileLayer.class).in(map).get();
        UrlTileSource tileSource =
                (UrlTileSource) field("mTileSource").
                        ofType(TileSource.class).in(baseLayer).get();
        HttpEngine.Factory engine = field("mHttpFactory").
                ofType(HttpEngine.Factory.class).in(tileSource).get();
        OkHttpClient client = field("mClient").ofType(OkHttpClient.class).in(engine).get();

        HttpResponseCache cache =
                (HttpResponseCache) field("responseCache").
                        ofType(OkResponseCache.class).in(client).get();
        assertThat(cache.getMaxSize()).isEqualTo(MapFragment.CACHE_SIZE);
    }

    @Test
    public void shouldUseResponseCacheStoredOnFile() throws Exception {
        Map map = mapFragment.getMap();
        TileLayer baseLayer = field("mBaseLayer").ofType(TileLayer.class).in(map).get();
        UrlTileSource tileSource =
                (UrlTileSource) field("mTileSource").
                        ofType(TileSource.class).in(baseLayer).get();
        HttpEngine.Factory engine = field("mHttpFactory").
                ofType(HttpEngine.Factory.class).in(tileSource).get();
        OkHttpClient client = field("mClient").ofType(OkHttpClient.class).in(engine).get();

        HttpResponseCache cache =
                (HttpResponseCache) field("responseCache").
                        ofType(OkResponseCache.class).in(client).get();
        assertThat(cache.getDirectory().getAbsolutePath()).
                isEqualTo(activity.getExternalCacheDir().getAbsolutePath() + "/tile-cache");
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

    @Test
    public void showProgress_shouldShowProgressView() throws Exception {
        FragmentTestUtil.startFragment(mapFragment);
        mapFragment.showProgress();
        assertThat(mapFragment.getView().findViewById(R.id.progress)).isVisible();
    }

    @Test
    public void showProgress_shouldDisableMap() throws Exception {
        FragmentTestUtil.startFragment(mapFragment);
        mapFragment.getView().findViewById(R.id.map).setClickable(true);
        mapFragment.showProgress();
        assertThat(mapFragment.getView().findViewById(R.id.map)).isNotClickable();
    }

    @Test
    public void hideProgress_shouldHideProgressView() throws Exception {
        FragmentTestUtil.startFragment(mapFragment);
        mapFragment.showProgress();
        mapFragment.hideProgress();
        assertThat(mapFragment.getView().findViewById(R.id.progress)).isNotVisible();
    }

    @Test
    public void hideProgress_shouldEnableMap() throws Exception {
        FragmentTestUtil.startFragment(mapFragment);
        mapFragment.getView().findViewById(R.id.map).setClickable(false);
        mapFragment.hideProgress();
        assertThat(mapFragment.getView().findViewById(R.id.map)).isClickable();
    }

    @Test
    public void onActivityCreated_shouldDoStylesheetDownload() throws Exception {
        FragmentTestUtil.startFragment(mapFragment);
        Mockito.verify(styleDownLoader).download();
    }

    @Test
    public void findMe_shouldNotResetZoomAndPointNorthAfterMapPositionEvent() throws Exception {
        FragmentTestUtil.startFragment(mapFragment);
        mapFragment.findMe();
        MapPosition mapPosition = new MapPosition();
        mapPosition.setZoomLevel(10);
        activity.getMap().events.fire(Map.POSITION_EVENT, mapPosition);
        mapFragment.findMe();
        assertThat(mapFragment.mapController.getZoomLevel()).isEqualTo(10);
    }

    @Test
    public void onActivityCreated_shouldVerifyTileCacheDirectoryIsAvailable() throws Exception {
        BaseActivityWithNullCache baseActivityWithNullCache = new BaseActivityWithNullCache();
        mapFragment.setAct(baseActivityWithNullCache);
        mapFragment.onActivityCreated(null);
    }

    private void setTileSourceConfiguration(String source) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString(activity.getString(R.string.settings_key_mapsource), source);
        prefEditor.commit();
    }

    public class BaseActivityWithNullCache extends BaseActivity {
        @Override
        public File getExternalCacheDir() {
            return null;
        }

        @Override
        public Map getMap() {
            return new TestMap();
        }
    }
}
