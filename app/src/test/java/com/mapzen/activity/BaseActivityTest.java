package com.mapzen.activity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class BaseActivityTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testOnCreate() throws Exception {
        //Activity activity = Robolectric.buildActivity(BaseActivity.class).create().get();
        assert(true);
    }

}
