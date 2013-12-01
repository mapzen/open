package com.mapzen.shadows;

import android.graphics.Canvas;
import android.util.Log;
import org.robolectric.annotation.Implements;

@Implements(Canvas.class)
public class ShadowCanvas extends org.robolectric.shadows.ShadowCanvas {
    public void __constructor__(int foo) {
    }
}
