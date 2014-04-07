package com.mapzen;

import android.app.Activity;

import com.mapzen.support.MapzenTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class MapzenApplicationTest {
    private MapzenApplication app;

    @Before
    public void setUp() throws Exception {
        app = (MapzenApplication) Robolectric.application;
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(app).isNotNull();
    }

    @Test
    public void shouldReturnSameInstance() throws Exception {
        assertThat(app).isSameAs((MapzenApplication) new Activity().getApplication());
    }
}
