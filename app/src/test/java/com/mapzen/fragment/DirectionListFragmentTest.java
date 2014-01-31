package com.mapzen.fragment;

import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mapzen.MapzenTestRunner;
import com.mapzen.R;
import com.mapzen.osrm.Instruction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@RunWith(MapzenTestRunner.class)
public class DirectionListFragmentTest {
    private DirectionListFragment fragment;
    private ListView listView;
    private TestListener listener;
    private Resources res;

    @Before
    public void setUp() throws Exception {
        final ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(new TestInstruction("First Instruction", 1, 0.1));
        instructions.add(new TestInstruction("Second Instruction", 2, 2.345));
        instructions.add(new TestInstruction("Last Instruction", 15, 15));
        listener = new TestListener();
        fragment = DirectionListFragment.newInstance(instructions, listener);
        startFragment(fragment);
        listView = (ListView) fragment.getView().findViewById(android.R.id.list);
        res = application.getResources();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(fragment).isNotNull();
    }

    @Test
    public void shouldHaveListView() throws Exception {
        assertThat(listView).isNotNull();
    }

    @Test
    public void firstListItem_shouldBeCurrentLocation() throws Exception {
        View view = listView.getAdapter().getView(0, null, null);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView instruction = (TextView) view.findViewById(R.id.simple_instruction);
        TextView distance = (TextView) view.findViewById(R.id.distance);

        assertThat(icon.getDrawable()).isEqualTo(res.getDrawable(R.drawable.ic_locate_active));
        assertThat(instruction).hasText("Current Location");
        assertThat(distance).isEmpty();
    }

    @Test
    public void secondListItem_shouldBeFirstInstruction() throws Exception {
        View view = listView.getAdapter().getView(1, null, null);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView instruction = (TextView) view.findViewById(R.id.simple_instruction);
        TextView distance = (TextView) view.findViewById(R.id.distance);

        assertThat(icon.getDrawable()).isEqualTo(res.getDrawable(R.drawable.ic_route_bl_1));
        assertThat(instruction).hasText("First Instruction");
        assertThat(distance).hasText("528 ft");
    }

    @Test
    public void thirdListItem_shouldBeSecondInstruction() throws Exception {
        View view = listView.getAdapter().getView(2, null, null);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView instruction = (TextView) view.findViewById(R.id.simple_instruction);
        TextView distance = (TextView) view.findViewById(R.id.distance);

        assertThat(icon.getDrawable()).isEqualTo(res.getDrawable(R.drawable.ic_route_bl_2));
        assertThat(instruction).hasText("Second Instruction");
        assertThat(distance).hasText("2.35 mi");
    }

    @Test
    public void lastListItem_shouldBeLastInstruction() throws Exception {
        int indexOfLastItem = listView.getAdapter().getCount() - 1;
        View view = listView.getAdapter().getView(indexOfLastItem, null, null);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView instruction = (TextView) view.findViewById(R.id.simple_instruction);
        TextView distance = (TextView) view.findViewById(R.id.distance);

        assertThat(icon.getDrawable()).isEqualTo(res.getDrawable(R.drawable.ic_route_bl_15));
        assertThat(instruction).hasText("Last Instruction");
        assertThat(distance).hasText("15 mi");
    }

    @Test
    public void listItemClick_shouldNotifyListener() throws Exception {
        int indexOfLastItem = listView.getAdapter().getCount() - 1;
        View view = listView.getAdapter().getView(indexOfLastItem, null, null);
        listView.performItemClick(view, indexOfLastItem, 0);
        assertThat(listener.index).isEqualTo(indexOfLastItem - 1);
    }

    class TestInstruction extends Instruction {
        private String simpleInstruction;
        private int turnInstruction;
        private double distanceInMiles;

        public TestInstruction(String simpleInstruction, int turnInstruction,
                               double distanceInMiles) throws Exception {
            this.simpleInstruction = simpleInstruction;
            this.turnInstruction = turnInstruction;
            this.distanceInMiles = distanceInMiles;
        }

        @Override
        public String getSimpleInstruction() {
            return simpleInstruction;
        }

        @Override
        public int getTurnInstruction() {
            return turnInstruction;
        }

        @Override
        public double getDistanceInMiles() {
            return distanceInMiles;
        }
    }

    class TestListener implements DirectionListFragment.DirectionListener {
        private int index;

        @Override
        public void onInstructionSelected(int index) {
            this.index = index;
        }
    }
}
