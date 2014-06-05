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

import android.widget.TextView;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.robolectric.Robolectric.application;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class DebugViewTest {
    DebugView debugView;

    @Before
    public void setUp() throws Exception {
        debugView = new DebugView(application);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(debugView).isNotNull();
    }

    @Test
    public void closestInstruction_shouldHaveTitle() throws Exception {
        assertThat((TextView) debugView.findViewById(R.id.closest_instruction))
                .hasText("Closest Instruction");
    }

    @Test
    public void closestInstruction_shouldHaveIndex() throws Exception {
        Route route = new Route(TestHelper.MOCK_AROUND_THE_BLOCK);
        Instruction instruction = route.getRouteInstructions().get(0);
        debugView.setClosestInstruction(instruction, 30, 0);
        assertThat(debugView.position).hasText("position 0");
    }

    @Test
    public void closestInstruction_shouldHaveTurn() throws Exception {
        Route route = new Route(TestHelper.MOCK_AROUND_THE_BLOCK);
        Instruction instruction = route.getRouteInstructions().get(0);
        debugView.setClosestInstruction(instruction, 30, 0);
        assertThat(debugView.turn).hasText("Head on");
    }

    @Test
    public void closestInstruction_shouldHaveName() throws Exception {
        Route route = new Route(TestHelper.MOCK_AROUND_THE_BLOCK);
        Instruction instruction = route.getRouteInstructions().get(0);
        debugView.setClosestInstruction(instruction, 30, 0);
        assertThat(debugView.name).hasText("19th Street");
    }

    @Test
    public void closestInstruction_shouldHaveTotalDistance() throws Exception {
        Route route = new Route(TestHelper.MOCK_AROUND_THE_BLOCK);
        Instruction instruction = route.getRouteInstructions().get(0);
        debugView.setClosestInstruction(instruction, 30, 0);
        assertThat(debugView.distance).hasText("0.1 mi");
    }

    @Test
    public void closestInstruction_shouldHaveBearing() throws Exception {
        Route route = new Route(TestHelper.MOCK_AROUND_THE_BLOCK);
        Instruction instruction = route.getRouteInstructions().get(0);
        debugView.setClosestInstruction(instruction, 30, 0);
        assertThat(debugView.bearing).hasText("SE 128°");
    }

    @Test
    public void closestInstruction_shouldHaveCoordinates() throws Exception {
        Route route = new Route(TestHelper.MOCK_AROUND_THE_BLOCK);
        Instruction instruction = route.getRouteInstructions().get(0);
        debugView.setClosestInstruction(instruction, 30, 0);
        assertThat(debugView.coordinates).hasText("40.660713, -73.989341");
    }

    @Test
    public void closestInstruction_shouldHaveDistanceFromHere() throws Exception {
        Route route = new Route(TestHelper.MOCK_AROUND_THE_BLOCK);
        Instruction instruction = route.getRouteInstructions().get(0);
        debugView.setClosestInstruction(instruction, 30, 0);
        assertThat(debugView.displacement).hasText("30 meters away");
    }
}
