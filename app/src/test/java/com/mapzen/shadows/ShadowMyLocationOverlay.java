package com.mapzen.shadows;

import android.content.Context;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.robolectric.annotation.Implements;

@Implements(org.osmdroid.views.overlay.MyLocationOverlay.class)
public class ShadowMyLocationOverlay {
    public void __constructor__(final Context ctx, final MapView mapView,
                             final ResourceProxy pResourceProxy) {
    }

}
