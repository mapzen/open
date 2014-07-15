package com.mapzen.adapters;

import com.mapzen.entity.SimpleFeature;
import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.fest.assertions.api.ANDROID.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class PlaceArrayAdapterTest {
    private PlaceArrayAdapter adapter;

    @Before
    public void setUp() throws Exception {
        adapter = new PlaceArrayAdapter(Robolectric.application, new ArrayList<SimpleFeature>());
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(adapter).isNotNull();
    }
}
