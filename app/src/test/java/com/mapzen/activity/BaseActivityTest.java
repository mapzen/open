package com.mapzen.activity;

import com.mapzen.shadows.ShadowCanvas;
import com.mapzen.shadows.ShadowMapView;
import com.mapzen.shadows.ShadowMyLocationOverlay;
import com.mapzen.shadows.ShadowPicture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertTrue;

@Config(shadows={ ShadowMapView.class, ShadowMyLocationOverlay.class, ShadowPicture.class, ShadowCanvas.class })
@RunWith(RobolectricTestRunner.class)
public class BaseActivityTest {
    @Test
    public void onCreate_initializesActivity() throws Exception {
        BaseActivity activity = Robolectric.buildActivity(BaseActivity.class).create().get();
        assertTrue(activity != null);
    }
}