package com.mapzen.core;

import com.mapzen.MapzenApplication;
import com.mapzen.activity.BaseActivity;
import com.mapzen.android.lost.LocationListener;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import android.content.Intent;
import android.location.Location;

import java.util.List;

import static com.mapzen.MapController.getMapController;
import static com.mapzen.core.MapzenLocation.KEY_LOCATION;
import static com.mapzen.support.TestHelper.getTestLocation;
import static org.fest.assertions.api.Assertions.assertThat;

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
        application.deactivateMapLocationUpdates();
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
        application.deactivateMapLocationUpdates();
        Location expected = getTestLocation(111.1f, 222.2f);
        listener.onLocationChanged(expected);
        List<Intent> intents = Robolectric.getShadowApplication().getBroadcastIntents();
        Intent expectedIntent = new Intent(MapzenLocation.COM_MAPZEN_FIND_ME);
        assertThat(intents.contains(expectedIntent)).isFalse();
    }

}
