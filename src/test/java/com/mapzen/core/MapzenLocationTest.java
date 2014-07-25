package com.mapzen.core;

import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.android.lost.LocationClient;
import com.mapzen.android.lost.LocationListener;
import com.mapzen.android.lost.LocationRequest;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import java.util.List;

import static com.mapzen.MapController.getMapController;
import static com.mapzen.core.MapzenLocation.KEY_LOCATION;
import static com.mapzen.support.TestHelper.getTestLocation;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class MapzenLocationTest {
    private MapzenApplication application;
    private LocationListener listener;

    @Before
    public void setup() {
        getMapController().setActivity(TestHelper.initBaseActivity());
        application = (MapzenApplication) Robolectric.application;
        listener = new MapzenLocation.Listener(application);
    }

    @Test
    public void onLocationChange_shouldUpdateMapController() throws Exception {
        Location expected = getTestLocation(111.1f, 222.2f);
        listener.onLocationChanged(expected);
        Location actual = getMapController().getLocation();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void onLocationChange_shouldNotUpdateMapController() {
        application.deactivateMoveMapToLocation();
        Location expected = getTestLocation(111.1f, 222.2f);
        listener.onLocationChanged(expected);
        Location actual = getMapController().getLocation();
        assertThat(actual).isNotEqualTo(expected);
    }

    @Test
    public void onLocationChange_shouldSendLocationBroadcast() {
        Location expected = getTestLocation(111.1f, 222.2f);
        listener.onLocationChanged(expected);
        List<Intent> intents = Robolectric.getShadowApplication().getBroadcastIntents();
        Intent expectedIntent = new Intent(BaseActivity.COM_MAPZEN_UPDATES_LOCATION);
        expectedIntent.putExtra(KEY_LOCATION, expected);
        assertThat(intents.contains(expectedIntent)).isTrue();
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
        LocationClient client = Mockito.mock(LocationClient.class);
        ArgumentCaptor<LocationRequest> argument = ArgumentCaptor.forClass(LocationRequest.class);
        MapzenLocation.ConnectionCallbacks callbacks =
                new MapzenLocation.ConnectionCallbacks(application);
        callbacks.setLocationClient(client);
        callbacks.onConnected(null);
        verify(client).requestLocationUpdates(argument.capture(), any(LocationListener.class));
        assertThat(argument.getValue().getInterval()).isEqualTo(1000);
    }

    @Test
    public void shouldSetCustomLocationUpdateInterval() throws Exception {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(application.getString(R.string.settings_location_update_interval_key), 2000);
        editor.commit();

        LocationClient client = Mockito.mock(LocationClient.class);
        ArgumentCaptor<LocationRequest> argument = ArgumentCaptor.forClass(LocationRequest.class);
        MapzenLocation.ConnectionCallbacks callbacks =
                new MapzenLocation.ConnectionCallbacks(application);
        callbacks.setLocationClient(client);
        callbacks.onConnected(null);
        verify(client).requestLocationUpdates(argument.capture(), any(LocationListener.class));
        assertThat(argument.getValue().getInterval()).isEqualTo(2000);
    }
}
