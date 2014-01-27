package com.mapzen.util;

import com.mapzen.MapzenTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(MapzenTestRunner.class)
public class ApiHelperTest {
    @Test
    public void testGetUrlForSuggest() throws Exception {
        String expected = "http://api-pelias-test.mapzen.com/suggest?query=testing";
        String actual = ApiHelper.getUrlForSuggest("testing");
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void testGetUrlForSearch() throws Exception {
        String expected = "http://api-pelias-test.mapzen.com/"
                + "search?query=testing&"
                + "viewbox=2.0%2C3.0%2C4.0%2C1.0";
        BoundingBox testBoundingBox = new BoundingBox(1.0, 2.0, 3.0, 4.0);
        String actual = ApiHelper.getUrlForSearch("testing", testBoundingBox);
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void testGetRouteUrl() throws Exception {
        String expected = "http://api-osrm-test.mapzen.com/"
                + "car/viaroute?z=17&output=json&loc=1.0%2C2.0&loc=3.0%2C4.0&instructions=true";
        String actual = ApiHelper.getRouteUrl(17, new GeoPoint(1.0, 2.0), new GeoPoint(3.0, 4.0));
        assertThat(actual, equalTo(expected));
    }
}
