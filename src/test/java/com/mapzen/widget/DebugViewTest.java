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
        assertThat((TextView) debugView.findViewById(R.id.position))
                .hasText("position 0");
    }

    @Test
    public void closestInstruction_shouldHaveTurn() throws Exception {
        Route route = new Route(TestHelper.MOCK_AROUND_THE_BLOCK);
        Instruction instruction = route.getRouteInstructions().get(0);
        debugView.setClosestInstruction(instruction, 30, 0);
        assertThat((TextView) debugView.findViewById(R.id.turn))
                .hasText(instruction.getHumanTurnInstruction());
    }

    @Test
    public void closestInstruction_shouldHaveName() throws Exception {
        Route route = new Route(TestHelper.MOCK_AROUND_THE_BLOCK);
        Instruction instruction = route.getRouteInstructions().get(0);
        debugView.setClosestInstruction(instruction, 30, 0);
        assertThat((TextView) debugView.findViewById(R.id.name))
                .hasText(instruction.getName());
    }

    @Test
    public void closestInstruction_shouldHaveTotalDistance() throws Exception {
        Route route = new Route(TestHelper.MOCK_AROUND_THE_BLOCK);
        Instruction instruction = route.getRouteInstructions().get(0);
        debugView.setClosestInstruction(instruction, 30, 0);
        assertThat((TextView) debugView.findViewById(R.id.distance))
                .hasText(instruction.getFormattedDistance());
    }

    @Test
    public void closestInstruction_shouldHaveBearing() throws Exception {
        Route route = new Route(TestHelper.MOCK_AROUND_THE_BLOCK);
        Instruction instruction = route.getRouteInstructions().get(0);
        debugView.setClosestInstruction(instruction, 30, 0);
        assertThat((TextView) debugView.findViewById(R.id.bearing))
                .hasText(instruction.getDirection() + " " + instruction.getBearing() + "°");
    }

    @Test
    public void closestInstruction_shouldHaveDistanceFromHere() throws Exception {
        Route route = new Route(TestHelper.MOCK_AROUND_THE_BLOCK);
        Instruction instruction = route.getRouteInstructions().get(0);
        debugView.setClosestInstruction(instruction, 30, 0);
        assertThat((TextView) debugView.findViewById(R.id.how_far_away))
                .hasText("30 meter(s) away");
    }
}
