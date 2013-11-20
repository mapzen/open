package com.mapzen.activity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class SearchActivityTest {
    @Test
    public void onCreate_initializesActivity() throws Exception {
        SearchActivity activity = Robolectric.buildActivity(SearchActivity.class).create().get();
        assertTrue(activity != null);
    }
}
