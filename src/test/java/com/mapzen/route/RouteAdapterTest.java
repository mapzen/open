package com.mapzen.route;

import com.mapzen.R;
import com.mapzen.osrm.Instruction;
import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.SpannedString;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.mapzen.support.TestHelper.getTestInstruction;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class RouteAdapterTest {
    private RouteAdapter routeAdapter;
    private ViewGroup viewGroup;

    @Before
    public void setUp() throws Exception {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(getTestInstruction(0, 0));
        instructions.add(getTestInstruction(0, 0));
        routeAdapter = new RouteAdapter(application, instructions, new RouteFragment());
        viewGroup = new TestViewGroup(application);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(routeAdapter).isNotNull();
    }

    @Test
    public void defaultInstruction_shouldHaveTransparentGrayBackground() throws Exception {
        View view = (View) routeAdapter.instantiateItem(viewGroup, 0);
        ColorDrawable background = (ColorDrawable) view.getBackground();
        int expectedColor = application.getResources().getColor(R.color.transparent_gray);
        assertThat(background.getColor()).isEqualTo(expectedColor);
    }

    @Test
    public void activeInstruction_shouldHaveTransparentWhiteBackground() throws Exception {
        View view = (View) routeAdapter.instantiateItem(viewGroup, 0);
        routeAdapter.setBackgroundColorActive(view);
        ColorDrawable background = (ColorDrawable) view.getBackground();
        int expectedColor = application.getResources().getColor(R.color.transparent_white);
        assertThat(background.getColor()).isEqualTo(expectedColor);
    }

    @Test
    public void inactiveInstruction_shouldHaveTransparentGrayBackground() throws Exception {
        View view = (View) routeAdapter.instantiateItem(viewGroup, 0);
        routeAdapter.setBackgroundColorActive(view);
        routeAdapter.setBackgroundColorInactive(view);
        ColorDrawable background = (ColorDrawable) view.getBackground();
        int expectedColor = application.getResources().getColor(R.color.transparent_gray);
        assertThat(background.getColor()).isEqualTo(expectedColor);
    }

    @Test
    public void lastInstruction_shouldHaveDestinationBackground() throws Exception {
        View view = (View) routeAdapter.instantiateItem(viewGroup, 1);
        ColorDrawable background = (ColorDrawable) view.getBackground();
        int expectedColor = application.getResources().getColor(R.color.destination_color);
        assertThat(background.getColor()).isEqualTo(expectedColor);
    }

    @Test
    public void lastInstruction_shouldHideFullInstruction() throws Exception {
        View view = (View) routeAdapter.instantiateItem(viewGroup, 1);
        assertThat(view.findViewById(R.id.full_instruction)).isNotVisible();
    }

    @Test
    public void lastInstruction_shouldShowYouHaveArrived() throws Exception {
        View view = (View) routeAdapter.instantiateItem(viewGroup, 1);
        assertThat(view.findViewById(R.id.you_have_arrived)).isVisible();
    }

    @Test
    public void lastInstruction_shouldShowDestinationIcon() throws Exception {
        View view = (View) routeAdapter.instantiateItem(viewGroup, 1);
        assertThat(view.findViewById(R.id.destination_icon)).isVisible();
    }

    @Test
    public void shouldBoldName() throws Exception {
        View view = (View) routeAdapter.instantiateItem(viewGroup, 0);
        TextView textView = (TextView) view.findViewById(R.id.full_instruction);
        SpannedString spannedString = (SpannedString) textView.getText();
        assertThat(spannedString.getSpans(0, spannedString.length(), StyleSpan.class)).hasSize(1);
    }

    class TestViewGroup extends ViewGroup {
        public TestViewGroup(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }
    }
}
