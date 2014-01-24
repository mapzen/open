package com.mapzen;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mapzen.shadows.ShadowVolley;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLocationManager;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MapzenTestRunner.class)
public class MapzenApplicationTest {
    private MapzenApplication app;

    @Before
    public void setUp() throws Exception {
        simulateLocation(1.0, 2.0);
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

    @Test
    public void shouldFindBestLocation() throws Exception {
        assertThat(app.findBestLocation()).hasLatitude(1.0);
        assertThat(app.findBestLocation()).hasLongitude(2.0);
    }

    @Test
    public void shouldEnqueueRequest() throws Exception {
        Request request = new JsonObjectRequest(null, null, null, null);
        app.enqueueApiRequest(request);
        assertThat(ShadowVolley.getMockRequestQueue().getRequests()).hasSize(1);
    }

    @Test
    public void shouldCancelRequest() throws Exception {
        Request request = new JsonObjectRequest(null, null, null, null);
        app.enqueueApiRequest(request);
        app.cancelAllApiRequests();
        assertThat(request.isCanceled()).isTrue();
    }

    public static void simulateLocation(double lat, double lon) {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setProvider(LocationManager.GPS_PROVIDER);

        LocationManager manager = (LocationManager)
                Robolectric.application.getSystemService(Context.LOCATION_SERVICE);
        ShadowLocationManager shadowManager = Robolectric.shadowOf(manager);
        shadowManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true);
        shadowManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
        shadowManager.setProviderEnabled(LocationManager.PASSIVE_PROVIDER, true);
        shadowManager.simulateLocation(location);
    }
}
