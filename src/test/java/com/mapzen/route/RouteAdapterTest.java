package com.mapzen.route;

import com.mapzen.osrm.Instruction;
import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class RouteAdapterTest {
    private RouteAdapter routeAdapter;

    @Before
    public void setUp() throws Exception {
        routeAdapter = new RouteAdapter(Robolectric.application, new ArrayList<Instruction>());
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(routeAdapter).isNotNull();
    }
}
