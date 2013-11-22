package com.mapzen.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowView;

@Implements(org.osmdroid.views.MapView.class)
public class ShadowMapView extends ShadowView {
    @Implementation
    public void getOverlays() {
    }
}

