package com.mapzen.location;

import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLocationManager;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;
import static com.mapzen.support.TestHelper.getTestLocation;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class LocationHelperTest {
    private LocationHelper locationHelper;
    private LocationManager locationManager;
    private ShadowLocationManager shadowLocationManager;
    private TestConnectionCallbacks connectionCallbacks;

    @Before
    public void setUp() throws Exception {
        connectionCallbacks = new TestConnectionCallbacks();
        locationHelper = new LocationHelper(application, connectionCallbacks);
        locationManager = (LocationManager) application.getSystemService(LOCATION_SERVICE);
        shadowLocationManager = shadowOf(locationManager);
        locationHelper.connect();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(locationHelper).isNotNull();
    }

    @Test
    public void connect_shouldCallOnConnected() throws Exception {
        assertThat(connectionCallbacks.connected).isTrue();
    }

    @Test
    public void disconnect_shouldCallOnDisconnected() throws Exception {
        locationHelper.disconnect();
        assertThat(connectionCallbacks.connected).isFalse();
    }

    @Test(expected = IllegalStateException.class)
    public void getLastLocation_shouldThrowExceptionIfNotConnected() throws Exception {
        locationHelper = new LocationHelper(application, connectionCallbacks);
        locationHelper.getLastLocation();
    }

    @Test
    public void getLastLocation_shouldReturnNullIfNoLocationAvailable() throws Exception {
        assertThat(locationHelper.getLastLocation()).isNull();
    }

    @Test
    public void getLastLocation_shouldReturnGpsLocationIfOnlyProvider() throws Exception {
        Location location = new Location(GPS_PROVIDER);
        shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, location);
        assertThat(locationHelper.getLastLocation()).isEqualTo(location);
    }

    @Test
    public void getLastLocation_shouldReturnNetworkLocationIfOnlyProvider() throws Exception {
        Location location = new Location(NETWORK_PROVIDER);
        shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, location);
        assertThat(locationHelper.getLastLocation()).isEqualTo(location);
    }

    @Test
    public void getLastLocation_shouldReturnPassiveLocationIfOnlyProvider() throws Exception {
        Location location = new Location(PASSIVE_PROVIDER);
        shadowLocationManager.setLastKnownLocation(PASSIVE_PROVIDER, location);
        assertThat(locationHelper.getLastLocation()).isEqualTo(location);
    }

    @Test
    public void getLastLocation_shouldReturnMostAccurateResult() throws Exception {
        Location gpsLocation = new Location(GPS_PROVIDER);
        gpsLocation.setAccuracy(1000);
        shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, gpsLocation);

        Location networkLocation = new Location(NETWORK_PROVIDER);
        networkLocation.setAccuracy(100);
        shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, networkLocation);

        Location passiveLocation = new Location(PASSIVE_PROVIDER);
        passiveLocation.setAccuracy(10);
        shadowLocationManager.setLastKnownLocation(PASSIVE_PROVIDER, passiveLocation);

        assertThat(locationHelper.getLastLocation()).isEqualTo(passiveLocation);
    }

    @Test(expected = IllegalStateException.class)
    public void requestLocationUpdates_shouldThrowExceptionIfNotConnected() throws Exception {
        locationHelper = new LocationHelper(application, connectionCallbacks);
        locationHelper.requestLocationUpdates(LocationRequest.create(), new TestLocationListener());
    }

    @Test
    public void requestLocationUpdates_shouldRegisterGpsAndNetworkListener() throws Exception {
        LocationListener listener = new TestLocationListener();
        locationHelper.requestLocationUpdates(LocationRequest.create(), listener);
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
    }

    @Test
    public void requestLocationUpdates_shouldNotifyOnLocationChangedGps() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        locationHelper.requestLocationUpdates(LocationRequest.create(), listener);
        Location location = new Location(GPS_PROVIDER);
        shadowLocationManager.simulateLocation(location);
        assertThat(listener.location).isEqualTo(location);
    }

    @Test
    public void requestLocationUpdates_shouldNotifyOnLocationChangedNetwork() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        locationHelper.requestLocationUpdates(LocationRequest.create(), listener);
        Location location = new Location(NETWORK_PROVIDER);
        shadowLocationManager.simulateLocation(location);
        assertThat(listener.location).isEqualTo(location);
    }

    @Test
    public void requestLocationUpdates_shouldNotNotifyIfDoesNotExceedCriteriaGps()
            throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        request.setFastestInterval(5000);
        request.setSmallestDisplacement(200000);
        locationHelper.requestLocationUpdates(request, listener);

        final long time = System.currentTimeMillis();
        Location location1 = getTestLocation(GPS_PROVIDER, 0, 0, time);
        Location location2 = getTestLocation(GPS_PROVIDER, 1, 1, time + 1000);

        shadowLocationManager.simulateLocation(location1);
        shadowLocationManager.simulateLocation(location2);
        assertThat(listener.location).isEqualTo(location1);
    }

    @Test
    public void requestLocationUpdates_shouldNotNotifyIfDoesNotExceedCriteriaNetwork()
            throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        request.setFastestInterval(5000);
        request.setSmallestDisplacement(200000);
        locationHelper.requestLocationUpdates(request, listener);

        final long time = System.currentTimeMillis();
        Location location1 = getTestLocation(NETWORK_PROVIDER, 0, 0, time);
        Location location2 = getTestLocation(NETWORK_PROVIDER, 1, 1, time + 1000);

        shadowLocationManager.simulateLocation(location1);
        shadowLocationManager.simulateLocation(location2);
        assertThat(listener.location).isEqualTo(location1);
    }

    @Test
    public void removeLocationUpdates_shouldUnregisterAllListeners() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        locationHelper.requestLocationUpdates(request, listener);
        locationHelper.removeLocationUpdates(listener);
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
    }

    @Test
    public void disconnect_shouldUnregisterAllListeners() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        locationHelper.requestLocationUpdates(request, listener);
        locationHelper.disconnect();
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
    }

    class TestConnectionCallbacks implements LocationHelper.ConnectionCallbacks {
        private boolean connected = false;

        @Override
        public void onConnected(Bundle connectionHint) {
            connected = true;
        }

        @Override
        public void onDisconnected() {
            connected = false;
        }
    }

    class TestLocationListener implements LocationListener {
        private Location location;

        @Override
        public void onLocationChanged(Location location) {
            this.location = location;
        }
    }
}
