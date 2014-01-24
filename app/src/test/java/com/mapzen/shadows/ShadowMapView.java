package com.mapzen.shadows;

import android.content.Context;
import android.util.AttributeSet;

import org.oscim.android.AndroidAssetAdapter;
import org.oscim.android.MapView;
import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.backend.AssetAdapter;
import org.oscim.backend.CanvasAdapter;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowView;

/**
 * Custom shadow implementation for {@link org.oscim.android.MapView}.
 */
@SuppressWarnings("unused")
@Implements(MapView.class)
public class ShadowMapView extends ShadowView {
    public void __constructor__(Context context, AttributeSet attributeSet) {
        CanvasAdapter.g = AndroidGraphics.INSTANCE;
        AssetAdapter.g = new AndroidAssetAdapter(context);
    }
}
