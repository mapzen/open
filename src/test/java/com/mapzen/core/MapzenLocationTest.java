package com.mapzen.core;

import com.mapzen.MapzenApplication;
import com.mapzen.android.lost.LocationListener;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import android.location.Location;

import static com.mapzen.MapController.getMapController;
import static com.mapzen.support.TestHelper.getTestLocation;
import static org.fest.assertions.api.ANDROID.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class MapzenLocationTest {

    @Before
    public void setup() {
        getMapController().setActivity(TestHelper.initBaseActivity());
    }

    @Test
    public void onLocationChange_shouldUpdateMapController() throws Exception {
        LocationListener listener =
                new MapzenLocation.Listener((MapzenApplication) Robolectric.application);
        Location expected = getTestLocation(111.1f, 222.2f);
        listener.onLocationChanged(expected);
        Location actual = getMapController().getLocation();
        assertThat(actual).isEqualTo(expected);
    }

}
