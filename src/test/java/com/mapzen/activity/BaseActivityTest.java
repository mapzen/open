package com.mapzen.activity;

import android.app.Activity;
import android.widget.SearchView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowView;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class BaseActivityTest {
    @Test
    @Config(shadows = {SearchView.class, ShadowView.class})
    public void onCreate_initializesActivity() throws Exception {
        Activity activity = Robolectric.buildActivity(BaseActivity.class).create().get();
        assertTrue(activity != null);
    }

}
