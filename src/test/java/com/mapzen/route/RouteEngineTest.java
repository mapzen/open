package com.mapzen.route;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class RouteEngineTest {
    private RouteEngine routeEngine;

    @Before
    public void setUp() throws Exception {
        routeEngine = new RouteEngine();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(routeEngine).isNotNull();
    }
}
