package com.mapzen.location;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class LocationRequestTest {
    private LocationRequest locationRequest;

    @Before
    public void setUp() throws Exception {
        locationRequest = LocationRequest.create();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(locationRequest).isNotNull();
    }
}
