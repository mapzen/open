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
import static com.mapzen.widget.DebugView.getDirectionForBearing;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class DebugViewTest {
    private DebugView debugView;

    @Before
    public void setUp() throws Exception {
        debugView = new DebugView(application);

        Location currentLocation = TestHelper.getTestLocation(40.660713, -73.989341);
        currentLocation.setBearing(130);
        currentLocation.setSpeed(milesPerHourToMetersPerSecond(30));
        debugView.setCurrentLocation(currentLocation);

        Location snapLocation = TestHelper.getTestLocation(41.660713, -74.989341);
        snapLocation.setBearing(140);
        snapLocation.setSpeed(milesPerHourToMetersPerSecond(40));
        debugView.setSnapLocation(snapLocation);

        Route route = new Route(TestHelper.MOCK_AROUND_THE_BLOCK);
        Instruction instruction = route.getRouteInstructions().get(0);
        debugView.setClosestInstruction(instruction, 30);
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
        assertThat(debugView.currentBearing).hasText("SE 130°");
    }

    @Test
    public void currentLocation_shouldHaveSpeed() throws Exception {
        assertThat(debugView.currentSpeed).hasText("30 mph");
    }

    @Test
    public void currentLocation_shouldHaveAverageSpeed() throws Exception {
        debugView.setAverageSpeed(milesPerHourToMetersPerSecond(45));
        assertThat(debugView.averageSpeed).hasText("(Avg: 45 mph)");
    }

    @Test
    public void snapLocation_shouldHaveTitle() throws Exception {
        assertThat((TextView) debugView.findViewById(R.id.snap_location))
                .hasText("Snap Location");
    }

    @Test
    public void snapLocation_shouldHaveCoordinates() throws Exception {
        assertThat(debugView.snapCoordinates).hasText("41.660713, -74.989341");
    }

    @Test
    public void closestInstruction_shouldHaveTitle() throws Exception {
        assertThat((TextView) debugView.findViewById(R.id.closest_instruction))
                .hasText("Closest Instruction");
    }

    @Test
    public void closestInstruction_shouldHaveCoordinates() throws Exception {
        assertThat(debugView.instructionCoordinates).hasText("40.660713, -73.989341");
    }

    @Test
    public void closestInstruction_shouldHaveBearing() throws Exception {
        assertThat(debugView.instructionBearing).hasText("SE 128°");
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
    public void closestInstruction_shouldHaveDistance() throws Exception {
        assertThat(debugView.instructionDistance).hasText("0.1 mi");
    }

    @Test
    public void closestInstruction_shouldHaveDistanceFromHere() throws Exception {
        assertThat(debugView.instructionDisplacement).hasText("30 meters away");
    }

    @Test
    public void getDirectionForBearing_0degreesShouldReturnNorth() throws Exception {
        assertThat(getDirectionForBearing(0)).isEqualTo("N");
    }

    @Test
    public void getDirectionForBearing_45degreesShouldReturnNorthEast() throws Exception {
        assertThat(getDirectionForBearing(45)).isEqualTo("NE");
    }

    @Test
    public void getDirectionForBearing_90degreesShouldReturnEast() throws Exception {
        assertThat(getDirectionForBearing(90)).isEqualTo("E");
    }

    @Test
    public void getDirectionForBearing_135degreesShouldReturnSouthEast() throws Exception {
        assertThat(getDirectionForBearing(135)).isEqualTo("SE");
    }

    @Test
    public void getDirectionForBearing_180degreesShouldReturnSouth() throws Exception {
        assertThat(getDirectionForBearing(180)).isEqualTo("S");
    }

    @Test
    public void getDirectionForBearing_180degreesShouldReturnSouthWest() throws Exception {
        assertThat(getDirectionForBearing(225)).isEqualTo("SW");
    }

    @Test
    public void getDirectionForBearing_270degreesShouldReturnWest() throws Exception {
        assertThat(getDirectionForBearing(270)).isEqualTo("W");
    }

    @Test
    public void getDirectionForBearing_315degreesShouldReturnNorthWest() throws Exception {
        assertThat(getDirectionForBearing(315)).isEqualTo("NW");
    }

    @Test
    public void getDirectionForBearing_shouldRoundFloatToNearest45Degrees() throws Exception {
        assertThat(getDirectionForBearing(7.9f)).isEqualTo("N");
        assertThat(getDirectionForBearing(202.6f)).isEqualTo("SW");
        assertThat(getDirectionForBearing(350.0f)).isEqualTo("N");
    }
}

