package com.mapzen.open.widget;

import com.mapzen.open.R;
import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.widget.LinearLayout;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(MapzenTestRunner.class)
public class InstructionViewTest {
    private static final Activity ACTIVITY = buildActivity(Activity.class).create().get();

    private InstructionView instructionView;

    @Before
    public void setUp() throws Exception {
        instructionView = new InstructionView(ACTIVITY);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(instructionView).isNotNull();
    }

    @Test
    public void shouldSetLayoutParams() throws Exception {
        LinearLayout.LayoutParams layoutParams =
                (LinearLayout.LayoutParams) instructionView.getLayoutParams();
        assertThat(layoutParams).hasWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        assertThat(layoutParams).hasHeight((int) ACTIVITY.getResources()
                .getDimension(R.dimen.instruction_height));
    }

    @Test
    public void shouldInflateInstructionView() throws Exception {
        assertThat(instructionView.turnContainer).isNotNull();
        assertThat(instructionView.instructionContainer).isNotNull();
    }

    @Test
    public void setBackgroundColor_shouldSetTurnContainerBackgroundColor() throws Exception {
        final int color = ACTIVITY.getResources().getColor(R.color.transparent_white);
        instructionView.setBackgroundColor(color);
        assertThat(((ColorDrawable) instructionView.turnContainer.getBackground()).getColor())
                .isEqualTo(color);
    }

    @Test
    public void setBackgroundColor_shouldSetInstructionContainerBackgroundColor() throws Exception {
        final int color = ACTIVITY.getResources().getColor(R.color.transparent_white);
        instructionView.setBackgroundColor(color);
        assertThat(((ColorDrawable) instructionView.instructionContainer.getBackground())
                .getColor()).isEqualTo(color);
    }

    @Test
    public void setDestination_shouldShowDestinationView() throws Exception {
        instructionView.setDestination("Central Bucks Family YMCA");
        assertThat(instructionView.turnIcon).hasVisibility(GONE);
        assertThat(instructionView.fullInstruction).hasVisibility(GONE);
        assertThat(instructionView.fullInstructionAfterAction).hasVisibility(GONE);
        assertThat(instructionView.youHaveArrived).hasVisibility(VISIBLE);
        assertThat(instructionView.destinationIcon).hasVisibility(VISIBLE);
        assertThat(instructionView.destinationBanner).hasText("Central Bucks Family YMCA");
    }

    @Test
    public void setDestination_shouldSetBackgroundColor() throws Exception {
        final int color = ACTIVITY.getResources().getColor(R.color.destination_color);
        instructionView.setDestination("Central Bucks Family YMCA");
        assertThat(((ColorDrawable) instructionView.turnContainer.getBackground()).getColor())
                .isEqualTo(color);
        assertThat(((ColorDrawable) instructionView.instructionContainer.getBackground())
                .getColor()).isEqualTo(color);
    }
}
