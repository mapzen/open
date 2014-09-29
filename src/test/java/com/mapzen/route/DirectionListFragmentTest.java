package com.mapzen.route;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.mapzen.R;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.osrm.Instruction;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.widget.DistanceView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.mapzen.entity.SimpleFeature.TEXT;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class DirectionListFragmentTest {
    private static final double METERS_IN_MILE = 1609.34;
    private static final String TEST_LOCATION = "Dalvik";
    private DirectionListFragment fragment;
    private ListView listView;
    private TestListener listener;
    private Resources res;
    private ArrayList<Instruction> instructions;
    private SimpleFeature simpleFeature;
    @InjectView(R.id.starting_point_list) TextView startingPointTextView;
    @InjectView(R.id.destination_list) TextView destinationTextView;

    @Before
    public void setUp() throws Exception {
        instructions = new ArrayList<Instruction>();
        instructions.add(new TestInstruction("First Instruction", 1, 0.1));
        instructions.add(new TestInstruction("Second Instruction", 2, 2.345));
        instructions.add(new TestInstruction("Last Instruction", 15, 0));
        simpleFeature = new SimpleFeature();
        simpleFeature.setProperty(TEXT, TEST_LOCATION);
        listener = new TestListener();
        fragment = DirectionListFragment.newInstance(instructions, listener, simpleFeature, false);
        startFragment(fragment);
        ButterKnife.inject(this, fragment.getView());
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
    public void destinationTextViewShouldBeDestination() {
        String destinationPointText = destinationTextView.getText().toString();
        assertThat(destinationPointText).isEqualTo(TEST_LOCATION);
    }

    @Test
    public void startingTextShouldBeCurrentLocation() {
        String startingPointText = startingPointTextView.getText().toString();
        assertThat(startingPointText).isEqualTo(fragment.getString(R.string.current_location));
    }

    @Test
    public void onReverse_destinationTextViewShouldBeCurrentLocation() throws Exception {
        setFragmentToBeReversed();
        String destinationPointText = destinationTextView.getText().toString();
        assertThat(destinationPointText).isEqualTo(fragment.getString(R.string.current_location));
    }

    @Test
    public void onReverse_startingTextShouldBeTestLocation() throws Exception {
        setFragmentToBeReversed();
        String startingPointText = startingPointTextView.getText().toString();
        assertThat(startingPointText).isEqualTo(TEST_LOCATION);
    }

    @Test
    public void firstListItem_shouldBeCurrentLocation() throws Exception {
        View view = listView.getAdapter().getView(0, null, null);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView instruction = (TextView) view.findViewById(R.id.simple_instruction);
        DistanceView distance = (DistanceView) view.findViewById(R.id.distance);

        assertThat(icon.getDrawable()).isEqualTo(res.getDrawable(R.drawable.ic_locate_active));
        assertThat(instruction).hasText("Current Location");
        assertThat(distance).isEmpty();
    }

    @Test
    public void secondListItem_shouldBeFirstInstruction() throws Exception {
        View view = listView.getAdapter().getView(1, null, null);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView instruction = (TextView) view.findViewById(R.id.simple_instruction);
        DistanceView distance = (DistanceView) view.findViewById(R.id.distance);

        assertThat(icon.getDrawable()).isEqualTo(res.getDrawable(R.drawable.ic_route_gr_1));
        assertThat(instruction).hasText("First Instruction");
        assertThat(distance).hasText("0.1 mi");
    }

    @Test
    public void thirdListItem_shouldBeSecondInstruction() throws Exception {
        View view = listView.getAdapter().getView(2, null, null);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView instruction = (TextView) view.findViewById(R.id.simple_instruction);
        DistanceView distance = (DistanceView) view.findViewById(R.id.distance);

        assertThat(icon.getDrawable()).isEqualTo(res.getDrawable(R.drawable.ic_route_gr_2));
        assertThat(instruction).hasText("Second Instruction");
        assertThat(distance).hasText("2.3 mi");
    }

    @Test
    public void lastListItem_shouldBeLastInstruction() throws Exception {
        int indexOfLastItem = listView.getAdapter().getCount() - 1;
        View view = listView.getAdapter().getView(indexOfLastItem, null, null);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView instruction = (TextView) view.findViewById(R.id.simple_instruction);
        DistanceView distance = (DistanceView) view.findViewById(R.id.distance);

        assertThat(icon.getDrawable()).isEqualTo(res.getDrawable(R.drawable.ic_route_gr_15));
        assertThat(instruction).hasText("Last Instruction");
        assertThat(distance).isEmpty();
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

        public TestInstruction(String simpleInstruction, int turnInstruction,
                double distanceInMiles) throws Exception {
            this.simpleInstruction = simpleInstruction;
            this.turnInstruction = turnInstruction;
            setDistance((int) Math.round(distanceInMiles * METERS_IN_MILE));
        }

        @Override
        public String getSimpleInstruction() {
            return simpleInstruction;
        }

        @Override
        public int getTurnInstruction() {
            return turnInstruction;
        }
    }

    class TestListener implements DirectionListFragment.DirectionListener {
        private int index;

        @Override
        public void onInstructionSelected(int index) {
            this.index = index;
        }
    }

    public void setFragmentToBeReversed() throws Exception {
        fragment = DirectionListFragment.newInstance(instructions, listener, simpleFeature, true);
        startFragment(fragment);
        ButterKnife.inject(this, fragment.getView());
    }
}
