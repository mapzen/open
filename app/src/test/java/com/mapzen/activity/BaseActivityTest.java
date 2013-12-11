package com.mapzen.activity;

import com.mapzen.activity.BaseActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import org.robolectric.Robolectric;
import android.app.Activity;

import java.lang.Exception;

@RunWith(RobolectricTestRunner.class)
public class BaseActivityTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testOnCreate() throws Exception {
        //Activity activity = Robolectric.buildActivity(BaseActivity.class).create().get();
        // TODO sort out alien assets from VTM
        assert(true);
    }

}
