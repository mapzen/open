package com.mapzen.open.core;

import com.mapzen.android.lost.api.FusedLocationProviderApi;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.open.MapController;
import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.support.MapzenTestRunner;
import com.mapzen.open.support.TestHelper;
import com.mapzen.open.support.TestHelper.LocationUpdateSubscriber;

import com.squareup.otto.Bus;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;

import java.util.List;

import javax.inject.Inject;

import static com.mapzen.android.lost.api.LocationRequest.PRIORITY_HIGH_ACCURACY;
import static com.mapzen.open.core.MapzenLocation.onLocationServicesConnected;
import static com.mapzen.open.support.TestHelper.getTestLocation;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.shadows.ShadowToast.getTextOfLatestToast;

@RunWith(MapzenTestRunner.class)
public class MapzenLocationTest {
    private MapzenApplication application;
    private LocationListener listener;
    @Inject MapController mapController;
    @Inject Bus bus;

    @Before
    public void setup() {
        application = (MapzenApplication) Robolectric.application;
        application.inject(this);
        LocationServices.FusedLocationApi = null;
        mapController.setActivity(TestHelper.initBaseActivity());
        listener = new MapzenLocation.Listener(application);
    }

    @Test
    public void onLocationChange_shouldUpdateMapController() throws Exception {
        Location expected = getTestLocation(111.1f, 222.2f);
        listener.onLocationChanged(expected);
        Location actual = mapController.getLocation();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void onLocationChange_shouldNotUpdateMapController() {
        application.deactivateMoveMapToLocation();
        Location expected = getTestLocation(111.1f, 222.2f);
        listener.onLocationChanged(expected);
        Location actual = mapController.getLocation();
        assertThat(actual).isNotEqualTo(expected);
    }

    @Test
    public void onLocationChange_shouldPostEvent() {
        LocationUpdateSubscriber locationUpdateSubscriber = new LocationUpdateSubscriber();
        bus.register(locationUpdateSubscriber);
        Location expected = getTestLocation(111.1f, 222.2f);
        listener.onLocationChanged(expected);
        assertThat(locationUpdateSubscriber.getEvent().getLocation()).isEqualTo(expected);
    }

    @Test
    public void onLocationChange_shouldSendFindMeBroadcast() {
        Location expected = getTestLocation(111.1f, 222.2f);
        listener.onLocationChanged(expected);
        List<Intent> intents = Robolectric.getShadowApplication().getBroadcastIntents();
        Intent expectedIntent = new Intent(MapzenLocation.COM_MAPZEN_FIND_ME);
        assertThat(intents.contains(expectedIntent)).isTrue();
    }

    @Test
    public void onLocationChange_shouldNotSendFindMeBroadcast() {
        application.deactivateMoveMapToLocation();
        Location expected = getTestLocation(111.1f, 222.2f);
        listener.onLocationChanged(expected);
        List<Intent> intents = Robolectric.getShadowApplication().getBroadcastIntents();
        Intent expectedIntent = new Intent(MapzenLocation.COM_MAPZEN_FIND_ME);
        assertThat(intents.contains(expectedIntent)).isFalse();
    }

    @Test
    public void shouldSetDefaultLocationUpdateInterval() throws Exception {
        FusedLocationProviderApi api = Mockito.mock(FusedLocationProviderApi.class);
        LocationServices.FusedLocationApi = api;
        ArgumentCaptor<LocationRequest> argument = ArgumentCaptor.forClass(LocationRequest.class);
        onLocationServicesConnected(mapController, api, application);
        verify(api).requestLocationUpdates(argument.capture(), any(LocationListener.class));
        assertThat(argument.getValue().getInterval()).isEqualTo(1000);
    }

    @Test
    public void shouldSetCustomLocationUpdateInterval() throws Exception {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(application.getString(R.string.settings_location_update_interval_key), 2000);
        editor.commit();

        FusedLocationProviderApi api = Mockito.mock(FusedLocationProviderApi.class);
        LocationServices.FusedLocationApi = api;
        ArgumentCaptor<LocationRequest> argument = ArgumentCaptor.forClass(LocationRequest.class);
        onLocationServicesConnected(mapController, api, application);
        verify(api).requestLocationUpdates(argument.capture(), any(LocationListener.class));
        assertThat(argument.getValue().getInterval()).isEqualTo(2000);
    }

    @Test @Ignore("Not applicable to demo version")
    public void onLocationServicesConnected_shouldUpdateMapController() throws Exception {
        Location expected = initLastLocation();
        onLocationServicesConnected(mapController, LocationServices.FusedLocationApi, application);
        Location actual = mapController.getLocation();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void onLocationServicesConnected_shouldResetZoomLevel() throws Exception {
        mapController.setZoomLevel(1);
        initLastLocation();
        onLocationServicesConnected(mapController, LocationServices.FusedLocationApi, application);
        assertThat(mapController.getZoomLevel()).isEqualTo(MapController.DEFAULT_ZOOM_LEVEL);
    }

    @Test
    public void onLocationServicesConnected_shouldSetPriority() throws Exception {
        FusedLocationProviderApi api = Mockito.mock(FusedLocationProviderApi.class);
        LocationServices.FusedLocationApi = api;
        ArgumentCaptor<LocationRequest> argument = ArgumentCaptor.forClass(LocationRequest.class);
        onLocationServicesConnected(mapController, api, application);
        verify(api).requestLocationUpdates(argument.capture(), any(LocationListener.class));
        assertThat(argument.getValue().getPriority()).isEqualTo(PRIORITY_HIGH_ACCURACY);
    }

    @Test @Ignore("Not applicable to demo version")
    public void shouldNotifyUserIfLastLocationNotAvailable() throws Exception {
        LocationManager locationManager = (LocationManager)
                application.getSystemService(Context.LOCATION_SERVICE);
        shadowOf(locationManager).setLastKnownLocation(LocationManager.GPS_PROVIDER, null);
        shadowOf(locationManager).setLastKnownLocation(LocationManager.NETWORK_PROVIDER, null);
        shadowOf(locationManager).setLastKnownLocation(LocationManager.GPS_PROVIDER, null);
        onLocationServicesConnected(mapController, LocationServices.FusedLocationApi, application);
        assertThat(getTextOfLatestToast()).isEqualTo(application.getString(R.string.waiting));
    }

    private Location initLastLocation() {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(1.0);
        location.setLongitude(2.0);
        shadowOf((LocationManager) application.getSystemService(Context.LOCATION_SERVICE))
                .setLastKnownLocation(LocationManager.GPS_PROVIDER, location);
        return location;
    }
}
