package com.mapzen.activity;

import com.mapzen.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.fest.assertions.api.ANDROID.assertThat;

@RunWith(MapzenTestRunner.class)
public class BaseActivityTest {
    private BaseActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(BaseActivity.class).create().get();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(activity).isNotNull();
    }
}
