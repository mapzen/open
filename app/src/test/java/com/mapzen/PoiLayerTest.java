package com.mapzen;

import android.widget.Toast;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscim.core.GeoPoint;
import org.oscim.event.Gesture;
import org.oscim.event.MotionEvent;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.map.TestMap;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowToast;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MapzenTestRunner.class)
public class PoiLayerTest {
    private PoiLayer<MarkerItem> poiLayer;

    @Before
    public void setUp() throws Exception {
        poiLayer = new PoiLayer<MarkerItem>(Robolectric.application, new TestMap(), null);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(poiLayer).isNotNull();
    }

    @Test
    public void onItemSingleTapUp_shouldToastMarkerItemTitle() throws Exception {
        poiLayer.addItem(new MarkerItem("Title", "Description", new GeoPoint(0, 0)));
        poiLayer.onGesture(Gesture.TAP, new FakeMotionEvent(0, 0));
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Title");
        assertThat(ShadowToast.getLatestToast()).hasDuration(Toast.LENGTH_SHORT);
    }

    public static class FakeMotionEvent extends MotionEvent {
        private int x;
        private int y;

        public FakeMotionEvent(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public long getTime() {
            return 0;
        }

        @Override
        public int getAction() {
            return 0;
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            return y;
        }

        @Override
        public float getX(int idx) {
            return 0;
        }

        @Override
        public float getY(int idx) {
            return 0;
        }

        @Override
        public int getPointerCount() {
            return 0;
        }
    }
}
