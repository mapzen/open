package com.mapzen.shadows;

import android.content.Context;
import android.util.Log;
import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.robolectric.annotation.Implements;

@Implements(org.osmdroid.views.overlay.MyLocationOverlay.class)
public class ShadowMyLocationOverlay {
    public void __constructor__(final Context ctx, final MapView mapView,
                             final ResourceProxy pResourceProxy) {
    }

}
