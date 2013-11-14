package com.mapzen.activity;

import android.app.Activity;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.views.MapView;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowPath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class MapActivityTest {
    @Test
    public void onCreate_initializesActivity() throws Exception {
        Activity activity = Robolectric.buildActivity(MapActivity.class).create().get();
        assertTrue(activity != null);
    }
}
