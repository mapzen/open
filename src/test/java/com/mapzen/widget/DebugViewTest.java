package com.mapzen.widget;

import com.mapzen.R;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.location.Location;
import android.widget.TextView;

import static com.mapzen.helpers.ZoomController.milesPerHourToMetersPerSecond;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.robolectric.Robolectric.application;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class DebugViewTest {
    private DebugView debugView;

    @Before
    public void setUp() throws Exception {
        debugView = new DebugView(application);

        Location location = TestHelper.getTestLocation(40.660713, -73.989341);
        location.setBearing(128);
        location.setSpeed(milesPerHourToMetersPerSecond(30));
        debugView.setCurrentLocation(location);

        Route route = new Route(TestHelper.MOCK_AROUND_THE_BLOCK);
        Instruction instruction = route.getRouteInstructions().get(0);
        debugView.setClosestInstruction(instruction, 30, 0);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(debugView).isNotNull();
    }

    @Test
    public void currentLocation_shouldHaveTitle() throws Exception {
        assertThat((TextView) debugView.findViewById(R.id.current_location))
                .hasText("Current Location");
    }

    @Test
    public void currentLocation_shouldHaveCoordinates() throws Exception {
        assertThat(debugView.currentCoordinates).hasText("40.660713, -73.989341");
    }

    @Test
    public void currentLocation_shouldHaveBearing() throws Exception {
        assertThat(debugView.currentBearing).hasText("128°");
    }

    @Test
    public void currentLocation_shouldHaveSpeed() throws Exception {
        assertThat(debugView.currentSpeed).hasText("30 mph");
    }

    @Test
    public void closestInstruction_shouldHaveTitle() throws Exception {
        assertThat((TextView) debugView.findViewById(R.id.closest_instruction))
                .hasText("Closest Instruction");
    }

    @Test
    public void closestInstruction_shouldHaveIndex() throws Exception {
        assertThat(debugView.instructionIndex).hasText("index 0");
    }

    @Test
    public void closestInstruction_shouldHaveTurn() throws Exception {
        assertThat(debugView.instructionTurn).hasText("Head on");
    }

    @Test
    public void closestInstruction_shouldHaveName() throws Exception {
        assertThat(debugView.instructionName).hasText("19th Street");
    }

    @Test
    public void closestInstruction_shouldHaveTotalDistance() throws Exception {
        assertThat(debugView.instructionDistance).hasText("0.1 mi");
    }

    @Test
    public void closestInstruction_shouldHaveBearing() throws Exception {
        assertThat(debugView.instructionBearing).hasText("SE 128°");
    }

    @Test
    public void closestInstruction_shouldHaveCoordinates() throws Exception {
        assertThat(debugView.instructionCoordinates).hasText("40.660713, -73.989341");
    }

    @Test
    public void closestInstruction_shouldHaveDistanceFromHere() throws Exception {
        assertThat(debugView.instructionDisplacement).hasText("30 meters away");
    }
}
