package com.mapzen.shadows;

import org.oscim.android.AndroidAssets;
import org.oscim.android.MapView;
import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.map.Map;
import org.oscim.map.TestMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowView;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Custom shadow implementation for {@link org.oscim.android.MapView}.
 */
@SuppressWarnings("unused")
@Implements(MapView.class)
public class ShadowMapView extends ShadowView {
    public void __constructor__(Context context, AttributeSet attributeSet) {
        AndroidGraphics.init();
        AndroidAssets.init(context);
    }

    @Implementation
    public Map map() {
        return new TestMap();
    }
}

