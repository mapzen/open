package com.mapzen.activity;

import android.app.Activity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class MapActivityTest {
    @Test
    public void onCreate_initializesActivity() throws Exception {
        Activity activity = Robolectric.buildActivity(BaseActivity.class).create().get();
        assertTrue(activity != null);
    }
}
