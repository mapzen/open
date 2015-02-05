package com.mapzen.open;

import com.mapzen.open.core.TestAppModule;
import com.mapzen.open.support.MapzenTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MapzenTestRunner.class)
public class MapzenApplicationTest {
    private MapzenApplication app;

    @Before
    public void setUp() throws Exception {
        app = (MapzenApplication) Robolectric.application;
    }

    @After
    public void tearDown() throws Exception {
        TestAppModule.setSimpleCryptEnabled(true);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(app).isNotNull();
    }

    @Test
    public void shouldReturnSameInstance() throws Exception {
        assertThat(app).isSameAs((MapzenApplication) new Activity().getApplication());
    }

    @Test
    public void onCreate_shouldInitOsmOauthService() throws Exception {
        app.onCreate();
        assertThat(app.getOsmOauthService()).isNotNull();
    }

    @Test
    public void onCreate_shouldNotInitOsmOauthServiceIfSimpleCryptIsNull() throws Exception {
        app.setOsmOauthService(null);
        TestAppModule.setSimpleCryptEnabled(false);
        app.onCreate();
        assertThat(app.getOsmOauthService()).isNull();
    }
}
